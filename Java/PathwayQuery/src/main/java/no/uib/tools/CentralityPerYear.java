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

/**
 *
 * @author Bram Burger
 */
public class CentralityPerYear {
    public static void main (String args[]) throws IOException {
        String postfixFile = ".sif";
        
        for (int year = 1934; year < 2018; year++) {
            String[] classInput = new String[]{"./"+Integer.toString(year)+postfixFile, Integer.toString(year)};
            Centrality.main(classInput);
        }
    }
}
