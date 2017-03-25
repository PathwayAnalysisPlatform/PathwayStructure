package no.uib.DB;

import no.uib.pathwayquery.Conf;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * @author Luis Francisco Hernández Sánchez
 */
public class ConnectionNeo4j {
    
    public static String host = "bolt://localhost";
    public static String username = "neo4j";
    public static String password = "neo4j2";
    
    public static Driver driver = GraphDatabase.driver(Conf.strMap.get(Conf.strVars.host.toString()), AuthTokens.basic(Conf.strMap.get(Conf.strVars.username.toString()), Conf.strMap.get(Conf.strVars.password.toString())));
}
