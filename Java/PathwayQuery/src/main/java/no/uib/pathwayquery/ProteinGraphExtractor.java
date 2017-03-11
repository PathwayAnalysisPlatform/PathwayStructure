package no.uib.pathwayquery;

import no.uib.DB.UniprotAccess;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.DB.ConnectionNeo4j;
import no.uib.DB.ReactomeAccess;
import no.uib.Model.BiMapShort;
import no.uib.Model.GraphAdjListEdgeTypes;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ProteinGraphExtractor {

    public static byte[][] proteins;
    public static short totalNumProt;
    private static GraphAdjListEdgeTypes G;

    public static void main(String args[]) throws IOException {

        // Load configuration
        initialize();

        //Get the list of proteins
        if (Configuration.allProteome) {
            UniprotAccess.getUniprotProteome(); //Get from the online website
        } else {
            ProteinGraphExtractor.getProteinList();
        }

        //Print the list
//        for (int I = 0; I < proteins.length && proteins[I] != null; I++) {
//            String id = new String(proteins[I]);
//            System.out.println(id);
//        }

        // Initialize graph
        G = new GraphAdjListEdgeTypes(totalNumProt);
        
        G.verticesMapping = new BiMapShort(totalNumProt);
        for (short I = 0; I < proteins.length && proteins[I] != null; I++) {
            verticesMap.put((short)I, proteins[I]);
            String id = new String(proteins[I]);
            System.out.println("Number --> Id: " + verticesMap.getId((short)I));
            System.out.println("Number --> String Id: " + verticesMap.getStringId((short)I));
            System.out.println("Id --> Number: " + verticesMap.getNum(proteins[I]));
        }
        
        
        

        // Gather reaction neighbors
        //Get reactions where the proteins play a role
        if (Configuration.io || Configuration.ci || Configuration.co || Configuration.ri || Configuration.ro) {
            ReactomeAccess.getReactionNeighbors();
        }

        // Gather Complex and Entity neighbors
        if (Configuration.cn || Configuration.ds || Configuration.cs || Configuration.os) {
            ReactomeAccess.getComplexOrSetNeighbors();
        }
        
        //Write the file
        G.writeSifGraph(Configuration.outputGraphFilePath + "/" + Configuration.outputFileName + ".sif");
    }

    private static void getProteinList() {
        int index = 0;
        BufferedReader input;
        try {
            totalNumProt = 0;
            input = new BufferedReader(new FileReader(Configuration.inputListFile));
            for (String id; (id = input.readLine()) != null && index < Configuration.maxNumProt;) {
                if (id.length() <= 6) {
                    ProteinGraphExtractor.proteins[ProteinGraphExtractor.totalNumProt] = id.getBytes();
                    ProteinGraphExtractor.totalNumProt++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int initialize() {

        try {
            //Read and set configuration values from file
            BufferedReader configBR = new BufferedReader(new FileReader(Configuration.configGraphPath));

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
                if (parts[0].equals("verboseConsole")) {
                    Configuration.verboseConsole = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("allProteome")) {
                    Configuration.allProteome = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("inputListFile")) {
                    Configuration.inputListFile = parts[1].replace("\\", "/");
                } else if (parts[0].equals("unitType")) {
                    Configuration.unitType = Configuration.ProteinType.valueOf(parts[1]);
                } else if (parts[0].equals("configGraphPath")) {
                    Configuration.configGraphPath = parts[1].replace("\\", "/");
                } else if (parts[0].equals("maxNumProt")) {
                    Configuration.maxNumProt = Integer.valueOf(parts[1]);
                } else if (parts[0].equals("onlyNeighborsInList")) {
                    Configuration.onlyNeighborsInList = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("onlyOrderedEdges")) {
                    Configuration.onlyOrderedEdges = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("showMissingProteins")) {
                    Configuration.showMissingProteins = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("reactionNeighbors")) {
                    Configuration.reactionNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("complexNeighbors")) {
                    Configuration.complexNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("entityNeighbors")) {
                    Configuration.entityNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("candidateNeighbors")) {
                    Configuration.candidateNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("topLevelPathwayNeighbors")) {
                    Configuration.topLevelPathwayNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("pathwayNeighbors")) {
                    Configuration.pathwayNeighbors = Boolean.valueOf(parts[1]);
                } else if (parts[0].equals("outputGraphFileType")) {
                    Configuration.outputGraphFileType = Configuration.GraphType.valueOf(parts[1]);
                } else if (parts[0].equals("outputGraphFilePath")) {
                    Configuration.outputGraphFilePath = parts[1].replace("\\", "/");
                } else if (parts[0].equals("outputFileName")) {
                    Configuration.outputFileName = parts[1];
                }
            }

            proteins = new byte[21000][];
            ConnectionNeo4j.driver = GraphDatabase.driver(ConnectionNeo4j.host, AuthTokens.basic(ConnectionNeo4j.username, ConnectionNeo4j.password));

            totalNumProt = 0;
        } catch (FileNotFoundException ex) {
            System.out.println("Configuration file not found at: " + Configuration.configPath);
            Logger
                    .getLogger(ProteinGraphExtractor.class
                            .getName()).log(Level.SEVERE, null, ex);
            return 1;
        } catch (IOException ex) {
            System.out.println("Not possible to read the configuration file: " + Configuration.configPath);
            Logger
                    .getLogger(ProteinGraphExtractor.class
                            .getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

}
