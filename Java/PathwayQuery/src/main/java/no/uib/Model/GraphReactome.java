/*
 * Copyright 2017 Luis Francisco Hernández Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uib.Model;

import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwayquery.Conf;
import no.uib.pathwayquery.Conf.EdgeType;
import no.uib.pathwayquery.ProteinGraphExtractor;
import org.neo4j.driver.v1.Record;

// Graph stored as an adjacency list. 
// It can store edge types.
// The number of vertices is fixed.
public class GraphReactome {

    /**
     * Translates from string -> byte[] -> int number. This reduces the amount
     * of memory necessary to store the ids in memory.
     */
    private BiMapIntToByteArray verticesMapping;

    /**
     * Translates from string -> byte[] -> byte number. This reduces the amount
     * of memory necessary to store the edge types in memory.
     */
    private BiMapByteToByteArray edgesMapping;

    private HashSet<AdjacentNeighbor>[] adjacencyList;

    /**
     * Create a new empty Graph
     *
     * @param numVertices
     */
    public GraphReactome(int numVertices) throws UnsupportedEncodingException {

        this.adjacencyList = (HashSet<AdjacentNeighbor>[]) new HashSet[numVertices];
        for (int I = 0; I < numVertices; I++) {
            adjacencyList[I] = new HashSet<>();
        }

        this.verticesMapping = new BiMapIntToByteArray(numVertices);
        this.edgesMapping = new BiMapByteToByteArray(21);

        byte cont = 0;
        for (EdgeType t : EdgeType.values()) {
            edgesMapping.put(cont, t.toString());
            System.out.println("byte to string: " + edgesMapping.getString(cont));
            System.out.println("string to byte: " + edgesMapping.getByte(t.toString()));
            cont++;
        }
        //Verify the contents of the edgesMapping
//        for (byte I = 0; I < edgesMapping.size(); I++) {
//            String edgeType = edgesMapping.getString(I);
//            String edgeLabel = Conf.EdgeLabel.valueOf(edgeType).toString();
//            System.out.println("byte --> edge type --> edge label: " + I + " --> " + edgeType + " --> " + edgeLabel);
//            System.out.println("edge label --> edge type --> byte: " + edgeLabel + " --> " + EdgeType.valueOf(edgeLabel).toString() + " --> " + edgesMapping.getByte(edgeType));
//        }
    }

    /**
     * Create Graph from file.
     *
     * @param path {String} The path to the file that contains the graph.
     */
    public GraphReactome(String path, int numVertices) throws UnsupportedEncodingException {
        
        //TODO when the edges are of certain types, I have to duplicate the edge in the reverse order
        this.adjacencyList = (HashSet<AdjacentNeighbor>[]) new HashSet[numVertices];
        for (int I = 0; I < numVertices; I++) {
            adjacencyList[I] = new HashSet<>();
        }

        this.verticesMapping = new BiMapIntToByteArray(numVertices);
        this.edgesMapping = new BiMapByteToByteArray(21);

        byte cont = 0;
        for (EdgeType t : EdgeType.values()) {
            edgesMapping.put(cont, t.toString());
            //System.out.println("byte to string: " + edgesMapping.getString(cont));
            //System.out.println("string to byte: " + edgesMapping.getByte(t.toString()));
            cont++;
        }

        int index = 0;
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(path));
            int c;
            String source = "";
            String type = "";
            String destiny = "";
            while ((c = input.read()) != -1) {
                char character = (char) c;
                //Read all the characters of the source
                do {
                    character = (char) c;
                    if (character == '\n' || character == ' ') {
                        break;
                    }
                    source += character;
                } while ((c = input.read()) != -1);
                if (!this.containsVertex(source)) {
                    this.addVertex(source);
                }

                //Read the type of edge
                c = input.read();
                type += (char) c;
                c = input.read();
                type += (char) c;

                c = input.read();
                //Read all the neighbours
                while ((c = input.read()) != -1) {
                    character = (char) c;
                    if (character == ' ' || character == '\n') {
                        if (!this.containsVertex(destiny)) {
                            this.addVertex(destiny);
                        }
                        EdgeType t = EdgeType.valueOf(Conf.EdgeLabel.valueOf(type).toString());
                        this.addEdge(source, destiny, t);
                        destiny = "";
                        if (character == '\n') {
                            type = "";
                            source = "";
                            break;
                        }
                    } else {
                        destiny += (char) c;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProteinGraphExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumVertices() {
        return verticesMapping.size();
    }

    /**
     * Adds an edge directly with the int vertex number and the
     * AdjacentNeighbour object.
     *
     * @param source {int} The source vertex of the edge (start).
     * @param n {AdjacentNeighbour} The object containing the byte number of the
     * neighbour and the byte number of the edge type.
     */
    public void addEdge(int source, AdjacentNeighbor n) {
        adjacencyList[source].add(n);
    }

    /**
     * Adds and edge without translating from String to int.
     *
     * @param source {int} The source vertex of the edge (start).
     * @param destiny {String} The destination vertex of the edge (end).
     * @param type {EdgeType} Takes a value of the enum of EdgeType
     */
    public void addEdge(int source, String destiny, EdgeType type) throws UnsupportedEncodingException {
        int dNum = verticesMapping.getInt(destiny);
        byte t = edgesMapping.getByte(type.toString());
        AdjacentNeighbor n = new AdjacentNeighbor(dNum, t);
        adjacencyList[source].add(n);
    }

    /**
     * Implement the abstract method for adding an edge.
     *
     * @param source {String} The source vertex of the edge (start).
     * @param destiny {String} The destination vertex of the edge (end).
     * @param type {EdgeType} Takes a value of the enum of EdgeType in
     * {@link AdjacentNeighbor}.
     * @throws java.io.UnsupportedEncodingException
     */
    public void addEdge(String source, String destiny, EdgeType type) throws UnsupportedEncodingException {
        int sNum = verticesMapping.getInt(source);
        int dNum = verticesMapping.getInt(destiny);
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
     * Get all the neighbours of the
     *
     * @param s
     * @return List<Integer> a list of indices of vertices.
     * @throws java.io.UnsupportedEncodingException
     */
    public HashSet<AdjacentNeighbor> getNeighbors(String s) throws UnsupportedEncodingException {
        int sNum = verticesMapping.getInt(s);
        return adjacencyList[sNum];
    }

    /**
     * Adds to the graph all the edges represented in the Records list. Every
     * record should contain two string fields named "source" and "destiny".
     *
     * @param records {List<Record>} The list of Record containing all the
     * edges.
     */
    public void addAllEdges(List<Record> records, Conf.EdgeType t) throws UnsupportedEncodingException {
        if (records == null) {
            return;
        }
        for (Record r : records) {
            String source = r.get("source").asString(); //Get the source vertex as a string
            String destiny = r.get("destiny").asString(); //Get the destiny vertex as a string
            this.addEdge(source, destiny, t);
        }
    }

    public void addVertex(String id) throws UnsupportedEncodingException {
        verticesMapping.put(id);
    }

    /**
     * Adds to the graph all the vertices represented in the Records list. Every
     * record should contain one string field named "id".
     *
     * @param records {List<Record>} The list of Record containing all the stIds
     * of the vertices.
     */
    public void addAllVertices(List<Record> records) throws UnsupportedEncodingException {
        if (records == null) {
            return;
        }
        for (Record r : records) {
            String id = r.get("id").asString(); //Get the vertex id as a string
            addVertex(id);
        }
    }

    public boolean containsVertex(String id) throws UnsupportedEncodingException {
        return verticesMapping.containsId(id);
    }

    public String getVertexId(int index) {
        return verticesMapping.getString(index);
    }

    /**
     * Writes all the graph to a file in the sif format.
     *
     */
    public void writeSifGraph() {

        try (FileWriter arch = new FileWriter(Conf.strMap.get(Conf.strVars.outputGraphFilePath.toString())
                + "/" + Conf.strMap.get(Conf.strVars.outputFileName.toString())
                + "." + Conf.strMap.get(Conf.strVars.outputGraphFileType.toString()))) {
            for (int I = 0; I < getNumVertices(); I++) {          //Iterate over all vertices
                String id = verticesMapping.getString(I);
                boolean anyNeighborAnyType = false;
                for (EdgeType t : EdgeType.values()) {            //Go through every edge type to print the grouped in one row of the file
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
                        anyNeighborAnyType = true;
                    }
                }
                if (Conf.boolMap.get(Conf.boolVars.showIsolatedVertices.toString())) {
                    if (!anyNeighborAnyType) {
                        arch.write(id + "\n");
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeGraphToFile() {
        switch (Conf.strMap.get(Conf.strVars.outputGraphFileType.toString())) {
            case "sif":
                writeSifGraph();
        }
    }

    public byte[][] shortestUnweightedPaths(TreeSet<String> proteinSet) throws UnsupportedEncodingException {
        TObjectShortHashMap<String> labelToIndex = new TObjectShortHashMap<String>();
        byte[][] d = new byte[proteinSet.size()][proteinSet.size()];

        //For each vertex on the list, perform Breadth-first search
        //Initialize distances
        for (int r = 0; r < proteinSet.size(); r++) {
            for (int c = 0; c < proteinSet.size(); c++) {
                d[r][c] = Byte.MAX_VALUE;
            }
        }
        int index = 0;
        for (String p : proteinSet) {
            labelToIndex.put(p, (short) index);
            index++;
        }

        // Get the shortest path from every vertex to every other vertex
        for (String source : proteinSet) {

            System.out.println("Calculating distances from: " + source);

            TIntSet visited = new TIntHashSet();
            HashSet<String> toBeVisited = new HashSet<>();
            Queue< Pair<String, Byte>> queued;
            queued = new LinkedList<Pair<String, Byte>>();

            for (String p : proteinSet) {
                toBeVisited.add(p);
            }

            visited.add(this.verticesMapping.getInt(source));
            queued.add(new Pair<>(source, (byte) 0));

            int cont = 0;
            int percentage = 0;
            System.out.print("Percentage of vertices visited: " + percentage + "% ");
            while (queued.size() > 0 && toBeVisited.size() > 0) {
                Pair<String, Byte> current = queued.poll();
                cont++;

                int newPercentage = cont * 100 / this.getNumVertices();
                if (newPercentage > percentage) {
                    percentage = newPercentage;
                    if (newPercentage % 4 == 0) { 
                        System.out.print(percentage + "% ");
                    }
                }

                if (proteinSet.contains(current.getL())) {
                    d[labelToIndex.get(source)][labelToIndex.get(current.getL())] = current.getR();
                    toBeVisited.remove(current.getL());
                }
                //Try to visit each neighbor of this vertex
                index = this.verticesMapping.getInt(current.getL());
                for (AdjacentNeighbor n : this.adjacencyList[index]) {
                    String nLabel = verticesMapping.getString(n.getNum());
                    if (!visited.contains(n.getNum())) {    //Add to the queue if it has not been visited
                        byte newDist = (byte) (current.getR() + 1);
                        queued.add(new Pair<String, Byte>(nLabel, newDist));
                        visited.add(this.verticesMapping.getInt(nLabel));
                    }
                }
            }
            System.out.println("");
        }

        //Print the values
        System.out.print("\t\t");
        for (String p : proteinSet) {
            System.out.print(p + "\t");
        }
        System.out.println("");
        int r = 0;
        for (String p : proteinSet) {
            System.out.print(p + "\t\t");
            for (int c = 0; c < proteinSet.size(); c++) {
                System.out.print(d[r][c] + "\t\t");
                d[r][c] = 0;
            }
            System.out.println("");
            r++;
        }

        return d;
    }
}
