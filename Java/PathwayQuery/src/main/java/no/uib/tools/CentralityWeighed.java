/*
 * Copyright 2017 Bram Burger.
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
package no.uib.tools;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.model.AdjacentNeighbor;
import no.uib.model.GraphReactome;

/**
 * This class computes centrality measures for a directed,
 * unweighed graph.
 * Code adapted from :
 * Ulrik Brandes
 * A Faster Algorithm for Betweenness Centrality
 * Journal of Mathematical Sociology 25(2):163-177, (2001).
 * 
 * The lessMonks.sif file refers to the Monks graph as described
 * and adapted in 
 * T.W. Valente and R.K. Foreman
 * Integration and radiality: measuring the extent of an
 * individual's connectedness and reachability in a network
 * Social Networks 20 (1998) 89-105
 *
 * @author Bram Burger
 */
public class CentralityWeighed {
    private static GraphReactome adjacencyList;
    private static double[][] centrality;
    // call as nShortestPaths[startNode][endNode]
    private static int[][] nShortestPaths;
    private static int[][] lengthShortestPaths;

    /**
     * 
     * @param args If empty or more than two elements, 
     * the input file will be the default.
     * If exactly one or two arguments is given the first arg will be 
     * taken to be the input file.
     * If exactly two arguments are gives, the second arg will be
     * taken to be the prefix for the output files.
     * @throws IOException 
     */
    public static void main(String args[]) throws IOException {
        String outPrefix = "";
        switch (args.length) {
            case 2:
                outPrefix = args[1];
            case 1:
                getGraph(args[0]);
                break;
            default:
                getGraph();
                break;
        }

        // get the number of proteins in the graph
        int nProteins = adjacencyList.getNumVertices();
        System.out.println(nProteins);

        initOutput(nProteins);

        long startTime = System.currentTimeMillis();
        runCentralityAlgorithm(nProteins);
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time Algorithm: "
                + (endTime - startTime) + " millisecs.");
        System.out.println("Finished calculating centralities.");

        System.out.println("Start printing centralities to files.");
        startTime = System.currentTimeMillis();
        centralitiesToCSV(outPrefix);
        nShortestToCSV(outPrefix);
        lengthShortestToCSV(outPrefix);
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time printing to files: "
                + (endTime - startTime) + " millisecs.");
    }

    /**
     * Generate the graph from a .sif file.
     * This function makes a graph from a default .sif file.
     * @throws UnsupportedEncodingException 
     */
    private static void getGraph ()
            throws UnsupportedEncodingException {
        // String path = "./AllInCatRegToOutput.sif";
        // String path = "./SignalTransductionInCatRegToOutput.sif";
        // String path = "./thing2.sif";
        String path = "./lessMonks.sif";

        // second number indicates how much space to reserve for graph
        // expressed in the number of nodes
        adjacencyList = new GraphReactome(path, 9000);
    }
    
    /**
     * Generate the graph from a .sif file.
     * This function takes a file name (and path) of an .sif file as input.
     * @param fileName the name of the sif file
     * @throws UnsupportedEncodingException 
     */
    private static void getGraph (String fileName) 
            throws UnsupportedEncodingException {
        // second number indicates how much space to reserve for graph
        // expressed in the number of nodes
        adjacencyList = new GraphReactome(fileName, 9000);
    }

    /**
     * Initialise the matrices which collect the output of the class.
     * These are the class level variables
     * @param nProteins the number of proteins in the graph.
     */
    private static void initOutput (int nProteins) {
        // Centrality measures
        // between, stess, radiality, integration,
        // closenessIn, closenessOut,
        // graphIn, graphOut
        // kIn, kOut
        // Matrix nrProteins rows, 10 columns
        // nProteins because of the mapping to integers for indices...
        centrality = new double[nProteins][12];

        // number of shortest paths (sigma)
        nShortestPaths = new int[nProteins][nProteins];
        // length of shortest paths (d)
        lengthShortestPaths = new int[nProteins][nProteins];
    }

    /**
     * This method runs the algorithm to compute shortest paths
     * and the centrality measures.
     * Implemented as a simple for loop over all nodes.
     * @param nProteins  the total number of proteins in the graph
     * (including proteins which have no outgoing edges)
     */
    private static void runCentralityAlgorithm (int nProteins)
            throws UnsupportedEncodingException {
        // For each of the proteins
        // compute centrality measures
        for (int i = 0; i < nProteins; i++) {
            getCentralityMeasures(i, nProteins);
        }

        calculateRadiality();
        calculateIntegration();
        getkIn();
        getkOut();
    }

    /**
     * This method computes shortest paths.
     * The output is written to a matrix.
     * The number and length of shortest paths is also computed.
     *
     * @param startNode the index of the node which is the starting
     * point for the shortest paths
     * @param nProteins the total number of proteins in the graph
     * (including proteins which have no outgoing edges)
     * @throws UnsupportedEncodingException
     */
    private static void getCentralityMeasures(int startNode, int nProteins)
            throws UnsupportedEncodingException {
        Deque<Integer> stack = new ArrayDeque<Integer>();
        Deque<Integer> queue = new ArrayDeque<Integer>();

        HashSet<Integer>[] parents = new HashSet[nProteins];
        for (int i = 0; i < nProteins; i++) {
            parents[i] = new HashSet<>();
        }
        // nr shortest paths to self is 1 by convention
        nShortestPaths[startNode][startNode] = 1;
        // initialise lengths of shortest paths
        for (int i = 0; i < nProteins; i++) {
            lengthShortestPaths[startNode][i] = -1;
        }
        lengthShortestPaths[startNode][startNode] = 0;

        queue.addLast(startNode);
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            stack.push(currentNode);
            HashSet<AdjacentNeighbor> neighbours =
                    adjacencyList.getNeighbors(adjacencyList.getVertexId(currentNode));

            for (AdjacentNeighbor currentNeighbour : neighbours) {
                int cNeighbour = currentNeighbour.getNum();
                // currentNeighbour found for first time?
                if (lengthShortestPaths[startNode][cNeighbour] < 0) {
                    queue.addLast(cNeighbour);
                    lengthShortestPaths[startNode][cNeighbour] =
                            lengthShortestPaths[startNode][currentNode]
                            + 1;
                }
                // shortest path to cNeighbour via startNode?
                if (lengthShortestPaths[startNode][cNeighbour]
                        == (lengthShortestPaths[startNode][currentNode] + 1)) {
                    nShortestPaths[startNode][cNeighbour] =
                            nShortestPaths[startNode][cNeighbour]
                            + nShortestPaths[startNode][currentNode];
                    parents[cNeighbour].add(currentNode);
                }
            }
        }

    }


    /**
     * Retrieve maximum value from integer list.
     * Breaks ugly if input is empty list.
     *
     * @param l integer list
     * @return highest value in the list
     */
    private static int getMaxFromIntList (int[] l) {
        int max = l[0];
        // iterate over remaining items
        for (int i = 1; i < l.length; i++) {
            if (l[i] > max) {
                max = l[i];
            }
        }
        return(max);
    }

    /**
     * Algorithm to calculate radiality.
     * Puts output in centrality[startNode][2]
     *
     */
    private static void calculateRadiality() {
        int diameter = getDiameter();
        // int maxRD = diameter + 1 - getMinShortestPath(diameter);
        
        for (int i = 0; i < lengthShortestPaths.length; i++) {
            int radiality = 0;
            for (int j = 0; j < lengthShortestPaths.length; j++) {
                if (lengthShortestPaths[i][j] > 0) {
                    radiality += 
                            diameter + 1 - lengthShortestPaths[i][j];
                }
            }
            centrality[i][2] = radiality / 
                    ((lengthShortestPaths.length - 1.0) * diameter);
        }
    }

    /**
     * Algorithm to calculate integration.
     * Puts output in centrality[startNode][3]
     *
     */
    private static void calculateIntegration () {
        int diameter = getDiameter();
        // int maxRD = diameter + 1 - getMinShortestPath(diameter);
        
        for (int i = 0; i < lengthShortestPaths.length; i++) {
            int radiality = 0;
            for (int j = 0; j < lengthShortestPaths.length; j++) {
                if (lengthShortestPaths[j][i] > 0) {
                    radiality += 
                            diameter + 1 - lengthShortestPaths[j][i];
                }
            }
            centrality[i][3] = radiality / 
                    ((lengthShortestPaths.length - 1.0) * diameter);
        }
    }

    /**
     * Get the diameter of the graph.
     * This is the longest shortest path from one node to another.
     * @return integer value of the diameter of the graph
     */
    private static int getDiameter () {
        int maxShortestPath = 0;
        for (int i = 0; i < lengthShortestPaths.length; i++) {
            for (int j = 0; j < lengthShortestPaths.length; j++) {
                if (i != j && 
                        lengthShortestPaths[i][j] > maxShortestPath) {
                    maxShortestPath = lengthShortestPaths[i][j];
                }
            }
        }
        return (maxShortestPath);
    }
    
    /**
     * Get the shortest path in the graph.
     * If there is at least one edge in the graph this should be 1.
     * @param diameterthe diameter of the graph (or a number which 
     * is larger than the shortest path. So 2 should be good as well)
     * @return integer value of the shortest shortest path in the graph
     */
    private static int getMinShortestPath (int diameter) { 
        int minShortestPath = diameter;
        for (int i = 0; i < lengthShortestPaths.length; i++) {
            for (int j = 0; j < lengthShortestPaths.length; j++) {
                if (lengthShortestPaths[i][j] > 0 &&
                        lengthShortestPaths[i][j] < minShortestPath) {
                    if (lengthShortestPaths[i][j] == 1) {
                        return (1);
                    } else {
                        minShortestPath = lengthShortestPaths[i][j];
                    }
                }
            }
        }
        return (minShortestPath);
    }
    
    /**
     * Gets the lengths of the longest shortest paths that end 
     * in each node.
     * Finds the maximum values from each column in 
     * lengthShortestPaths matrix.
     * @return integer list containing the lengths of the 
     * longest shortest paths that end in each node.
     */
    private static int[] getLongestShortestPathsToNodes() {
        int longestShortestTo[] = new int[lengthShortestPaths.length];
        // initialise values with first row of matrix
        for (int i = 0; i < longestShortestTo.length; i++) {
            longestShortestTo[i] = lengthShortestPaths[0][i];
        }
        // for each row (can skip first)
        for (int i = 1; i < lengthShortestPaths.length; i++) {
            // check each column
            for (int j = 0; j < lengthShortestPaths.length; j++) {
                if (lengthShortestPaths[i][j] > longestShortestTo[j]) {
                    longestShortestTo[j] = lengthShortestPaths[i][j];
                }
            }
        }
        return (longestShortestTo);
    }

    /**
     * Calculates the number of incoming edges for each node
     * Puts the result in centrality[node][8]
     */
    private static void getkIn () throws UnsupportedEncodingException {
        for (int i = 0; i < adjacencyList.getNumVertices(); i++) {
            for (AdjacentNeighbor neighbour : adjacencyList.getNeighbors(adjacencyList.getVertexId(i))) {
                centrality[neighbour.getNum()][8] += 1.0;
            }
        }
    }
    
    /**
     * Gets the number of outgoing edges for each node.
     * Puts the result in centrality[node][9]
     * @throws UnsupportedEncodingException 
     */
    private static void getkOut () throws UnsupportedEncodingException {
        for (int i = 0; i < adjacencyList.getNumVertices(); i++) {
            centrality[i][9] = 
                    adjacencyList.getNeighbors(adjacencyList.getVertexId(i)).size();
        }
    }
    
    /**
     * Write the centralities to a csv file.
     * Centrality measures are taken from the class level variable 
     * centrality.
     * Writes to "centralities.csv". This file will be overwritten 
     * without warning if it already exists.
     * 
     * @param outPrefix prefix for the file to create
     */
    private static void centralitiesToCSV(String outPrefix) {
        String fileName = outPrefix + "centralities.csv";
        try (FileWriter outFile = new FileWriter(fileName)) {
            // nr of centrality measures (columns)
            int nCentralities = centrality[0].length;
            // nr of proteins (rows)
            int nProteins = centrality.length;
            // write the header for the file
            writeCentralitiesHeader(outFile);
            // then write each protein
            for (int i = 0; i < nProteins; i++) {
                // get the name of the protein from the index
                String outLine = adjacencyList.getVertexId(i);
                // write each centrality measure
                for (int j = 0; j < nCentralities; j++) {
                    outLine += "," + centrality[i][j];
                }
                // end the line with a newline
                outFile.write(outLine +"\n");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write the first line of the csv file containing centralities.
     * List the centralities in the order, from low to high index, 
     * in which they occur in the class level variable centrality.
     *
     * @param outFile the file to write to
     * @throws IOException
     */
    private static void writeCentralitiesHeader(FileWriter outFile) 
            throws IOException {
        String header = "Protein"
                + ",Betweenness,Stress,Radiality,Integration"
                + ",ClosenessIn,ClosenessOut,GraphIn,GraphOut"
                + ",kIn,kOut,harmonicIn,harmonicOut";
        outFile.write(header + "\n");
    }

    /**
     * Write the number of shortest paths matrix to a csv file.
     * Uses the class level variable nShortestPaths as input.
     * Writes to "nShortestPaths.csv". This file will be overwritten 
     * without warning if it already exists.
     * 
     * @param outPrefix prefix for the file to create
     */
    private static void nShortestToCSV (String outPrefix) {
        String fileName = outPrefix + "nShortestPaths.csv";
        try (FileWriter outFile = new FileWriter(fileName)) {
            // nr of proteins (rows)
            int nProteins = adjacencyList.getNumVertices();

            // write the header for the file
            writeProteinsHeader(outFile);

            // then write each protein
            for (int i = 0; i < nProteins; i++) {
                // get the name of the protein from the index
                String outLine = adjacencyList.getVertexId(i);
                // write nr of shortest paths to each node
                for (int j = 0; j < nProteins; j++) {
                    outLine += "," + nShortestPaths[i][j];
                }
                // end the line with a newline
                outFile.write(outLine +"\n");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write the length of the shortest paths matrix to a csv file.
     * Uses the class level variable lengthShortestPaths as input.
     * Writes to "lengthShortestPaths.csv". This file will be 
     * overwritten without warning if it already exists.
     * 
     * @param outPrefix prefix for the file to create
     */
    private static void lengthShortestToCSV (String outPrefix) {
        String fileName = outPrefix + "lengthShortestPaths.csv";
        try (FileWriter outFile = new FileWriter(fileName)) {
            // nr of proteins (rows)
            int nProteins = adjacencyList.getNumVertices();

            // write the header for the file
            writeProteinsHeader(outFile);

            // then write each protein
            for (int i = 0; i < nProteins; i++) {
                // get the name of the protein from the index
                String outLine = adjacencyList.getVertexId(i);
                // write nr of shortest paths to each node
                for (int j = 0; j < nProteins; j++) {
                    outLine += "," + lengthShortestPaths[i][j];
                }
                // end the line with a newline
                outFile.write(outLine +"\n");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphReactome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes the Proteins as the header of the csv.
     * 
     * @param outFile the FileWriter object to write to
     * @throws IOException 
     */
    private static void writeProteinsHeader (FileWriter outFile) 
            throws IOException {
        int nProteins = adjacencyList.getNumVertices();
        String header = "Protein";
        for (int i = 0; i < nProteins; i++) {
            header = header + "," + adjacencyList.getVertexId(i);
        }
        outFile.write(header + "\n");
    }
}
