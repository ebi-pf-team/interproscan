package uk.ac.ebi.interpro.scan.search.text;

import uk.ac.ebi.ebinocle.webservice.ArrayOfDomainResult;
import uk.ac.ebi.ebinocle.webservice.DomainResult;
import uk.ac.ebi.ebinocle.webservice.FacetValue;
import uk.ac.ebi.interpro.scan.search.sequence.helper.SequenceHelper;
import uk.ac.ebi.interpro.scan.search.sequence.SequenceSearch;
import uk.ac.ebi.interpro.scan.search.sequence.UrlLocator;
import uk.ac.ebi.webservices.jaxws.EBeyeClient;

import javax.xml.rpc.ServiceException;
import javax.xml.ws.soap.SOAPFaultException;
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

    private static final String FACET_TYPE = "type:";

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
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
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
            ArrayOfDomainResult a = null;
            try {
                DomainResult dr = client.getDetailledNumberOfResults("allebi", query, false);
                a = dr.getSubDomainsResults().getValue();
            }
            catch (SOAPFaultException e) {
                // TODO: Add logging
                System.out.println("Could not get related results for " + query + ": " + e);
            }
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
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Facet> getFacets(String query, String queryWithoutFacets) {
        String currentFacet = findFacetName(query);
        boolean isAllSelected = currentFacet.isEmpty();
        List<Facet> facets = new ArrayList<Facet>();
        try {
            List<uk.ac.ebi.ebinocle.webservice.Facet> f = client.getFacets(DOMAIN, queryWithoutFacets);
            for (uk.ac.ebi.ebinocle.webservice.Facet facet : f) {
                String prefix = facet.getLabel().getValue().toLowerCase();
                for (FacetValue v : facet.getFacetValues().getValue().getFacetValue()) {
                    String label = Facet.convertLabel(v.getLabel());
                    String id    = label.replaceAll(" ", "_").toLowerCase();
                    boolean selected = false;
                    if (!isAllSelected) {
                        selected = id.equals(currentFacet);
                    }
                    facets.add(new Facet(prefix + ":", id, label, v.getHitCount(), selected));
                }
            }
        }
        catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
        }
        return facets;
    }

    public String stripFacets(String query) {
        if (query.contains(FACET_TYPE)) {
            String s = query.replaceAll(FACET_TYPE + "\\w+", "");
            return s.trim();
        }
        else {
            return query;
        }
    }

    public String findFacetName(String query) {

        // TODO Perhaps one day we will need to saerch for multiple facets and values, e.g.
        // "kinase type:(domain OR ptm)" and other things in Lucene query syntax
        // http://lucene.apache.org/core/old_versioned_docs/versions/3_0_0/queryparsersyntax.html
        // But for now it's OK!

        if (query.contains(FACET_TYPE)) {
            // Assumes the facet is the last part of the query, eg. "kinase type:domain"
            // Would be better if worked no matter where facet is in query string
            String[] s = query.split(FACET_TYPE);
            return s[1].trim();
        }
        else {
            return "";
        }
    }

    /**
     * Perform a text search.
     *
     * @param query The query
     * @param resultIndex Retrieve results from this (zero indexed) search result number
     * @param resultsPerPage The number of results to retrieve for a page
     * @param includeDescription Include full search result description text
     * @return A page object containing all necessary variables required to display a search results page
     */
    public Page search(String query, int resultIndex, int resultsPerPage, boolean includeDescription) {
        if (query.isEmpty()) {
            return new Page(
                    "", "",
                    0, 0, 1,
                    Collections.<Result>emptyList(),
                    Collections.<RelatedResult>emptyList(),
                    Collections.<LinkInfoBean>emptyList(),
                    Collections.<Facet>emptyList());
        }
        TextHighlighter highlighter = new TextHighlighter(query);
        try {
            //return client.listDomains();
            List<Result> records = new ArrayList<Result>();
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
                String[][] results = client.getResults(DOMAIN, query, fields, resultIndex, resultsPerPage);
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
                    records.add(new Result(id, name, type, description));
                }
            }
            String queryWithoutFacets = stripFacets(query);
            List<RelatedResult> relatedResults = new ArrayList<RelatedResult>();
            List<Facet> facets = new ArrayList<Facet>();
            int countWithoutFacets = 0;
            if (!queryWithoutFacets.isEmpty()) {
                countWithoutFacets = client.getNumberOfResults(DOMAIN, queryWithoutFacets);
                relatedResults = getRelatedResults(queryWithoutFacets);
                facets = getFacets(query, queryWithoutFacets);
            }
            return new Page(
                    query,
                    queryWithoutFacets,
                    count,
                    countWithoutFacets,
                    getCurrentPage(resultIndex, resultsPerPage),
                    records,
                    relatedResults,
                    getLinks("search?q="+query+"&amp;start=${start}", resultIndex, count, resultsPerPage),
                    facets);
        }
        catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
        catch (ServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    private int getCurrentPage(final int resultIndex, final int resultsPerPage) {
        return (resultIndex / resultsPerPage) + 1;
    }

    /**
     * Construct a list of pagination links for the search based on the current page and other parameters.
     * This code is based on that provided by external services.
     *
     * @param urlPattern Link URL pattern
     * @param resultIndex First result to retrieve (zero indexed)
     * @param count Total number of search results
     * @param resultsPerPage The number of results to show per page
     * @return List of link information objects
     */
    private List<LinkInfoBean> getLinks(final String urlPattern,
                                        final int resultIndex,
                                        final int count,
                                        final int resultsPerPage) {

        final int currentPage = getCurrentPage(resultIndex, resultsPerPage);

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
            bean.setLink(urlPattern.replaceAll("\\$\\{start\\}", "0"));
            bean.setDescription("Go to the first page");
            links.add(bean);

            bean = new LinkInfoBean();
            bean.setName("‹ Previous");
            bean.setLink(urlPattern.replaceAll("\\$\\{start\\}", Integer.toString(pageNumToStartIndex(currentPage, resultsPerPage) - resultsPerPage)));
            bean.setDescription("Go to the previous page");
            links.add(bean);
        }
        for (int i = startPage; i <= endPage; i++) {
            bean = new LinkInfoBean();
            bean.setName("" + i);
            if (i != currentPage) {
                bean.setLink(urlPattern.replaceAll("\\$\\{start\\}", Integer.toString(pageNumToStartIndex(i,resultsPerPage))));
            }
            bean.setDescription("Go to page " + i);
            links.add(bean);
        }
        if (currentPage < numberOfPages) {
            bean = new LinkInfoBean();
            bean.setName("Next ›");
            bean.setLink(urlPattern.replaceAll("\\$\\{start\\}", Integer.toString(pageNumToStartIndex(currentPage, resultsPerPage) + resultsPerPage)));
            bean.setDescription("Go to the next page");
            links.add(bean);

            bean = new LinkInfoBean();
            bean.setName("Last »");
            bean.setLink(urlPattern.replaceAll("\\$\\{start\\}", Integer.toString(pageNumToStartIndex(numberOfPages, resultsPerPage))));
            bean.setDescription("Go to the last page");
            links.add(bean);
        }
        return links;
    }

    /**
     * Use the supplied page number and the number of search results shown per page to calculate the start index of
     * the result required.
     * @param pageNum Page number
     * @param resultsPerPage Number of results per page
     * @return Equivalent search result index
     */
    private static int pageNumToStartIndex(final int pageNum, final int resultsPerPage) {
        return (pageNum - 1) * resultsPerPage;
    }


    public static final class Page {

        private final String query;
        private final String queryWithoutFacets;
        private final int count;
        private final int countWithoutFacets;
        private final int currentPage;
        private final List<Result> results;
        private final List<RelatedResult> relatedResults;
        private final List<LinkInfoBean> paginationLinks;
        private final List<Facet> facets;

        public Page(String query, String queryWithoutFacets,
                    int count, int countWithoutFacets, int currentPage,
                    List<Result> results, List<RelatedResult> relatedResults,
                    List<LinkInfoBean> paginationLinks, List<Facet> facets) {
            this.query           = query;
            this.queryWithoutFacets = queryWithoutFacets;
            this.count           = count;
            this.countWithoutFacets = countWithoutFacets;
            this.currentPage     = currentPage;
            this.results         = results;
            this.relatedResults  = relatedResults;
            this.paginationLinks = paginationLinks;
            this.facets          = facets;
        }

        /**
         * Get the total number of results for this search (for selected facets only)
         * @return The count
         */
        public int getCount() {
            return count;
        }

        /**
         * Get the total number of results for this search (without taking facets into account)
         * @return The count
         */
        public int getCountWithoutFacets() {
            return countWithoutFacets;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public List<Facet> getFacets() {
            return facets;
        }

        public List<Result> getResults() {
            return results;
        }

        /**
         * Get the number of results (number available for display on this page only)
         * @return The count
         */
        public int getResultsCount() {
            if (results == null) {
                return 0;
            }
            return results.size();
        }


        public List<RelatedResult> getRelatedResults() {
            return relatedResults;
        }

        public List<LinkInfoBean> getPaginationLinks() {
            return paginationLinks;
        }

        public String getQuery() {
            return query;
        }

        public String getQueryWithoutFacets() {
            return queryWithoutFacets;
        }

    }

    public static final class Result {

        private final String id;
        private final String name;
        private final String type;
        private final String description;

        public Result(String id, String name, String type, String description) {
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

    public static final class Facet {

        private final String  prefix;
        private final String  id;
        private final String  label;
        private final int     count;
        private final boolean isSelected;

        public Facet(String prefix, String id, String  label, int count, boolean isSelected) {
            this.prefix     = prefix;
            this.id         = convertId(id);
            this.label      = convertLabel(label);
            this.count      = count;
            this.isSelected = isSelected;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getCount() {
            return count;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public static String convertId(String id) {
            if (id.equals("post-translational_modifications")) {
                return "ptm";
            }
            return id;
        }

        public static String convertLabel(String label) {
            if (label.equals("Post-translational Modifications")) {
                return "PTM";
            }
            if (label.endsWith("Site")) {
                return label.replace("Site", "site");
            }
            return label;
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
            if (query.equals("\"\"")) {
                query = "";
            }
        }
        else {
            throw new IllegalArgumentException("Please pass in a search term or sequence");
        }

        // Get query
        String endPointUrl = "";
        if (args.length > 1) {
            System.out.println();
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

            TextSearch.Page page = search.search(query, 0, 10, includeDescription);
            List<Result> results = page.getResults();

            if (page.getCount() > 0) {

                System.out.println("Page " + page.getCurrentPage() + " of " + page.getCount() + " results for '" + query + "':");
                System.out.println();

                // Show search results
                for (Result r : results) {
                    System.out.println(r.getName() + " (" + r.getId() + ") [" + r.getType() + "]");
                    if (r.getDescription() != null) {
                        System.out.println(r.getDescription());
                        System.out.println("-------------------------------------------------------------------------------");
                    }
                }

                // Display pagination link text (not interactive for the user here though)
                for (TextSearch.LinkInfoBean link : page.getPaginationLinks()) {
                    System.out.println(link.getName() + " (" + link.getDescription() + ") -> " + link.getLink());
                }
                System.out.println();

                // Show facets
                System.out.println("Facets:");
                System.out.println("All results (" + page.getCountWithoutFacets() + ")");
                for (Facet f : page.getFacets()) {
                    String s = "";
                    if (f.isSelected()) {
                        s = " -- selected";
                    }
                    System.out.println(f.getLabel() + " (" + f.getCount() + ") [" + f.getPrefix() + f.getId() + "]" + s);
                }
                System.out.println();

            }
            else {
                System.out.println("No results for '" + query + "'.");
            }

            if (page.getRelatedResults().isEmpty()) {
                System.out.println("No related results for '" + page.getQueryWithoutFacets() + "'.");
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
