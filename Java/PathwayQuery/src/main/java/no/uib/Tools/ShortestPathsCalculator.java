/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uib.Tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.Model.GraphReactome;
import no.uib.pathwayquery.Conf;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ShortestPathsCalculator {
    
    private static TreeSet<String> proteinSet;

    public static void main(String args[]) throws IOException {
        String path = "./Graph.sif";
        GraphReactome G = new GraphReactome(path, 45000);
        proteinSet = new TreeSet<>();
        
        proteinSet = getProteinList();
        
        G.shortestUnweightedPaths(proteinSet);
    }
    
    private static TreeSet<String> getProteinList(){
        TreeSet<String> s = new TreeSet<>();
        //Read protein list
        int index = 0;
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader("./src/main/resources/input/listLeukDiat.csv"));
            for (String id; (id = input.readLine()) != null; ) {
                if (id.length() <= 6) {
                    s.add(id);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ShortestPathsCalculator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ShortestPathsCalculator.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return s;
    }
}
