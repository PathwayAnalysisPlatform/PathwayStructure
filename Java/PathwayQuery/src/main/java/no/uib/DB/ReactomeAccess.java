package no.uib.DB;

import java.util.ArrayList;
import java.util.List;
import no.uib.Model.AdjacentNeighbor;
import no.uib.Model.GraphAdjListEdgeTypes;
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

    // Get the reactions associated with the requested proteins.
    // Reads directly from Neo4j
    public static void getReactions() {
        //Check if file with all reactions exists

        //If file does not exist
        //Iterate over all proteins and get their roles in all reactions
        //Store in a Reactions.csv file
        //Read through the reactions file once
        //If a protein I am interested in is present then use
        Session session = ConnectionNeo4j.driver.session();

        String query = "MATCH (rle:ReactionLikeEvent)-[role:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)\n"
                + "WHERE re.identifier in [\"P00519\",\"P31749\",\"P11274\",\"P22681\"]\n"
                + "RETURN DISTINCT rle.stId AS Reaction, re.identifier as Participant, extract(x IN role | type(x)) as Role";         //Get all the reactions where the requested proteins play a role
        //StatementResult queryResult = session.run(query, Values.parameters("id", id));

//        if (queryResult.hasNext()) {
//            
//        }
        session.close();
        ConnectionNeo4j.driver.close();
    }

    //
    public static void getAllReactions() {

    }

    /**
     * Gets the complex or set neighbours of all the proteins contained in the
     * graph. The graph G must me initialised already in
     * {@link ProteinGraphExtractor}
     */
    public static void getComplexOrSetNeighbors() {
        //Iterate over the proteins in the Graph
        for (short I = 0; I < G.numVertices; I++) {
//            if (!G.verticesMapping.getId(I).equals("P22681")) {
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
                    + "WHERE ANY (l IN labels(e) WHERE l IN ['Complex', 'DefinedSet', 'CandidateSet', 'EntitySet'])\n"
                    + "RETURN DISTINCT last(labels(e)) as role, nP.identifier as id";
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
                }
                else{
                    System.out.println("Vertex " +  n + " not found in list.");
                }
            }
        }

        return neighboursList;
    }

    public static void getReactionNeighbors() {
        if (Configuration.allProteome) {
            ReactomeAccess.getAllReactions();
        } else {
            ReactomeAccess.getReactions();
        }
    }

}
