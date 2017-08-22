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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static no.uib.conversion.Utils.encoding;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * This class converts a set of kegg files to igraph files.
 *
 * @author Marc Vaudel
 */
public class KeggToIgraph {

    /**
     * The main method takes a folder containing kegg files and writes igraph
     * files.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            args = new String[]{"C:\\Github\\PathwayProjectQueries\\resources\\kegg_access\\xml",
                "C:\\Github\\PathwayProjectQueries\\resources\\uniprot_names_human_21.08.17.tab.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\HUMAN_9606_idmapping.dat.gz",
                "C:\\Github\\PathwayProjectQueries\\resources\\iGraph\\kegg",
                "kegg_21.08.17"};

            KeggToIgraph keggToIgraph = new KeggToIgraph();

            File keggFolder = new File(args[0]);
            File namesMappingFile = new File(args[1]);
            File idMappingFile = new File(args[2]);
            File outputFolder = new File(args[3]);
            String baseName = args[4];

            System.out.println(new Date() + " Parsing uniprot id mapping file");

            HashMap<String, HashSet<String>> accessions = keggToIgraph.getUniprotAccessions(idMappingFile);

            System.out.println(new Date() + " Parsing uniprot names mapping file");

            HashMap<String, String> proteinNames = Utils.getNamesMap(namesMappingFile);

            for (String fileName : keggFolder.list()) {

                System.out.println(new Date() + " Parsing kegg file " + fileName + ".");

                File keggFile = new File(keggFolder, fileName);
                HashMap<String, HashMap<String, HashSet<String>>> keggMapping = keggToIgraph.parseKeggFile(keggFile, accessions);
                keggToIgraph.importMapping(keggMapping);

            }
            
            keggToIgraph.getVertices();

            System.out.println(new Date() + " Exporting results");

            keggToIgraph.writeIGraphFiles(outputFolder, baseName, proteinNames);

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
                String db = lineSplit[1];
                String id = lineSplit[2];

                if (db.equals("KEGG")) {

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
     * Parses a kegg file and returns a map of the different reactions, reaction
     * name to accession to targets.
     *
     * @param keggfile the kegg xml file to parse
     * @param idMapping the kegg to uniprot mapping
     *
     * @return the relations mappings
     *
     * @throws IOException exception thrown if an error occurred while reading
     * the file
     * @throws XmlPullParserException exception thrown if an error occurred
     * while parsing the xml file
     */
    private HashMap<String, HashMap<String, HashSet<String>>> parseKeggFile(File keggfile, HashMap<String, HashSet<String>> idMapping) throws IOException, XmlPullParserException {

        HashMap<Integer, String[]> entryNames = new HashMap<>();
        HashMap<String, HashMap<Integer, HashSet<Integer>>> relations = new HashMap<>();

        InputStream fileStream = new FileInputStream(keggfile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(br);

            int type = parser.next();

            while (type != XmlPullParser.END_DOCUMENT) {

                if (type == XmlPullParser.START_TAG && parser.getName().equals("entry")) {

                    Integer id = null;
                    String[] names = null;
                    boolean group = false;

                    for (int i = 0; i < parser.getAttributeCount(); i++) {

                        String attributeName = parser.getAttributeName(i);

                        if (attributeName.equals("id")) {

                            String value = parser.getAttributeValue(i);
                            id = new Integer(value);

                        } else if (attributeName.equals("name")) {

                            String value = parser.getAttributeValue(i);
                            names = value.split(" ");

                        } else if (attributeName.equals("type")) {

                            String value = parser.getAttributeValue(i);

                            if (value.equals("group")) {

                                group = true;

                            }
                        }
                    }

                    if (!group) {

                        if (id != null && names != null) {

                            entryNames.put(id, names);

                        }

                    } else {

                        HashSet<String> groupNamesSet = new HashSet<>();

                        while (!((type = parser.next()) == XmlPullParser.END_TAG && parser.getName().equals("entry"))) {

                            if (type == XmlPullParser.START_TAG && parser.getName().equals("component")) {

                                for (int i = 0; i < parser.getAttributeCount(); i++) {

                                    String attributeName = parser.getAttributeName(i);

                                    if (attributeName.equals("id")) {

                                        String value = parser.getAttributeValue(i);
                                        Integer subId = new Integer(value);

                                        names = entryNames.get(subId);

                                        if (names == null) {

                                            throw new IllegalArgumentException("No names found for sub id.");

                                        }

                                        for (String name : names) {

                                            groupNamesSet.add(name);

                                        }
                                    }

                                }
                            }
                        }

                        String[] groupNames = groupNamesSet.toArray(new String[0]);
                        entryNames.put(id, groupNames);
                        
                    }

                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("relation")) {

                    Integer entry1 = null, entry2 = null;
                    String typeName = null;

                    for (int i = 0; i < parser.getAttributeCount(); i++) {

                        String attributeName = parser.getAttributeName(i);

                        if (attributeName.equals("entry1")) {

                            String value = parser.getAttributeValue(i);
                            entry1 = new Integer(value);

                        } else if (attributeName.equals("entry2")) {

                            String value = parser.getAttributeValue(i);
                            entry2 = new Integer(value);

                        } else if (attributeName.equals("type")) {

                            typeName = parser.getAttributeValue(i);

                        }
                    }

                    HashSet<String> names = new HashSet<String>(1);

                    while (!((type = parser.next()) == XmlPullParser.END_TAG && parser.getName().equals("relation"))) {

                        if (type == XmlPullParser.START_TAG && parser.getName().equals("subtype")) {

                            for (int i = 0; i < parser.getAttributeCount(); i++) {

                                String attributeName = parser.getAttributeName(i);

                                if (attributeName.equals("name")) {

                                    String subName = parser.getAttributeValue(i);
                                    names.add(subName);

                                }

                            }
                        }
                    }

                    if (names.isEmpty() && typeName != null) {

                        names.add(typeName);

                    }

                    if (entry1 == null || entry2 == null || names.isEmpty()) {

                        throw new IllegalArgumentException("Null value in relation.");

                    }

                    for (String name : names) {
                        
                        String key = name.replace(' ', '_');

                        HashMap<Integer, HashSet<Integer>> relationEntries = relations.get(key);

                        if (relationEntries == null) {

                            relationEntries = new HashMap<>(1);
                            relations.put(key, relationEntries);

                        }

                        HashSet<Integer> targets = relationEntries.get(entry1);

                        if (targets == null) {

                            targets = new HashSet<>(1);
                            relationEntries.put(entry1, targets);

                        }

                        targets.add(entry2);
                    }
                }

                type = parser.next();

            }
        }

        HashMap<String, HashMap<String, HashSet<String>>> keggMapping = new HashMap<>();

        for (String relationName : relations.keySet()) {

            HashMap<Integer, HashSet<Integer>> relationsEntries = relations.get(relationName);

            HashMap<String, HashSet<String>> namemapping = new HashMap<>(relationsEntries.size());
            keggMapping.put(relationName, namemapping);

            for (int id1 : relationsEntries.keySet()) {

                HashSet<Integer> ids2 = relationsEntries.get(id1);

                String[] names1 = entryNames.get(id1);

                for (String name1 : names1) {

                    HashSet<String> accessions1 = idMapping.get(name1);

                    if (accessions1 != null) {

                        HashSet<String> accessions2 = namemapping.get(name1);

                        if (accessions2 == null) {

                            accessions2 = new HashSet<>(ids2.size());
                            namemapping.put(name1, accessions2);

                        }

                        for (Integer id2 : ids2) {

                            String[] names2 = entryNames.get(id2);

                            for (String name2 : names2) {

                                HashSet<String> accessions2Temp = idMapping.get(name2);

                                if (accessions2Temp != null) {

                                    accessions2.addAll(accessions2Temp);

                                }
                            }
                        }
                    }
                }
            }
        }

        return keggMapping;
    }

    /**
     * Imports a file mapping to the merged mapping.
     *
     * @param keggMapping the kegg file mapping
     */
    private void importMapping(HashMap<String, HashMap<String, HashSet<String>>> keggMapping) {

        for (String reactionName : keggMapping.keySet()) {

            HashMap<String, HashSet<String>> fileEntries = keggMapping.get(reactionName);
            HashMap<String, HashSet<String>> mergedEntries = mergedMap.get(reactionName);

            if (mergedEntries == null) {

                mergedMap.put(reactionName, fileEntries);

            } else {

                for (String accession1 : fileEntries.keySet()) {

                    HashSet<String> fileTargets = fileEntries.get(accession1);
                    HashSet<String> mergedTargets = mergedEntries.get(accession1);

                    if (mergedTargets == null) {

                        mergedEntries.put(accession1, fileTargets);

                    } else {

                        mergedTargets.addAll(fileTargets);

                    }
                }
            }
        }
    }
    
    /**
     * Adds the vertices from the merged map to the vertices set.
     */
    private void getVertices() {
        
        for (HashMap<String, HashSet<String>> accessionsMapping : mergedMap.values()) {
            
            allNodes.addAll(accessionsMapping.keySet());
            
            for (HashSet<String> targets : accessionsMapping.values()) {
                
                allNodes.addAll(targets);
                
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

            bw.write("from to type");
            bw.newLine();

            for (Map.Entry<String, HashMap<String, HashSet<String>>> entry : mergedMap.entrySet()) {

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
