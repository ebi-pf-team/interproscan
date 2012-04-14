package uk.ac.ebi.interpro.scan.search;

import java.io.*;
import java.net.*;

/**
 * Sequence search.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class SequenceSearch {

    private static final String IPRSCAN_URL = "http://www.ebi.ac.uk/Tools/services/web_iprscan/";

    private final UrlLocator urlLocator;

    private SequenceSearch() {
        this.urlLocator = null;
    }

    public SequenceSearch(UrlLocator urlLocator) {
        this.urlLocator = urlLocator;
    }

    /**
     * Returns URL of sequence match: either a pre-calculated InterPro protein page or an InterProScan result page.
     *
     * @param sequence Protein sequence
     * @return URL of sequence match
     */
    public String locateUrl(String sequence) {
        // Do we have a pre-calculated match?
        String url = urlLocator.locateUrl(sequence);
        if (url != null) {
            return url;
        }
        else {
            return runInterProScan(sequence);
        }
    }

    /**
     * Submits sequence to InterProScan web service and returns URL of result page.
     *
     * @param sequence Protein sequence
     * @return URL of sequence match
     */
    private String runInterProScan(String sequence) {

        String params;
        try {
            params = "tool="       + URLEncoder.encode("iprscan", "UTF-8") +
                     "&sequence="  + URLEncoder.encode(sequence, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not URL encode sequence: " + sequence, e);
        }

        // The URL we redirect to is contained in a JavaScript variable, for example:
        // var url = 'toolresult.ebi?tool=iprscan&jobId=iprscan-I20101208-131121-0091-41355458'
        String startsWith = "var url = '";
        String endsWith   = "';";

        // Search sequence on InterProScan
        String result, url = "";
        try {
            url = IPRSCAN_URL + "toolform.ebi";
            result = executePost(url, params, startsWith);
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not execute HTTP POST [url=" + url + ", params=" + params + "]", e);
        }

        if (result.length() > 0) {
            // Parse URL from InterProScan response
            int start = result.indexOf(startsWith);
            int end   = result.indexOf(endsWith);
            if (start < 0 || start > end) {
                throw new IndexOutOfBoundsException("Could not find '" + startsWith + "' or '" + endsWith + "' in InterProScan response: " + result);
            }
            url = IPRSCAN_URL + result.substring(start + startsWith.length(), end);
        }
        else {
            throw new IllegalStateException("Could not get results page URL from InterProScan");
        }

        return url;

    }

    /**
     * Executes HTTP POST and returns response.
     *
     * Source: http://www.xyzws.com/Javafaq/how-to-use-httpurlconnection-post-data-to-web-server/139
     *
     * @param targetURL     End-point URL
     * @param urlParameters URL parameters, for example "fName=" + URLEncoder.encode("???", "UTF-8")
     * @param lineFilter    Only return lines containing this string
     * @return HTTP response
     * @throws IOException  if could not connect or read response
     */
    private String executePost(String targetURL, String urlParameters, String lineFilter) throws IOException {

        URL url;
        HttpURLConnection connection = null;

        try {

            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Check for HTTP 200 "OK"
            if (connection.getResponseCode() != 200) {
                throw new IllegalStateException("Server returned HTTP code " + connection.getResponseCode());
            }

            // Get response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                if (line.indexOf(lineFilter) > -1) {
                    response.append(line).append('\n');
                }
            }
            rd.close();
            return response.toString();
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

}