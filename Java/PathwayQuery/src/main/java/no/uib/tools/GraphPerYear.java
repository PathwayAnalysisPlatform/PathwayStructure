/*
 * Copyright 2017 Bram Burger.
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

import java.io.IOException;
import no.uib.pathwayquery.ProteinGraphExtractor;

/**
 *
 * @author Bram Burger
 */
public class GraphPerYear {
    public static void main(String args[]) throws IOException {
        // ProteinGraphExtractor.main(new String[]{"-o", "1970.sif", "-l", "-io", "-co", "-ro", "-y", "1970"});
        
        /* this does not check whether anything actually changed between years... */
        for (int i = 1934; i < 2018; i++) {
            ProteinGraphExtractor.main(new String[]{"-o", Integer.toString(i)+".sif", "-l", "-io", "-co", "-ro", "-y", Integer.toString(i)});
        }
        
    }
}
