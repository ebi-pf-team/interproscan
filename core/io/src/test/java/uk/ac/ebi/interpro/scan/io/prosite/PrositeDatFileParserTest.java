package uk.ac.ebi.interpro.scan.io.prosite;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

/**
 * Test of the PrositeDatFileParser class.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PrositeDatFileParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PrositeDatFileParserTest.class.getName());

    private static final String HAMAP_TEST_FILE = "data/hamap/hamap.prf";
    private static final String PROSITE_TEST_FILE = "data/prosite/prosite.dat";
    private static final String HAMAP_TEST_VERSION = "123.45";
    private static final String PROSITE_TEST_VERSION = "567.8";

    @Test
    public void testWithHAMAP() throws IOException {
        PrositeDatFileParser parser = new PrositeDatFileParser();
        Resource testFile = new ClassPathResource(HAMAP_TEST_FILE);
        parser.setModelFiles(testFile);
        parser.setSignatureLibrary(SignatureLibrary.HAMAP);
        parser.setReleaseVersionNumber(HAMAP_TEST_VERSION);
        parser.setPrositeModelType(PrositeDatFileParser.PrositeModelType.ALL);
        SignatureLibraryRelease sigLib = parser.parse();
        assertNotNull(sigLib);
        assertEquals(HAMAP_TEST_VERSION, sigLib.getVersion());
        assertEquals(SignatureLibrary.HAMAP, sigLib.getLibrary());
        assertNotNull(sigLib.getSignatures());
        assertEquals(20, sigLib.getSignatures().size());
        boolean foundExpectedSignature = false;
        for (Signature signature : sigLib.getSignatures()) {
            assertNotNull(signature);
            assertNotNull(signature.getAccession());
            assertNotNull(signature.getName());
            assertNotNull(signature.getDescription());
            if ("MF_01012".equals(signature.getAccession())) {
                foundExpectedSignature = true;
                assertEquals("23SrRNA_methyltr_RumB", signature.getName());
                assertEquals("23S rRNA (uracil-5-)-methyltransferase rumB [rumB].", signature.getDescription());
            }
            assertNotNull(signature.getModels());
            assertEquals(1, signature.getModels().size());
            String modelAc = signature.getModels().keySet().iterator().next();
            assertNotNull(modelAc);
            assertEquals(signature.getAccession(), modelAc);
            Model model = signature.getModels().get(modelAc);
            assertNotNull(model);
            assertEquals(signature.getAccession(), model.getAccession());
            assertEquals(signature.getName(), model.getName());
            assertEquals(signature.getDescription(), model.getDescription());
        }
        assertTrue(foundExpectedSignature);
    }

    @Test
    public void testPatternParsing() throws IOException {
        PrositeDatFileParser parser = new PrositeDatFileParser();
        Resource testFile = new ClassPathResource(PROSITE_TEST_FILE);
        parser.setModelFiles(testFile);
        parser.setSignatureLibrary(SignatureLibrary.PROSITE_PATTERNS);
        parser.setReleaseVersionNumber(PROSITE_TEST_VERSION);
        parser.setPrositeModelType(PrositeDatFileParser.PrositeModelType.PATTERNS);
        SignatureLibraryRelease sigLib = parser.parse();
        assertNotNull(sigLib);
        assertEquals(PROSITE_TEST_VERSION, sigLib.getVersion());
        assertEquals(SignatureLibrary.PROSITE_PATTERNS, sigLib.getLibrary());
        assertNotNull(sigLib.getSignatures());
        assertEquals(8, sigLib.getSignatures().size());
    }

    @Test
    public void testProfileParsing() throws IOException {
        PrositeDatFileParser parser = new PrositeDatFileParser();
        Resource testFile = new ClassPathResource(PROSITE_TEST_FILE);
        parser.setModelFiles(testFile);
        parser.setSignatureLibrary(SignatureLibrary.PROSITE_PROFILES);
        parser.setReleaseVersionNumber(PROSITE_TEST_VERSION);
        parser.setPrositeModelType(PrositeDatFileParser.PrositeModelType.PROFILES);
        SignatureLibraryRelease sigLib = parser.parse();
        assertNotNull(sigLib);
        assertEquals(PROSITE_TEST_VERSION, sigLib.getVersion());
        assertEquals(SignatureLibrary.PROSITE_PROFILES, sigLib.getLibrary());
        assertNotNull(sigLib.getSignatures());
        assertEquals(2, sigLib.getSignatures().size());
    }

}
