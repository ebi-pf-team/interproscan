package uk.ac.ebi.interpro.scan.search;

import uk.ac.ebi.interpro.scan.search.helper.SequenceHelper;
import uk.ac.ebi.webservices.jaxws.EBeyeClient;

import javax.xml.rpc.ServiceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * @author  Antony Quinn
 * @version $Id$
 */
public final class TextSearch {

    // "Domain" in this sense means the name of the index in the EBI search engine
    private static String DOMAIN = "interpro";

    // Could configure in Spring
    private final EBeyeClient client;

    public TextSearch() {
        client = new EBeyeClient();
    }

    public TextSearch(String endPointUrl) {
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

    public static void main(String[] args) throws IOException {

        // Get query
        String query = "";
        if (args.length > 0) {
            query = args[0];
        }
        else {
            throw new IllegalArgumentException("Please pass in a search term or sequence");
        }

        // Is it a sequence?
        if (SequenceHelper.isProteinSequence(query)) {

            // TODO: Better to just take MD5 of the query string and check against search index for any hits

            // Don't look up MD5s from database
            UrlLocator urlLocator = new UrlLocator() {
                @Override public String locateUrl(String query) {
                    return null;
                }
            };

            // Get URL of pre-calculated match or InterProScan result page
            System.out.println("Sending to InterProScan...");
            SequenceSearch sequenceSearch = new SequenceSearch(urlLocator);
            String url = sequenceSearch.locateUrl(query);
            System.out.println("Done. Results available at: \n" + url);

        }
        else {

            // It's not a sequence

            boolean includeDescription = false;
            if (args.length > 1) {
                includeDescription = Boolean.valueOf(args[1]);
            }

            // TODO: get resultsPerPage from args (default to 10)
            TextSearch search = new TextSearch();

            // TODO: Add paging and get input from keyboard to show next page
            TextSearch.Page page = search.search(query, 1, 10, includeDescription);
            List<TextSearch.Record> records = page.getRecords();

            if (page.count > 0) {
                System.out.println("Found " + page.count + " results for '" + query + "'.");
                if (records.size() > 9) {
                    System.out.println("Showing results 1 to " + records.size() + ":");
                }
                System.out.println();
                for (TextSearch.Record r : records) {
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

}
