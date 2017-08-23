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
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static no.uib.conversion.Utils.encoding;

/**
 * This class converts a String file to iGraph data frames.
 *
 * @author Marc Vaudel
 */
public class StringToIgraph {

    /**
     * Enum of the different sources of information.
     */
    private enum LinkLevel {

        textMining(0),
        coexpression(1),
        database(2),
        experimental(3);

        public final int index;

        private LinkLevel(int index) {

            this.index = index;

        }

    }

    /**
     * The main method takes a String file and writes igraph files.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            args = new String[]{"C:\\Projects\\Bram\\graphs\\resources\\String\\9606.protein.actions.v10.5.txt.gz",
                "C:\\Projects\\Bram\\graphs\\resources\\String\\9606.protein.links.detailed.v10.5.txt.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\HUMAN_9606_idmapping.dat.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\uniprot_names_human_21.08.17.tab.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\iGraph\\string",
                "string_v10.5"};

            StringToIgraph stringToIgraph = new StringToIgraph();

            File actionsFile = new File(args[0]);
            File linksFile = new File(args[1]);
            File idMappingFile = new File(args[2]);
            File namesMappingFile = new File(args[3]);
            File outputFolder = new File(args[4]);
            String baseName = args[5];

            System.out.println(new Date() + " Parsing uniprot id mapping file");

            HashMap<String, HashSet<String>> accessions = stringToIgraph.getUniprotAccessions(idMappingFile);

            System.out.println(new Date() + " Parsing uniprot names mapping file");

            HashMap<String, String> proteinNames = Utils.getNamesMap(namesMappingFile);

            System.out.println(new Date() + " Parsing actions file");

            HashMap<String, HashMap<String, HashSet<String>>> actions = stringToIgraph.parseActions(actionsFile, accessions);

            System.out.println(new Date() + " Parsing links file");

            HashMap<String, HashMap<String, LinkLevel>> links = stringToIgraph.parseLinks(linksFile, accessions);

            System.out.println(new Date() + " Merging links");

            stringToIgraph.mergeActionsLinks(actions, links);

            System.out.println(new Date() + " Exporting results");

            stringToIgraph.writeIGraphFiles(outputFolder, baseName, proteinNames);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * Merged map.
     */
    private HashMap<String, HashMap<String, HashSet<String>>> mergedMap = new HashMap<>();
    /**
     * Set of all nodes.
     */
    private HashSet<String> allNodes = new HashSet<>();
    /**
     * Boolean indicating whether the isoform number should be removed from the
     * uniprot accession.
     */
    public final boolean removeIsoforms = true;

    public StringToIgraph() {

    }

    /**
     * Parses the uniprot ID mapping and returns a map ensembl protein
     * accessions to uniprot accession.
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

                if (db.equals("Ensembl_PRO")) {

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
     * Parses a String actions file and populates the network attributes.
     *
     * @param actionsFile the string actions file
     * @param idMapping ensembl to uniprot mapping
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private HashMap<String, HashMap<String, HashSet<String>>> parseActions(File actionsFile, HashMap<String, HashSet<String>> idMapping) throws IOException {

        HashMap<String, HashMap<String, HashSet<String>>> actions = new HashMap<>();

        // Parse actions and populate the action map
        InputStream fileStream = new FileInputStream(actionsFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\t");

                String accessionA = lineSplit[0].substring(5);
                String accessionB = lineSplit[1].substring(5);

                if (!accessionA.equals(accessionB)) {

                    HashSet<String> uniprotAccessionsA = idMapping.get(accessionA);

                    if (uniprotAccessionsA != null) {

                        HashSet<String> uniprotAccessionsB = idMapping.get(accessionB);

                        if (uniprotAccessionsB != null) {

                            allNodes.addAll(uniprotAccessionsA);
                            allNodes.addAll(uniprotAccessionsB);

                            String mode = lineSplit[2];
                            boolean directional = lineSplit[4].charAt(0) == 't';
                            boolean aActing = lineSplit[5].charAt(0) == 't';

                            if (!directional || aActing) {

                                for (String accession : uniprotAccessionsA) {

                                    HashSet<String> currentTargets = uniprotAccessionsB.stream()
                                            .filter(participant -> !participant.equals(accession))
                                            .collect(Collectors.toCollection(HashSet::new));

                                    HashMap<String, HashSet<String>> accessionMap = actions.get(accession);

                                    if (accessionMap == null) {

                                        accessionMap = new HashMap<>(currentTargets.size());
                                        actions.put(accession, accessionMap);

                                    }

                                    for (String target : currentTargets) {

                                        HashSet<String> targetActions = accessionMap.get(target);

                                        if (targetActions == null) {

                                            targetActions = new HashSet<>(1);
                                            accessionMap.put(target, targetActions);

                                        }

                                        targetActions.add(mode);

                                    }
                                }

                            }

                            if (directional || !aActing) {

                                for (String accession : uniprotAccessionsB) {

                                    HashSet<String> currentTargets = uniprotAccessionsA.stream()
                                            .filter(participant -> !participant.equals(accession))
                                            .collect(Collectors.toCollection(HashSet::new));

                                    HashMap<String, HashSet<String>> accessionMap = actions.get(accession);

                                    if (accessionMap == null) {

                                        accessionMap = new HashMap<>(currentTargets.size());
                                        actions.put(accession, accessionMap);

                                    }

                                    for (String target : currentTargets) {

                                        HashSet<String> targetActions = accessionMap.get(target);

                                        if (targetActions == null) {

                                            targetActions = new HashSet<>(1);
                                            accessionMap.put(target, targetActions);

                                        }

                                        targetActions.add(mode);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return actions;
    }

    /**
     * Parses a String links file and populates the network attributes.
     *
     * @param linksFile the string links file
     * @param idMapping ensembl to uniprot mapping
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private HashMap<String, HashMap<String, LinkLevel>> parseLinks(File linksFile, HashMap<String, HashSet<String>> idMapping) throws IOException {

        HashMap<String, HashMap<String, LinkLevel>> links = new HashMap<>();

        // Parse actions and populate the action map
        InputStream fileStream = new FileInputStream(linksFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split(" ");

                String accessionA = lineSplit[0].substring(5);
                String accessionB = lineSplit[1].substring(5);

                if (!accessionA.equals(accessionB)) {

                    HashSet<String> uniprotAccessionsA = idMapping.get(accessionA);

                    if (uniprotAccessionsA != null) {

                        HashSet<String> uniprotAccessionsB = idMapping.get(accessionB);

                        if (uniprotAccessionsB != null) {

                            LinkLevel linkLevel = null;

                            if (!lineSplit[6].equals("0")) {

                                linkLevel = LinkLevel.experimental;

                            } else if (!lineSplit[7].equals("0")) {

                                linkLevel = LinkLevel.database;

                            } else if (!lineSplit[7].equals("5")) {

                                linkLevel = LinkLevel.coexpression;

                            } else if (!lineSplit[7].equals("8")) {

                                linkLevel = LinkLevel.textMining;

                            }

                            if (linkLevel != null) {

                                for (String uniprotAccessionA : uniprotAccessionsA) {

                                    HashMap<String, LinkLevel> links2 = links.get(uniprotAccessionA);

                                    if (links2 == null) {

                                        links2 = new HashMap<>(uniprotAccessionsB.size());

                                        for (String uniprotAccessionB : uniprotAccessionsB) {

                                            links2.put(uniprotAccessionB, linkLevel);

                                        }

                                        links.put(uniprotAccessionA, links2);

                                    } else {

                                        for (String uniprotAccessionB : uniprotAccessionsB) {

                                            LinkLevel previousLink = links2.get(uniprotAccessionB);

                                            if (previousLink == null || previousLink.index < linkLevel.index) {

                                                links2.put(uniprotAccessionB, linkLevel);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return links;
    }

    /**
     * Merges the actions and links maps into a single map.
     *
     * @param actions the actions map
     * @param links the links map
     */
    private void mergeActionsLinks(HashMap<String, HashMap<String, HashSet<String>>> actions, HashMap<String, HashMap<String, LinkLevel>> links) {

        HashSet<String> accessionsA = new HashSet<>(links.size());
        accessionsA.addAll(links.keySet());
        accessionsA.addAll(actions.keySet());

        for (String accessionA : accessionsA) {

            HashMap<String, LinkLevel> linksA = links.get(accessionA);
            HashMap<String, HashSet<String>> actionsA = actions.get(accessionA);

            if (linksA == null) {

                linksA = new HashMap<>(0);

            }

            if (actionsA == null) {

                actionsA = new HashMap<>(0);

            }
            
            HashSet<String> accessionsB = new HashSet<>(linksA.size());
            accessionsB.addAll(linksA.keySet());
            accessionsB.addAll(actionsA.keySet());

            for (String accessionB : accessionsB) {
                
                LinkLevel linkLevel = linksA.get(accessionB);
                    String levelName = linkLevel == null ? "other" : linkLevel.name();
                    
                    HashSet<String> actionsAB = actionsA.get(accessionB);
                    
                    String actionAB = actionsAB == null ? "unknown" : actionsAB.stream()
                            .sorted().collect(Collectors.joining(","));
                    
                    StringBuilder sb = new StringBuilder(actionAB.length() + levelName.length() + 1);
                    sb.append(actionAB).append(' ').append(levelName);
                    String key = sb.toString();

                    HashMap<String, HashSet<String>> interactions = mergedMap.get(key);

                    if (interactions == null) {

                        interactions = new HashMap<>();
                        mergedMap.put(key, interactions);

                    }

                    HashSet<String> targets = interactions.get(accessionA);

                    if (targets == null) {

                        targets = new HashSet<>(1);
                        interactions.put(accessionA, targets);

                    }

                    targets.add(accessionB);

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
     * @throws IOException exception thrown if an error occurred while writing
     * the file
     */
    private void writeIGraphFiles(File folder, String baseFileName, HashMap<String, String> proteinNames) throws IOException {

        File edgeFile = new File(folder, baseFileName + "_edges");

        FileOutputStream outputFileStream = new FileOutputStream(edgeFile);
        GZIPOutputStream outputGzipStream = new GZIPOutputStream(outputFileStream);
        OutputStreamWriter outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

        try (BufferedWriter bw = new BufferedWriter(outputEncoder)) {

            bw.write("from to type level");
            bw.newLine();

            for (Entry<String, HashMap<String, HashSet<String>>> entry : mergedMap.entrySet()) {

                Utils.writeEdges(bw, entry.getValue(), entry.getKey());

            }
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
