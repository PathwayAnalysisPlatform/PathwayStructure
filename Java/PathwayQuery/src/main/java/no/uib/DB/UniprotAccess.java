package no.uib.DB;

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

            ProteinGraphExtractor.proteins = new byte[21000][];
            int a = 0;
            String id = "";
            int cont = 0, col = 0;
            ProteinGraphExtractor.totalNumProt = 0;
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
                        ProteinGraphExtractor.proteins[ProteinGraphExtractor.totalNumProt] = id.getBytes();
                        ProteinGraphExtractor.totalNumProt++;
                    }
                    id = "";
                } else {
                    id += (char) a;
                    col++;
                }
            }
            //System.out.println(builder.toString());
        } else {
            LOG.severe("Failed, got " + conn.getResponseMessage() + " for " + location);
        }
        conn.disconnect();
    }

    public static void getUniprotProteome() {
        try {
            run("uniprot", new ParameterNameValue[]{
                new ParameterNameValue("from", "ACC"),
                new ParameterNameValue("to", "P_REFSEQ_AC"),
                new ParameterNameValue("format", "tab"),
                new ParameterNameValue("columns", "id"),
                new ParameterNameValue("query", "reviewed:yes+AND+organism:9606"),});
        } catch (Exception ex) {
            Logger.getLogger(UniprotAccess.class.getName()).log(Level.SEVERE, null, ex);
            LOG.severe("Failed to get Uniprot Proteome.");
        }
    }

    public static void main(String[] args)
            throws Exception {
        run("uniprot", new ParameterNameValue[]{
            new ParameterNameValue("from", "ACC"),
            new ParameterNameValue("to", "P_REFSEQ_AC"),
            new ParameterNameValue("format", "tab"),
            new ParameterNameValue("columns", "id"),
            new ParameterNameValue("query", "reviewed:yes+AND+organism:9606"),});
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
