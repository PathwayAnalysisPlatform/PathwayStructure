package no.uib.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

/**
 * This class contains utilities methods for graph files conversion.
 *
 * @author Marc Vaudel
 */
public class Utils {

    /**
     * Encoding.
     */
    public static final String encoding = "UTF-8";

    /**
     * Parses the given uniprot names mapping and returns a map accession to
     * protein name.
     *
     * @param uniprotNamesFile the uniprot names mapping file
     *
     * @return the accessions in a set
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    public static HashMap<String, String> getNamesMap(File uniprotNamesFile) throws IOException {

        HashMap<String, String> namesMap = new HashMap<>();

        InputStream fileStream = new FileInputStream(uniprotNamesFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        char[] separators = {'[', '('};

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\t");

                String accession = lineSplit[0];
                String proteinName = lineSplit[1];

                int separatorIndex = indexOf(proteinName, separators, 7);

                if (separatorIndex > -1) {

                    proteinName = proteinName.substring(0, separatorIndex - 1);

                }

                namesMap.put(accession, proteinName);
            }
        }

        return namesMap;
    }

    /**
     * Returns the first index of the separators in the protein name. -1 if not
     * found.
     *
     * @param proteinName the protein name
     * @param separators the separators
     * @param startIndex the index where to start looking for separators
     *
     * @return the first index of the separators in the protein name
     */
    public static int indexOf(String proteinName, char[] separators, int startIndex) {
        
        if (startIndex >= proteinName.length() - 1) {
            
            return -1;
            
        }

        char[] nameAsCharArray = proteinName.toCharArray();

        for (int i = startIndex; i < nameAsCharArray.length; i++) {

            char ref = nameAsCharArray[i];

            for (char sep : separators) {

                if (ref == sep) {

                    return i;

                }
            }
        }

        return -1;
    }

    /**
     * Returns the line to export for a node.
     *
     * @param accession the accession of the protein
     * @param proteinNames the protein names map
     *
     * @return the line to export for a node
     */
    public static String getNodeLine(String accession, HashMap<String, String> proteinNames) {

        String proteinName = proteinNames.get(accession);

        if (proteinName == null) {

            proteinName = accession;

        }

        StringBuilder sb = new StringBuilder(accession.length() + proteinName.length() + 1);
        sb.append(accession).append('\t').append(proteinName);
        return sb.toString();
    }

    /**
     * Writes the given edges using the given writer. Writing exceptions are
     * thrown as runtime exception.
     *
     * @param bw the writer
     * @param targetsMap the accession to target map
     * @param category the category of the mapping
     */
    public static void writeEdges(BufferedWriter bw, HashMap<String, HashSet<String>> targetsMap, String category) {
        targetsMap.keySet().stream()
                .sorted()
                .forEach(accession -> writeEdges(bw, accession, targetsMap.get(accession), category));

    }

    /**
     * Writes the given edges using the given writer. Writing exceptions are
     * thrown as runtime exception.
     *
     * @param bw the writer
     * @param accession the accession
     * @param targets the targets
     * @param category the category of the mapping
     */
    public static void writeEdges(BufferedWriter bw, String accession, HashSet<String> targets, String category) {

        targets.stream()
                .sorted()
                .forEach(target -> writeEdge(bw, accession, target, category));
    }

    /**
     * Writes the given edge using the given writer. Writing exceptions are
     * thrown as runtime exception.
     *
     * @param bw the writer
     * @param accession the accession
     * @param target the target
     * @param category the category of the mapping
     */
    public static void writeEdge(BufferedWriter bw, String accession, String target, String category) {

        try {

            StringBuilder sb = new StringBuilder(accession.length() + target.length() + category.length() + 2);
            sb.append(accession).append(' ').append(target).append(' ').append(category);
            bw.write(sb.toString());
            bw.newLine();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the number of edges found.
     *
     * @param targetsMap the accession to targets map
     *
     * @return the number of edges found
     */
    public static int getNEdges(HashMap<String, HashSet<String>> targetsMap) {

        return targetsMap.values().stream().mapToInt(targets -> targets.size()).sum();

    }
}
