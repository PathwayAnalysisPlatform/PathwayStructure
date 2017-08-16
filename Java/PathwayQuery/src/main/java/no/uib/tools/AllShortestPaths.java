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
package no.uib.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.model.GraphReactome;
import no.uib.pathwayquery.Conf;
import static no.uib.pathwayquery.Conf.options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Bram Burger
 * @author Luis Francisco Hernández Sánchez
 * 
 */
public class AllShortestPaths {
    
    private static TreeSet<String> proteinSet;

    public static void main(String args[]) {
        
        // Define and parse command line options
        Conf.options = new Options();
        
        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "output file path");
        output.setRequired(true);
        options.addOption(output);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {

            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }
        
        
        
//        GraphReactome G = new GraphReactome(cmd.getOptionValue("input").toString(), 45000);
//        proteinSet = new TreeSet<>();
//        
//        proteinSet = getProteinList();
//        
//        G.shortestUnweightedPaths(proteinSet);
    }
    
    
    
    private static TreeSet<String> getProteinList(){
        TreeSet<String> s = new TreeSet<>();
        //Read protein list
        int index = 0;
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader("./src/main/resources/input/diabetesProteinsUniProt.txt"));
            for (String id; (id = input.readLine()) != null; ) {
                if (id.length() <= 6) {
                    s.add(id);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AllShortestPaths.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AllShortestPaths.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return s;
    }
}
