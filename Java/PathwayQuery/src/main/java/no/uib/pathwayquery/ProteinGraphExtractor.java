package no.uib.pathwayquery;

import no.uib.DB.UniprotAccess;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.DB.ConnectionNeo4j;
import no.uib.DB.ReactomeAccess;
import no.uib.Model.GraphReactome;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ProteinGraphExtractor {

    public static GraphReactome G;

    /**
     * This is the starting point of the whole extractor project.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        // Load configuration
        System.out.println("Initializing graph extractor...");
        initialize();

        // Initialize graph
        // In this part I don't know how many proteins are required, then it is set to the maximum capacity
        String varName = Conf.intVars.maxNumVertices.toString();
        int num = Conf.intMap.get(varName);
        G = new GraphReactome(Conf.intMap.get(Conf.intVars.maxNumVertices.toString()));

        System.out.println(G.getNumVertices());

        // Get complexes, sets, reactions and pathways
        for (Conf.EntityType t : Conf.EntityType.values()) {
            System.out.println("Getting " + t.toString() + " vertices...");
            List<Record> records = ReactomeAccess.getVerticesByType(t);
            System.out.println("Found " + (records != null ? records.size() : 0) + " vertices...");
            G.addAllVertices(records);
        }

        System.out.println(G.getNumVertices());

        //Get the proteins
        if (Conf.boolMap.get(Conf.boolVars.allProteome.toString())) {
            UniprotAccess.getUniProtProteome(); //Get from the online website. This also gets the real number of proteins requested in the variable totalNumProt
        } else {
            ProteinGraphExtractor.getProteinList();
        }

        System.out.println(G.getNumVertices());

        //        Verify the contents of the verticesMapping
//        for (short I = 0; I < G.verticesMapping.size() && I < 10; I++) {
//            String vertexString = G.verticesMapping.getString(I);
//            short vertexShort = G.verticesMapping.getShort(vertexString);
//            System.out.println("short --> string: " + vertexShort + " --> " + vertexString);
//            System.out.println("string --> short: " + vertexString + " --> " + vertexShort);
//        }
        /**
         * ************* Horizontal edges *******************
         */
        // Gather reaction neighbors
        //Get reactions where the proteins play a role
        System.out.println("Getting horizontal edges...");
        System.out.println("Getting reaction interactions...");
        if (Conf.boolMap.get(Conf.EdgeType.InputToOutput.toString())
                || Conf.boolMap.get(Conf.EdgeType.CatalystToInput.toString())
                || Conf.boolMap.get(Conf.EdgeType.CatalystToOutput.toString())
                || Conf.boolMap.get(Conf.EdgeType.RegulatorToInput.toString())
                || Conf.boolMap.get(Conf.EdgeType.RegulatorToOutput.toString())) {
            ReactomeAccess.getReactionNeighbors();
        }

        // Gather ComplexNeighbor and Entity neighbors
        System.out.println("Getting complex or set neighbors...");
        if (Conf.boolMap.get(Conf.EdgeType.ComplexNeighbor.toString())
                || Conf.boolMap.get(Conf.EdgeType.DefinedSetNeighbor.toString())
                || Conf.boolMap.get(Conf.EdgeType.CandidateSetNeighbor.toString())
                || Conf.boolMap.get(Conf.EdgeType.OpenSetNeighbor.toString())) {
            ReactomeAccess.getComplexOrSetNeighbors();
        }
        /**
         * ************* Vertical edges *******************
         */
        System.out.println("Getting vertical edges...");
        for (Conf.EdgeType t : Conf.EdgeType.values()) {
            if (Conf.boolMap.get(t.toString())) {
                System.out.println("Getting " + t.toString() + " edges...");
                if (t.equals(Conf.EdgeType.ReactionChainedToReaction)) {
                    List<Record> reactionList = ReactomeAccess.getVerticesByType(Conf.EntityType.Reaction);
                    int cont = 0;
                    int percentage = 0;
                    System.out.print(percentage + "% ");
                    for(Record reaction : reactionList){
                        List<Record> records = ReactomeAccess.getEdgesByTypeAndId(t, reaction.get("id").asString());
                        G.addAllEdges(records, t);
                        cont++;
                        if(cont%400 == 0){
                            int newPercentage = cont*100/reactionList.size();
                            if(percentage < newPercentage){
                                System.out.print(newPercentage + "% ");
                                percentage = newPercentage;
                            }
                        }
                    }
                    System.out.println("");
                } else {
                    List<Record> records = ReactomeAccess.getEdgesByType(t);
                    System.out.println("Found " + (records != null ? records.size() : 0) + " edges.");
                    G.addAllEdges(records, t);
                }
            }
        }

        //Write the file
        G.writeGraphToFile();
    }

    /**
     * Reads the list from the specified file in the configuration. This method
     * executes when the "allProteome" variable is false. The file containing
     * the list is specified in the configuration variable "inputListFile". The
     * number of proteins read is also influenced by the configuration variable
     * "maxNumVertices". If the file contains less proteins that
     * "maxNumVertices" then all the proteins of the file are considered. If the
     * file contains more proteins than "maxNumVertices" then only the first
     * "maxNumVertices" proteins of the file will be used.
     */
    private static void getProteinList() {
        int index = 0;
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(Conf.strMap.get(Conf.strVars.inputListFile.toString())));
            for (String id; (id = input.readLine()) != null && index < Conf.intMap.get(Conf.intVars.maxNumVertices.toString());) {
                if (id.length() <= 6) {
                    ProteinGraphExtractor.G.addVertex(id);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads the configuration variables and initializes the driver to connect
     * to Reactome in Neo4j. Opens the configuration file located in the same
     * folder as the executable of the program. Reads the desired values for the
     * configuration variables. If a configuration variable is not present in
     * the configuration file, then it remains with the default value.
     *
     *
     * @return {int} If the execution ended successfully returns 0. Otherwise
     * returns 1.
     */
    private static int initialize() {

        try {
            Conf.setDefaultValues();

            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Conf.strMap.get(Conf.strVars.configPath.toString())));

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

            ConnectionNeo4j.driver = GraphDatabase.driver(ConnectionNeo4j.host, AuthTokens.basic(ConnectionNeo4j.username, ConnectionNeo4j.password));

        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            Logger
                    .getLogger(ProteinGraphExtractor.class
                            .getName()).log(Level.SEVERE, null, ex);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Conf.strMap.get(Conf.strVars.configPath.toString()));
            Logger
                    .getLogger(ProteinGraphExtractor.class
                            .getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

}
