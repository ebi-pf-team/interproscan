//package uk.ac.ebi.interpro.scan.precalc.client;
//
//import org.junit.Assert;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpException;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.entity.FileEntity;
//import org.apache.http.localserver.LocalTestServer;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.protocol.HttpRequestHandler;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import uk.ac.ebi.interpro.scan.model.*;
//import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
//import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
//import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion.*;
//import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
//import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatchXML;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Tests the web service client using a org.apache.http.localserver.LocalTestServer
// * which serves an XML file.
// *
// * @author Phil Jones
// * @version $Id$
// * @since 1.0-SNAPSHOT
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
//public class MatchHttpClientTest {
//
//    private LocalTestServer server;
//
//    @Resource
//    private FileSystemResource servedXml;
//
//    @Resource
//    private FileSystemResource serverTSV;
//
//
//    HttpRequestHandler matchHandler = new HttpRequestHandler() {
//        @Override
//        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
//            HttpEntity httpEntity = new FileEntity(servedXml.getFile(), "text/xml");
//            response.setEntity(httpEntity);
//        }
//    };
//
//    HttpRequestHandler md5ToAnalysehandler = new HttpRequestHandler() {
//        @Override
//        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
//            HttpEntity httpEntity = new FileEntity(serverTSV.getFile(), "text/tab-separated-values");
//            response.setEntity(httpEntity);
//        }
//    };
//
//    @Resource
//    private MatchHttpClient matchClient;
//
//    private Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter;
//
//    private Map<String, Signature> signatureLookupService;
//
//
//    @Before
//    public void setUp() throws Exception {
//        server = new LocalTestServer(null, null);
//        server.register(MatchHttpClient.MATCH_SERVICE_PATH, matchHandler);
//        server.register(MatchHttpClient.PROTEINS_TO_ANALYSE_SERVICE_PATH, md5ToAnalysehandler);
//        server.start();
//        final String serverPath = "http:/" + server.getServiceAddress().toString();
//        matchClient.setUrl(serverPath);
//        initConverterMap();
//        initSignatureLookupService();
//    }
//
//    private void initSignatureLookupService() {
//        this.signatureLookupService = new HashMap<String, Signature>();
//        signatureLookupService.put("PTHR24026", new Signature("PTHR24026"));
//        signatureLookupService.put("PTHR24026:SF32", new Signature("PTHR24026:SF32"));
//        signatureLookupService.put("SSF49313", new Signature("SSF49313"));
//        signatureLookupService.put("SSF49899", new Signature("SSF49899"));
//        signatureLookupService.put("SSF57184", new Signature("SSF57184"));
//        signatureLookupService.put("SSF57196", new Signature("SSF57196"));
//        signatureLookupService.put("SignalPeptide", new Signature("SignalPeptide"));
//    }
//
//    private void initConverterMap() {
//        signatureLibraryToMatchConverter = new HashMap<SignatureLibrary, BerkeleyMatchConverter>();
//        signatureLibraryToMatchConverter.put(SignatureLibrary.COILS, new CoilsBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PRINTS, new FingerPrintsBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.GENE3D, new Hmmer3BerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.HAMAP, new PrositeProfilesBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PANTHER, new PantherBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PFAM, new Hmmer3BerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PIRSF, new Hmmer2BerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PRODOM, new ProdomMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PROSITE_PATTERNS, new PrositePatternsBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.PROSITE_PROFILES, new PrositeProfilesBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.TIGRFAM, new Hmmer3BerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.SMART, new Hmmer2BerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.SUPERFAMILY, new SuperfamilyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.SIGNALP_EUK, new SignalPBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.SIGNALP_GRAM_NEGATIVE, new SignalPBerkeleyMatchConverter());
//        signatureLibraryToMatchConverter.put(SignatureLibrary.SIGNALP_GRAM_POSITIVE, new SignalPBerkeleyMatchConverter());
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        server.stop();
//    }
//
//
//    /**
//     * This test is reading in a static XML file, rather than connecting to a real service.
//     *
//     * @throws IOException
//     */
//    @Test
//    @Ignore // TODO This test doesn't work on all systems, beware: Connection to http://localhost:80 refused" type="org.apache.http.conn.HttpHostConnectException">org.apache.http.conn.HttpHostConnectException: Connection to http://localhost:80 refused
//    public void testMatchClientOnLocalTestServer() throws IOException {
//        BerkeleyMatchXML berkeleyMatchXML = matchClient.getMatches("40292377942FBC93A8D66A2C4DC58D70");
//        Assert.assertNotNull("The Berkeley matches XML should not be NULL!", berkeleyMatchXML);
//        List<BerkeleyMatch> berkeleyMatches = berkeleyMatchXML.getMatches();
//        Assert.assertNotNull("List of Berkeley matches should not be NULL!", berkeleyMatches);
//        Assert.assertEquals(42, berkeleyMatches.size());
//        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
//            final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
//            final Signature signature = signatureLookupService.get(berkeleyMatch.getSignatureAccession());
//            BerkeleyMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);
//            if (matchConverter != null) {
//                Match i5Match = matchConverter.convertMatch(berkeleyMatch, signature);
//                Assert.assertNotNull("Any match converter should never return null if it consumes valid Berkeley Match XML!", i5Match);
//                Assert.assertNotNull(i5Match.getLocations());
//                for (Location location : (Set<Location>) i5Match.getLocations()) {
//                    Assert.assertTrue(location.getStart() > 0);
//                    Assert.assertTrue(location.getEnd() > 0);
//                }
//            }
//        }
//    }
//
//    /**
//     * This test is connecting to a real service.
//     *
//     * @throws IOException
//     */
//    @Test
//    public void testMatchClientOnPublicServer() throws IOException {
//        //set URL to public service
//        matchClient.setUrl("http://www.ebi.ac.uk/interpro/match-lookup/");
//        BerkeleyMatchXML berkeleyMatchXML = matchClient.getMatches("40292377942FBC93A8D66A2C4DC58D70");
//        Assert.assertNotNull("The Berkeley matches XML should not be NULL!", berkeleyMatchXML);
//        List<BerkeleyMatch> berkeleyMatches = berkeleyMatchXML.getMatches();
//        Assert.assertNotNull("List of Berkeley matches should not be NULL!", berkeleyMatches);
//        Assert.assertTrue(berkeleyMatches.size() > 0);
//        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
//            final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
//            final Signature signature = signatureLookupService.get(berkeleyMatch.getSignatureAccession());
//            BerkeleyMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);
//            if (matchConverter != null) {
//                Match i5Match = matchConverter.convertMatch(berkeleyMatch, signature);
//                Assert.assertNotNull("Any match converter should never return null if it consumes valid Berkeley Match XML!", i5Match);
//                Assert.assertNotNull(i5Match.getLocations());
//                for (Location location : (Set<Location>) i5Match.getLocations()) {
//                    Assert.assertTrue(location.getStart() > 0);
//                    Assert.assertTrue(location.getEnd() > 0);
//                }
//            }
//        }
//    }
//
//    @Test
//    public void testProteinsToAnalyseClient() throws IOException {
//        // NOTE - this is reading in a static file, rather than connecting to a real service.
//        List<String> proteinsToAnalyse = matchClient.getMD5sOfProteinsAlreadyAnalysed("D000022E87E6B7B84CCCBA9BAF34568A", "HoHoHo");
//    }
//
//    @Test
//    public void testIsProxyEnabled() {
//        //Set proxy host to NULL will disable the proxy
//        matchClient.setProxyHost(null);
//        Assert.assertFalse(matchClient.isProxyEnabled());
//        //Set proxy host and port will enable the proxy
//        matchClient.setProxyHost("localhost");
//        matchClient.setProxyPort("80");
//        Assert.assertTrue(matchClient.isProxyEnabled());
//    }
//
//
//}
