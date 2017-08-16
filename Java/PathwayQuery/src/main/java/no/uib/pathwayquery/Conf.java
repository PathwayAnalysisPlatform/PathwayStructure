package no.uib.pathwayquery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Conf {

    public static Options options;

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

    public static void setValue(String name, Boolean value) {

        if (boolMap.containsKey(name)) {
            boolMap.put(name, Boolean.valueOf(value));
        }
    }

    public interface StrVars {

        String input = "input";
        String output = "output";
        String conf = "conf";

        String host = "host";
        String username = "username";
        String password = "password";

        String reactionsFile = "reactionsFile";
    }

    public interface IntVars {

        String maxNumVertices = "maxNumVertices";
    }

    public interface BoolVars {

        String onlyProteinsInList = "onlyProteinsInList";
        //String showMissingProteins = "showMissingProteins";
        String showDisconnectedProteins = "showIsolatedProteins";
    }

    /**
     * Creates the command line options for the ProteinGraphExtractor. The
     */
    public static void createCLIOptions() {

        options = new Options();

        // Program Input and Output
        newOption("i", StrVars.input, true, false, "Input file path and name");
        newOption("o", StrVars.output, true, false, "Output file path and name");
        newOption("c", StrVars.conf, true, false, "Configuration file path and name");

        // Neo4j database access
        newOption("h", StrVars.host, true, false, "The url where Neo4j is accessible with Reactome knowledgebase loaded.");
        newOption("u", StrVars.username, true, false, "The password associated to the username specified.");
        newOption("p", StrVars.password, true, false, "The password associated to the username specified.");

        // Graph vertices
        newOption("n", IntVars.maxNumVertices, true, false, "Use only the first __n__ proteins");
        newOption("l", BoolVars.onlyProteinsInList, false, false, "Output graph will only contain proteins(vertices) in the input list.");
        //newOption("m", BoolVars.showMissingProteins, false, false, "Add to graph proteins not found in Reactome");
        newOption("d", BoolVars.showDisconnectedProteins, false, false, "If set to true, the output graph will include the proteins contained in the input file but do not have connections.");

        // Graph arcs
        newOption(EdgeType.InputToOutput.toString(), EdgeType.InputToOutput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as input and the other as output.");
        newOption(EdgeType.CatalystToInput.toString(), EdgeType.CatalystToInput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as catalyst and the other as input.");
        newOption(EdgeType.CatalystToOutput.toString(), EdgeType.CatalystToOutput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as catalyst and the other as output.");
        newOption(EdgeType.RegulatorToInput.toString(), EdgeType.RegulatorToInput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as regulator and the other as input.");
        newOption(EdgeType.RegulatorToOutput.toString(), EdgeType.RegulatorToOutput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as regulator and the other as output.");
        newOption(EdgeType.OutputToInput.toString(), EdgeType.OutputToInput.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as output and the other as input. ");
        newOption(EdgeType.OutputToCatalyst.toString(), EdgeType.OutputToCatalyst.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as output and the other as catalyst.");
        newOption(EdgeType.OutputToRegulator.toString(), EdgeType.OutputToRegulator.name(), false, false, "Show edges indicating two proteins participate in the same reaction, having the first one as output and the other as catalyst.");

        newOption(EdgeType.ComplexNeighbour.toString(), EdgeType.ComplexNeighbour.name(), false, false, "Show arcs indicating complex neighbours.");
        newOption(EdgeType.DefinedSetNeighbour.toString(), EdgeType.DefinedSetNeighbour.name(), false, false, "Show edges indicating defined set neighbours.");
        newOption(EdgeType.OpenSetNeighbour.toString(), EdgeType.OpenSetNeighbour.name(), false, false, "Show edges indicating open set neighbours.");
        newOption(EdgeType.CandidateSetNeighbour.toString(), EdgeType.CandidateSetNeighbour.name(), false, false, "Show edges indicating candidate set neighbours.");
        newOption(EdgeType.EntitySetNeighbour.toString(), EdgeType.EntitySetNeighbour.name(), false, false, "Show edges indicating entity set neighbours.");
    }

    private static void newOption(String shortName, String name, boolean needsValue, boolean required, String description) {
        Option cla = new Option(shortName, name, needsValue, description);
        cla.setRequired(required);
        options.addOption(cla);
    }

    public static void setDefaultValuesGraphExtractor() {

        setEmptyMaps();

        // Input and Output
        strMap.put(StrVars.input, "");
        strMap.put(StrVars.output, "./output.sif");
        strMap.put(StrVars.conf, "./Config.txt");

        setDefaultNeo4jValues();

        //Vertices configuration
        intMap.put(IntVars.maxNumVertices, 21000);
        boolMap.put(BoolVars.onlyProteinsInList, Boolean.FALSE);
        //boolMap.put(BoolVars.showMissingProteins, Boolean.FALSE);  //TODO
        boolMap.put(BoolVars.showDisconnectedProteins, Boolean.FALSE);

        strMap.put(StrVars.reactionsFile, "./Reactions.txt");

        //Arcs configuration
        //Set default values to all vertical edges
        for (Conf.EdgeType t : Conf.EdgeType.values()) {
            boolMap.put(t.toString(), Boolean.TRUE);
        }
    }

    public static int readConf() {
        return readConf(Conf.strMap.get(Conf.StrVars.conf));
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
            System.out.println("Configuration file not found at: " + Conf.strMap.get(Conf.StrVars.conf));
            System.out.println(System.getProperty("user.dir"));

            System.exit(1);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(Conf.StrVars.conf));
            System.exit(1);
        }

        return 0;
    }

    public static void setEmptyMaps() {
        intMap = new HashMap<>();
        boolMap = new HashMap<>();
        strMap = new HashMap<>();
    }

    public static void setDefaultNeo4jValues() {
        //Database access
        strMap.put(StrVars.host, "bolt://127.0.0.1:7687");
        strMap.put(StrVars.username, "neo4j");
        strMap.put(StrVars.password, "neo4j2");
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

    public enum ReactionArcs {
        InputToOutput {
            @Override
            public String toString() {
                return "io";
            }
        },
        CatalystToInput {
            @Override
            public String toString() {
                return "ci";
            }
        },
        CatalystToOutput {
            @Override
            public String toString() {
                return "co";
            }
        },
        RegulatorToInput {
            @Override
            public String toString() {
                return "ri";
            }
        },
        RegulatorToOutput {
            @Override
            public String toString() {
                return "ro";
            }
        },
        OutputToInput {
            @Override
            public String toString() {
                return "oi";
            }
        },
        OutputToCatalyst {
            @Override
            public String toString() {
                return "oc";
            }
        },
        OutputToRegulator {
            @Override
            public String toString() {
                return "or";
            }
        }
    }

    public enum SetArcs {
        ComplexNeighbour {
            @Override
            public String toString() {
                return "cn";
            }
        },
        CandidateSetNeighbour {
            @Override
            public String toString() {
                return "cs";
            }
        },
        DefinedSetNeighbour {
            @Override
            public String toString() {
                return "ds";
            }
        },
        OpenSetNeighbour {
            @Override
            public String toString() {
                return "os";
            }
        },
        EntitySetNeighbour {
            @Override
            public String toString() {
                return "es";
            }
        }
    }

    public enum HierarchicalArcs {
        ComplexHasProtein {
            @Override
            public String toString() {
                return "Cp";
            }
        },
        SetHasProtein {
            @Override
            public String toString() {
                return "Sp";
            }
        },
        ComplexHasComplex {
            @Override
            public String toString() {
                return "CC";
            }
        },
        SetHasComplex {
            @Override
            public String toString() {
                return "SC";
            }
        },
        ComplexHasSet {
            @Override
            public String toString() {
                return "CS";
            }
        },
        SetHasSet {
            @Override
            public String toString() {
                return "SS";
            }
        },
        ReactionHasProtein {
            @Override
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
            @Override
            public String toString() {
                return "RS";
            }
        },
        PathwayHasReaction {
            @Override
            public String toString() {
                return "PR";
            }
        },
        PathwayHasPathway {
            @Override
            public String toString() {
                return "PP";
            }
        },
        ReactionChainedToReaction {
            @Override
            public String toString() {
                return "RR";
            }
        }
    }

    public enum EdgeType {
        ComplexNeighbour {
            public String toString() {
                return "cn";
            }
        },
        CandidateSetNeighbour {
            public String toString() {
                return "cs";
            }
        },
        DefinedSetNeighbour {
            public String toString() {
                return "ds";
            }
        },
        OpenSetNeighbour {
            public String toString() {
                return "os";
            }
        },
        EntitySetNeighbour {
            public String toString() {
                return "es";
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
        OutputToInput {
            public String toString() {
                return "oi";
            }
        },
        OutputToCatalyst {
            public String toString() {
                return "oc";
            }
        },
        OutputToRegulator {
            public String toString() {
                return "or";
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
