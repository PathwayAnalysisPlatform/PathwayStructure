package no.uib.pathwayquery;

import no.uib.db.UniprotAccess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.uib.db.ConnectionNeo4j;
import no.uib.db.ReactomeAccess;
import no.uib.model.GraphReactome;

import static no.uib.pathwayquery.Conf.createCLIOptions;
import static no.uib.pathwayquery.Conf.options;
import static no.uib.pathwayquery.Conf.strMap;
import no.uib.pathwayquery.Conf.StrVars;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.apache.commons.cli.*;
import org.neo4j.driver.v1.Session;

/**
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
        initialize(args);

        // Initialize graph
        // In this part I don't know how many proteins are required, then it is set to the maximum capacity
        G = new GraphReactome(Conf.intMap.get(Conf.IntVars.maxNumVertices));

        // Get complexes, sets, reactions and pathways
//        for (Conf.EntityType t : Conf.EntityType.values()) {
//            System.out.println("Getting " + t.toString() + " vertices...");
//            List<Record> records = ReactomeAccess.getVerticesByType(t);
//            System.out.println("Found " + (records != null ? records.size() : 0) + " vertices...");
//            G.addAllVertices(records);
//        }
        //Get the proteins
        if (Conf.strMap.get(Conf.StrVars.input).equals("")) {
            UniprotAccess.getUniProtProteome(); //Get from the online website. This also gets the real number of proteins requested in the variable totalNumProt
        } else {
            G.addAllVertex(getProteinList()); // Get from the input list
        }
        System.out.println("The number of vertices is: " + G.getNumVertices());
        //System.out.println(G.getNumVertices());
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
        System.out.println("\nGetting reaction interactions...\n");
        if (Conf.boolMap.get(Conf.EdgeType.InputToOutput.toString())
                || Conf.boolMap.get(Conf.EdgeType.CatalystToInput.toString())
                || Conf.boolMap.get(Conf.EdgeType.CatalystToOutput.toString())
                || Conf.boolMap.get(Conf.EdgeType.RegulatorToInput.toString())
                || Conf.boolMap.get(Conf.EdgeType.RegulatorToOutput.toString())) {
            ReactomeAccess.getReactionNeighbours();
        }

        // Gather ComplexNeighbour and Entity neighbors
        System.out.println("\nGetting complex or set neighbors...\n");
        if (Conf.boolMap.get(Conf.EdgeType.ComplexNeighbour.toString())
                || Conf.boolMap.get(Conf.EdgeType.DefinedSetNeighbour.toString())
                || Conf.boolMap.get(Conf.EdgeType.CandidateSetNeighbour.toString())
                || Conf.boolMap.get(Conf.EdgeType.OpenSetNeighbour.toString())) {
            ReactomeAccess.getComplexOrSetNeighbours();
        }
        /**
         * ************* Vertical edges ******************* These are the ones
         * corresponding to the hierarchy of the pathways and reactions.
         */
//        System.out.println("Getting vertical edges...");
//        for (Conf.EdgeType t : Conf.EdgeType.values()) {
//            if (Conf.boolMap.get(t.toString())) {
//                System.out.println("Getting " + t.toString() + " edges...");
//                if (t.equals(Conf.EdgeType.ReactionChainedToReaction)) {
//                    List<Record> reactionList = ReactomeAccess.getVerticesByType(Conf.EntityType.Reaction);
//                    int cont = 0;
//                    int percentage = 0;
//                    System.out.print(percentage + "% ");
//                    for (Record reaction : reactionList) {
//                        List<Record> records = ReactomeAccess.getEdgesByTypeAndId(t, reaction.get("id").asString());
//                        G.addAllEdges(records, t);
//                        cont++;
//                        if (cont % 400 == 0) {
//                            int newPercentage = cont * 100 / reactionList.size();
//                            if (percentage < newPercentage) {
//                                System.out.print(newPercentage + "% ");
//                                percentage = newPercentage;
//                            }
//                        }
//                    }
//                    System.out.println("");
//                } else {
//                    List<Record> records = ReactomeAccess.getEdgesByType(t);
//                    System.out.println("Found " + (records != null ? records.size() : 0) + " edges.");
//                    G.addAllEdges(records, t);
//                }
//            }
//        }

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
    private static List<String> getProteinList() {
        List<String> result = new ArrayList<>();
        int index = 0;
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(strMap.get(Conf.StrVars.input)));
            for (String id; (id = input.readLine()) != null && index < Conf.intMap.get(Conf.IntVars.maxNumVertices);) {
                if (id.length() <= 6) {
                    result.add(id);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Could not read file: " + strMap.get(Conf.StrVars.input));
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Loads the configuration variables. Initializes the driver to connect to
     * Reactome in Neo4j. Opens the configuration file located in the same
     * folder as the executable of the program. Reads the desired values for the
     * configuration variables. If a configuration variable is not present in
     * the configuration file, then it remains with the default value. Reads the
     * command line arguments.
     *
     * @return {int} If the execution ended successfully returns 0. Otherwise
     * returns 1.
     */
    private static int initialize(String args[]) {

        /**
         * *** Set default values ***
         */
        Conf.setDefaultValuesGraphExtractor();

        /**
         * *** Read config file ***
         */
        try {
            // Verify if configuration file exists
            File f = new File(strMap.get(Conf.StrVars.conf));
            //if (f.exists() && !f.isDirectory()) {
                //Read and set configuration values from file
                BufferedReader configBR = new BufferedReader(new FileReader(strMap.get(Conf.StrVars.conf)));

                //For every valid variable found in the config.txt file, the variable value gets updated
                String line;
                while ((line = configBR.readLine()) != null) {
                    if (line.length() == 0) {
                        continue;
                    }
                    line = line.trim();
                    if (line.startsWith("//")) {    //Discard the comment lines in the 
                        continue;
                    }
                    if (!line.contains("=")) {      //Set to true the flag arguments
                        Conf.setValue(line, Boolean.TRUE);
                    } else {                        // Set the value for the valued arguments
                        String[] parts = line.split("=");
                        if (Conf.contains(parts[0])) {
                            Conf.setValue(parts[0], parts[1]);
                        }
                    }
                }
            //}
        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + strMap.get(Conf.StrVars.conf));
            //Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + strMap.get(Conf.StrVars.conf));
            //Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        /**
         * *** Parse the command line parameters ***
         */
        // Define and parse command line options
        createCLIOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            // Load all the values received in the command line. All variables, except the edge types, because those are referenced with the short name
            boolean anyFlag = false;
            for (Option opt : options.getOptions()) {
                if (cmd.hasOption(opt.getLongOpt())) {
                    if (opt.hasArg()) {
                        Conf.setValue(opt.getLongOpt(), cmd.getOptionValue(opt.getLongOpt()));
                    } else {
                        Conf.setValue(opt.getLongOpt(), Boolean.TRUE);
                    }
                }
            }

            // If one of the edge type flags is specified, turn off all the flags for edge types
            for (Conf.EdgeType c : Conf.EdgeType.values()) {
                if (cmd.hasOption(c.toString())) {
                    for (Conf.EdgeType et : Conf.EdgeType.values()) {
                        Conf.setValue(et.toString(), Boolean.FALSE);
                    }
                    anyFlag = true;
                    break;
                }
            }

            // If there was no flag on for edge types, then uses the default configuration
            // If at least one flag was on, then turn on all the selected flags
            if (anyFlag) {
                for (Conf.EdgeType c : Conf.EdgeType.values()) {
                    if (cmd.hasOption(c.toString())) {
                        Conf.setValue(c.toString(), Boolean.TRUE);
                    }
                }
            }

        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        ConnectionNeo4j.driver = GraphDatabase.driver(strMap.get(StrVars.host), AuthTokens.basic(strMap.get(StrVars.username), strMap.get(StrVars.password)));

        try {
            Session session = ConnectionNeo4j.driver.session();
            session.close();
        } catch (org.neo4j.driver.v1.exceptions.ClientException e) {
            System.out.println(" Unable to connect to \"" + strMap.get(StrVars.host.toString()) + "\", ensure the database is running and that there is a working network connection to it.");
            System.exit(1);
        }

        return 0;
    }

}
