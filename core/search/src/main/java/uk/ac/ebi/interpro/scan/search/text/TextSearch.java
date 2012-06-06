package uk.ac.ebi.interpro.scan.search.text;

import uk.ac.ebi.ebinocle.webservice.ArrayOfDomainResult;
import uk.ac.ebi.ebinocle.webservice.DomainResult;
import uk.ac.ebi.interpro.scan.search.sequence.helper.SequenceHelper;
import uk.ac.ebi.interpro.scan.search.sequence.SequenceSearch;
import uk.ac.ebi.interpro.scan.search.sequence.UrlLocator;
import uk.ac.ebi.webservices.jaxws.EBeyeClient;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

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

    public List<String> getFields() {
        try {
            return Arrays.asList(client.listFields(DOMAIN));
        }
        catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Add caching (very slow!!)
    public List<RelatedResult> getRelatedResults(String query) {
        try {
            List<RelatedResult> relatedResults = new ArrayList<RelatedResult>();
            DomainResult dr = client.getDetailledNumberOfResults("allebi", query, false);
            ArrayOfDomainResult a = dr.getSubDomainsResults().getValue();
            if (a != null) {
                for (DomainResult sd : a.getDomainResult()) {
                    String id = sd.getDomainId().getValue();
                    int count = sd.getNumberOfResults();
                    if (count > 0) {
                        // TODO: use enum to exclude
                        if (!id.equals("ebiweb") && !id.equals("proteinFamilies")) {
                            relatedResults.add(new RelatedResult(id, sd.getNumberOfResults()));
                        }
                    }
                }
            }
            return relatedResults;
        }
        catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public Page search(String query, int pageNumber, int resultsPerPage, boolean includeDescription) {
        TextHighlighter highlighter = new TextHighlighter(query);
        try {
            //return client.listDomains();
            List<Record> records = new ArrayList<Record>();
            int count = client.getNumberOfResults(DOMAIN, query);
            if (count > 0) {
                List<String> f = new ArrayList<String>();
                f.add("id");
                f.add("name");
                f.add("type");
                if (includeDescription) {
                    f.add("description");
                }
                String[] fields = f.toArray(new String[f.size()]);
                String[][] results = client.getResults(DOMAIN, query, fields, pageNumber - 1, resultsPerPage);
                for (String[] a : results) {
                    String id = null, name = null, type = null, description = null;
                    int len = a.length;
                    if (len > 0) {
                        id = a[0];
                        if (len > 1) {
                            name = a[1];
                            if (len > 2) {
                                type = a[2];
                                if (len > 3) {
                                    description = a[3];
                                }
                            }
                        }
                    }
                    else {
                        throw new IllegalStateException("No results, yet result count reported as " + count);
                    }
                    // Highlight
                    name = highlighter.highlightTitle(name);
                    description = highlighter.highlightDescription(description);
                    records.add(new Record(id, name, type, description));
                }
            }
            return new Page(query, count, records, getRelatedResults(query));
        }
        catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Page {

        private final String query;
        private final int count;
        private final List<Record> records;
        private final List<RelatedResult> relatedResults;

        public Page(String query, int count, List<Record> records, List<RelatedResult> relatedResults) {
            this.query          = query;
            this.count          = count;
            this.records        = records;
            this.relatedResults = relatedResults;
        }

        public int getCount() {
            return count;
        }

        public List<Record> getRecords() {
            return records;
        }

        public List<RelatedResult> getRelatedResults() {
            return relatedResults;
        }

        public String getQuery() {
            return query;
        }

    }

    public static final class Record {

        private final String id;
        private final String name;
        private final String type;
        private final String description;

        public Record(String id, String name, String type, String description) {
            this.id          = id;
            this.name        = name;
            this.type        = type;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

    }

    public static final class RelatedResult {

        private final String id;
        private final String name;
        private final int    count;

        // TODO: better as enum
        private static final Map<String, String> NAMES = new HashMap<String, String>();
        static {
            NAMES.put("genomes",                "Genomes");
            NAMES.put("nucleotideSequences",    "Nucleotide Sequences");
            NAMES.put("proteinSequences",       "Protein Sequences");
            NAMES.put("macromolecularStructures", "Macromolecular Structures");
            NAMES.put("smallMolecules",         "Small Molecules");
            NAMES.put("geneExpression",         "Gene Expression");
            NAMES.put("molecularInteractions",  "Molecular Interactions");
            NAMES.put("reactionsPathways",      "Reactions & Pathways");
            NAMES.put("proteinFamilies",        "Protein Families");
            NAMES.put("enzymes",                "Enzymes");
            NAMES.put("literature",             "Literature");
            NAMES.put("ontologies",             "Ontologies");
            NAMES.put("ebiweb",                 "EBI Website");
        }

        public RelatedResult(String id, int count) {
            this.id     = id;
            this.name   = NAMES.containsKey(id) ? NAMES.get(id) : id;
            this.count  = count;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
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

        // Get query
        String endPointUrl = "";
        if (args.length > 1) {
            endPointUrl = args[1];
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

            boolean includeDescription = true;
            if (args.length > 2) {
                includeDescription = Boolean.valueOf(args[2]);
            }

            // TODO: get resultsPerPage from args (default to 10)
            TextSearch search;
            if (endPointUrl.isEmpty()) {
                search = new TextSearch();
            }
            else {
                search = new TextSearch(endPointUrl);
            }

            //System.out.println(search.getFields());

            // TODO: Add paging and get input from keyboard to show next page
            TextSearch.Page page = search.search(query, 1, 10, includeDescription);
            List<TextSearch.Record> records = page.getRecords();

            if (page.getCount() > 0) {
                System.out.println("Found " + page.getCount() + " results for '" + query + "'.");
                if (records.size() > 9) {
                    System.out.println("Showing results 1 to " + records.size() + ":");
                }
                System.out.println();
                for (TextSearch.Record r : records) {
                    System.out.println(r.getName() + " (" + r.getId() + ") [" + r.getType() + "]");
                    if (r.getDescription() != null) {
                        // TODO: Show only 5 words either same of query? What if fuzzy query? Better if search index does this!
                        System.out.println(r.getDescription());
                        System.out.println("-------------------------------------------------------------------------------");
                    }
                }
            }
            else {
                System.out.println("No results for '" + query + "'.");
            }

            if (page.getRelatedResults().isEmpty()) {
                System.out.println("No related results");
            }
            else {
                System.out.println("Related results:");
                for (RelatedResult r : page.getRelatedResults()) {
                    System.out.println(r.getName() + " (" + r.getCount() + ")");
                }
            }

        }
    }

}
