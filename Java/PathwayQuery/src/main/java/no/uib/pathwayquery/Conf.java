package no.uib.pathwayquery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

        if (strMap.containsKey(name)) {
            return true;
        }
        if (boolMap.containsKey(name)) {
            return true;
        }
        if (intMap.containsKey(name)) {
            return true;
        }

        return false;
    }

    public static void setValue(String name, String value) {
        
        if (strMap.containsKey(name)) {
            strMap.put(name, value);
        }
        if (boolMap.containsKey(name)) {
            boolMap.put(name, Boolean.valueOf(value));
        }
        if (intMap.containsKey(name)) {
            intMap.put(name, Integer.valueOf(value));
        }
    }

    public static void setDefaultValuesGraphExtractor() {

        setEmptyMaps();

        // Set general configuration
        intMap.put("version", 2);
        boolMap.put("allProteome", Boolean.TRUE);
        intMap.put("maxNumVertices", 21000); //The graph will ask for memory to accomodate this number of proteins. Then it has to be as accurate as possible.
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

        setDefaultReactomeValues();

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

    public static int readConf() {
        return readConf(Conf.strMap.get(Conf.strVars.configPath.toString()));
    }

    public static int readConf(String path) {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(path));

            //For every valid variable found in the config.txt file, the variable value gets updated
            String line;
            while ((line = configBR.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("//")) {
                    continue;
                }
                if (!line.contains("=")) {
                    continue;
                }
                String[] parts = line.split("=");
                if (Conf.contains(parts[0])) {
                    Conf.setValue(parts[0], parts[1]);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            System.out.println(System.getProperty("user.dir"));

            System.exit(1);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            System.exit(1);
        }

        return 0;
    }

    public static void setEmptyMaps() {
        intMap = new HashMap<>();
        boolMap = new HashMap<>();
        strMap = new HashMap<>();
    }

    public static void setDefaultReactomeValues() {
        //Database access
        strMap.put(strVars.host.toString(), "bolt://localhost");
        strMap.put(strVars.username.toString(), "neo4j");
        strMap.put(strVars.password.toString(), "neo4j2");
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
        },
        ReactionChainedToReaction {
            public String toString() {
                return "RR";
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
        },
        Cp {
            public String toString() {
                return "ComplexHasProtein";
            }
        },
        Sp {
            public String toString() {
                return "SetHasProtein";
            }
        },
        CC {
            public String toString() {
                return "ComplexHasComplex";
            }
        },
        SC {
            public String toString() {
                return "SetHasComplex";
            }
        },
        CS {
            public String toString() {
                return "ComplexHasSet";
            }
        },
        SS {
            public String toString() {
                return "SetHasSet";
            }
        },
        Rp {
            public String toString() {
                return "ReactionHasProtein";
            }
        },
        RC {
            public String toString() {
                return "ReactionHasComplex";
            }
        },
        RS {
            public String toString() {
                return "ReactionHasSet";
            }
        },
        PR {
            public String toString() {
                return "PathwayHasReaction";
            }
        },
        PP {
            public String toString() {
                return "PathwayHasPathway";
            }
        },
        RR {
            public String toString() {
                return "ReactionChainedToReaction";
            }
        }
    }
}
