/* $Id: EBeyeClient.java 1660 2010-11-23 15:07:26Z hpm $
 * ======================================================================
 * EB-eye Java client.
 * ====================================================================== */
package uk.ac.ebi.webservices.jaxws;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import uk.ac.ebi.ebinocle.webservice.*;
import uk.ac.ebi.ebisearchservice.*;
import uk.ac.ebi.ebisearchservice.ObjectFactory;

import javax.xml.rpc.ServiceException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.rmi.RemoteException;
import java.util.*;

// TODO: why is this in stubs? Is there a more recent version of this code?
//import uk.ac.ebi.webservices.jaxws.stubs.ebeye.*;

/**
 * Java EB-eye client using JAX-WS RI.
 * <p/>
 * See:
 * <a href="http://www.ebi.ac.uk/Tools/Webservices/services/ebeye">http://www.ebi.ac.uk/Tools/Webservices/services/ebeye</a>
 * <a href="http://www.ebi.ac.uk/Tools/Webservices/tutorials/java">http://www.ebi.ac.uk/Tools/Webservices/tutorials/java</a>
 */
public class EBeyeClient {
    /**
     * Output level. Controlled by the --verbose and --quiet options.
     */
    protected int outputLevel = 1;
    /**
     * Debug level. Controlled by the --debugLevel option.
     */
    private int debugLevel = 0;
    /**
     * URL for service endpoint.
     */
    private String serviceEndPoint = null;
    /**
     * Service proxy
     */
    private EBISearchService srvProxy = null;
    /**
     * Object factory to create service data types.
     */
    private ObjectFactory objFactory = new ObjectFactory();
    /**
     * Client version/revision
     */
    private String revision = "$Revision: 1660 $";
    /**
     * Client user-agent string.
     */
    private String clientUserAgent = null;
    /**
     * Usage message
     */
    private static final String usageMsg = "EB-eye\n"
            + "======\n"
            + "\n"
            + "--listDomains\n"
            + "  Returns a list of all the domains identifiers which can be used in a query.\n"
            + "\n"
            + "--getNumberOfResults <domain> <query>\n"
            + "  Executes a query and returns the number of results found.\n"
            + "\n"
            + "--getResultsIds <domain> <query> <start> <size>\n"
            + "  Executes a query and returns the list of identifiers for the entries found.\n"
            + "\n"
            + "--getAllResultsIds <domain> <query>\n"
            + "  Executes a query and returns the list of all the identifiers for the entries\n"
            + "  found.\n"
            + "\n"
            + "--listFields <domain>\n"
            + "  Returns the list of fields that can be retrieved for a particular domain.\n"
            + "\n"
            + "--getResults <domain> <query> <fields> <start> <size>\n"
            + "  Executes a query and returns a list of results. Each result contains the \n"
            + "  values for each field specified in the \"fields\" argument in the same order\n"
            + "  as they appear in the \"fields\" list.\n"
            + "\n"
            + "--getEntry <domain> <entry> <fields>\n"
            + "  Search for a particular entry in a domain and returns the values for some \n"
            + "  of the fields of this entry. The result contains the values for each field \n"
            + "  specified in the \"fields\" argument in the same order as they appear in the\n"
            + "  \"fields\" list.\n"
            + "\n"
            + "--getEntries <domain> <entries> <fields>\n"
            + "  Search for entries in a domain and returns the values for some of the \n"
            + "  fields of these entries. The result contains the values for each field \n"
            + "  specified in the \"fields\" argument in the same order as they appear in the\n"
            + "  \"fields\" list.\n"
            + "\n"
            + "--getEntryFieldUrls <domain> <entry> <fields>\n"
            + "  Search for a particular entry in a domain and returns the urls configured \n"
            + "  for some of the fields of this entry. The result contains the urls for each \n"
            + "  field specified in the \"fields\" argument in the same order as they appear\n"
            + "  in the \"fields\" list. \n"
            + "\n"
            + "--getEntriesFieldUrls <domain> <entries> <fields>\n"
            + "  Search for a list of entries in a domain and returns the urls configured for\n"
            + "  some of the fields of these entries. Each result contains the url for each \n"
            + "  field specified in the \"fields\" argument in the same order as they appear in\n"
            + "  the \"fields\" list.\n"
            + "\n"
            + "--getDomainsReferencedInDomain <domain>\n"
            + "  Returns the list of domains with entries referenced in a particular domain.\n"
            + "  These domains are indexed in the EB-eye.\n"
            + "\n"
            + "--getDomainsReferencedInEntry <domain> <entry>\n"
            + "  Returns the list of domains with entries referenced in a particular domain\n"
            + "  entry. These domains are indexed in the EB-eye.\n"
            + "\n"
            + "--listAdditionalReferenceFields <domain>\n"
            + "  Returns the list of fields corresponding to databases referenced in the\n"
            + "  domain but not included as a domain in the EB-eye.\n"
            + "\n"
            + "--getReferencedEntries <domain> <entry> <referencedDomain>\n"
            + "  Returns the list of referenced entry identifiers from a domain referenced\n"
            + "  in a particular domain entry.\n"
            + "\n"
            + "--getReferencedEntriesSet <domain> <entries> <referencedDomain> <fields>\n"
            + "  Returns the list of referenced entries from a domain referenced in a set of\n"
            + "  entries. The result will be returned as a list of objects, each representing\n"
            + "  an entry reference.\n"
            + "\n"
            + "--getReferencedEntriesFlatSet <domain> <entries> <referencedDomain> <fields>\n"
            + "  Returns the list of referenced entries from a domain referenced in a set of \n"
            + "  entries. The result will be returned as a flat table corresponding to the \n"
            + "  list of results where, for each result, the first value is the original \n"
            + "  entry identifier and the other values correspond to the fields values.\n"
            + "\n"
            + "--getDomainsHierarchy\n"
            + "  Returns the hierarchy of the domains available.\n"
            + "\n"
            + "--getDetailledNumberOfResults <domain> <query> <flat>\n"
            + "  Executes a query and returns the number of results found per domain.\n"
            + "\n"
            + "--listFieldsInformation <domain>\n"
            + "  Returns the list of fields that can be retrievedand/or searched for a\n"
            + "  particular domain.\n"
            + "\n"
            + "Further information:\n"
            + "\n"
            + "  http://www.ebi.ac.uk/Tools/webservices/services/eb-eye\n"
            + "  http://www.ebi.ac.uk/Tools/webservices/tutorials/java\n"
            + "\n"
            + "Support/Feedback:\n"
            + "\n"
            + "  http://www.ebi.ac.uk/support/\n"
            + "\n";

    /**
     * Default constructor
     */
    public EBeyeClient() {
        // Set the HTTP user agent string for requests
        this.setUserAgent();
    }

    /**
     * Set the HTTP User-agent header string for Java web calls (java.net).
     */
    private void setUserAgent() {
        printDebugMessage("setUserAgent", "Begin", 1);
        // Java web calls (java.net) use the http.agent property as a prefix to the default user-agent.
        String clientVersion = this.revision.substring(11, this.revision.length() - 2);
        this.clientUserAgent = "EBI-Sample-Client/" + clientVersion + " (" + this.getClass().getName() + "; " + System.getProperty("os.name") + ")";
        if (System.getProperty("http.agent") != null) {
            System.setProperty("http.agent", clientUserAgent + " " + System.getProperty("http.agent"));
        } else System.setProperty("http.agent", clientUserAgent);
        printDebugMessage("setUserAgent", "End", 1);
    }

    /**
     * Set the HTTP User-Agent for web services requests via the JAX-WS service proxy.
     */
    private void setPortUserAgent() {
        printDebugMessage("setPortUserAgent", "Begin", 1);
        if (this.clientUserAgent != null && this.clientUserAgent.length() > 0) {
            ((BindingProvider) this.srvProxy).getRequestContext().put(
                    MessageContext.HTTP_REQUEST_HEADERS,
                    Collections.singletonMap("User-Agent", Collections.singletonList(this.clientUserAgent)));
        }
        printDebugMessage("setPortUserAgent", "End", 1);
    }

    /**
     * Print the usage message to STDOUT.
     */
    private static void printUsage() {
        System.out.print(usageMsg);
    }

    /**
     * Set debug level.
     *
     * @param level Debug level. 0 = off.
     */
    public void setDebugLevel(int level) {
        printDebugMessage("setDebugLevel", "Begin " + level, 1);
        if (level > -1) {
            debugLevel = level;
        }
        printDebugMessage("setDebugLevel", "End", 1);
    }

    /**
     * Get current debug level.
     *
     * @return Debug level.
     */
    public int getDebugLevel() {
        printDebugMessage("getDebugLevel", Integer.toString(debugLevel),
                1);
        return debugLevel;
    }

    /**
     * Output debug message at specified level
     *
     * @param methodName Name of the method to appear in the message
     * @param message    The message
     * @param level      Level at which to output message
     */
    protected void printDebugMessage(String methodName, String message, int level) {
        if (level <= debugLevel) {
            System.err.println("[" + methodName + "()] " + message);
        }
    }

    /**
     * Set the output level.
     *
     * @param level Output level. 0 = quiet, 1 = normal and 2 = verbose.
     */
    public void setOutputLevel(int level) {
        printDebugMessage("setOutputLevel", "Begin " + level, 1);
        if (level > -1) {
            this.outputLevel = level;
        }
        printDebugMessage("setOutputLevel", "End", 1);
    }

    /**
     * Get the current output level.
     *
     * @return Output level.
     */
    public int getOutputLevel() {
        printDebugMessage("getOutputLevel", Integer.toString(this.outputLevel), 1);
        return this.outputLevel;
    }

    /**
     * Set the service endpoint URL for generating the service connection.
     *
     * @param urlStr Service endpoint URL as a string.
     */
    public void setServiceEndPoint(String urlStr) {
        printDebugMessage("setServiceEndpoint", "urlStr: " + urlStr, 1);
        this.serviceEndPoint = urlStr;
    }

    /**
     * Get the current service endpoint URL.
     *
     * @return The service endpoint URL as a string.
     */
    public String getServiceEndPoint() {
        printDebugMessage("getServiceEndpoint", "serviceEndPoint: " + this.serviceEndPoint, 1);
        return this.serviceEndPoint;
    }

    /**
     * Print a progress message.
     *
     * @param msg   The message to print.
     * @param level The output level at or above which this message should be displayed.
     */
    protected void printProgressMessage(String msg, int level) {
        if (outputLevel >= level) {
            System.err.println(msg);
        }
    }

    /**
     * Ensure that a service proxy is available to call the web service.
     *
     * @throws ServiceException
     */
    protected void srvProxyConnect() throws ServiceException {
        printDebugMessage("srvProxyConnect", "Begin", 2);
        if (this.srvProxy == null) {
            EBISearchService_Service service = null;
            if (this.getServiceEndPoint() != null) {
                try {
                    service = new EBISearchService_Service(new java.net.URL(this.getServiceEndPoint()), new javax.xml.namespace.QName("http://www.ebi.ac.uk/EBISearchService", "EBISearchService"));
                } catch (java.net.MalformedURLException ex) {
                    System.err.println(ex.getMessage());
                    System.err.println("Warning: problem with specified endpoint URL. Default endpoint used.");
                    service = new EBISearchService_Service();
                }
            } else {
                service = new EBISearchService_Service();
            }
            this.srvProxy = service.getEBISearchServiceHttpPort();
            this.setPortUserAgent();
        }
        printDebugMessage("srvProxyConnect", "End", 2);
    }

    /**
     * Get the web service proxy.
     *
     * @return The web service proxy.
     * @throws javax.xml.rpc.ServiceException
     */
    public EBISearchService getSrvProxy() throws javax.xml.rpc.ServiceException {
        printDebugMessage("getSrvProxy", "Begin", 2);
        this.srvProxyConnect(); // Ensure the service proxy exists
        printDebugMessage("getSrvProxy", "End", 2);
        return this.srvProxy;
    }

    /**
     * Split a string into an array using a specified set of separators.
     *
     * @param inStr      String to split.
     * @param sepListStr Set of characters to use as separators.
     * @return Array of strings.
     */
    @SuppressWarnings("unchecked")
    private String[] splitString(String inStr, String sepListStr) {
        printDebugMessage("splitString", "Begin", 11);
        String[] retVal = null;
        Vector vec = new Vector();
        StringTokenizer tok = new StringTokenizer(inStr, sepListStr);
        while (tok.hasMoreTokens()) {
            vec.add(tok.nextToken());
        }
        retVal = new String[vec.size()];
        for (int i = 0; i < vec.size(); i++) {
            retVal[i] = (String) vec.elementAt(i);
        }
        printDebugMessage("splitString", "End", 11);
        return retVal;
    }

    /**
     * Split a string into an array of strings based on the separators ' ', ','
     * and ';'.
     *
     * @param inStr String to split.
     * @return Array of strings.
     */
    private String[] splitString(String inStr) {
        return splitString(inStr, " ,;");
    }

    /**
     * Print an array of strings to STDOUT.
     *
     * @param strList Array of strings to print.
     */
    private void printStrList(String[] strList) {
        printDebugMessage("printStrList", "Begin", 1);
        for (int i = 0; i < strList.length; i++) {
            System.out.println(strList[i]);
        }
        printDebugMessage("printStrList", "End", 1);
    }

    /**
     * Print an array of array of strings to STDOUT.
     *
     * @param arrayList Array of array of strings to print.
     * @param table     Output in table format (multiple columns), otherwise list format (single column) is used.
     */
    private void printArrayOfStringList(String[][] arrayList, boolean table) {
        printDebugMessage("printArrayOfStringList", "Begin", 1);
        for (int i = 0; i < arrayList.length; i++) {
            String[] strList = arrayList[i];
            for (int j = 0; j < strList.length; j++) {
                if (j > 0) {
                    if (table) {
                        System.out.print("\t");
                    } else {
                        System.out.println();
                    }
                }
                System.out.print(strList[j]);
            }
            System.out.println();
        }
        printDebugMessage("printArrayOfStringList", "End", 1);
    }

    /**
     * Get a list of supported domain names.
     *
     * @return an array of domain names
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public String[] listDomains() throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("listDomains", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.listDomains();
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("listDomains", "End", 1);
        return retVal;
    }

    /**
     * Print list of supported domain names to STDOUT.
     *
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printListDomains() throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("printListDomains", "Begin", 1);
        printStrList(listDomains());
        printDebugMessage("printListDomains", "End", 1);
    }

    /**
     * Get the number of entries in a domain matching a query
     *
     * @param domain Name of the domain to search
     * @param query  The query to perform
     * @return an array of domain names
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public int getNumberOfResults(String domain, String query) throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("getNumberOfResults", "Begin", 1);
        int retVal = -1;
        srvProxyConnect(); // Ensure we have a service proxy
        retVal = this.srvProxy.getNumberOfResults(domain, query);
        printDebugMessage("getNumberOfResults", "End", 1);
        return retVal;
    }

    /**
     * Print number of entries matching a query
     *
     * @param domain Name of the domain to search
     * @param query  The query to perform
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printGetNumberOfResults(String domain, String query) throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("printGetNumberOfResults", "Begin", 1);
        System.out.println(getNumberOfResults(domain, query));
        printDebugMessage("printGetNumberOfResults", "End", 1);
    }

    /**
     * Get the identifiers of the entries matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @return Array of identifiers
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public String[] getResultsIds(String domain, String query, int start, int size) throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("getResultsIds", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.getResultsIds(domain, query, start, size);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getResultsIds", "End", 1);
        return retVal;
    }

    /**
     * Print identifiers matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printGetResultsIds(String domain, String query, int start, int size) throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("printGetResultsIds", "Begin", 1);
        printStrList(getResultsIds(domain, query, start, size));
        printDebugMessage("printGetResultsIds", "End", 1);
    }

    /**
     * Print identifiers matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    private void printGetResultsIds(String domain, String query, String start,
                                    String size) throws java.rmi.RemoteException,
            javax.xml.rpc.ServiceException {
        printDebugMessage("printGetResultsIds", "Begin", 1);
        printGetResultsIds(domain, query, new Integer(start), new Integer(size));
        printDebugMessage("printGetResultsIds", "End", 1);
    }

    /**
     * Get all the identifiers of the entries matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @return Array of identifiers
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public String[] getAllResultsIds(String domain, String query)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("getAllResultsIds", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.getAllResultsIds(domain, query);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getAllResultsIds",
                "retVal.length: " + retVal.length, 2);
        printDebugMessage("getAllResultsIds", "End", 1);
        return retVal;
    }

    /**
     * Print all identifiers matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printGetAllResultsIds(String domain, String query)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("printGetAllResultsIds", "Begin", 1);
        printStrList(getAllResultsIds(domain, query));
        printDebugMessage("printGetAllResultsIds", "End", 1);
    }

    /**
     * Get the list of fields from a domain
     *
     * @param domain The domain to examine
     * @return An array of field names
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public String[] listFields(String domain) throws java.rmi.RemoteException,
            javax.xml.rpc.ServiceException {
        printDebugMessage("listFields", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.listFields(domain);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("listFields", "End", 1);
        return retVal;
    }

    /**
     * Print the list of fields for a domain
     *
     * @param domain The name of the domain to examine
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printListFields(String domain)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("printListFields", "Begin", 1);
        printStrList(listFields(domain));
        printDebugMessage("printListFields", "End", 1);
    }

    /**
     * Get the list of facets for the query.
     *
     * @param domain The domain to examine
     * @param query  The query to perform
     * @return An array of facets
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public List<Facet> getFacets(String domain, String query)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("getFacets", "Begin", 1);
        List<Facet> retVal = null;
        srvProxyConnect();
        ArrayOfFacet result = this.srvProxy.getFacets(domain, query);
        retVal = result.getFacet(); //.toArray(new Facet[0]);
        printDebugMessage("getFacets", "End", 1);
        return retVal;
    }

    /**
     * Get the selected fields for the entries matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @return Array of identifiers
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public String[][] getResults(String domain, String query, String[] fields,
                                 int start, int size) throws java.rmi.RemoteException,
            javax.xml.rpc.ServiceException {
        printDebugMessage("getResults", "Begin", 1);
        String[][] retVal = null;
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfArrayOfString result = this.srvProxy.getResults(domain, query, tmpFields, start, size);
        List<ArrayOfString> tmpResult = result.getArrayOfString();
        retVal = new String[tmpResult.size()][];
        for (int i = 0; i < tmpResult.size(); i++) {
            retVal[i] = tmpResult.get(i).getString().toArray(new String[0]);
        }
        printDebugMessage("getResults", "End", 1);
        return retVal;
    }

    /**
     * Print selected fields for entries matching a query
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printGetResults(String domain, String query, String[] fields,
                                int start, int size) throws java.rmi.RemoteException,
            javax.xml.rpc.ServiceException {
        printDebugMessage("printGetResults", "Begin", 1);
        printArrayOfStringList(getResults(domain, query, fields, start, size),
                false);
        printDebugMessage("printGetResults", "End", 1);
    }

    /**
     * Print selected fields for entries matching a query.
     *
     * @param domain The domain to search
     * @param query  The query to perform
     * @param start  Starting index in results
     * @param size   Number of results to return
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    private void printGetResults(String domain, String query, String fields,
                                 String start, String size) throws java.rmi.RemoteException,
            javax.xml.rpc.ServiceException {
        printDebugMessage("printGetResults", "Begin", 1);
        String[] fieldNames = splitString(fields);
        printGetResults(domain, query, fieldNames, new Integer(start),
                new Integer(size));
        printDebugMessage("printGetResults", "End", 1);
    }

    /**
     * Get information about a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @return an array containing the values for the fields for the entry.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] getEntry(String domain, String entry, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getEntry", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString result = this.srvProxy.getEntry(domain, entry, tmpFields);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getEntry", "End", 1);
        return retVal;
    }

    /**
     * Print information about a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetEntry(String domain, String entry, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntry", "Begin", 1);
        printStrList(getEntry(domain, entry, fields));
        printDebugMessage("printGetEntry", "End", 1);
    }

    /**
     * Print information about a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    private void printGetEntry(String domain, String entry, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntry", "Begin", 1);
        String[] fieldNames = splitString(fields);
        printGetEntry(domain, entry, fieldNames);
        printDebugMessage("printGetEntry", "End", 1);
    }

    /**
     * Get information about a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @return an array containing the values for the fields for the entry.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[][] getEntries(String domain, String[] entries, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getEntries", "Begin", 1);
        String[][] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString tmpEntries = objFactory.createArrayOfString();
        tmpEntries.getString().addAll(Arrays.asList(entries));
        ArrayOfArrayOfString result = this.srvProxy.getEntries(domain, tmpEntries, tmpFields);
        List<ArrayOfString> tmpResult = result.getArrayOfString();
        retVal = new String[tmpResult.size()][];
        for (int i = 0; i < tmpResult.size(); i++) {
            retVal[i] = tmpResult.get(i).getString().toArray(new String[0]);
        }
        printDebugMessage("getEntries", "End", 1);
        return retVal;
    }

    /**
     * Print information about a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetEntries(String domain, String[] entries, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntries", "Begin", 1);
        printArrayOfStringList(getEntries(domain, entries, fields), false);
        printDebugMessage("printGetEntries", "End", 1);
    }

    /**
     * Print information about a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    private void printGetEntries(String domain, String entries, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntries", "Begin", 1);
        String[] entryIdentifiers = splitString(entries);
        String[] fieldNames = splitString(fields);
        printGetEntries(domain, entryIdentifiers, fieldNames);
        printDebugMessage("printGetEntries", "End", 1);
    }

    /**
     * Get URLs for a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @return an array containing the values for the fields for the entry.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] getEntryFieldUrls(String domain, String entry, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getEntryFieldUrls", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString result = this.srvProxy.getEntryFieldUrls(domain, entry, tmpFields);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getEntryFieldUrls", "End", 1);
        return retVal;
    }

    /**
     * Print URLs for a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetEntryFieldUrls(String domain, String entry, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntryFieldUrls", "Begin", 1);
        printStrList(getEntryFieldUrls(domain, entry, fields));
        printDebugMessage("printGetEntryFieldUrls", "End", 1);
    }

    /**
     * Print URLs for a specified entry.
     *
     * @param domain The domain to search.
     * @param entry  The entry identifier.
     * @param fields The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    private void printGetEntryFieldUrls(String domain, String entry, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntryFieldUrls", "Begin", 1);
        String[] fieldNames = splitString(fields);
        printGetEntryFieldUrls(domain, entry, fieldNames);
        printDebugMessage("printGetEntryFieldUrls", "End", 1);
    }

    /**
     * Get URLs for a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @return an array containing the values for the fields for the entry.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[][] getEntriesFieldUrls(String domain, String[] entries, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getEntriesFieldUrls", "Begin", 1);
        String[][] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString tmpEntries = objFactory.createArrayOfString();
        tmpEntries.getString().addAll(Arrays.asList(entries));
        ArrayOfArrayOfString result = this.srvProxy.getEntriesFieldUrls(domain, tmpEntries, tmpFields);
        List<ArrayOfString> tmpResult = result.getArrayOfString();
        retVal = new String[tmpResult.size()][];
        for (int i = 0; i < tmpResult.size(); i++) {
            retVal[i] = tmpResult.get(i).getString().toArray(new String[0]);
        }
        printDebugMessage("getEntriesFieldUrls", "End", 1);
        return retVal;
    }

    /**
     * Print URLs for a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetEntriesFieldUrls(String domain, String[] entries, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntriesFieldUrls", "Begin", 1);
        printArrayOfStringList(getEntriesFieldUrls(domain, entries, fields), false);
        printDebugMessage("printGetEntriesFieldUrls", "End", 1);
    }

    /**
     * Print URLs for a set of specified entries.
     *
     * @param domain  The domain to search.
     * @param entries The entry identifiers.
     * @param fields  The fields to retrieve.
     * @throws RemoteException
     * @throws ServiceException
     */
    private void printGetEntriesFieldUrls(String domain, String entries, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetEntriesFieldUrls", "Begin", 1);
        String[] entryIdentifers = splitString(entries);
        String[] fieldNames = splitString(fields);
        printGetEntriesFieldUrls(domain, entryIdentifers, fieldNames);
        printDebugMessage("printGetEntriesFieldUrls", "End", 1);
    }

    /**
     * Get list of domains referenced by the specified domain.
     *
     * @param domain Domain name.
     * @return List of domains.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] getDomainsReferencedInDomain(String domain) throws RemoteException, ServiceException {
        printDebugMessage("getDomainsReferencedInDomain", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.getDomainsReferencedInDomain(domain);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getDomainsReferencedInDomain", "End", 1);
        return retVal;
    }

    /**
     * Print list of domains referenced by the specified domain.
     *
     * @param domain Domain name.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetDomainsReferencedInDomain(String domain) throws RemoteException, ServiceException {
        printDebugMessage("printGetDomainsReferencedInDomain", "Begin", 1);
        printStrList(getDomainsReferencedInDomain(domain));
        printDebugMessage("printGetDomainsReferencedInDomain", "End", 1);
    }

    /**
     * Get list of domains referenced by the specified entry.
     *
     * @param domain Domain name.
     * @param entry  The entry identifier.
     * @return List of domains.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] getDomainsReferencedInEntry(String domain, String entry) throws RemoteException, ServiceException {
        printDebugMessage("getDomainsReferencedInEntry", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.getDomainsReferencedInEntry(domain, entry);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getDomainsReferencedInEntry", "End", 1);
        return retVal;
    }

    /**
     * Print list of domains referenced by the specified entry.
     *
     * @param domain Domain name.
     * @param entry  Entry identifier.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetDomainsReferencedInEntry(String domain, String entry) throws RemoteException, ServiceException {
        printDebugMessage("printGetDomainsReferencedInEntry", "Begin", 1);
        printStrList(getDomainsReferencedInEntry(domain, entry));
        printDebugMessage("printGetDomainsReferencedInEntry", "End", 1);
    }

    /**
     * Get list of additional cross-reference fields.
     *
     * @param domain Domain name.
     * @return List of field names.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] listAdditionalReferenceFields(String domain) throws RemoteException, ServiceException {
        printDebugMessage("listAdditionalReferenceFields", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.listAdditionalReferenceFields(domain);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("listAdditionalReferenceFields", "End", 1);
        return retVal;
    }

    /**
     * Print list of additional cross-reference fields.
     *
     * @param domain Domain name.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printListAdditionalReferenceFields(String domain) throws RemoteException, ServiceException {
        printDebugMessage("printListAdditionalReferenceFields", "Begin", 1);
        printStrList(listAdditionalReferenceFields(domain));
        printDebugMessage("printListAdditionalReferenceFields", "End", 1);
    }

    /**
     * Get list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entry            Entry identifier.
     * @param referencedDomain Cross-referenced domain.
     * @return List of entry identifiers.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[] getReferencedEntries(String domain, String entry, String referencedDomain) throws RemoteException, ServiceException {
        printDebugMessage("getReferencedEntries", "Begin", 1);
        String[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString result = this.srvProxy.getReferencedEntries(domain, entry, referencedDomain);
        retVal = result.getString().toArray(new String[0]);
        printDebugMessage("getReferencedEntries", "End", 1);
        return retVal;
    }

    /**
     * Print list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entry            Entry identifier.
     * @param referencedDomain Cross-referenced domain.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetReferencedEntries(String domain, String entry, String referencedDomain) throws RemoteException, ServiceException {
        printDebugMessage("printGetReferencedEntries", "Begin", 1);
        printStrList(getReferencedEntries(domain, entry, referencedDomain));
        printDebugMessage("printGetReferencedEntries", "End", 1);
    }

    /**
     * Get list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @return List of entry identifiers.
     * @throws RemoteException
     * @throws ServiceException
     */
    public EntryReferences[] getReferencedEntriesSet(String domain, String[] entries, String referencedDomain, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getReferencedEntriesSet", "Begin", 1);
        EntryReferences[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString tmpEntries = objFactory.createArrayOfString();
        tmpEntries.getString().addAll(Arrays.asList(entries));
        ArrayOfEntryReferences result = this.srvProxy.getReferencedEntriesSet(domain, tmpEntries, referencedDomain, tmpFields);
        retVal = result.getEntryReferences().toArray(new EntryReferences[0]);
        printDebugMessage("getReferencedEntriesSet", "End", 1);
        return retVal;
    }

    /**
     * Print list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetReferencedEntriesSet(String domain, String[] entries, String referencedDomain, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetReferencedEntriesSet", "Begin", 1);
        EntryReferences[] entryList = getReferencedEntriesSet(domain, entries, referencedDomain, fields);
        for (int i = 0; i < entryList.length; i++) {
            EntryReferences entry = entryList[i];
            System.out.println(entry.getEntry().getValue());
            List<ArrayOfString> entryRefList = entry.getReferences().getValue().getArrayOfString();
            for (ArrayOfString entryRef : entryRefList) {
                List<String> refList = entryRef.getString();
                for (String ref : refList) {
                    System.out.print("\t" + ref);
                }
                System.out.println();
            }
            System.out.println();
        }
        printDebugMessage("printGetReferencedEntriesSet", "End", 1);
    }

    /**
     * Print list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetReferencedEntriesSet(String domain, String entries, String referencedDomain, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetReferencedEntriesSet", "Begin", 1);
        String[] entryIdentifiers = splitString(entries);
        String[] fieldNames = splitString(fields);
        printGetReferencedEntriesSet(domain, entryIdentifiers, referencedDomain, fieldNames);
        printDebugMessage("printGetReferencedEntriesSet", "End", 1);
    }

    /**
     * Get list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @return List of entry identifiers.
     * @throws RemoteException
     * @throws ServiceException
     */
    public String[][] getReferencedEntriesFlatSet(String domain, String[] entries, String referencedDomain, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("getReferencedEntriesFlatSet", "Begin", 1);
        String[][] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfString tmpFields = objFactory.createArrayOfString();
        tmpFields.getString().addAll(Arrays.asList(fields));
        ArrayOfString tmpEntries = objFactory.createArrayOfString();
        tmpEntries.getString().addAll(Arrays.asList(entries));
        ArrayOfArrayOfString result = this.srvProxy.getReferencedEntriesFlatSet(domain, tmpEntries, referencedDomain, tmpFields);
        List<ArrayOfString> tmpResult = result.getArrayOfString();
        retVal = new String[tmpResult.size()][];
        for (int i = 0; i < tmpResult.size(); i++) {
            retVal[i] = tmpResult.get(i).getString().toArray(new String[0]);
        }
        printDebugMessage("getReferencedEntriesFlatSet", "End", 1);
        return retVal;
    }

    /**
     * Print list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetReferencedEntriesFlatSet(String domain, String[] entries, String referencedDomain, String[] fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetReferencedEntriesFlatSet", "Begin", 1);
        printArrayOfStringList(getReferencedEntriesFlatSet(domain, entries, referencedDomain, fields), true);
        printDebugMessage("printGetReferencedEntriesFlatSet", "End", 1);
    }

    /**
     * Print list of entries in referenced domain cross-referenced by entry.
     *
     * @param domain           Domain name.
     * @param entries          Entry identifiers.
     * @param referencedDomain Cross-referenced domain.
     * @param fields           Field names.
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetReferencedEntriesFlatSet(String domain, String entries, String referencedDomain, String fields) throws RemoteException, ServiceException {
        printDebugMessage("printGetReferencedEntriesFlatSet", "Begin", 1);
        String[] entryIdentifiers = splitString(entries);
        String[] fieldNames = splitString(fields);
        printGetReferencedEntriesFlatSet(domain, entryIdentifiers, referencedDomain, fieldNames);
        printDebugMessage("printGetReferencedEntriesFlatSet", "End", 1);
    }

    /**
     * Get domain description tree.
     *
     * @return Domain description object tree.
     * @throws RemoteException
     * @throws ServiceException
     */
    public DomainDescription getDomainsHierarchy() throws RemoteException, ServiceException {
        printDebugMessage("getDomainsHierarchy", "Begin", 1);
        DomainDescription retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        retVal = this.srvProxy.getDomainsHierarchy();
        printDebugMessage("getDomainsHierarchy", "End", 1);
        return retVal;
    }

    /**
     * Print domain description tree.
     *
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetDomainsHierarchy() throws RemoteException, ServiceException {
        printDebugMessage("printGetDomainsHierarchy", "Begin", 1);
        DomainDescription rootDomain = getDomainsHierarchy();
        printDomainDescription(rootDomain, "");
        printDebugMessage("printGetDomainsHierarchy", "End", 1);
    }

    /**
     * Recursive method to print tree of domain descriptions.
     *
     * @param domainDes Domain description node from tree.
     * @param indent    Prefix string providing indent for this level in the tree.
     */
    private void printDomainDescription(DomainDescription domainDes, String indent) {
        printDebugMessage("printDomainDescription", "Begin", 1);
        System.out.println(indent + domainDes.getId().getValue() + " : " + domainDes.getName().getValue());
        if (domainDes.getSubDomains().getValue() != null) {
            List<DomainDescription> subDomainList = domainDes.getSubDomains().getValue().getDomainDescription();
            if (subDomainList != null) {
                String tmpIndent = indent + "\t";
                for (DomainDescription subDomain : subDomainList) {
                    printDomainDescription(subDomain, tmpIndent);
                }
            }
        }
        printDebugMessage("printDomainDescription", "End", 1);
    }

    /**
     * Get results description tree.
     *
     * @param domain Search domain.
     * @param query  Query string.
     * @param flat   Flattened representation (true) or tree (false)
     * @return Results description object tree.
     * @throws RemoteException
     * @throws ServiceException
     */
    public DomainResult getDetailledNumberOfResults(String domain, String query, boolean flat) throws RemoteException, ServiceException {
        printDebugMessage("getDetailledNumberOfResults", "Begin", 1);
        DomainResult retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        retVal = this.srvProxy.getDetailledNumberOfResults(domain, query, flat);
        printDebugMessage("getDetailledNumberOfResults", "End", 1);
        return retVal;
    }

    /**
     * Print domain description tree.
     *
     * @param domain Search domain.
     * @param query  Query string.
     * @param flat   Flattened representation (true) or tree (false)
     * @throws RemoteException
     * @throws ServiceException
     */
    public void printGetDetailledNumberOfResults(String domain, String query, boolean flat) throws RemoteException, ServiceException {
        printDebugMessage("", "Begin", 1);
        DomainResult rootDomain = getDetailledNumberOfResults(domain, query, flat);
        printDomainResult(rootDomain, "");
        printDebugMessage("", "End", 1);
    }

    /**
     * Print domain description tree.
     *
     * @param domain Search domain.
     * @param query  Query string.
     * @param flat   Flattened representation (true) or tree (false)
     * @throws RemoteException
     * @throws ServiceException
     */
    private void printGetDetailledNumberOfResults(String domain, String query, String flat) throws RemoteException, ServiceException {
        printDebugMessage("", "Begin", 1);
        boolean flatFlag = false;
        String flag = flat.toLowerCase();
        if (flag.equals("t") || flag.equals("true") || flag.equals("y") || flag.equals("yes") || flag.equals("1")) {
            flatFlag = true;
        }
        printGetDetailledNumberOfResults(domain, query, flatFlag);
        printDebugMessage("", "End", 1);
    }

    /**
     * Recursive method to print tree of domain results.
     *
     * @param domainRes Domain result node from tree.
     * @param indent    Prefix string providing indent for this level in the tree.
     */
    private void printDomainResult(DomainResult domainRes, String indent) {
        printDebugMessage("printDomainResult", "Begin", 1);
        System.out.println(indent + domainRes.getDomainId().getValue() + " : " + domainRes.getNumberOfResults().intValue());
        if (domainRes.getSubDomainsResults().getValue() != null) {
            List<DomainResult> subDomainList = domainRes.getSubDomainsResults().getValue().getDomainResult();
            if (subDomainList != null) {
                String tmpIndent = indent + "\t";
                for (DomainResult subDomain : subDomainList) {
                    printDomainResult(subDomain, tmpIndent);
                }
            }
        }
        printDebugMessage("printDomainResult", "End", 1);
    }

    /**
     * Get the list of field information from a domain
     *
     * @param domain The domain to examine
     * @return An array of field info
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public FieldInfo[] listFieldsInformation(String domain)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        printDebugMessage("listFieldsInformation", "Begin", 1);
        FieldInfo[] retVal = null;
        srvProxyConnect(); // Ensure we have a service proxy
        ArrayOfFieldInfo result = this.srvProxy.listFieldsInformation(domain);
        retVal = result.getFieldInfo().toArray(new FieldInfo[0]);
        printDebugMessage("listFieldsInformation", "End", 1);
        return retVal;
    }

    /**
     * Print the list of fields for a domain
     *
     * @param domain The name of the domain to examine
     * @throws java.rmi.RemoteException
     * @throws javax.xml.rpc.ServiceException
     */
    public void printListFieldsInformation(String domain)
            throws java.rmi.RemoteException, javax.xml.rpc.ServiceException {
        FieldInfo[] fieldList = listFieldsInformation(domain);
        for (int i = 0; i < fieldList.length; i++) {
            FieldInfo a = fieldList[i];
            System.out.print(a.getId().getValue());
            System.out.print("\t" + a.getName().getValue());
            System.out.print("\t" + a.getDescription().getValue());
            System.out.print("\t" + a.isRetrievable());
            System.out.println("\t" + a.isSearchable());
        }
    }

    /**
     * Built the option descriptions for processing the command-line arguments.
     *
     * @param options Command-line options description.
     */
    public static void addCliOptions(Options options) {
        // --quiet
        options.addOption("q", "quiet", false, "Decrease output");
        // --verbose
        options.addOption("v", "verbose", false, "Increase output");
        // --debugLevel
        options.addOption("debugLevel", true, "Level of debug output");
        // --endpoint
        options.addOption("endpoint", true, "Alternative server endpoint");

        // --listDomains
        options.addOption("listDomains", false, "Get list of search domains");
        // --getNumberOfResults <domain> <query>
        options.addOption("getNumberOfResults", true,
                "Get the number of entries matching a query");
        options.getOption("getNumberOfResults").setArgs(2);
        // --getResultsIds <domain> <query> <start> <size>
        options.addOption("getResultsIds", true,
                "Get identifiers of entries which match query");
        options.getOption("getResultsIds").setArgs(4);
        // --getAllResultsIds <domain> <query>
        options.addOption("getAllResultsIds", true,
                "Get identifiers of entries which match query");
        options.getOption("getAllResultsIds").setArgs(2);
        // --listFields <domain>
        options.addOption("listFields", true, "List display fields for domain");
        // --getResults <domain> <query> <fields> <start> <size>
        options.addOption("getResults", true, "Get entries which match query");
        options.getOption("getResults").setArgs(5);
        // --getEntry <domain> <entry> <fields>
        options.addOption("getEntry", true, "Get data from a specific entry");
        options.getOption("getEntry").setArgs(3);
        // --getEntries <domain> <entries> <fields>
        options.addOption("getEntries", true, "Get data from a specific set of entries");
        options.getOption("getEntries").setArgs(3);
        // --getEntryFieldUrls <domain> <entry> <fields>
        options.addOption("getEntryFieldUrls", true,
                "Get URLs associated with a specific entry");
        options.getOption("getEntryFieldUrls").setArgs(3);
        // --getEntriesFieldUrls <domain> <entries> <fields>
        options.addOption("getEntriesFieldUrls", true,
                "Get URLs associated with specific entries");
        options.getOption("getEntriesFieldUrls").setArgs(3);
        // --getDomainsReferencedInDomain <domain>
        options.addOption("getDomainsReferencedInDomain", true,
                "Domains cross-referenced by domain");
        // --getDomainsReferencedInEntry <domain> <entry>
        options.addOption("getDomainsReferencedInEntry", true,
                "Domains cross-referenced by entry");
        options.getOption("getDomainsReferencedInEntry").setArgs(2);
        // --listAdditionalReferenceFields <domain>
        options.addOption("listAdditionalReferenceFields", true,
                "External references");
        // --getReferencedEntries <domain> <entry> <referencedDomain>
        options.addOption("getReferencedEntries", true,
                "Entries referenced by an entry");
        options.getOption("getReferencedEntries").setArgs(3);
        // --getReferencedEntriesSet <domain> <entries> <referencedDomain>
        // <fields>
        options.addOption("getReferencedEntriesSet", true,
                "Entries referenced by an entry");
        options.getOption("getReferencedEntriesSet").setArgs(4);
        // --getReferencedEntriesFlatSet <domain> <entries> <referencedDomain>
        // <fields>
        options.addOption("getReferencedEntriesFlatSet", true,
                "Entries referenced by an entry");
        options.getOption("getReferencedEntriesFlatSet").setArgs(4);
        // --getDomainsHierarchy
        options.addOption("getDomainsHierarchy", false,
                "Get the hierarchy of search domains");
        // --getDetailledNumberOfResults <domain> <query> <flat>
        options.addOption("getDetailledNumberOfResults", true,
                "Number of results for a query, per domain");
        options.getOption("getDetailledNumberOfResults").setArgs(3);
        // --listFieldsInformation <domain>
        options.addOption("listFieldsInformation", true,
                "List search and display fields");
    }

    /**
     * Entry point for running as an application
     *
     * @param args list of command-line options
     */
    public static void main(String[] args) {
        int exitVal = 0; // Exit value
        int argsLength = args.length; // Number of command-line arguments

        // Configure the command-line options
        Options options = new Options();
        addCliOptions(options);
        CommandLineParser cliParser = new GnuParser(); // Create the command
        // line parser

        // Create the client object.
        EBeyeClient ebeye = new EBeyeClient();
        try {
            // Parse the command-line
            CommandLine cli = cliParser.parse(options, args);
            // Usage info
            if (argsLength == 0 || cli.hasOption("help")) {
                printUsage();
                System.exit(0);
            }
            // Modify output level according to the quiet and verbose options
            if (cli.hasOption("q")) {
                ebeye.outputLevel--;
            }
            if (cli.hasOption("v")) {
                ebeye.outputLevel++;
            }
            // Set debug level
            if (cli.hasOption("debugLevel")) {
                ebeye.setDebugLevel(Integer.parseInt(cli
                        .getOptionValue("debugLevel")));
            }
            // Alternative service endpoint
            if (cli.hasOption("endpoint")) {
                ebeye.setServiceEndPoint(cli.getOptionValue("endpoint"));
            }

            // --listDomains
            if (cli.hasOption("listDomains")) {
                ebeye.printListDomains();
            }
            // --getNumberOfResults <domain> <query>
            else if (cli.hasOption("getNumberOfResults")) {
                String[] vals = cli.getOptionValues("getNumberOfResults");
                ebeye.printGetNumberOfResults(vals[0], vals[1]);
            }
            // --getResultsIds <domain> <query> <start> <size>
            else if (cli.hasOption("getResultsIds")) {
                String[] vals = cli.getOptionValues("getResultsIds");
                ebeye.printGetResultsIds(vals[0], vals[1], vals[2], vals[3]);
            }
            // --getAllResultsIds <domain> <query>
            else if (cli.hasOption("getAllResultsIds")) {
                String[] vals = cli.getOptionValues("getAllResultsIds");
                ebeye.printGetAllResultsIds(vals[0], vals[1]);
            }
            // --listFields <domain>
            else if (cli.hasOption("listFields")) {
                ebeye.printListFields(cli.getOptionValue("listFields"));
            }
            // --getResults <domain> <query> <fields> <start> <size>
            else if (cli.hasOption("getResults")) {
                String[] vals = cli.getOptionValues("getResults");
                ebeye.printGetResults(vals[0], vals[1], vals[2], vals[3], vals[4]);
            }
            // --getEntry <domain> <entry> <fields>
            else if (cli.hasOption("getEntry")) {
                String[] vals = cli.getOptionValues("getEntry");
                ebeye.printGetEntry(vals[0], vals[1], vals[2]);
            }
            // --getEntries <domain> <entries> <fields>
            else if (cli.hasOption("getEntries")) {
                String[] vals = cli.getOptionValues("getEntries");
                ebeye.printGetEntries(vals[0], vals[1], vals[2]);
            }
            // --getEntryFieldUrls <domain> <entry> <fields>
            else if (cli.hasOption("getEntryFieldUrls")) {
                String[] vals = cli.getOptionValues("getEntryFieldUrls");
                ebeye.printGetEntryFieldUrls(vals[0], vals[1], vals[2]);
            }
            // --getEntriesFieldUrls <domain> <entries> <fields>
            else if (cli.hasOption("getEntriesFieldUrls")) {
                String[] vals = cli.getOptionValues("getEntriesFieldUrls");
                ebeye.printGetEntriesFieldUrls(vals[0], vals[1], vals[2]);
            }
            // --getDomainsReferencedInDomain <domain>
            else if (cli.hasOption("getDomainsReferencedInDomain")) {
                ebeye.printGetDomainsReferencedInDomain(cli.getOptionValue("getDomainsReferencedInDomain"));
            }
            // --getDomainsReferencedInEntry <domain> <entry>
            else if (cli.hasOption("getDomainsReferencedInEntry")) {
                String[] vals = cli.getOptionValues("getDomainsReferencedInEntry");
                ebeye.printGetDomainsReferencedInEntry(vals[0], vals[1]);
            }
            // --listAdditionalReferenceFields <domain>
            else if (cli.hasOption("listAdditionalReferenceFields")) {
                ebeye.printListAdditionalReferenceFields(cli.getOptionValue("listAdditionalReferenceFields"));
            }
            // --getReferencedEntries <domain> <entry> <referencedDomain>
            else if (cli.hasOption("getReferencedEntries")) {
                String[] vals = cli.getOptionValues("getReferencedEntries");
                ebeye.printGetReferencedEntries(vals[0], vals[1], vals[2]);
            }
            // --getReferencedEntriesSet <domain> <entries> <referencedDomain>
            // <fields>
            else if (cli.hasOption("getReferencedEntriesSet")) {
                String[] vals = cli.getOptionValues("getReferencedEntriesSet");
                ebeye.printGetReferencedEntriesSet(vals[0], vals[1], vals[2], vals[3]);
            }
            // --getReferencedEntriesFlatSet <domain> <entries>
            // <referencedDomain> <fields>
            else if (cli.hasOption("getReferencedEntriesFlatSet")) {
                String[] vals = cli.getOptionValues("getReferencedEntriesFlatSet");
                ebeye.printGetReferencedEntriesFlatSet(vals[0], vals[1], vals[2], vals[3]);
            }
            // --getDomainsHierarchy
            else if (cli.hasOption("getDomainsHierarchy")) {
                ebeye.printGetDomainsHierarchy();
            }
            // --getDetailledNumberOfResults <domain> <query> <flat>
            else if (cli.hasOption("getDetailledNumberOfResults")) {
                String[] vals = cli.getOptionValues("getDetailledNumberOfResults");
                ebeye.printGetDetailledNumberOfResults(vals[0], vals[1], vals[2]);
            }
            // --listFieldsInformation <domain>
            else if (cli.hasOption("listFieldsInformation")) {
                ebeye.printListFieldsInformation(cli.getOptionValue("listFieldsInformation"));
            } else {
                System.err.println("Error: unknown action, see --help");
                exitVal = 1;
            }
        } catch (Exception ex) {
            System.err.println("ERROR: " + ex.getMessage());
            if (ebeye.getOutputLevel() > 0) {
                ex.printStackTrace();
            }
            exitVal = 1;
        }
        System.exit(exitVal);
    }
}
