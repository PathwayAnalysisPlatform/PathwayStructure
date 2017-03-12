package no.uib.Model;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public BiMapShortString verticesMapping;
    public BiMapByteString edgesMapping;
    public short numVertices;

    ArrayList<AdjacentNeighbor>[] adjacencyList;

    /**
     * Create a new empty Graph
     *
     * @param numVertices
     */
    public GraphAdjListEdgeTypes(int numVertices) {

        this.adjacencyList = (ArrayList<AdjacentNeighbor>[]) new ArrayList[numVertices];
        for (int I = 0; I < numVertices; I++) {
            adjacencyList[I] = new ArrayList<>();
        }

        this.verticesMapping = new BiMapShortString(numVertices);
        this.edgesMapping = new BiMapByteString(9);

        byte cont = 0;
        for (EdgeTypes t : EdgeTypes.values()) {
            edgesMapping.put(cont, t.toString());
//            System.out.println("byte to string: " + edgesMapping.getId(cont));
//            System.out.println("string to byte: " + edgesMapping.getNum(t.toString()));
            cont++;
        }

//        Verify the contents of the edgesMapping
//        for (byte I = 0; I < 9; I++) {
//            String edgeType = edgesMapping.getId(I);
//            String edgeLabel = EdgeLabels.valueOf(edgeType).toString();
//            System.out.println("byte --> edge type --> edge label: " + I + " --> " + edgeType + " --> " + edgeLabel);
//            System.out.println("edge label --> edge type --> byte: " + edgeLabel + " --> " + EdgeTypes.valueOf(edgeLabel).toString() + " --> " + edgesMapping.getNum(edgeType));
//        }
    }

    /**
     * Implement the abstract method for adding an edge.
     *
     * @param s {String} The source vertex of the edge (start).
     * @param d {String} The destination vertex of the edge (end).
     * @param type {EdgeTypes} Takes a value of the enum of EdgeTypes in
     * {@link AdjacentNeighbor}.
     */
    public void addEdge(String s, String d, EdgeTypes type) {
        short sNum = verticesMapping.getNum(s);
        short dNum = verticesMapping.getNum(d);
        byte t = edgesMapping.getNum(type.toString());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
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
    public void addEdge(short s, String d, EdgeTypes type) {
        short dNum = verticesMapping.getNum(d);
        byte t = edgesMapping.getNum(type.toString());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
        adjacencyList[s].add(n);
    }

    public boolean containsVertex(String id) {
        return verticesMapping.containsId(id);
    }

    /**
     * Get all the neighbours of the
     *
     * @param s
     * @return List<Integer> a list of indices of vertices.
     */
    public ArrayList<AdjacentNeighbor> getNeighbors(String s) {
        short sNum = verticesMapping.getNum(s);
        return adjacencyList[sNum];
    }

    /**
     * Writes all the graph to a file in the sif format.
     *
     */
    public void writeSifGraph() {

        try (FileWriter arch = new FileWriter(Configuration.outputGraphFilePath + "/" + Configuration.outputFileName + "/" + Configuration.outputGraphFilePath)) {
            for (short I = 0; I < this.numVertices; I++) {          //Iterate over all vertices
                String id = verticesMapping.getId(I);
                for (EdgeTypes t : EdgeTypes.values()) {            //Go through every edge type to print the grouped in one row of the file
                    boolean foundOne = false;
                    for (AdjacentNeighbor n : this.adjacencyList[I]) {  //Iterate over all the neighbours of the current vertex
                        if (edgesMapping.getId(n.getType()).equals(t.toString())) {
                            String nId = verticesMapping.getId(n.getNum());
                            if (id.compareTo(nId) <= 0) {            //Allow only relations to vertices with higher lexicographical Id. Halves the number of edges.
                                if (!foundOne) {                    //Raise flag that there are neighbors if this type
                                    foundOne = true;
                                    arch.write(id + " " + t.toString());
                                }
                                arch.write(" " + nId);
                            }
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
