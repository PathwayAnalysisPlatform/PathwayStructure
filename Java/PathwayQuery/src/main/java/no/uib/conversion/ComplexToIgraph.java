package no.uib.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static no.uib.conversion.Utils.encoding;

/**
 * This class converts a protein complex mapping to igraph files.
 *
 * @author Marc Vaudel
 */
public class ComplexToIgraph {

    /**
     * The main method takes a gzipped complex mapping file and writes igraph
     * files.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            args = new String[]{"C:\\Github\\PathwayProjectQueries\\resources\\iGraph\\complexes\\homo_sapiens_complexes.tsv.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\uniprot_names_human_21.08.17.tab.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\iGraph\\complexes",
                "complexes_18.08.17"};

            ComplexToIgraph complexToIgraph = new ComplexToIgraph();

            File sifFile = new File(args[0]);
            File namesMappingFile = new File(args[1]);
            File outputFolder = new File(args[2]);
            String baseName = args[3];

            System.out.println(new Date() + " Parsing uniprot names mapping file");

            HashMap<String, String> proteinNames = Utils.getNamesMap(namesMappingFile);

            System.out.println(new Date() + " Parsing complex file");

            complexToIgraph.parseComplexFile(sifFile);

            System.out.println(new Date() + " Exporting results");
            complexToIgraph.writeIGraphFiles(outputFolder, baseName, proteinNames);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * Map of complexes, from input to outputs.
     */
    private HashMap<String, HashSet<String>> complexes = new HashMap<>();
    /**
     * Set of all nodes.
     */
    private HashSet<String> allNodes = new HashSet<>();

    /**
     * Parses a complex file and populates the network attributes.
     *
     * @param complexFile the complex file
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private void parseComplexFile(File complexFile) throws IOException {

        InputStream vepFileStream = new FileInputStream(complexFile);
        InputStream vepGzipStream = new GZIPInputStream(vepFileStream);
        Reader decoder = new InputStreamReader(vepGzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                char[] lineAsCharArray = line.toCharArray();
                int nSeparators = 0;

                for (int i = 0; i < lineAsCharArray.length; i++) {

                    char character = lineAsCharArray[i];

                    if (character == '\t') {

                        if (nSeparators < 3) {
                            
                            nSeparators++;

                        } else if (nSeparators == 3) {
                            
                            HashSet<String> participants = new HashSet<>(2);
                            int lastStart = i+1;
                            
                            cellIteration:
                            for (int j = i + 1; j < lineAsCharArray.length ; j++) {
                                
                                character = lineAsCharArray[j];
                                
                                switch (character) {
                                    case '\t':
                                        break cellIteration;
                                    case '(':
                                        String participant = line.substring(lastStart, j);
                                        participants.add(participant);
                                        break;
                                    case '|':
                                        lastStart = j+1;
                                }
                            }
                            
                            if (participants.size() > 1) {
                                
                                allNodes.addAll(participants);
                                
                                for (String accession : participants) {
                                    
                                    HashSet<String> currentTargets = participants.stream()
                                            .filter(participant -> !participant.equals(accession))
                                            .collect(Collectors.toCollection(HashSet::new));
                                    
                                    HashSet<String> targets = complexes.get(accession);
                                    
                                    if (targets == null) {
                                        
                                        complexes.put(accession, currentTargets);
                                        
                                    } else {
                                        
                                        targets.addAll(currentTargets);
                                        
                                    }
                                }
                            }

                            break;

                        }
                    }
                }
            }
        }
    }

    /**
     * Write the igraph files.
     * 
     * @param folder the destination folder
     * @param baseFileName the base name for the edges and vertices files
     * @param proteinNames the accession to protein name map
     * 
     * @throws IOException exception thrown if an error occurred while writing the file
     */
    private void writeIGraphFiles(File folder, String baseFileName, HashMap<String, String> proteinNames) throws IOException {

        File edgeFile = new File(folder, baseFileName + "_edges");

        FileOutputStream outputFileStream = new FileOutputStream(edgeFile);
        GZIPOutputStream outputGzipStream = new GZIPOutputStream(outputFileStream);
        OutputStreamWriter outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

        try (BufferedWriter bw = new BufferedWriter(outputEncoder)) {

            bw.write("from to type");
            bw.newLine();

            Utils.writeEdges(bw, complexes, "Complex");

        }

        File nodesFile = new File(folder, baseFileName + "_vertices");

        outputFileStream = new FileOutputStream(nodesFile);
        outputGzipStream = new GZIPOutputStream(outputFileStream);
        outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

        try (BufferedWriter bw = new BufferedWriter(outputEncoder)) {

            bw.write("id\tname");
            bw.newLine();

            bw.write(
                    allNodes.stream()
                            .sorted()
                            .map(accession -> Utils.getNodeLine(accession, proteinNames))
                            .collect(Collectors.joining(System.lineSeparator()))
            );
        }
    }
}
