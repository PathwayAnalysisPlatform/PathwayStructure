package no.uib.kegg_access;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;
import static no.uib.conversion.Utils.encoding;

/**
 * This class downloads the kegg xml files.
 *
 * @author Marc Vaudel
 */
public class DownloadXml {

    /**
     * The main method downloads xml files of KEGG pathways.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            File destinationFolder = new File("C:\\Github\\PathwayProjectQueries\\resources\\kegg_access\\xml");
            File pathwayListFile = new File("C:\\Github\\PathwayProjectQueries\\resources\\kegg_access\\pathwaysKEGG.txt");

            DownloadXml downloadXml = new DownloadXml();

            System.out.println(new Date() + " Parsing KEGG pathway list");
            HashSet<String> pathwayList = downloadXml.getKeggPathwayAccessions(pathwayListFile);

            int i = 1, total = pathwayList.size();

            for (String accession : pathwayList) {

                System.out.println(new Date() + " Getting pathway " + accession + " (" + i + "/" + total + ")");

                downloadXml.downloadXml(accession, destinationFolder);
                
                i++;

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * Returns a list of pathway accessions from a file.
     * 
     * @param accessionsFile the file containing the pathway accessions
     * 
     * @return a list of pathway accessions from a file
     * 
     * @throws IOException exception thrown if an error occurred while reading
     * the file.
     */
    private HashSet<String> getKeggPathwayAccessions(File accessionsFile) throws IOException {

        HashSet<String> accessions = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(accessionsFile))) {

            String line;
            while ((line = br.readLine()) != null) {

                accessions.add(line);

            }
        }

        return accessions;
    }

    /**
     * Downloads the xml file of the given pathway.
     * 
     * @param accession the accession of the pathway
     * @param destinationFolder the folder where to download the file
     * 
     * @throws MalformedURLException exception thrown if the pathway URL is malformed
     * @throws IOException exception thrown if an error occurs while writing the file
     */
    private void downloadXml(String accession, File destinationFolder) throws MalformedURLException, IOException {

        File destinationFile = new File(destinationFolder, accession);

        URL url = new URL("http://rest.kegg.jp/get/" + accession + "/kgml");
        URLConnection conn = url.openConnection();
        
        FileOutputStream outputFileStream = new FileOutputStream(destinationFile);
        GZIPOutputStream outputGzipStream = new GZIPOutputStream(outputFileStream);
        OutputStreamWriter outputEncoder = new OutputStreamWriter(outputGzipStream, encoding);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                BufferedWriter bw = new BufferedWriter(outputEncoder)) {

            String line;
            while ((line = br.readLine()) != null) {

                bw.write(line);
                bw.newLine();
            }
        }
    }

}
