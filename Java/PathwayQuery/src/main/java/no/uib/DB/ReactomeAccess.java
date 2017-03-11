package no.uib.DB;

import no.uib.pathwayquery.Configuration;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

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

        String query = "MATCH (rle:ReactionLikeEvent)-[role:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity)\n" +
"WHERE re.identifier in [\"P00519\",\"P31749\",\"P11274\",\"P22681\"]\n" +
"RETURN DISTINCT rle.stId AS Reaction, re.identifier as Participant, extract(x IN role | type(x)) as Role";         //Get all the reactions where the requested proteins play a role
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

    public static void getComplexOrSetNeighbors() {
        
    }

    public static void getReactionNeighbors() {
        if (Configuration.allProteome) {
                ReactomeAccess.getAllReactions();
            } else {
                ReactomeAccess.getReactions();
            }
    }

}
