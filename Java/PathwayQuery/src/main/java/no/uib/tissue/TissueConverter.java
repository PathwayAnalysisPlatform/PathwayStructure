package no.uib.tissue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import no.uib.conversion.BiogridToIgraph;
import no.uib.conversion.Utils;
import static no.uib.conversion.Utils.encoding;

/**
 *
 * @author Marc Vaudel
 */

//public class TissueConverter {
//    
//    public static void main(String[] args) {
//
//        try {
//
//            args = new String[]{"C:\\Github\\PathwayProjectQueries\\resources\\tissues\\Protein_AB.gz",
//                "C:\\Github\\PathwayProjectQueries\\resources\\HUMAN_9606_idmapping.dat.gz",
//                "C:\\Github\\PathwayProjectQueries\\resources\\uniprot_names_human_21.08.17.tab.gz",
//                "C:\\Github\\PathwayProjectQueries\\resources\\tissues\\Protein_AB_identifiers.gz"};
//
//            TissueConverter converter = new TissueConverter();
//
//            File mappingFile = new File(args[0]);
//            File idMappingFile = new File(args[1]);
//            File namesMappingFile = new File(args[2]);
//            File destinationFile = new File(args[3]);
//
//            System.out.println(new Date() + " Parsing uniprot id mapping file");
//
//            HashMap<String, HashSet<String>> accessions = converter.getUniprotAccessions(idMappingFile);
//
//            System.out.println(new Date() + " Parsing uniprot names mapping file");
//
//            HashMap<String, String> proteinNames = Utils.getNamesMap(namesMappingFile);
//
//            System.out.println(new Date() + " ConvertingFile");
//            
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//    }
//    
//    private void convertFile(File inputFile, File destinationFile, HashMap<String, HashSet<String>> accessionMapping, HashMap<String, String> proteinNames) {
//        
//        
//        
//    }

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
//    private HashMap<String, HashSet<String>> getUniprotAccessions(File uniprotFile) throws IOException {
//
//        HashMap<String, HashSet<String>> mapping = new HashMap<>();
//
//        InputStream fileStream = new FileInputStream(uniprotFile);
//        InputStream gzipStream = new GZIPInputStream(fileStream);
//        Reader decoder = new InputStreamReader(gzipStream, encoding);
//
//        try (BufferedReader br = new BufferedReader(decoder)) {
//
//            String line;
//            while ((line = br.readLine()) != null) {
//
//                String[] lineSplit = line.split("\t");
//
//                String uniprot = lineSplit[0];
//
//                if (removeIsoforms) {
//                    int dashIndex = uniprot.indexOf('-');
//                    if (dashIndex > -1) {
//                        uniprot = uniprot.substring(0, dashIndex);
//                    }
//                }
//
//                String db = lineSplit[1];
//                String id = lineSplit[2];
//
//                if (db.equals("GeneID")) {
//
//                    HashSet<String> ids = mapping.get(id);
//
//                    if (ids == null) {
//
//                        ids = new HashSet<>(1);
//                        mapping.put(id, ids);
//
//                    }
//
//                    ids.add(uniprot);
//
//                }
//            }
//        }
//
//        return mapping;
//    }
//
//}
