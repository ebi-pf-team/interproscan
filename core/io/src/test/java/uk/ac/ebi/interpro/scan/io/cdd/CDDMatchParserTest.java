//package uk.ac.ebi.interpro.scan.io.cdd;
//
//import org.apache.log4j.Logger;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import uk.ac.ebi.interpro.scan.io.match.cdd.CDDMatchParser;
//import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
//import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collection;
//import java.util.Set;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * Tests createMatch method of CDDMatchParser class.
// *
// * @author Gift Nuka
// * @version $Id$
// * @since 1.0-SNAPSHOT
// */
//public class CDDMatchParserTest {
//
//    private static final Logger LOGGER = Logger.getLogger(CDDMatchParserTest.class.getName());
//
//    private static final String TEST_MODEL_FILE = "data/cdd/cdd.output.txt";
//
//    private CDDMatchParser instance;
//
//    @Before
//    public void setUp() {
//        instance = new CDDMatchParser();
//    }
//
//    @Test
//    public void testCreateMatch() throws IOException {
//        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
//        InputStream is = modelFileResource.getInputStream();
//
//        Set<RawProtein<CDDRawMatch>> result = instance.parse(is);
//        LOGGER.debug("result: " + result.toString());
//        System.out.println("result: " + result);
//        assertEquals(2, result.size());
//        for (final RawProtein<CDDRawMatch> rawProtein : result) {
//            final Collection<CDDRawMatch> rawMatches = rawProtein.getMatches();
//            assertEquals(1, rawMatches.size());
//            final CDDRawMatch rawMatch = rawMatches.iterator().next();
//            final String modelId = rawMatch.getModelId();
//            assertTrue(modelId.equals("cd07765") || modelId.equals("cd14735"));
//        }
//    }
//}