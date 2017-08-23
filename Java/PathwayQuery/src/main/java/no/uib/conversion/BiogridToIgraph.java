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
 * This class converts an intact file to igraph files.
 *
 * @author Marc Vaudel
 */
public class BiogridToIgraph {

    /**
     * The main method takes a biogrid file and writes igraph files.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            args = new String[]{"C:\\Projects\\Bram\\graphs\\resources\\biogrid\\BIOGRID-ORGANISM-Homo_sapiens-3.4.151.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\HUMAN_9606_idmapping.dat.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\uniprot_names_human_21.08.17.tab.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\iGraph\\biogrid",
                "BIOGRID-ORGANISM-Homo_sapiens-3.4.151"};

            BiogridToIgraph biogridToIgraph = new BiogridToIgraph();

            File sifFile = new File(args[0]);
            File idMappingFile = new File(args[1]);
            File namesMappingFile = new File(args[2]);
            File outputFolder = new File(args[3]);
            String baseName = args[4];

            System.out.println(new Date() + " Parsing uniprot id mapping file");

            HashMap<String, HashSet<String>> accessions = biogridToIgraph.getUniprotAccessions(idMappingFile);

            System.out.println(new Date() + " Parsing uniprot names mapping file");

            HashMap<String, String> proteinNames = Utils.getNamesMap(namesMappingFile);

            System.out.println(new Date() + " Parsing Biogrid file");

            biogridToIgraph.parseBiogridFile(sifFile, accessions);

            System.out.println(new Date() + " Exporting results");
            biogridToIgraph.writeIGraphFiles(outputFolder, baseName, proteinNames);

            int nEdges = Utils.getNEdges(biogridToIgraph.getInteractions());
            System.out.println(new Date() + " " + nEdges + " interractions found");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * Map of interactions, from input to outputs.
     */
    private HashMap<String, HashSet<String>> interactions = new HashMap<>();
    /**
     * Set of all nodes.
     */
    private HashSet<String> allNodes = new HashSet<>();
    /**
     * Boolean indicating whether accessions should be converted to Uniprot.
     */
    public final boolean uniprotConversion = true;
    /**
     * Boolean indicating whether the isoform number should be removed from the
     * uniprot accession.
     */
    public final boolean removeIsoforms = true;

    public BiogridToIgraph() throws IOException {

    }

    /**
     * Parses the uniprot ID mapping and returns a map GeneId to uniprot
     * accession.
     *
     * @param uniprotFile the uniprot mapping file
     *
     * @return the accessions in a set
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private HashMap<String, HashSet<String>> getUniprotAccessions(File uniprotFile) throws IOException {

        HashMap<String, HashSet<String>> mapping = new HashMap<>();

        InputStream fileStream = new FileInputStream(uniprotFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\t");

                String uniprot = lineSplit[0];

                if (removeIsoforms) {
                    int dashIndex = uniprot.indexOf('-');
                    if (dashIndex > -1) {
                        uniprot = uniprot.substring(0, dashIndex);
                    }
                }

                String db = lineSplit[1];
                String id = lineSplit[2];

                if (db.equals("GeneID")) {

                    HashSet<String> ids = mapping.get(id);

                    if (ids == null) {

                        ids = new HashSet<>(1);
                        mapping.put(id, ids);

                    }

                    ids.add(uniprot);

                }
            }
        }

        return mapping;
    }

    /**
     * Parses a biogrid file and populates the network attributes.
     *
     * @param biogridFile the biogrid file
     * @param idMapping gene to uniprot mapping
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private void parseBiogridFile(File biogridFile, HashMap<String, HashSet<String>> idMapping) throws IOException {

        InputStream fileStream = new FileInputStream(biogridFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\t");

                HashSet<String> accessionsA = getAccessions(lineSplit[0], idMapping);

                if (accessionsA.isEmpty()) {

                    accessionsA = getAccessions(lineSplit[2], idMapping);

                }

                HashSet<String> accessionsB = getAccessions(lineSplit[1], idMapping);

                if (accessionsB.isEmpty()) {

                    accessionsB = getAccessions(lineSplit[3], idMapping);

                }

                if (!accessionsA.isEmpty() && !accessionsB.isEmpty()) {

                    allNodes.addAll(accessionsA);
                    allNodes.addAll(accessionsB);

                    for (String accession : accessionsA) {

                        HashSet<String> currentTargets = accessionsB.stream()
                                .filter(participant -> !participant.equals(accession))
                                .collect(Collectors.toCollection(HashSet::new));

                        HashSet<String> targets = interactions.get(accession);

                        if (targets == null) {

                            interactions.put(accession, currentTargets);

                        } else {

                            targets.addAll(currentTargets);

                        }
                    }
                }
            }
        }
    }

    /**
     * Splits the biogrid entry and extracts the uniprot accessions present in
     * the given list.
     *
     * @param biogridEntry the biogrid entry
     * @param idMapping geneId to Uniprot mapping
     *
     * @return the accessions found in a set
     */
    private HashSet<String> getAccessions(String biogridEntry, HashMap<String, HashSet<String>> idMapping) {

        String[] splittedEntry = biogridEntry.split("\\|");
        HashSet<String> result = new HashSet<>(splittedEntry.length);

        for (String entry : splittedEntry) {
            if (entry.length() >= 22) {

                String accession = entry.substring(22);

                if (!uniprotConversion) {

                    result.add(accession);

                } else {

                    HashSet<String> uniprotAccessions = idMapping.get(accession);

                    if (uniprotAccessions != null) {

                        result.addAll(uniprotAccessions);

                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the interactions found in a map.
     *
     * @return the interactions found in a map
     */
    public HashMap<String, HashSet<String>> getInteractions() {
        return interactions;
    }

    /**
     * Write the igraph files.
     *
     * @param folder the destination folder
     * @param baseFileName the base name for the edges and vertices files
     * @param proteinNames the accession to protein name map
     *
     * @throws IOException exception thrown if an error occurred while writing
     * the file
     */
    private void writeIGraphFiles(File folder, String baseFileName, HashMap<String, String> proteinNames) throws IOException {

        File edgeFile = new File(folder, baseFileName + "_edges");

        FileOutputStream outputFileStream = new FileOutputStream(edgeFile);
        GZIPOutputStream outputGzipStream = new GZIPOutputStream(outputFileStream);
        OutputStreamWriter outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

        try (BufferedWriter bw = new BufferedWriter(outputEncoder)) {

            bw.write("from to type");
            bw.newLine();

            Utils.writeEdges(bw, interactions, "Complex");

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
