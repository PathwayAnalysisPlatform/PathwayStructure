package no.uib.DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.Model.AdjacentNeighbor;
import no.uib.Model.GraphAdjListEdgeTypes;
import no.uib.Model.Pair;
import no.uib.Model.Reaction;
import no.uib.pathwayquery.Configuration;
import static no.uib.pathwayquery.ProteinGraphExtractor.G;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class ReactomeAccess {

    /**
     * Gets the complex or set neighbours of all the proteins contained in the
     * graph. The graph G must me initialised already in
     * {@link ProteinGraphExtractor}
     */
    public static void getComplexOrSetNeighbors() {
        //Iterate over the proteins in the Graph
        for (short I = 0; I < G.numVertices; I++) {
//            if (!G.verticesMapping.getId(I).equals("P31749")) {
//                continue;
//            }
            System.out.println("Getting neighbours of: " + I + " " + G.verticesMapping.getId(I));

            for (AdjacentNeighbor n : queryComplexOrSetNeighbours(G.verticesMapping.getId(I))) {
                G.addEdge(I, n);
            }
        }
    }

    /**
     * Makes a query to neo4j to get the complex and set neighbours of a
     * specific protein.
     */
    private static List<AdjacentNeighbor> queryComplexOrSetNeighbours(String id) {
        List<AdjacentNeighbor> neighboursList = new ArrayList<AdjacentNeighbor>();
        try (Session session = ConnectionNeo4j.driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(e)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                    + "WHERE ANY (l IN labels(e) WHERE l IN [";
            boolean setOne = false;
            if (Configuration.cn) {
                if (setOne) {
                    query += ", ";
                }
                query += "'Complex'";
                setOne = true;
            }
            if (Configuration.ds) {
                if (setOne) {
                    query += ", ";
                }
                query += "'DefinedSet'";
                setOne = true;
            }
            if (Configuration.cs) {
                if (setOne) {
                    query += ", ";
                }
                query += "'CandidateSet'";
                setOne = true;
            }
            if (Configuration.os) {
                if (setOne) {
                    query += ", ";
                }
                query += "'OpenSet'";
                setOne = true;
            }
            query += "]) RETURN DISTINCT last(labels(e)) as role, nP.identifier as id";
            StatementResult result = tx.run(query, Values.parameters("id", id));
            List<Record> records = result.list();
            for (Record r : records) {
                String n = r.get("id").asString(); //Get the neighbour id as a string.
                String t = r.get("role").asString(); //Get the edge type as a string
                switch (t) {
                    case "Complex":
                        if (!Configuration.cn) {
                            continue;
                        }
                        break;
                    case "DefinedSet":
                        if (!Configuration.ds) {
                            continue;
                        }
                        break;
                    case "OpenSet":
                        if (!Configuration.os) {
                            continue;
                        }
                        break;
                    case "CandidateSet":
                        if (!Configuration.cs) {
                            continue;
                        }
                        break;
                }
                //System.out.println(t + " " + n);
                if (G.containsVertex(n)) {
                    short nShort = G.verticesMapping.getNum(n);
                    byte tByte = G.edgesMapping.getNum(GraphAdjListEdgeTypes.EdgeTypes.valueOf(t).toString());
                    //System.out.println(tByte + " " + nShort);
                    neighboursList.add(new AdjacentNeighbor(nShort, tByte));
                } else {
                    System.out.println("Vertex " + n + " not found in list.");
                }
            }
        }

        return neighboursList;
    }

    /**
     * Adds to the graph in {@link ProteinGraphExtractor} the
     * interactions(edges) between proteins(vertices) in the same reaction.
     * Creates a file with all reactions in Reactome and the participants with
     * their role in the reaction.
     * <p>
     * If the number of proteins in the input is more than 100 then all
     * reactions are fetched from Reactome. The file does not exist then it
     * creates it. Afterwards, the reaction file read through once to calculate
     * the interactions.</p>
     * <p>
     * If the number of proteins is less than or equal to 100, then the
     * reactions for each participant are queried from Reactome in the
     * moment.</p>
     *
     */
    public static void getReactionNeighbors() {

        //Check if file with all reactions exists
        File f = new File("./Reactions.txt");
        if (!f.exists() || f.isDirectory()) {
            //Write all reactions with their participants to file
            createReactionsFile();
        }

        try {
            //Iterate over all reactions of the file asking for their participants and roles
            BufferedReader reactionsBR = new BufferedReader(new FileReader(Configuration.reactionsFile));
            String line = "";
            try {
                while ((line = reactionsBR.readLine()) != null) //Read a reaction row
                {
                    Reaction r = new Reaction(line);
                    
//                    if(r.stId.equals("R-HSA-382613")){
//                        int i = 8;
//                    }

                    //Check how many participants of this reaction are in the input protein list
                    //If there are at least two participants of this reaction contained in the input list
                    if (checkReactionIsCompatible(r)) {

                        for (Pair<String, String> interaction : r.getIOInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), GraphAdjListEdgeTypes.EdgeTypes.InputToOutput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getCIInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), GraphAdjListEdgeTypes.EdgeTypes.CatalystToInput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getCOInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), GraphAdjListEdgeTypes.EdgeTypes.CatalystToOutput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getRIInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), GraphAdjListEdgeTypes.EdgeTypes.RegulatorToInput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getROInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), GraphAdjListEdgeTypes.EdgeTypes.RegulatorToOutput);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ReactomeAccess.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error reading the content of reactions file.");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReactomeAccess.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error opening reactions file.");
        }
    }

    /**
     * Get the reactions associated with the requested proteins. Reads directly
     * from Neo4j. Writes 5 columns in the file: Reaction stId, catalysts,
     * inputs, outputs and regulators Each column is separated with "," Each id
     * inside the role columns is separated with ";"
     */
    public static void createReactionsFile() {
        System.out.println("Creating reactions file...");
        try {
            FileWriter reactionsFW = new FileWriter("./Reactions.txt"); //Create or empty the file for reactions
            Session session = ConnectionNeo4j.driver.session();
            String query = "MATCH (n:ReactionLikeEvent) RETURN n.stId as reaction";
            StatementResult result = session.run(query);
            List<Record> records = result.list();
            int progress = 0;
            int currentProgress = 0;
            int processed = 0;

            System.out.println(" 0%");
            for (Record r : records) {
                String stId = r.get("reaction").asString();
                if (stId.equals("R-HSA-382613")) {
                    System.out.print("");
                }
                List<Record> participantRecords = getReactionParticipantsWithRoles(stId);
                if (records.size() > 0) {
                    try {

                        reactionsFW.write(stId);    //Send the reaction stId to the beginning of the row
                        boolean sendOne = false;
                        boolean hasInput = false;
                        boolean hasOutput = false;
                        boolean hasCatalyst = false;
                        boolean hasRegulator = false;
                        for (Record participant : participantRecords) {
                            if (participant.get("protein").asString().length() > 6) {
                                continue;
                            }
                            String role = participant.get("role").asString();

                            switch (role) {
                                case "catalystActivity":
                                    if (hasCatalyst) {
                                        reactionsFW.write(";"); //Write separator of protein inside the columns of catalyst proteins
                                    } else {
                                        reactionsFW.write(",");   //Write separator of column in the file
                                    }
                                    hasCatalyst = true;
                                    reactionsFW.write(participant.get("protein").asString());
                                    break;
                                case "input":
                                    if (hasInput) {
                                        reactionsFW.write(";"); //Write separator of protein inside the columns of input proteins
                                    } else {
                                        if (!hasCatalyst) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasCatalyst = true;
                                        }
                                        reactionsFW.write(",");   //Write separator of column in the file
                                    }
                                    hasInput = true;
                                    reactionsFW.write(participant.get("protein").asString());
                                    break;
                                case "output":
                                    if (hasOutput) {
                                        reactionsFW.write(";"); //Write separator of protein inside the columns of output proteins
                                    } else {
                                        if (!hasCatalyst) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasCatalyst = true;
                                        }
                                        if (!hasInput) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasInput = true;
                                        }
                                        reactionsFW.write(",");   //Write separator of column in the file
                                    }
                                    hasOutput = true;
                                    reactionsFW.write(participant.get("protein").asString());
                                    break;
                                case "regulatedBy":
                                    if (hasRegulator) {
                                        reactionsFW.write(";"); //Write separator of protein inside the columns of output proteins
                                    } else {
                                        if (!hasCatalyst) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasCatalyst = true;
                                        }
                                        if (!hasInput) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasInput = true;
                                        }
                                        if (!hasOutput) {
                                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                                            hasOutput = true;
                                        }
                                        reactionsFW.write(",");   //Write separator of column in the file
                                    }
                                    hasRegulator = true;
                                    reactionsFW.write(participant.get("protein").asString());
                                    break;
                            }
                        }
                        if (!hasCatalyst) {
                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                            hasCatalyst = true;
                        }
                        if (!hasInput) {
                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                            hasInput = true;
                        }
                        if (!hasOutput) {
                            reactionsFW.write(","); //If there were no proteins of the previous type, then send the separator to move to this column
                            hasOutput = true;
                        }
                        if (!hasRegulator) {
                            reactionsFW.write(","); //If there were no proteins of any type, send the separators to create the columns
                            hasRegulator = true;
                        }
                        reactionsFW.write("\n");

                    } catch (IOException ex) {
                        Logger.getLogger(ReactomeAccess.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
                processed++;
                currentProgress = processed * 100 / records.size();
                if (currentProgress % 10 == 0) {
                    if (currentProgress > progress) {
                        progress = currentProgress;
                        if (progress < 10) {
                            System.out.print(" ");
                        }
                        System.out.println(progress + "%");
                    }
                }
            }
            reactionsFW.close();
            session.close();

        } catch (IOException ex) {
            Logger.getLogger(ReactomeAccess.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Makes a query to Reactome in Neo4j to get the participant proteins with
     * their role in the reaction. The proteins are returned as a string using
     * the uniprot id. The labels used to describe the roles of a protein are:
     * input, output, regulatedBy and catalystActivity
     *
     *
     * @param reactionId {String} The id is a "stId" in the Reactome language.
     * @return A list of the records with the fields: "protein", "role".
     */
    private static List<Record> getReactionParticipantsWithRoles(String reactionId) {
        Session session = ConnectionNeo4j.driver.session();
        String query = "MATCH (rle:ReactionLikeEvent{stId:{id}})-[role:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity{databaseName:'UniProt'})\n"
                + "RETURN DISTINCT re.identifier as protein, head(extract(x IN role | type(x))) as role ORDER BY role";         //Get all the proteins that play a role in this reaction
        StatementResult result = session.run(query, Values.parameters("id", reactionId));

        session.close();
        return result.list();
    }

    private static List<Record> getReactionsContainingAProtein(String proteinId) {
        return null;

    }

    private static boolean checkReactionIsCompatible(Reaction r) {
        int foundParticipants = 0;
        for (String p : r.getParticipants()) {
            if (G.containsVertex(p)) {
                foundParticipants++;
            }
        }
        return (foundParticipants >= 2);
    }
}
