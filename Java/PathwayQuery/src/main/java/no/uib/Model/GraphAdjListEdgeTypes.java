package no.uib.Model;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwayquery.Configuration;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
// Graph stored as an adjacency list. 
// It can store edge types.
// The number of vertices is fixed.
public class GraphAdjListEdgeTypes {

    public BiMapShortToByteArray verticesMapping;
    public BiMapByte edgesMapping;
    public short numVertices;

    ArrayList<AdjacentNeighbor>[] adjacencyList;

    /**
     * Create a new empty Graph
     *
     * @param numVertices
     */
    public GraphAdjListEdgeTypes(int numVertices) {

        this.adjacencyList = (ArrayList<AdjacentNeighbor>[]) new ArrayList[numVertices];
        Arrays.fill(adjacencyList, new ArrayList<>());

        this.verticesMapping = new BiMapShortToByteArray(numVertices);
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
     * @param s
     * @return List<Integer> a list of indices of vertices.
     */
    public ArrayList<AdjacentNeighbor> getNeighbors(String s) {
        short sNum = verticesMapping.getNum(s.getBytes());
        return adjacencyList[sNum];
    }

    /**
     * Writes all the graph to a file in the sif format.
     *
     * @param path The path comes complete, with the folder, file name and
     * ending
     */
    public void writeSifGraph() {

        try (FileWriter arch = new FileWriter(Configuration.outputGraphFilePath + "/" + Configuration.outputFileName + "/" + Configuration.outputGraphFilePath)) {
            for (short I = 0; I < this.numVertices; I++) {
                arch.write(verticesMapping.getStringId(I) + " " + " " + "\n");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphAdjListEdgeTypes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphAdjListEdgeTypes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeGraphToFile() {
        switch (Configuration.outputGraphFileType) {
            case sif:
                writeSifGraph();
        }
    }
}
