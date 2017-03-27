package no.uib.pathwayquery;

import java.util.HashMap;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Conf {

    /**
     * Contains a map from a variable to its value
     */
    public static HashMap<String, String> strMap;
    public static HashMap<String, Boolean> boolMap;
    public static HashMap<String, Integer> intMap;

    public static boolean contains(String name) {

        for (strVars v : strVars.values()) {
            if (v.name().equals(name)) {
                return true;
            }
        }
        for (boolVars v : boolVars.values()) {
            if (v.name().equals(name)) {
                return true;
            }
        }
        for (intVars v : intVars.values()) {
            if (v.name().equals(name)) {
                return true;
            }
        }
        for (EdgeType v : EdgeType.values()) {
            if (v.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static void setValue(String name, String value) {
        for (strVars v : strVars.values()) {
            if (v.name().equals(name)) {
                strMap.put(name, value);
                return;
            }
        }
        for (boolVars v : boolVars.values()) {
            if (v.name().equals(name)) {
                boolMap.put(name, Boolean.valueOf(value));
                return;
            }
        }
        for (intVars v : intVars.values()) {
            if (v.name().equals(name)) {
                intMap.put(name, Integer.valueOf(value));
                return;
            }
        }
        for (EdgeType v : EdgeType.values()) {
            if (v.name().equals(name)) {
                boolMap.put(name, Boolean.valueOf(value));
                return;
            }
        }
    }

    public static void setDefaultValues() {
        intMap = new HashMap<>();
        boolMap = new HashMap<>();
        strMap = new HashMap<>();

        // Set general configuration
        intMap.put("version", 2);
        boolMap.put("allProteome", Boolean.TRUE);
        intMap.put("maxNumProt", 21000); //The graph will ask for memory to accomodate this number of proteins. Then it has to be as accurate as possible.
        strMap.put("inputListFile", "./src/main/resources/input/uniprotList.txt"); //Input to create a json graph
        strMap.put("reactionsFile", "./Reactions.txt"); //Input to create a json graph
        boolMap.put("ignoreMisformatedRows", Boolean.TRUE);
        boolMap.put("verboseConsole", Boolean.TRUE);        //TODO
        strMap.put("unitType", "UniProt");
        strMap.put("configPath", "./Config.txt");    //TODO

        //Results
        strMap.put("outputGraphFileType", "sif"); //TODO
        strMap.put("outputGraphFilePath", ".");
        strMap.put("outputFileName", "ProteomeGraph");

        //Database access
        strMap.put("host", "bolt://localhost");
        strMap.put("username", "neo4j");
        strMap.put("password", "neo4j2");

        //Vertices configuration
        boolMap.put("onlyNeighborsInList", Boolean.TRUE);  //TODO
        boolMap.put("onlyOrderedEdges", Boolean.TRUE);     //TODO
        boolMap.put("showMissingProteins", Boolean.TRUE);  //TODO
        boolMap.put("showIsolatedVertices", Boolean.TRUE);

        //Edges configuration
        //Set default values to all vertical edges
        for (Conf.EdgeType t : Conf.EdgeType.values()) {
            boolMap.put(t.toString(), Boolean.TRUE);
        }
    }

    public enum EntityType {
        Protein {
            public String toString() {
                return "p";
            }
        },
        Complex {
            public String toString() {
                return "C";
            }
        },
        Set {
            public String toString() {
                return "S";
            }
        },
        Reaction {
            public String toString() {
                return "R";
            }
        },
        Pathway {
            public String toString() {
                return "P";
            }
        }
    }

    public enum strVars {
        inputListFile, reactionsFile, unitType, configPath, outputGraphFileType,
        outputGraphFilePath, outputFileName, host, username, password,
    }

    public enum intVars {
        version, maxNumVertices
    }

    public enum boolVars {
        allProteome, ignoreMisformatedRows, verboseConsole, onlyNeighborsInList, onlyOrderedEdges, showMissingProteins, showIsolatedVertices
    }

    public enum ProteinType {
        ewas, uniprot
    }

    public enum GraphType {
        json {
            public String toString() {
                return "json";
            }
        },
        graphviz {
            public String toString() {
                return "graphviz";
            }
        },
        sif {
            public String toString() {
                return "sif";
            }
        }
    }

    public enum EdgeType {
        ComplexNeighbor {
            public String toString() {
                return "cn";
            }
        },
        CandidateSetNeighbor {
            public String toString() {
                return "cs";
            }
        },
        DefinedSetNeighbor {
            public String toString() {
                return "ds";
            }
        },
        OpenSetNeighbor {
            public String toString() {
                return "os";
            }
        },
        InputToOutput {
            public String toString() {
                return "io";
            }
        },
        CatalystToInput {
            public String toString() {
                return "ci";
            }
        },
        CatalystToOutput {
            public String toString() {
                return "co";
            }
        },
        RegulatorToInput {
            public String toString() {
                return "ri";
            }
        },
        RegulatorToOutput {
            public String toString() {
                return "ro";
            }
        },
        ComplexHasProtein {
            public String toString() {
                return "Cp";
            }
        },
        SetHasProtein {
            public String toString() {
                return "Sp";
            }
        },
        ComplexHasComplex {
            public String toString() {
                return "CC";
            }
        },
        SetHasComplex {
            public String toString() {
                return "SC";
            }
        },
        ComplexHasSet {
            public String toString() {
                return "CS";
            }
        },
        SetHasSet {
            public String toString() {
                return "SS";
            }
        },
        ReactionHasProtein {
            public String toString() {
                return "Rp";
            }
        },
        ReactionHasComplex {
            public String toString() {
                return "RC";
            }
        },
        ReactionHasSet {
            public String toString() {
                return "RS";
            }
        },
        PathwayHasReaction {
            public String toString() {
                return "PR";
            }
        },
        PathwayHasPathway {
            public String toString() {
                return "PP";
            }
        }
    }

    public enum EdgeLabel {
        cn {
            public String toString() {
                return "Complex";
            }
        },
        cs {
            public String toString() {
                return "CandidateSet";
            }
        },
        ds {
            public String toString() {
                return "DefinedSet";
            }
        },
        os {
            public String toString() {
                return "OpenSet";
            }
        },
        io {
            public String toString() {
                return "InputToOutput";
            }
        },
        ci {
            public String toString() {
                return "CatalystToInput";
            }
        },
        co {
            public String toString() {
                return "CatalystToOutput";
            }
        },
        ri {
            public String toString() {
                return "RegulatorToInput";
            }
        },
        ro {
            public String toString() {
                return "RegulatorToOutput";
            }
        }
    }
}
