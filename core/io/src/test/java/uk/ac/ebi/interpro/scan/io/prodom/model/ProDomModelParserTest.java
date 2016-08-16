package uk.ac.ebi.interpro.scan.io.prodom.model;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test the ProDom model parser (prodom.ipr file).
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProDomModelParserTest {
    private static final Logger LOGGER = Logger.getLogger(ProDomModelParserTest.class.getName());

    private static final SignatureLibrary TEST_LIBRARY = SignatureLibrary.PRODOM;

    private static final String TEST_RELEASE_VERSION = "2006.1";

    private static final String TEST_MODEL_FILE = "data/prodom/prodom.ipr";

    @Test
    public void testParse() throws IOException {
        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
        ProDomModelParser parser = new ProDomModelParser();
        parser.setSignatureLibrary(TEST_LIBRARY);
        parser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        LOGGER.debug("test model file: " + modelFileResource.getFilename());
        parser.setModelFiles(modelFileResource);
        SignatureLibraryRelease release = parser.parse();

        assertEquals(TEST_LIBRARY, release.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, release.getVersion());
        Set<Signature> signatures = release.getSignatures();
        assertNotNull(signatures);
        assertEquals(2, signatures.size());
        for (Signature signature : signatures) {
            assertNotNull(signature);
            assertNotNull(signature.getModels());
            assertEquals(1, signature.getModels().size());

            // Detailed signature checks
            String accession = signature.getAccession();
            String description = signature.getDescription();
            if (!(accession.equals("PD400414") || accession.equals("PD021296"))) {
                fail("Unexpected accession");
            }
            if (!(description.equals("PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED")
                    || description.equals("J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN SEQUENCING DIRECT IGJ_PREDICTED ACID PYRROLIDONE CARBOXYLIC"))) {
                fail("Unexpected description");
            }
        }
    }

}
