package uk.ac.ebi.interpro.scan.io.panther;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of the PantherModelDirectoryParser class.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherModelDirectoryParserTest {

    private PantherModelDirectoryParser parser;

    @Before
    public void setUp() {
        parser = new PantherModelDirectoryParser();
    }

    private static final String PANTHER_TEST_MODEL_DIR = "data/panther";
    private static final String PANTHER_TEST_VERSION = "12.0";
    private static final String PANTHER_TEST_NAMES_TAB_FILE = "names.tab";


    @Test
    public void testPantherModelParser() throws IOException {
        Resource testModelDir = new ClassPathResource(PANTHER_TEST_MODEL_DIR);
        parser.setModelFiles(testModelDir);
        parser.setSignatureLibrary(SignatureLibrary.PANTHER);
        parser.setReleaseVersionNumber(PANTHER_TEST_VERSION);
        parser.setNamesTabFile(PANTHER_TEST_NAMES_TAB_FILE);
        //
        assertEquals(SignatureLibrary.PANTHER, parser.getSignatureLibrary());
        assertEquals(PANTHER_TEST_VERSION, parser.getReleaseVersionNumber());
        assertEquals(PANTHER_TEST_NAMES_TAB_FILE, parser.getNamesTabFileStr());
        //
        SignatureLibraryRelease sigLib = parser.parse();
        //
        assertNotNull(sigLib);
        assertEquals(PANTHER_TEST_VERSION, sigLib.getVersion());
        assertEquals(SignatureLibrary.PANTHER, sigLib.getLibrary());
        assertNotNull(sigLib.getSignatures());
        //3 super and 11 sub family signature
        assertEquals(14, sigLib.getSignatures().size());

        boolean foundExpectedSignature = false;
        for (Signature signature : sigLib.getSignatures()) {
            assertNotNull(signature);
            assertNotNull(signature.getAccession());
            assertNotNull(signature.getName());
            if ("PTHR10003:SF11".equals(signature.getAccession())) {
                foundExpectedSignature = true;
                assertEquals("SUPEROXIDE DISMUTASE [CU-ZN]", signature.getName());
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
}
