package uk.ac.ebi.interpro.scan.precalc.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;

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
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchHttpClient {

    private static final Logger LOG = Logger.getLogger(MatchHttpClient.class.getName());

    private static final String MD5_PARAMETER = "md5";

    private String url;

    private Jaxb2Marshaller unmarshaller;

    public static final String MATCH_SERVICE_PATH = "/matches";

    public static final String PROTEINS_TO_ANALYSE_SERVICE_PATH = "/isPrecalculated";

    public MatchHttpClient(Jaxb2Marshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * The URL parameter may be injected or set directly.
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

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
        HttpClient httpclient = new DefaultHttpClient();
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
                if (responseEntity != null) {
                    // Stream in the response to the unmarshaller
                    BufferedInputStream bis = null;

                    try {
                        bis = new BufferedInputStream(responseEntity.getContent());
                        return (BerkeleyMatchXML) unmarshaller.unmarshal(new StreamSource(bis));
                    }
//                    }
                    finally {
                        if (bis != null) {
                            bis.close();
                        }
                    }
                }
                return null;
            }
        };
        BerkeleyMatchXML matchXML = httpclient.execute(post, handler);
        httpclient.getConnectionManager().shutdown();
        return matchXML;
    }

    public List<String> getMD5sOfProteinsAlreadyAnalysed(String... md5s) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("The url must be set for the MatchHttpClient.getMD5sOfProteinsAlreadyAnalysed method to function");
        }
        HttpClient httpclient = new DefaultHttpClient();
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
        List<String> response = httpclient.execute(post, handler);
        httpclient.getConnectionManager().shutdown();
        return response;
    }

    /**
     * Method to quickly indicate if the service is not configured.
     *
     * @return
     */
    public boolean isConfigured() {
        return url != null && !url.isEmpty();
    }
}
