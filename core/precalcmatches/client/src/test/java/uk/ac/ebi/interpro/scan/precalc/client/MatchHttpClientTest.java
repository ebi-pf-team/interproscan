package uk.ac.ebi.interpro.scan.precalc.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;

/**
 * Tests the web service client using a org.apache.http.localserver.LocalTestServer
 * which serves an XML file.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MatchHttpClientTest {

    private LocalTestServer server;

    @Resource
    private FileSystemResource servedXml;

    HttpRequestHandler handler = new HttpRequestHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            HttpEntity httpEntity = new FileEntity(servedXml.getFile(), "text/xml");
            response.setEntity(httpEntity);
        }
    };

    @Resource
    private MatchHttpClient matchClient;

    private static final String HANDLER_PATH = "/matches";

    @Before
    public void setUp() throws Exception {
        server = new LocalTestServer(null, null);
        server.register(HANDLER_PATH, handler);
        server.start();
        final String serverPath = "http:/" + server.getServiceAddress().toString() + HANDLER_PATH;
        matchClient.setUrl(serverPath);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }


    @Test
    public void testClient() throws IOException {
        // NOTE - this is reading in a static XML file, rather than connecting to a real service.
        BerkeleyMatchXML matchXML = matchClient.getMatches(Arrays.asList("D000022E87E6B7B84CCCBA9BAF34568A"));
        System.out.println(matchXML);
    }
}
