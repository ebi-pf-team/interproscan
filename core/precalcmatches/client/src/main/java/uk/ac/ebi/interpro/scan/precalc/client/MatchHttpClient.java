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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.IOException;
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

    private static final String MD5_PARAMETER = "md5";

    private String url;

    private Jaxb2Marshaller unmarshaller;

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

    public BerkeleyMatchXML getMatches(List<String> md5s) throws IOException {
        if (url == null) {
            throw new IllegalStateException("The url must be set for the MatchHttpClient to function");
        }
        HttpClient httpclient = new DefaultHttpClient();
        final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        for (String md5 : md5s) {
            qparams.add(new BasicNameValuePair(MD5_PARAMETER, md5));
        }
        UrlEncodedFormEntity encodedParameterEntity;
        encodedParameterEntity = new UrlEncodedFormEntity(qparams, "UTF-8");

        // Using HttpPost to ensure no problems with long URLs.
        HttpPost post = new HttpPost(url);
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
                    } finally {
                        if (bis != null) {
                            bis.close();
                        }
                    }
                } else {
                    return null;
                }
            }
        };
        return httpclient.execute(post, handler);
    }
}
