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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EBI Search web service client.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class TextSearch {

    // "Domain" in this sense means the name of the index in the EBI search engine
    private static final String DOMAIN = "interpro";

    // TODO Remove these as part of supporting multiple facets...
    private static final String FACET_TYPE_NAME = "type";
    private static final String FACET_TYPE = FACET_TYPE_NAME + ":";

    // Could configure in Spring
    private final EBeyeClient client;

    public TextSearch() {
        client = new EBeyeClient();
    }

    public TextSearch(String endPointUrl) {
        client = new EBeyeClient();
        client.setServiceEndPoint(endPointUrl); // E.g. "http://www.ebi.ac.uk/ebisearch/service.ebi"
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
     * @param query The search query to use (as entered/viewed by the user, no escape characters required)
     * @return The list of related results
     */
    public List<RelatedResult> getRelatedResults(final String query) {
        final String escapedQuery = escapeSpecialChars(query);
        try {
            List<RelatedResult> relatedResults = new ArrayList<RelatedResult>();
            ArrayOfDomainResult a = null;
            try {
                DomainResult dr = client.getDetailledNumberOfResults("allebi", escapedQuery, false);
                a = dr.getSubDomainsResults().getValue();
            }
            catch (SOAPFaultException e) {
                // TODO: Add logging
                System.out.println("Could not get related results for " + escapedQuery + ": " + e);
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

    /**
     * Get facets for the queries.
     * @param query The query with facet selections taken into account (assumes escape characters have already been added)
     * @param queryWithoutFacets The query ignoring any facet selections (assumes escape characters have already been added)
     * @return The list of facets
     */
    private List<Facet> getFacets(final String query, final String queryWithoutFacets) {

        /*
         * Example facet to currently selected values map:
         * dbname -> [pfam, tigrfam]
         * type -> [domain]
         */
        Map<String, Set<String>> facetToSelectedValuesMap = getFacetToValuesMap(query);
        boolean isAllSelected = facetToSelectedValuesMap.isEmpty();

        List<Facet> facets = new ArrayList<Facet>();
        try {
            List<uk.ac.ebi.ebinocle.webservice.Facet> f = client.getFacets(DOMAIN, queryWithoutFacets);
            for (uk.ac.ebi.ebinocle.webservice.Facet facet : f) {
                String facetName = facet.getLabel().getValue().toLowerCase(); // E.g. "dbname" or "type"
                for (FacetValue v : facet.getFacetValues().getValue().getFacetValue()) {
                    String label = Facet.convertLabel(v.getLabel());
                    String id    = label.replaceAll(" ", "_").toLowerCase(); // E.g. "domain"
                    boolean selected = false;
                    if (!isAllSelected) {
                        // Is this ID in our set of known selected values (e.g. "[domain]") for this facet (e.g. "type")?
                        final Set<String> selectedValues = facetToSelectedValuesMap.get(facetName);
                        boolean isAllThisFacetSelected = selectedValues.isEmpty();
                        if (!isAllThisFacetSelected) {
                            selected = selectedValues.contains(id);
                        }
                    }
                    facets.add(new Facet(facetName + ":", id, label, v.getHitCount(), selected));
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

    /**
     * Extract text relevant to InterPro facets from the supplied query.
     *
     * Example query:
     * domain_source:interpro GO:0005524 kinase type:domain insulin dbname:(pfam OR tigrfam)
     * Returns a map:
     * dbname -> [pfam, tigrfam]
     * type -> [domain]
     *
     * @param query
     * @return
     */
    private Map<String, Set<String>> getFacetToValuesMap(final String query) {

        String regex = "(";
        boolean firstFacet = true;
        for (FacetName facetName : FacetName.values()) {
            if (facetName.isInterProSpecific()) {
                if (!firstFacet) {
                    regex += "|";
                }
                regex += facetName;
                firstFacet = false;
            }
        }
        regex += "):(\\w+|\\([^)]+\\))";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);

        Map<String, Set<String>> facetToValuesMap = new HashMap<String, Set<String>>();
        /*
         * Example query:
         * domain_source:interpro GO:0005524 kinase type:domain insulin dbname:(pfam OR tigrfam)
         * Returns matches:
         * - type:domain
         * - dbname:(pfam OR tigrfam)
         */
        while (matcher.find()) {
            // Example text to parse: "type:(family OR domain)"
            String textToParse = matcher.group();
            String[] s = textToParse.split(":\\(?");
            final String facetName = s[0]; // E.g. "type"
            textToParse = s[1]; // E.g. "family OR domain)"
            if (textToParse.endsWith(")")) {
                textToParse = textToParse.substring(0, textToParse.length()-1); // E.g. "family OR domain"
            }
            s = textToParse.split("\\sOR\\s"); // [family, domain]
            Set<String> facetValues = new HashSet<String>();
            for (int i = 0; i < s.length; i++) {
                facetValues.add(s[i]);
            }
            if (facetToValuesMap.containsKey(facetName)) {
                //TODO Throw an error here instead??
                // Key already exists so add to it (values are unique in a set)
                facetToValuesMap.get(facetName).addAll(facetValues);
            }
            else {
                facetToValuesMap.put(facetName, facetValues);
            }
        }
        return facetToValuesMap;
    }


    /**
     * Just remove any facet search filtering terms from the query text provided.
     * @param query The query to remove facets from (if any).
     * @param interproOnly Only remove InterPro specific facets?
     * @return The query with facets removed
     */
    private String stripFacets(String query, boolean interproOnly) {
        for (FacetName facetName : FacetName.values()) {
            final String facet = facetName + ":";
            if (query.contains(facet)) {
                if (!interproOnly || facetName.isInterProSpecific()) {
                    // Remove this facet text! E.g. remove "type:domain" or "type:(domain OR family)"
                    // TODO Won't work with "kinase (type:domain OR type:family)"
                    query = query.replaceAll(facet + "(\\w+|\\([^)]+\\))", "").trim();
                }
            }
        }

        return query;
    }

    /**
     * Take the query and add escape characters as appropriate.
     * E.g. "infected cells: Cp-IAP type:domain" would become "infected cells\\: Cp-IAP type:domain", note that the escape
     * character was added to the colon in the plain text, but not to the "type" facet.
     * @param query The query without escape characters
     * @return The same query with escape characters added
     */
    private String escapeSpecialChars(String query) {
        StringBuilder sb = new StringBuilder("(?<!");
        FacetName[] facetNames = FacetName.values();
        for (int i = 0; i < facetNames.length; i++) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append(facetNames[i].getName()); // Look for colons that are not preceded with a recognised facet name
        }
        sb.append("):");
        final String regex = sb.toString();
        query = query.replaceAll(regex, "\\\\:");
        return query;
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
    public SearchPage search(final String query, int resultIndex, int resultsPerPage, boolean includeDescription) {
        String internalQuery = query; // An internal version of the query (as entered by the user, but changed behind
                                      // the scenes to pass the the EBI search webservice).

        // Always add on "domain_source:interpro" to every search page query, even though it's only really necessary
        // when the user entered an empty query, or just a facet with no search terms!
        final String DOMAIN_SOURCE = FacetName.DOMAIN_SOURCE.getName() + ":" + DOMAIN;
        if (internalQuery.isEmpty()) {
            internalQuery =  DOMAIN_SOURCE;
        }
        else if (!internalQuery.contains(DOMAIN_SOURCE)) {
            internalQuery = DOMAIN_SOURCE + " " + internalQuery;
        }

        final String escapedInternalQuery = escapeSpecialChars(internalQuery); // Query with escape chars added, not to be displayed to the user!
        TextHighlighter highlighter = new TextHighlighter(escapedInternalQuery);

        try {
            //String[] domains = client.listDomains(); // See what domains are available besides INTERPRO
            List<Result> records = new ArrayList<Result>();
            int count = client.getNumberOfResults(DOMAIN, escapedInternalQuery);
            if (count > 0) {
                List<String> f = new ArrayList<String>();
                f.add("id");
                f.add("name");
                f.add("type");
                if (includeDescription) {
                    f.add("description");
                }
                String[] fields = f.toArray(new String[f.size()]);
                String[][] results = client.getResults(DOMAIN, escapedInternalQuery, fields, resultIndex, resultsPerPage);
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
            // Only strip InterPro specific facets (e.g. type:family, dbname:pfam), leave others (e.g. domain_source:interpro)
            final String queryWithoutFacets  = stripFacets(internalQuery, false);
            final String internalQueryWithoutInterProFacets = stripFacets(internalQuery, true);
            final String escapedInternalQueryWithoutInterProFacets = escapeSpecialChars(internalQueryWithoutInterProFacets);
            List<Facet> facets = new ArrayList<Facet>();
            int countWithoutFacets = 0;
            if (!escapedInternalQueryWithoutInterProFacets.isEmpty()) {
                countWithoutFacets = client.getNumberOfResults(DOMAIN, escapedInternalQueryWithoutInterProFacets);
                facets = getFacets(escapedInternalQuery, escapedInternalQueryWithoutInterProFacets);
            }
            return new SearchPage(
                    query, // For display to the user, therefore use the query as entered by the user
                    queryWithoutFacets, // As entered by the user, with all facets removed
                    count,
                    countWithoutFacets,
                    getCurrentPage(resultIndex, resultsPerPage),
                    records,
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

    /**
     * Retrieve data for related search results from other EBI resources (not from InterPro).
     * @param query The search query (without escape chars).
     * @return Data required to display related search results on a web page.
     */
    public RelatedResultsPage getRelatedResultsPage(final String query) {
        // Strip all facets (incl domain_source:interpro), not just the InterPro specific facets (e.g. type:family, dbname:pfam)
        final String internalQueryWithoutAllFacets = stripFacets(query, false);
        final String escapedInternalQueryWithoutAllFacets = escapeSpecialChars(internalQueryWithoutAllFacets);
        List<RelatedResult> relatedResults = getRelatedResults(escapedInternalQueryWithoutAllFacets);
        return new RelatedResultsPage(query, internalQueryWithoutAllFacets, relatedResults);
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


    /**
     * A page class to hold necessary data for displaying an InterPro search results page.
     * Related results (non-InterPro) are not included, but can be retrieved separately.
     */
    public static final class SearchPage {

        private final String query; // The query as entered/read by the user (without escape chars added)
        private final String queryWithoutFacets; // The same as query, but with facets removed (without escape chars added)
        private final int count; // The number of results returned by the query, taking selected facets into account
        private final int countWithoutFacets; // The total number of results returned by the query (so ignoring facets)
        private final int currentPage; // The current page number in the search results (pagination)
        private final List<Result> results; // Data for the search results (for the current page only)
        private final List<LinkInfoBean> paginationLinks; // The pagination links to display for the current page
        private final List<Facet> facets; // The facets available, to allow filtering of search results

        public SearchPage(String query, String queryWithoutFacets, int count, int countWithoutFacets, int currentPage,
                          List<Result> results, List<LinkInfoBean> paginationLinks, List<Facet> facets) {
            this.query = query;
            this.queryWithoutFacets = queryWithoutFacets;
            this.count = count;
            this.countWithoutFacets = countWithoutFacets;
            this.currentPage = currentPage;
            this.results = results;
            this.paginationLinks = paginationLinks;
            this.facets = facets;
        }

        public String getQuery() {
            return query;
        }

        public String getQueryWithoutFacets() {
            return queryWithoutFacets;
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

        public List<LinkInfoBean> getPaginationLinks() {
            return paginationLinks;
        }

    }

    /**
     * A page class to hold necessary data for displaying the related results only.
     */
    public static final class RelatedResultsPage {

        private final String query;
        private final String queryWithoutFacets;
        private final List<RelatedResult> relatedResults;

        public RelatedResultsPage(String query, String queryWithoutFacets, List<RelatedResult> relatedResults) {
            this.query              = query;
            this.queryWithoutFacets = queryWithoutFacets;
            this.relatedResults     = relatedResults;
        }

        public String getQuery() {
            return query;
        }

        public String getQueryWithoutFacets() {
            return queryWithoutFacets;
        }

        public List<RelatedResult> getRelatedResults() {
            return relatedResults;
        }

        public boolean areRelatedResultsNotEmpty() {
            return getResultsCount() > 0;
        }

        /**
         * Get the number of results (number available for display on this page only)
         * @return The count
         */
        public int getResultsCount() {
            if (relatedResults == null) {
                return 0;
            }
            return relatedResults.size();
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

    /**
     * A related search result (not from InterPro, but from other EBI resources).
     */
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
     * A search facet.
     */
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

            SearchPage searchPage = search.search(query, 0, 10, includeDescription);
            List<Result> results = searchPage.getResults();

            if (searchPage.getCount() > 0) {

                System.out.println("Page " + searchPage.getCurrentPage() + " of " + searchPage.getCount() + " results for '" + query + "':");
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
                for (TextSearch.LinkInfoBean link : searchPage.getPaginationLinks()) {
                    System.out.println(link.getName() + " (" + link.getDescription() + ") -> " + link.getLink());
                }
                System.out.println();

                // Show facets
                System.out.println("Facets:");
                System.out.println("All results (" + searchPage.getCountWithoutFacets() + ")");
                for (Facet f : searchPage.getFacets()) {
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

            // Show related results
            List<RelatedResult> relatedResults = search.getRelatedResults(query);
            System.out.println("Related Results:");
            if (relatedResults == null || relatedResults.size() < 1) {
                System.out.println("None");
            }
            else {
                for (RelatedResult rr : relatedResults) {
                    System.out.println(rr.getName() + " (" + rr.getId() + ") [" + rr.getCount() + "]");
                }
            }
        }
    }

}
