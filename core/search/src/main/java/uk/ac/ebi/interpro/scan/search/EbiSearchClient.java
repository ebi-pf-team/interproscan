package uk.ac.ebi.interpro.scan.search;

import uk.ac.ebi.webservices.jaxws.EBeyeClient;

import javax.xml.rpc.ServiceException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * EBI Search web service client.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public final class EbiSearchClient {

    // "Domain" in this sense means the name of the index in the EBI search engine
    private static String DOMAIN = "interpro";

    // Could configure in Spring
    private final EBeyeClient client;

    public EbiSearchClient() {
        client = new EBeyeClient();
    }

    public EbiSearchClient(String endPointUrl) {
        client = new EBeyeClient();
        client.setServiceEndPoint(endPointUrl); //eg. "http://www.ebi.ac.uk/ebisearch/service.ebi"
    }

    public String getServiceEndPoint() {
        return client.getServiceEndPoint();
    }

    public Page search(String query, int pageNumber, int resultsPerPage, boolean includeDescription) {
        try {
            //return client.listDomains();
            List<Record> records = new ArrayList<Record>();
            int count = client.getNumberOfResults(DOMAIN, query);
            if (count > 0) {
                List<String> f = new ArrayList<String>();
                f.add("id");
                f.add("name");
                if (includeDescription) {
                    f.add("description");
                }
                String[] fields = f.toArray(new String[f.size()]);
                String[][] results = client.getResults(DOMAIN, query, fields, pageNumber - 1, resultsPerPage);
                for (String[] a : results) {
                    String id = null, name = null, description = null;
                    int len = a.length;
                    if (len > 0) {
                        id = a[0];
                        if (len > 1) {
                            name = a[1];
                            if (len > 2) {
                                description = a[2];
                            }
                        }
                    }
                    else {
                        throw new IllegalStateException("No results, yet result count reported as " + count);
                    }
                    records.add(new Record(id, name, description));
                }
            }
            return new Page(count, records);
        }
        catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Page {

        private final int count;
        private final List<Record> records;

        public Page(int count, List<Record> records) {
            this.count = count;
            this.records = records;
        }

        public int getCount() {
            return count;
        }

        public List<Record> getRecords() {
            return records;
        }

    }

    public static final class Record {

        private final String id;
        private final String name;
        private final String description;

        public Record(String id, String name, String description) {
            this.id          = id;
            this.name        = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    public static final class Query {

        /**
         * Any letter of the alphabet allowed, in UniParc at least:
         *
         * (1) Following are allowed in addition to the 20 standard amino acids:
         *     Selenocysteine	                    U
         *     Pyrrolysine	                        O
         *
         * (2) Placeholders are used where chemical or crystallographic analysis of a peptide or protein
         *     cannot conclusively determine the identity of a residue:
         *     Asparagine or aspartic acid		    B
         *     Glutamine or glutamic acid		    Z
         *     Leucine or Isoleucine		        J
         *     Unspecified or unknown amino acid	X
         */
        private static final String AMINO_ACID = "^[A-Z]";

        // Minimum sequence length (estimate from http://en.wikipedia.org/wiki/Longest_word_in_English)
        private static final String MIN_LENGTH = "{30,}";

        // Match anything
        private static final String ANY_CHAR   = ".*";

        // Line breaks are not significant (http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#DOTALL)
        private static final Pattern MIN_LEN_AMINO_ACID_PATTERN = Pattern.compile(AMINO_ACID + MIN_LENGTH + ANY_CHAR, Pattern.DOTALL);

        private static final Pattern FASTA_HEADER_PATTERN = Pattern.compile(">.+\\s*", Pattern.MULTILINE);

        public static boolean isSequence(String query) {
            // Remove header line if present
            String s = FASTA_HEADER_PATTERN.matcher(query).replaceAll("");
            return (MIN_LEN_AMINO_ACID_PATTERN.matcher(s).matches());
        }

    }

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new IllegalArgumentException("Please pass in a search term or sequence");
        }

        String query = args[0];

        boolean includeDescription = false;
        if (args.length > 1) {
            includeDescription = Boolean.valueOf(args[1]);
        }

        // Amino acid sequence?
        // TODO: Better to just take MD5 of the query string and check against search index for any hits
        if (Query.isSequence(query)) {

            // Strip whitespace
            String sequence = query.replaceAll("\\s+", "").toUpperCase();

            // Get MD5
            String md5;
            try {
                int HEXADECIMAL_RADIX = 16;
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(sequence.getBytes(), 0, sequence.length());
                md5 = new BigInteger(1, m.digest()).toString(HEXADECIMAL_RADIX).toLowerCase(Locale.ENGLISH);
            }
            catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Cannot find MD5 algorithm", e);
            }


            System.out.println("Check InterPro database for MD5: " + md5);

            // Following is based on uk.ac.ebi.interpro.web.pageObjects.InterProScanClient in DBML:
            String baseUrl    = "http://www.ebi.ac.uk/Tools/services/web_iprscan/";
            String params;
            try {
                params = "tool="       + URLEncoder.encode("iprscan", "UTF-8") +
                         "&sequence="  + URLEncoder.encode(sequence, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Could not URL encode sequence: " + sequence, e);
            }
            String url = baseUrl + "toolform.ebi" + "?" + params;
            System.out.println("If no MD5 match, forward sequence to InterProScan: " + url);

            System.exit(0); // OK

        }

        // TODO: get resultsPerPage from args (default to 10)
        // TODO: check if query looks like protein sequence
        EbiSearchClient search = new EbiSearchClient();

        // TODO: Add paging and get input from keyboard to show next page
        EbiSearchClient.Page page = search.search(query, 1, 10, includeDescription);
        List<EbiSearchClient.Record> records = page.getRecords();

        if (page.count > 0) {
            System.out.println("Found " + page.count + " results for '" + query + "'.");
            if (records.size() > 9) {
                System.out.println("Showing results 1 to " + records.size() + ":");
            }
            System.out.println();
            for (EbiSearchClient.Record r : records) {
                System.out.println(r.name + " (" + r.id + ")");
                if (r.description != null) {
                    // TODO: Show only 5 words either same of query? What if fuzzy query? Better if search index does this!
                    System.out.println(r.description);
                    System.out.println("-------------------------------------------------------------------------------");
                }
            }
        }
        else {
            System.out.println("No results for '" + query + "'.");
        }

    }

}
