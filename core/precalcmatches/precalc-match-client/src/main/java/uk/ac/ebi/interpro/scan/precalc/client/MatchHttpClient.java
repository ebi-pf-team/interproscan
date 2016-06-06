package uk.ac.ebi.interpro.scan.precalc.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Client to query the REST web service for matches
 * and return an unmarshalled BerkeleyMatchXML object.
 *
 * @author Phil Jones, Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchHttpClient {

    private static final Logger LOG = Logger.getLogger(MatchHttpClient.class.getName());

    private static final String MD5_PARAMETER = "md5";

    private String url;

    private String proxyHost;

    private String proxyPort;

    private final Jaxb2Marshaller unmarshaller;

    public static final String MATCH_SERVICE_PATH = "/matches";

    public static final String PROTEINS_TO_ANALYSE_SERVICE_PATH = "/isPrecalculated";

    public static final String VERSION_PATH = "/version";

    public static final String SERVER_VERSION_PREFIX = "SERVER:";

    public MatchHttpClient(Jaxb2Marshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * The URL parameter may be injected or set directly.
     * <p/>
     * TODO (Phil): It's only like this to allow the test to work.  This could be moved to Constructor injection along with the Marshaller.
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() { return url;    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyHost() { return proxyHost;   }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyPort() { return proxyPort;  }

    public BerkeleyMatchXML getMatches(String... md5s) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Call to MatchHttpClient.getMatches:");
            for (String md5 : md5s) {
                LOG.debug("Protein match requested for MD5: " + md5);
            }
        }

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("The url must be set for the MatchHttpClient.getMatches method to function");
        }


//        HttpClient httpclient = new DefaultHttpClient();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (String md5 : md5s) {
            qparams.add(new BasicNameValuePair(MD5_PARAMETER, md5));
        }
        UrlEncodedFormEntity encodedParameterEntity;
        encodedParameterEntity = new UrlEncodedFormEntity(qparams, "UTF-8");

        // Using HttpPost to ensure no problems with long URLs.
        HttpPost post = new HttpPost(url + MATCH_SERVICE_PATH);
        post.setEntity(encodedParameterEntity);


        ResponseHandler<BerkeleyMatchXML> handler = new ResponseHandler<BerkeleyMatchXML>() {
            public BerkeleyMatchXML handleResponse(
                    HttpResponse response) throws IOException {
                HttpEntity responseEntity = response.getEntity();
//                Utilities.verboseLog("response:" + response.toString());
//                Utilities.verboseLog("responseEntity:" + responseEntity.toString());
                if (responseEntity != null) {
                    // Stream in the response to the unmarshaller
                    BufferedInputStream bis = null;

                    try {
                        bis = new BufferedInputStream(responseEntity.getContent());
//                        Utilities.verboseLog("xmlBufferedInputStream:" + bis.toString());
                        return (BerkeleyMatchXML) unmarshaller.unmarshal(new StreamSource(bis));
                    } finally {
                        if (bis != null) {
                            bis.close();
                        }
                    }
                }
                return null;
            }
        };
        //set the proxy if needed
        if (isProxyEnabled()) {
            LOG.debug("Using a Proxy server in getMatches: " + proxyHost + ":" + proxyPort);

            HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
            httpclient = getClient(proxy);

            //httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            //httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("http://localhost/");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                //do something
            } finally {
                response.close();
            }

            //try use newer API as ConnRoutePNames is deprecated
//            CloseableHttpClient client = HttpClients.custom()
//                .setRoutePlanner(
//                     new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
//                .build();

        }

        BerkeleyMatchXML matchXML = httpclient.execute(post, handler);
//        httpclient.getConnectionManager().shutdown();
        httpclient.close();
//        Utilities.verboseLog("matchXML:" + matchXML.toString());
        return matchXML;
    }

    /**
     * This method is used to determine if a sequence has already been analysed.  This is necessary as some sequences
     * will have no matches and so will not appear in the results of the match query, however should not be
     * reanalysed as this is inefficient.
     *
     * @param md5s any number of MD5 checksums.
     * @return a List<String> of MD5 checksums for all proteins that have pre-calculated matches available.
     * @throws IOException in the event of a problem communicating with the server.
     */
    public List<String> getMD5sOfProteinsAlreadyAnalysed(String... md5s) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("The url must be set for the MatchHttpClient.getMD5sOfProteinsAlreadyAnalysed method to function");
        }
        CloseableHttpClient httpclient = getClient(); //new DefaultHttpClient();
        final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (String md5 : md5s) {
            qparams.add(new BasicNameValuePair(MD5_PARAMETER, md5));
        }
        UrlEncodedFormEntity encodedParameterEntity;
        encodedParameterEntity = new UrlEncodedFormEntity(qparams, "UTF-8");

        // Using HttpPost to ensure no problems with long URLs.
        HttpPost post = new HttpPost(url + PROTEINS_TO_ANALYSE_SERVICE_PATH);
        post.setEntity(encodedParameterEntity);


        ResponseHandler<List<String>> handler = new ResponseHandler<List<String>>() {
            public List<String> handleResponse(
                    HttpResponse response) throws IOException {
                HttpEntity responseEntity = response.getEntity();
                List<String> md5sAlreadyAnalysed = new ArrayList<String>();
                if (responseEntity != null) {
                    // Stream in the response
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                md5sAlreadyAnalysed.add(line);
                            }
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
                return md5sAlreadyAnalysed;
            }
        };

        //set the proxy if needed
        if (isProxyEnabled()) {
            LOG.debug("Using a Proxy server in getMD5sOfProteinsAlreadyAnalysed : " + proxyHost + ":" + proxyPort);
            HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
            httpclient = getClient(proxy);
            //httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        }

        List<String> response = httpclient.execute(post, handler);
        httpclient.close();
//        httpclient.getConnectionManager().shutdown();
        return response;
    }

    public String getServerVersion() throws IOException {

            LOG.debug("Call to MatchHttpClient.getServerVersion:");

            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("The url must be set for the MatchHttpClient.getServerVersion method to function");
            }

            CloseableHttpClient httpclient = getClient();

            // Use HttpGet as the URL will be very short
            HttpGet get = new HttpGet(url + VERSION_PATH);


            ResponseHandler<String> handler = new ResponseHandler<String>() {
                public String handleResponse(
                        HttpResponse response) throws IOException {
                    String serverVersion = "";
                    HttpEntity responseEntity = response.getEntity();
                    if (responseEntity != null) {

                        // Stream in the response
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                            String line;
                            line = reader.readLine().trim();
                            if (!line.isEmpty()) {
                                    serverVersion  = line;
                            }

                        } finally {
                            if (reader != null) {
                                reader.close();
                            }
                        }

                        if (serverVersion.startsWith(SERVER_VERSION_PREFIX)) {
                            serverVersion = serverVersion.replace(SERVER_VERSION_PREFIX, "");
                        }  else {
                            throw new IOException("Could not determine server version");
                        }
                }
                    return serverVersion;
            }
            };

        //set the proxy if needed
        if(isProxyEnabled()){
            LOG.debug("Using a Proxy server in getServerVersion: " + proxyHost+":"+proxyPort);
            HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
            httpclient = getClient(proxy);
        }

        String serverVersion = httpclient.execute(get, handler);
        httpclient.close();
        //httpclient.getConnectionManager().shutdown();
        return serverVersion;

    }

    /**
     * Method to quickly indicate if the service is not configured.
     *
     * @return
     */
    public boolean isConfigured() {
        LOG.debug("lookup url: " + url );
        return url != null && !url.isEmpty();
    }

    /**
     * check if the http proxy is enabled
     * if enabled configure the system properties
     */
    public boolean isProxyEnabled() {
        LOG.debug("proxy Host: " + proxyHost + " proxyPort: " + proxyPort );
        //set the proxy if needed
        if (proxyHost == null || proxyHost.isEmpty() || proxyPort == null || proxyPort.isEmpty()) {
//            System.setProperty("proxySet", "true");
//            System.setProperty("http.proxyHost", proxyHost);
//            System.setProperty("http.proxyPort", proxyPort);
            return false;
        }
        return true;
    }


    public CloseableHttpClient getClient() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        return httpclient;
    }

    public CloseableHttpClient getClient(HttpHost proxy){
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        CloseableHttpClient httpclient = HttpClients.custom()
//                .setConnectionManager(connManager)
//                .setDefaultCookieStore(cookieStore)
//                .setDefaultCredentialsProvider(credentialsProvider)
                .setProxy(proxy)
//                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        return httpclient;

    }

    public CloseableHttpClient getClient(HttpHost proxy, CredentialsProvider credsProvider ) throws  Exception {
        CredentialsProvider credsProvider2 = new BasicCredentialsProvider();
        credsProvider2.setCredentials(
                new AuthScope("localhost", 8080),
                new UsernamePasswordCredentials("username", "password"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        try {
            HttpHost target = new HttpHost("www.verisign.com", 443, "https");

            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet httpget = new HttpGet("/");
            httpget.setConfig(config);

            System.out.println("Executing request " + httpget.getRequestLine() + " to " + target + " via " + proxy);

            CloseableHttpResponse response = httpclient.execute(target, httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }

        return httpclient;

    }
}
