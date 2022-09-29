package uk.ac.ebi.interpro.scan.io.panther;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of the PantherModelDirectoryParser class.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherModelDirectoryParserTest {

    private PantherModelDirectoryParser parser;

    private static final String PANTHER_TEST_MODEL_DIR = "data/panther/16.0";
    private static final String PANTHER_TEST_VERSION = "16.0";
    private static final String PANTHER_TEST_NAMES_TAB_FILE = "names.tab";

    @BeforeEach
    public void setUp() {
        parser = new PantherModelDirectoryParser();
    }

    @Test
    public void testPantherModelParser() throws IOException {
        Resource testModelDir = new ClassPathResource(PANTHER_TEST_MODEL_DIR);
        parser.setModelFiles(testModelDir);
        parser.setSignatureLibrary(SignatureLibrary.PANTHER);
        parser.setReleaseVersionNumber(PANTHER_TEST_VERSION);
        parser.setNamesTabFile(PANTHER_TEST_NAMES_TAB_FILE);

        assertEquals(SignatureLibrary.PANTHER, parser.getSignatureLibrary());
        assertEquals(PANTHER_TEST_VERSION, parser.getReleaseVersionNumber());
        assertEquals(PANTHER_TEST_NAMES_TAB_FILE, parser.getNamesTabFile());
        SignatureLibraryRelease sigLib = parser.parse();
        assertNotNull(sigLib);
        assertEquals(PANTHER_TEST_VERSION, sigLib.getVersion());
        assertEquals(SignatureLibrary.PANTHER, sigLib.getLibrary());
        assertNotNull(sigLib.getSignatures());
        assertEquals(3, sigLib.getSignatures().size());

        boolean foundExpectedSignature = false;
        for (Signature signature : sigLib.getSignatures()) {
            assertNotNull(signature);
            assertNotNull(signature.getAccession());
            assertNotNull(signature.getName());
            assertNotNull(signature.getModels());

            for (Map.Entry<String, Model> entry: signature.getModels().entrySet()) {
                String modelAccession = entry.getKey();
                Model model = entry.getValue();
                assertNotNull(modelAccession);
                assertNotNull(model);
                assertEquals(model.getAccession(), modelAccession);
            }

            if (signature.getAccession().equals("PTHR23076")) {
                foundExpectedSignature = true;
                assertEquals("METALLOPROTEASE M41 FTSH", signature.getName());

                Map<String, Model> models = signature.getModels();
                signature.getModels().get("PTHR23076:SF97");
                assertNotNull(models);
                assertEquals(24, models.size());

                Model model = signature.getModels().get("PTHR23076:SF97");
                assertNotNull(model);
                assertEquals("PTHR23076:SF97", model.getAccession());
                assertEquals("ATP-DEPENDENT ZINC METALLOPROTEASE YME1L1", model.getName());
            }
        }

        assertTrue(foundExpectedSignature);
    }
}
