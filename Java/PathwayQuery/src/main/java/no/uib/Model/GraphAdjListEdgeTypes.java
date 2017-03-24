package no.uib.Model;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
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

    /**
     * Translates from string ->  byte[] -> short number. This reduces the amount of
     * memory necessary to store the ids in memory.
     */
    public BiMapShortToByteArray verticesMapping;

    /**
     * Translates from string ->  byte[] -> byte number. This reduces the amount of
     * memory necessary to store the edge types in memory.
     */
    public BiMapByteToByteArray edgesMapping;
    public short numVertices;

    HashSet<AdjacentNeighbor>[] adjacencyList;

    /**
     * Create a new empty Graph
     *
     * @param numVertices
     */
    public GraphAdjListEdgeTypes(int numVertices) throws UnsupportedEncodingException {

        this.adjacencyList = (HashSet<AdjacentNeighbor>[]) new HashSet[numVertices];
        for (int I = 0; I < numVertices; I++) {
            adjacencyList[I] = new HashSet<>();
        }

        this.verticesMapping = new BiMapShortToByteArray(numVertices);
        this.edgesMapping = new BiMapByteToByteArray(9);

        byte cont = 0;
        for (EdgeTypes t : EdgeTypes.values()) {
            edgesMapping.put(cont, t.toString());
//            System.out.println("byte to string: " + edgesMapping.getString(cont));
//            System.out.println("string to byte: " + edgesMapping.getByte(t.toString()));
            cont++;
        }

//        Verify the contents of the edgesMapping
//        for (byte I = 0; I < edgesMapping.size(); I++) {
//            String edgeType = edgesMapping.getString(I);
//            String edgeLabel = EdgeLabels.valueOf(edgeType).toString();
//            System.out.println("byte --> edge type --> edge label: " + I + " --> " + edgeType + " --> " + edgeLabel);
//            System.out.println("edge label --> edge type --> byte: " + edgeLabel + " --> " + EdgeTypes.valueOf(edgeLabel).toString() + " --> " + edgesMapping.getByte(edgeType));
//        }
    }

    /**
     * Implement the abstract method for adding an edge.
     *
     * @param s {String} The source vertex of the edge (start).
     * @param d {String} The destination vertex of the edge (end).
     * @param type {EdgeTypes} Takes a value of the enum of EdgeTypes in
     * {@link AdjacentNeighbor}.
     * @throws java.io.UnsupportedEncodingException
     */
    public void addEdge(String s, String d, EdgeTypes type) throws UnsupportedEncodingException {
        short sNum = verticesMapping.getShort(s);
        short dNum = verticesMapping.getShort(d);
        byte t = (byte) edgesMapping.getByte(type.toString());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
        for (AdjacentNeighbor savedNeighbor : adjacencyList[sNum]) {    //Check if the neighbour is already in the set
            if (savedNeighbor.equals(n)) {
                return;
            }
        }
        adjacencyList[sNum].add(n);
    }

    /**
     * Adds an edge directly with the short vertex number and the
     * AdjacentNeighbour object.
     *
     * @param s {short} The source vertex of the edge (start).
     * @param n {AdjacentNeighbour} The object containing the byte number of the
     * neighbour and the byte number of the edge type.
     */
    public void addEdge(short s, AdjacentNeighbor n) {
        adjacencyList[s].add(n);
    }

    /**
     * Adds and edge without translating from String to short.
     *
     * @param s {short} The source vertex of the edge (start).
     * @param d {String} The destination vertex of the edge (end).
     * @param type {EdgeTypes} Takes a value of the enum of EdgeTypes
     */
    public void addEdge(short s, String d, EdgeTypes type) throws UnsupportedEncodingException {
        short dNum = verticesMapping.getShort(d);
        byte t = edgesMapping.getByte(type.toString());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
        adjacencyList[s].add(n);
    }

    public boolean containsVertex(String id) throws UnsupportedEncodingException {
        return verticesMapping.containsId(id);
    }

    /**
     * Get all the neighbours of the
     *
     * @param s
     * @return List<Integer> a list of indices of vertices.
     * @throws java.io.UnsupportedEncodingException
     */
    public HashSet<AdjacentNeighbor> getNeighbors(String s) throws UnsupportedEncodingException {
        short sNum = verticesMapping.getShort(s);
        return adjacencyList[sNum];
    }

    /**
     * Writes all the graph to a file in the sif format.
     *
     */
    public void writeSifGraph() {

        try (FileWriter arch = new FileWriter(Configuration.outputGraphFilePath + "/" + Configuration.outputFileName + "." + Configuration.outputGraphFileType.toString())) {
            for (short I = 0; I < this.numVertices; I++) {          //Iterate over all vertices
                String id = verticesMapping.getString(I);
                for (EdgeTypes t : EdgeTypes.values()) {            //Go through every edge type to print the grouped in one row of the file
                    boolean foundOne = false;
                    for (AdjacentNeighbor n : this.adjacencyList[I]) {  //Iterate over all the neighbours of the current vertex
                        String nType = edgesMapping.getString(n.getType());
                        if (nType.equals(t.toString())) {       //If it is of the type of neighbours that will be printed in this row.
                            String nId = verticesMapping.getString(n.getNum());
                            if (id.compareTo(nId) <= 0) {       //Allow only relations to vertices with higher lexicographical Id. Halves the number of edges.
                                if (nType.equals("cn") || nType.equals("ds") || nType.equals("os") || nType.equals("cs")) {   //Only applies for complex or set neighbours       
                                    continue;
                                }
                            }
                            if (!foundOne) {                    //Raise flag that there are neighbors if this type
                                foundOne = true;
                                arch.write(id + " " + t.toString());
                            }
                            arch.write(" " + nId);
                        }
                    }
                    if (foundOne) {
                        arch.write("\n");
                    }
                }
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

    public enum EdgeLabels {
        cn {
            public String toString() {
                return "Complex";
            }
        },
        cs {
            public String toString() {
                return "CandidateSet";
            }
        },
        ds {
            public String toString() {
                return "DefinedSet";
            }
        },
        os {
            public String toString() {
                return "OpenSet";
            }
        },
        io {
            public String toString() {
                return "InputToOutput";
            }
        },
        ci {
            public String toString() {
                return "CatalystToInput";
            }
        },
        co {
            public String toString() {
                return "CatalystToOutput";
            }
        },
        ri {
            public String toString() {
                return "RegulatorToInput";
            }
        },
        ro {
            public String toString() {
                return "RegulatorToOutput";
            }
        }
    }

    public enum EdgeTypes {
        Complex {
            public String toString() {
                return "cn";
            }
        },
        CandidateSet {
            public String toString() {
                return "cs";
            }
        },
        DefinedSet {
            public String toString() {
                return "ds";
            }
        },
        OpenSet {
            public String toString() {
                return "os";
            }
        },
        InputToOutput {
            public String toString() {
                return "io";
            }
        },
        CatalystToInput {
            public String toString() {
                return "ci";
            }
        },
        CatalystToOutput {
            public String toString() {
                return "co";
            }
        },
        RegulatorToInput {
            public String toString() {
                return "ri";
            }
        },
        RegulatorToOutput {
            public String toString() {
                return "ro";
            }
        }
    }

}
