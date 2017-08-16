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
public class CentralityPerPathway {
    public static void main (String args[]) throws IOException {
        String path = "./../../../Pathways/shortestPaths/";
        String[] pathways = {"Cell-Cellcommunication",
            "CellCycle",
            "Cellularresponsestostress",
            "Chromatinorganization",
            "CircadianClock",
            "DevelopmentalBiology",
            "Disease",
            "DNARepair",
            "DNAReplication",
            "Extracellularmatrixorganization",
            "GeneExpression",
            "Hemostasis",
            "ImmuneSystem",
            "Metabolismofproteins",
            "Metabolism",
            "Mitophagy",
            "Musclecontraction",
            "NeuronalSystem",
            "Organellebiogenesisandmaintenance",
            "ProgrammedCellDeath",
            "Reproduction",
            "SignalTransduction",
            "Transmembranetransportofsmallmolecules",
            "Vesicle-mediatedtransport"};
        String postfixFile = "ToOut.sif";
        
        for (String pathway : pathways) {
            String[] classInput = new String[]{path+pathway+postfixFile, pathway};
            Centrality.main(classInput);
        }
    }
}
