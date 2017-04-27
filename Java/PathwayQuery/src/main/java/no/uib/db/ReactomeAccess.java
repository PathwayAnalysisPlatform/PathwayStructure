package no.uib.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.model.Pair;
import no.uib.model.Reaction;
import no.uib.pathwayquery.Conf;
import static no.uib.pathwayquery.ProteinGraphExtractor.G;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import no.uib.db.ReactomeQueries.Queries;

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
    public static void getComplexOrSetNeighbors() throws UnsupportedEncodingException {
        //Iterate over the proteins in the Graph
        for (int I = 0; I < G.getNumVertices(); I++) {
            if (G.getVertexId(I).length() > 6) {
                continue;
            }
            System.out.println("Getting neighbours of: " + I + " " + G.getVertexId(I));
            List<Record> records = queryComplexOrSetNeighbours(G.getVertexId(I));

            for (Record r : records) {
                String n = r.get("id").asString(); //Get the neighbour id as a string.
                String t = r.get("role").asString() + "Neighbor"; //Get the edge type as a string
                switch (t) {
                    case "Complex":
                        if (!Conf.boolMap.get(Conf.EdgeType.ComplexNeighbor.toString())) {
                            continue;
                        }
                        break;
                    case "DefinedSet":
                        if (!Conf.boolMap.get(Conf.EdgeType.DefinedSetNeighbor.toString())) {
                            continue;
                        }
                        break;
                    case "OpenSet":
                        if (!Conf.boolMap.get(Conf.EdgeType.OpenSetNeighbor.toString())) {
                            continue;
                        }
                        break;
                    case "CandidateSet":
                        if (!Conf.boolMap.get(Conf.EdgeType.CandidateSetNeighbor.toString())) {
                            continue;
                        }
                        break;
                }
                //System.out.println(t + " " + n);
                if (G.containsVertex(n)) {
                    G.addEdge(G.getVertexId(I), n, Conf.EdgeType.valueOf(t));
                } else {
                    System.out.println("Vertex " + n + " not found in list.");
                }
            }
        }
    }

    /**
     * Makes a query to neo4j to get the complex and set neighbours of a
     * specific protein.
     */
    private static List<Record> queryComplexOrSetNeighbours(String id) throws UnsupportedEncodingException {

        try (Session session = ConnectionNeo4j.driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (re:ReferenceEntity{identifier:{id}})<-[:referenceEntity]-(p:EntityWithAccessionedSequence)<-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]-(e)-[:hasComponent|hasMember|hasCandidate|repeatedUnit*]->(nE:EntityWithAccessionedSequence)-[:referenceEntity]->(nP:ReferenceEntity)\n"
                    + "WHERE ANY (l IN labels(e) WHERE l IN [";
            boolean setOne = false;
            if (Conf.boolMap.get(Conf.EdgeType.ComplexNeighbor.toString())) {
                if (setOne) {
                    query += ", ";
                }
                query += "'Complex'";
                setOne = true;
            }
            if (Conf.boolMap.get(Conf.EdgeType.DefinedSetNeighbor.toString())) {
                if (setOne) {
                    query += ", ";
                }
                query += "'DefinedSet'";
                setOne = true;
            }
            if (Conf.boolMap.get(Conf.EdgeType.CandidateSetNeighbor.toString())) {
                if (setOne) {
                    query += ", ";
                }
                query += "'CandidateSet'";
                setOne = true;
            }
            if (Conf.boolMap.get(Conf.EdgeType.OpenSetNeighbor.toString())) {
                if (setOne) {
                    query += ", ";
                }
                query += "'OpenSet'";
                setOne = true;
            }
            query += "]) RETURN DISTINCT last(labels(e)) as role, nP.identifier as id";
            StatementResult result = tx.run(query, Values.parameters("id", id));

            return result.list();
        }
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
            BufferedReader reactionsBR = new BufferedReader(new FileReader(Conf.strMap.get(Conf.strVars.reactionsFile.toString())));
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
                                G.addEdge(interaction.getL(), interaction.getR(), Conf.EdgeType.InputToOutput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getCIInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), Conf.EdgeType.CatalystToInput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getCOInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), Conf.EdgeType.CatalystToOutput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getRIInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), Conf.EdgeType.RegulatorToInput);
                            }
                        }
                        for (Pair<String, String> interaction : r.getROInteractions()) {
                            if (G.containsVertex(interaction.getL()) && G.containsVertex(interaction.getR())) {
                                G.addEdge(interaction.getL(), interaction.getR(), Conf.EdgeType.RegulatorToOutput);
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
            String query = "MATCH (n:ReactionLikeEvent) \n"
                    + "WHERE n.speciesName = 'Homo sapiens'\n"
                    + "RETURN n.stId as reaction";
            StatementResult result = session.run(query);
            List<Record> records = result.list();
            int progress = 0;
            int currentProgress = 0;
            int processed = 0;

            System.out.println(" 0%");
            for (Record r : records) {
                String stId = r.get("reaction").asString();
//                if (stId.equals("R-HSA-382613")) {
//                    System.out.print("");
//                }
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

    private static boolean checkReactionIsCompatible(Reaction r) throws UnsupportedEncodingException {
        int foundParticipants = 0;
        for (String p : r.getParticipants()) {
            if (G.containsVertex(p)) {
                foundParticipants++;
            }
        }
        return (foundParticipants >= 2);
    }

    public static List<Record> getEdgesByType(Conf.EdgeType t) {

        String query = "";
        switch (t) {
            case ComplexHasProtein:
                query = "MATCH (c:Complex)-[:hasComponent]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT c.stId as source, re.identifier as destiny";
                break;
            case SetHasProtein:
                query = "MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE es.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT es.stId as source, re.identifier as destiny";
                break;
            case ComplexHasComplex:
                query = "MATCH (c1:Complex)-[:hasComponent]->(c2:Complex)\n"
                        + "WHERE c1.speciesName = 'Homo sapiens' AND c2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c1.stId as source, c2.stId as destiny";
                break;
            case SetHasComplex:
                query = "MATCH (es:EntitySet)-[:hasMember|hasCandidate]->(c:Complex)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c.stId as source, es.stId as destiny";
                break;
            case ComplexHasSet:
                query = "MATCH (c:Complex)-[:hasComponent]->(es:EntitySet)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c.stId as source, es.stId as destiny";
                break;
            case SetHasSet:
                query = "MATCH (es1:EntitySet)-[:hasMember|hasCandidate]->(es2:EntitySet)\n"
                        + "WHERE es1.speciesName = 'Homo sapiens' AND es2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT es1.stId as source, es2.stId as destiny";
                break;
            case ReactionHasProtein:
                query = "MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT r.stId AS source, re.identifier as destiny";
                break;
            case ReactionHasComplex:
                query = "MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(c:Complex)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND c.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT r.stId AS source, c.stId as destiny";
                break;
            case ReactionHasSet:
                query = "MATCH (r:Reaction)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(es:EntitySet)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT r.stId AS source, es.stId as destiny";
                break;
            case PathwayHasReaction:
                query = "MATCH (p:Pathway)-[:hasEvent]->(r:Reaction)\n"
                        + "WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT p.stId AS source, r.stId AS destiny";
                break;
            case PathwayHasPathway:
                query = "MATCH (p1:Pathway)-[:hasEvent]->(p2:Pathway)\n"
                        + "WHERE p1.speciesName = 'Homo sapiens' AND p2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT p1.stId as source, p2.stId as destiny";
                break;
            case ReactionChainedToReaction:
                query = "MATCH (r1:Reaction{stId:{id}})-[role1:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(ewas:EntityWithAccessionedSequence)<-[role2:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]-(r2:Reaction)\n"
                        + "WHERE r1.stId <> r2.stId AND head(extract(x IN role1 | type(x))) = 'output' AND last(extract(x IN role2 | type(x))) = 'input'\n"
                        + "RETURN DISTINCT r1.stId as source, r2.stId as destiny";
                break;
            default:
                return null;
        }

        Session session = ConnectionNeo4j.driver.session();
        StatementResult result = session.run(query);

        session.close();
        return result.list();
    }

    public static List<Record> getEdgesByTypeAndId(Conf.EdgeType t, String id) {

        String query = "";
        switch (t) {
            case ComplexHasProtein:
                query = "MATCH (c:Complex{stId:{id}})-[:hasComponent]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT c.stId as source, re.identifier as destiny";
                break;
            case SetHasProtein:
                query = "MATCH (es:EntitySet{stId:{id}})-[:hasMember|hasCandidate]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE es.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT es.stId as source, re.identifier as destiny";
                break;
            case ComplexHasComplex:
                query = "MATCH (c1:Complex{stId:{id}})-[:hasComponent]->(c2:Complex)\n"
                        + "WHERE c1.speciesName = 'Homo sapiens' AND c2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c1.stId as source, c2.stId as destiny";
                break;
            case SetHasComplex:
                query = "MATCH (es:EntitySet{stId:{id}})-[:hasMember|hasCandidate]->(c:Complex)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c.stId as source, es.stId as destiny";
                break;
            case ComplexHasSet:
                query = "MATCH (c:Complex{stId:{id}})-[:hasComponent]->(es:EntitySet)\n"
                        + "WHERE c.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT c.stId as source, es.stId as destiny";
                break;
            case SetHasSet:
                query = "MATCH (es1:EntitySet{stId:{id}})-[:hasMember|hasCandidate]->(es2:EntitySet)\n"
                        + "WHERE es1.speciesName = 'Homo sapiens' AND es2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT es1.stId as source, es2.stId as destiny";
                break;
            case ReactionHasProtein:
                query = "MATCH (r:Reaction{stId:{id}})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(ewas:EntityWithAccessionedSequence)-[:referenceEntity]->(re:ReferenceEntity)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND re.databaseName = 'UniProt'\n"
                        + "RETURN DISTINCT r.stId AS source, re.identifier as destiny";
                break;
            case ReactionHasComplex:
                query = "MATCH (r:Reaction{stId:{id}})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(c:Complex)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND c.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT r.stId AS source, c.stId as destiny";
                break;
            case ReactionHasSet:
                query = "MATCH (r:Reaction{stId:{id}})-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator*]->(es:EntitySet)\n"
                        + "WHERE r.speciesName = 'Homo sapiens' AND es.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT r.stId AS source, es.stId as destiny";
                break;
            case PathwayHasReaction:
                query = "MATCH (p:Pathway{stId:{id}})-[:hasEvent]->(r:Reaction)\n"
                        + "WHERE p.speciesName = 'Homo sapiens' AND r.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT p.stId AS source, r.stId AS destiny";
                break;
            case PathwayHasPathway:
                query = "MATCH (p1:Pathway{stId:{id}})-[:hasEvent]->(p2:Pathway)\n"
                        + "WHERE p1.speciesName = 'Homo sapiens' AND p2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT p1.stId as source, p2.stId as destiny";
                break;
            case ReactionChainedToReaction:
                query = "MATCH (r1:Reaction{stId:{id}})-[role1:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(ewas:EntityWithAccessionedSequence)<-[role2:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]-(r2:Reaction)\n"
                        + "WHERE r1.stId <> r2.stId AND head(extract(x IN role1 | type(x))) = 'output' AND last(extract(x IN role2 | type(x))) = 'input' AND r1.speciesName = 'Homo sapiens' AND r2.speciesName = 'Homo sapiens'\n"
                        + "RETURN DISTINCT r1.stId as source, r2.stId as destiny";
                break;
            default:
                return null;
        }

        Session session = ConnectionNeo4j.driver.session();
        StatementResult result = session.run(query, Values.parameters("id", id));

        session.close();
        return result.list();
    }

    /**
     * Gets the list of standard ids existent in Reactome for the specified type
     * of entity, such as Complex, Set, Reaction, Pathway. The proteins are
     * obtained from the input list or uniprot updated list.
     *
     * @param t {EntityType} Type of entity
     * @return List of object Record that contain a field called "id" that
     * contains the stId of every object of the specified type.
     */
    public static List<Record> getVerticesByType(Conf.EntityType t) {

        String query = "";
        switch (t) {
            case Complex:
                query = "MATCH (c:Complex) WHERE c.speciesName = 'Homo sapiens' RETURN c.stId as id";
                break;
            case Set:
                query = "MATCH (es:EntitySet) WHERE es.speciesName = 'Homo sapiens' RETURN es.stId as id";
                break;
            case Reaction:
                query = "MATCH (r:Reaction) WHERE r.speciesName = 'Homo sapiens' RETURN r.stId as id";
                break;
            case Pathway:
                query = "MATCH (p:Pathway) WHERE p.speciesName = 'Homo sapiens' RETURN p.stId as id";
                break;
            default:
                return null;
        }

        Session session = ConnectionNeo4j.driver.session();
        StatementResult result = session.run(query);

        session.close();
        return result.list();
    }

    public static List<String> getProteinListByMods(List<String> mods) {

        String query = ReactomeQueries.Queries.getProteinsByPsiMod.toString();
        mods.replaceAll(mod -> "\"" + mod + "\"");
        query = query.replace("{modList}", mods.toString());
        List<String> result = new ArrayList<>();

        Session session = ConnectionNeo4j.driver.session();
        StatementResult queryResult = session.run(query);

        for (Record record : queryResult.list()) {
            result.add(record.get("protein").asString());
        }

        session.close();
        return result;
    }
}
