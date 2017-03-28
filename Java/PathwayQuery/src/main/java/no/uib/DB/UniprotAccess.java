package no.uib.DB;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.pathwayquery.ProteinGraphExtractor;
//import static no.uib.PathwayQuery.ProteinGraphExtractor.proteins;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class UniprotAccess {

    private static final String UNIPROT_SERVER = "http://www.uniprot.org/";
    private static final Logger LOG = Logger.getAnonymousLogger();

    /**
     * Query Uniprot online to get the full list of curated proteins in human.
     * Also fills the protein list of the graph using the reference in
     * {@link ProteinGraphExtractor}
     *
     * @param tool
     * @param params
     * @throws Exception
     */
    private static void run(String tool, ParameterNameValue[] params)
            throws Exception {
        StringBuilder locationBuilder = new StringBuilder(UNIPROT_SERVER + tool + "/?");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                locationBuilder.append('&');
            }
            locationBuilder.append(params[i].name).append('=').append(params[i].value);
        }
        String location = locationBuilder.toString();
        URL url = new URL(location);
        LOG.info("Submitting...");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        conn.setDoInput(true);
        conn.connect();

        int status = conn.getResponseCode();
        while (true) {
            int wait = 0;
            String header = conn.getHeaderField("Retry-After");
            if (header != null) {
                wait = Integer.valueOf(header);
            }
            if (wait == 0) {
                break;
            }
            LOG.info("Waiting (" + wait + ")...");
            conn.disconnect();
            Thread.sleep(wait * 1000);
            conn = (HttpURLConnection) new URL(location).openConnection();
            conn.setDoInput(true);
            conn.connect();
            status = conn.getResponseCode();
        }
        if (status == HttpURLConnection.HTTP_OK) {
            LOG.info("Got a OK reply");
            InputStream reader = conn.getInputStream();
            URLConnection.guessContentTypeFromStream(reader);
            StringBuilder builder = new StringBuilder();
            int a = 0;
            while ((a = reader.read()) != -1) {
                builder.append((char) a);
            }
            System.out.println(builder.toString());
        } else {
            LOG.severe("Failed, got " + conn.getResponseMessage() + " for "
                    + location);
        }
        conn.disconnect();
    }

    public static void getUniProtProteome() {
        try {
            String location = "http://www.uniprot.org/uniprot/?from=ACC&to=P_REFSEQ_AC&format=tab&columns=id&query=reviewed%3Ayes%2BAND%2Borganism%3A9606";
            URL url = new URL(location);
            LOG.info("Submitting...");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            conn.setDoInput(true);
            conn.connect();

            int status = conn.getResponseCode();
            while (true) {
                int wait = 0;
                String header = conn.getHeaderField("Retry-After");
                if (header != null) {
                    wait = Integer.valueOf(header);
                }
                if (wait == 0) {
                    break;
                }
                LOG.info("Waiting (" + wait + ")...");
                conn.disconnect();
                Thread.sleep(wait * 1000);
                conn = (HttpURLConnection) new URL(location).openConnection();
                conn.setDoInput(true);
                conn.connect();
                status = conn.getResponseCode();
            }
            if (status == HttpURLConnection.HTTP_OK) {
                LOG.info("Got a OK reply");
                InputStream reader = conn.getInputStream();
                URLConnection.guessContentTypeFromStream(reader);

                int a = 0;
                String id = "";
                int cont = 0, col = 0;
                //Remove header
                do {
                    a = reader.read();
                } while (a != -1 && (char) a != '\n');

                //Separate into id strings
                while ((a = reader.read()) != -1) {
                    if ((char) a == '\n') {
                        cont++;
                        col = 0;
                        if (id.length() <= 6) {
                            ProteinGraphExtractor.G.addVertex(id);
                        }
                        id = "";
                    } else {
                        id += (char) a;
                        col++;
                    }
                }
            } else {
                LOG.severe("Failed, got " + conn.getResponseMessage() + " for " + location);
            }
            conn.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(UniprotAccess.class.getName()).log(Level.SEVERE, null, ex);
            LOG.severe("Failed to get Uniprot Proteome.");
        }
    }

    private static void createUniProtProteomeFile(String path) {
        try {
            FileWriter fw = new FileWriter(path);
            String location = "http://www.uniprot.org/uniprot/?from=ACC&to=P_REFSEQ_AC&format=tab&columns=id&query=reviewed%3Ayes%2BAND%2Borganism%3A9606";
            URL url = new URL(location);
            LOG.info("Submitting...");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            conn.setDoInput(true);
            conn.connect();

            int status = conn.getResponseCode();
            while (true) {
                int wait = 0;
                String header = conn.getHeaderField("Retry-After");
                if (header != null) {
                    wait = Integer.valueOf(header);
                }
                if (wait == 0) {
                    break;
                }
                LOG.info("Waiting (" + wait + ")...");
                conn.disconnect();
                Thread.sleep(wait * 1000);
                conn = (HttpURLConnection) new URL(location).openConnection();
                conn.setDoInput(true);
                conn.connect();
                status = conn.getResponseCode();
            }
            if (status == HttpURLConnection.HTTP_OK) {
                LOG.info("Got a OK reply");
                InputStream reader = conn.getInputStream();
                URLConnection.guessContentTypeFromStream(reader);

                int a = 0;
                String id = "";
                int cont = 0, col = 0;
                
                //Remove header
                do {
                    a = reader.read();
                } while (a != -1 && (char) a != '\n');

                //Separate into id strings
                while ((a = reader.read()) != -1) {
                    if ((char) a == '\n') {
                        cont++;
                        col = 0;
                        if (id.length() <= 6) {
                            fw.write(id + "\n");
                        }
                        id = "";
                    } else {
                        id += (char) a;
                        col++;
                    }
                }
            } else {
                LOG.severe("Failed, got " + conn.getResponseMessage() + " for " + location);
            }
            conn.disconnect();
            fw.close();
        } catch (Exception ex) {
            Logger.getLogger(UniprotAccess.class.getName()).log(Level.SEVERE, null, ex);
            LOG.severe("Failed to get Uniprot Proteome.");
        }
    }

    public static void main(String[] args)
            throws Exception {
        createUniProtProteomeFile("./UniProtHumanProteome.txt");
        //getUniProtProteome();
    }

    private static class ParameterNameValue {

        private final String name;
        private final String value;

        public ParameterNameValue(String name, String value)
                throws UnsupportedEncodingException {
            this.name = URLEncoder.encode(name, "UTF-8");
            this.value = URLEncoder.encode(value, "UTF-8");
        }
    }
}
