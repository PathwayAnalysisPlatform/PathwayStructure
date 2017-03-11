package no.uib.Model;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */

// Graph stored as an adjacency list. 
// It can store edge types.
// The number of vertices is fixed.
public class GraphAdjListEdgeTypes{
    
    public BiMapShort verticesMapping;
    public BiMapByte edgesMapping;
    
    ArrayList<AdjacentNeighbor>[] adjacencyList;

    /**
     * Create a new empty Graph
     * 
     * @param numVertices
     */
    public GraphAdjListEdgeTypes(short numVertices) {
        this.adjacencyList = (ArrayList<AdjacentNeighbor>[])new ArrayList[numVertices];
        Arrays.fill(adjacencyList, new ArrayList<>());
        
        new BiMapShort(numVertices);
    } 

    /**
     * Implement the abstract method for adding an edge.
     *
     * @param s
     * @param d
     * @param type
     */
    public void addEdge(String s, String d, AdjacentNeighbor.EdgeTypes type) {
        short sNum = verticesMapping.getNum(s.getBytes());
        short dNum = verticesMapping.getNum(d.getBytes());
        byte t = (byte) edgesMapping.getNum(type.toString().getBytes());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
        adjacencyList[sNum].add(n);
    }

    /**
     * Get all the neighbours of the 
     *
     * @param v the index of vertex.
     * @return List<Integer> a list of indices of vertices.
     */
    public ArrayList<AdjacentNeighbor> getNeighbors(String s) {
        short sNum = verticesMapping.getNum(s.getBytes());
        return adjacencyList[sNum];
    }
    
    /**
     * Writes all the graph to a file in the sif format.
     */
    public void writeSifGraph(String path){
        try {
            Writer arch = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            
            for (short I = 0; I < adjacencyList.length; I++) {
                arch.write(verticesMapping.getStringId(I) + " " + " " + "\n");
            }
            
            arch.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphAdjListEdgeTypes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphAdjListEdgeTypes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
