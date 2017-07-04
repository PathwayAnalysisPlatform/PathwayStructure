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
 * @author Bram Burger
 */
public class Centrality {
    private static GraphReactome adjacencyList;
    private static double[][] centrality;
    // call as nShortestPaths[startNode][endNode]
    private static int[][] nShortestPaths;
    private static int[][] lengthShortestPaths;

    /**
     * 
     * @param args If empty or more than two elements, 
     * the input file will be the default.
     * If one argument is given this will be taken to be the input file.
     * If two arguments are give, the first will be taken to be the
     * input file, and the second to be the prefix for the output files.
     * @throws IOException 
     */
    public static void main(String args[]) throws IOException {
        String outPrefix = "";
        if (args.length == 1) {
            getGraph(args[0]);
        } else if (args.length == 2) {
            getGraph(args[0]);
            outPrefix = args[1];
        } else {
            getGraph();
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
        String path = "./thing2.sif";

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
        // between, closeness, graph, stress, radiality
        // Matrix nrProteins rows, 5 columns
        // nProteins because of the mapping to integers for indices...
        centrality = new double[nProteins][5];

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
        // stress centrality needs shortest paths from AND to node i
        // (the way I've implemented it now, anyway)
        // so can only be calculated when we have performed the algorithm on all the nodes
        for (int i = 0; i < nProteins; i++) {
            calculateStressCentrality(i);
        }
        calculateRadiality();
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

        calculateBetweennessCentrality(startNode, stack,
                parents, nProteins);
        calculateClosenessCentrality(startNode);
        calculateGraphCentrality(startNode);
    }

    /**
     * Algorithm to calculate betweenness centrality.
     * Puts the output in centrality[startNode][0]
     *
     * The betweenness centrality of a node v is the number of
     * shortest paths from s to t going through v, divided by
     * the total number of shortest paths from s to t.
     * where s != v, t != v.
     *
     * @param startNode index of the node for which betweenness
     * centrality is calculated
     * @param stack the stack containing indices of the nodes as
     * calculated in getCentralityMeasures
     * @param parents the hash set containing indices of parents
     * of nodes as calculated in getCentralityMeasures
     * @param nProteins  the total number of proteins in the graph
     * (including proteins which have no outgoing edges)
     */
    private static void calculateBetweennessCentrality (int startNode,
            Deque<Integer> stack, HashSet<Integer>[] parents,
            int nProteins) {
        double[] delta = new double[nProteins];
        // the stack returns vertices in order of non-increasing
        // distance from startNode
        while (!stack.isEmpty()) {
            int currentNode = stack.pop();
            for (int parent : parents[currentNode]) {
                delta[parent] = delta[parent]
                        + ( (double) nShortestPaths[startNode][parent]
                        / nShortestPaths[startNode][currentNode])
                        * (1 + delta[currentNode]);
            }
            if (currentNode != startNode) {
                centrality[currentNode][0] =
                        centrality[currentNode][0] + delta[currentNode];
            }
        }
    }

    /**
     * Algorithm to calculate stress centrality.
     * Puts the output in centrality[startNode][3]
     * Needs access to the complete nShortestPaths matrix, so can
     * only be calculated after all nodes have been evaluated
     * as starting node.
     *
     * Stress centrality of a node is calculated as the number of
     * shortest paths which go through that node, but do not
     * start of end in that node.
     *
     * @param startNode index of the node for which
     * stress centrality is calculated
     */
    private static void calculateStressCentrality (int startNode) {
        // from each node ...
        for (int i = 0; i < nShortestPaths.length; i++) {
            // ... which can reach, and is not equal to, the node 
            // we are calculating for
            if (lengthShortestPaths[i][startNode] > 0) {
                // to each node ...
                for (int j = 0; j < nShortestPaths.length; j++) {
                    // which is reachable from, but not equal to 
                    // the node we are calculating for
                    if (j != i &&
                            lengthShortestPaths[startNode][j] > 0 &&
                            lengthShortestPaths[i][j] == 
                            lengthShortestPaths[i][startNode] 
                            + lengthShortestPaths[startNode][j]) {
                        centrality[startNode][3] = 
                                centrality[startNode][3] 
                                + nShortestPaths[i][startNode]
                                * nShortestPaths[startNode][j];
                    }
                }
            }
        }
    }

    /**
     * Algorithm to calculate closeness centrality.
     * Puts the output in centrality[starltNode][1]
     *
     * Closeness centrality of a node is calculated as the reciprocal 
     * of the sum of the shortest paths starting at the node.
     * In a disconnected graph, in this version, only paths 
     * to nodes which can be reached are considered.
     *
     * @param startNode index of the node for which 
     * closeness centrality is calculated
     */
    private static void calculateClosenessCentrality (int startNode) {
        int runningSum = 0;
        for (int i = 0; i < lengthShortestPaths[startNode].length; i++) {
            if (i != startNode) {
                runningSum += lengthShortestPaths[startNode][i];
            }
        }
        if (runningSum == 0) {
            centrality[startNode][1] = 0.0;
        } else {
            centrality[startNode][1] = 1.0 / runningSum;
        }
    }

    /**
     * Algorithm to calculate graph centrality.
     * Puts the output in centrality[startNode][2]
     *
     * Graph centrality of a node is the reciprocal of the longest 
     * shortest path starting from that node.
     * In a disconnected graph, it only considers 
     * (in this version at least) paths to nodes which 
     * the node can reach.
     *
     * @param startNode index of the node for which 
     * graph centrality is calculated
     */
    private static void calculateGraphCentrality (int startNode) {
        int longestShortest = getMaxFromIntList(lengthShortestPaths[startNode]);
        if (longestShortest == 0) {
            centrality[startNode][2] = 0.0;
        } else {
            centrality[startNode][2] = 1.0 / longestShortest;
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
     * Puts output in centrality[startNode][4]
     *
     */
    private static void calculateRadiality() {
        int longestShortestTo[] = getLongestShortestPathsToNodes();
        int diameter = getMaxFromIntList(longestShortestTo);

        for (int i = 0; i < longestShortestTo.length; i++) {
            if ( diameter == 0) {
                centrality[i][4] = 0.0;
            } else {
                for (int j = 0; j < longestShortestTo.length; j++) {
                    centrality[i][4] += longestShortestTo[j] + 1.0 
                            - lengthShortestPaths[i][j];
                }
                centrality[i][4] = centrality[i][4] / 
                        ((longestShortestTo.length - 1) * diameter);
            }
        }
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
        String header = "Protein,Betweenness,Closeness,Graph,Stress,Radiality";
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