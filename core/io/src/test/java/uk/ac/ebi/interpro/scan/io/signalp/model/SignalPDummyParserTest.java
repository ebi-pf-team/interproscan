package uk.ac.ebi.interpro.scan.io.signalp.model;

import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test class for SignalP model parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPDummyParserTest {
    private static final SignatureLibrary TEST_LIBRARY = SignatureLibrary.SIGNALP_EUK;

    private static final String TEST_RELEASE_VERSION = "4.0";
    private static final String SIGNALP_TM = "SignalP-TM";
    private static final String SIGNALP_NOTM = "SignalP-noTM";


    @Test
    public void testParse() throws IOException {
        SignalPDummyParser parser = new SignalPDummyParser();
        parser.setSignatureLibrary(TEST_LIBRARY);
        parser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        SignatureLibraryRelease release = parser.parse();

        assertEquals(TEST_LIBRARY, release.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, release.getVersion());

        // Check signatures
        Set<Signature> signatures = release.getSignatures();
        assertNotNull(signatures);
        assertEquals(2, signatures.size());
        for (Signature signature : signatures) {
            assertNotNull(signature);
            String accession = signature.getAccession();
            if (!(accession.equals(SIGNALP_TM) || accession.equals(SIGNALP_NOTM))) {
                fail("Unexpected signature accession: " + accession);
            }

            // Check model for this signature
            Map<String, Model> models = signature.getModels();
            assertNotNull(models);
            assertEquals(1, models.size());
            for (Model model : models.values()) {
                String modelId = model.getAccession();
                if (!(modelId.equals(SIGNALP_TM) || modelId.equals(SIGNALP_NOTM))) {
                    fail("Unexpected model accession: " + modelId);
                }
            }
        }
    }

}
