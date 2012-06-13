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

    /**
     * Get related results for this query.
     *
     * @param query The search query to use
     * @return The list of related results
     */
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
                        if (!id.equals("ebiweb") && !id.equals("proteinFamilies") && !id.equals("ontologies")) {
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
            return new Page(query,
                    count,
                    records,
                    getRelatedResults(query),
                    getLinks("search?q="+query+"&amp;page=${page}", pageNumber, count, resultsPerPage));
        }
        catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Construct a list of pagination links for the search based on the current page and other parameters.
     * This code is based on that provided by external services.
     *
     * @param urlPattern Link URL pattern
     * @param currentPage Current page number
     * @param count Total number of search results
     * @param resultsPerPage The number of results to show per page
     * @return List of link information objects
     */
    private List<LinkInfoBean> getLinks(final String urlPattern,
                                        final int currentPage,
                                        final int count,
                                        final int resultsPerPage) {

        // TODO
        // Example search URLs where urlPattern is "search?q=kinase":
        // http://frontier.ebi.ac.uk/interpro/search?q=kinase&start=0 // Query for "kinase" show page 1.
        // http://frontier.ebi.ac.uk/interpro/search?q=kinase&start=20 // Query for "kinase", starting from the 21st
        // result, but page number will depend on
        // how many results per page the user is showing.

        int numberOfPages = 0;
        if (resultsPerPage > 0) {
            numberOfPages = count / resultsPerPage;
            if (count % resultsPerPage > 0) {
                numberOfPages++;
            }
        }

        final List<LinkInfoBean> links = new ArrayList<LinkInfoBean>();
        if (numberOfPages <= 1) {
            return links;
        }
        LinkInfoBean bean;
        int startPage = 1;
        int endPage = numberOfPages;
        if (numberOfPages > 10) {
            startPage = currentPage - 4;
            if (startPage <= 0) {
                startPage = 1;
            }
            endPage = startPage + 9;
            if (endPage > numberOfPages) {
                endPage = numberOfPages;
            }
        }
        if (currentPage > 1) {
            bean = new LinkInfoBean();
            bean.setName("« First");
            bean.setLink(urlPattern.replaceAll("\\$\\{page\\}", "1"));
            bean.setDescription("Go to the first page");
            links.add(bean);

            bean = new LinkInfoBean();
            bean.setName("‹ Previous");
            bean.setLink(urlPattern.replaceAll("\\$\\{page\\}", Integer.toString(currentPage - 1)));
            bean.setDescription("Go to the previous page");
            links.add(bean);
        }
        for (int i = startPage; i <= endPage; i++) {
            bean = new LinkInfoBean();
            bean.setName("" + i);
            if (i != currentPage) {
                bean.setLink(urlPattern.replaceAll("\\$\\{page\\}", Integer.toString(i)));
            }
            bean.setDescription("Go to page " + i);
            links.add(bean);
        }
        if (currentPage < numberOfPages) {
            bean = new LinkInfoBean();
            bean.setName("Next ›");
            bean.setLink(urlPattern.replaceAll("\\$\\{page\\}", Integer.toString(currentPage + 1)));
            bean.setDescription("Go to the next page");
            links.add(bean);

            bean = new LinkInfoBean();
            bean.setName("Last »");
            bean.setLink(urlPattern.replaceAll("\\$\\{page\\}", Integer.toString(numberOfPages)));
            bean.setDescription("Go to the last page");
            links.add(bean);
        }
        return links;
    }


    public static final class Page {

        private final String query;
        private final int count;
        private final List<Record> records;
        private final List<RelatedResult> relatedResults;
        private final List<LinkInfoBean> paginationLinks;

        public Page(String query, int count, List<Record> records, List<RelatedResult> relatedResults,
                    List<LinkInfoBean> paginationLinks) {
            this.query           = query;
            this.count           = count;
            this.records         = records;
            this.relatedResults  = relatedResults;
            this.paginationLinks = paginationLinks;
        }

        public String getQuery() {
            return query;
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

        public List<LinkInfoBean> getPaginationLinks() {
            return paginationLinks;
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
            NAMES.put("nucleotideSequences",    "DNA"); // Nucleotide Sequences
            NAMES.put("proteinSequences",       "Proteins"); // Protein Sequences
            NAMES.put("macromolecularStructures", "3D Structures"); // Macromolecular Structures
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

    /**
     * A Java bean to hold information about a link (search pagination) on the user interface.
     *
     * @author Matthew Fraser, EMBL-EBI, InterPro
     * @version $Id$
     * @since 1.0-SNAPSHOT
     */
    public static final class LinkInfoBean {
        private String name; // The link text shown to the user
        private String description; // Extra text description about the link (e.g. for use in title text)
        private String link; // The URL pattern to link to

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    /**
     * This main method allows user testing of the search algorithms without the graphical (HTML) output.
     * @param args
     * @throws IOException
     */
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

                // Show actual search result entries
                for (TextSearch.Record r : records) {
                    System.out.println(r.getName() + " (" + r.getId() + ") [" + r.getType() + "]");
                    if (r.getDescription() != null) {
                        // TODO: Show only 5 words either same of query? What if fuzzy query? Better if search index does this!
                        System.out.println(r.getDescription());
                        System.out.println("-------------------------------------------------------------------------------");
                    }
                }

                // Display pagination link text (not interactive for the user here though)
                for (TextSearch.LinkInfoBean link : page.getPaginationLinks()) {
                    System.out.println(link.getName() + " (" + link.getDescription() + ") -> " + link.getLink());
                }
                System.out.println();
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
