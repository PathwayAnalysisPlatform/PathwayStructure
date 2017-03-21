package no.uib.pathwayquery;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Configuration {

    //General configuration
    public static boolean allProteome = true;
    public static int maxNumProt = 21000;   //The graph will ask for memory to accomodate this number of proteins. Then it has to be as accurate as possible.
    public static String configPath = "./Config.txt";
    public static String inputListFile = "./src/main/resources/input/uniprotList.txt";      //Input to create a json graph
    public static String reactionsFile = "./Reactions.txt";
    
    //Input configuration
    public static Boolean ignoreMisformatedRows = true;

    //Database access
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";

    public static boolean verboseConsole = true;
    public static ProteinType unitType = ProteinType.uniprot;
    public static String configGraphPath = "./Config.txt";
    
        //Vertices - Proteins/Reactions
    public static boolean onlyNeighborsInList = false;
    public static boolean onlyOrderedEdges = false;
    public static boolean showMissingProteins = false;
    
        //Edges - Interactions/Relationships
    
        //General Neighbors
    //public static boolean pn = false; // Physical Entities belong to the same Pathway
    
        //Complex/Set neighbors. These are bidirectional relations.
    public static boolean cn = true; // Physical Entities belong to the same Complex
    public static boolean ds = true; // Physical Entities belong to the same DefinedSet
    public static boolean os = true; // Physical Entities belong to the same OpenSet
    public static boolean cs = true; // Physical Entities belong to the same CandidateSet
        //Reaction neighbors. These are not bidirectional relations.
    public static boolean io = false; // p1 is Input and p2 is output
    public static boolean ci = false; // p1 is catalystActivity and p2 is input
    public static boolean co = false; // p1 is catalystActivity and p2 is output
    public static boolean ri = false; // p1 is regulator and p2 is input
    public static boolean ro = false; // p1 is regulator and p2 is output

    
    public static boolean reactionNeighbors = true;
    public static boolean complexNeighbors = true;
    public static boolean entityNeighbors = false;
    public static boolean candidateNeighbors = false;
    public static boolean topLevelPathwayNeighbors = false;
    public static boolean pathwayNeighbors = false;
    
        //Results
    public static GraphType outputGraphFileType = GraphType.sif;
    public static String outputGraphFilePath = ".";
    public static String outputFileName = "ProteomeInteractions";

    // public static String inputListFile = "./src/main/resources/csv/listBjorn.csv";
    public enum ProteinType {
        ewas, uniprot
    }

    public enum InputType {
        maxQuantMatrix, 
        peptideList, 
        peptideListAndSites, 
        peptideListAndModSites, 
        uniprotList, 
        uniprotListAndSites,
        uniprotListAndModSites, 
        unknown
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
}
