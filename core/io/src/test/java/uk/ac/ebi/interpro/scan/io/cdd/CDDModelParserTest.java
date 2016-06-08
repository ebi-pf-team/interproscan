package uk.ac.ebi.interpro.scan.io.cdd;


import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test the CDD model parser (cddid.tbl file).
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @author Siew Yit
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CDDModelParserTest {
    private static final Logger LOGGER = Logger.getLogger(CDDModelParserTest.class.getName());

    private static final SignatureLibrary TEST_LIBRARY = SignatureLibrary.CDD;

    private static final String TEST_RELEASE_VERSION = "3.14";

    private static final String TEST_MODEL_FILE = "data/cdd/cddid.tbl";

    @Test
    public void testParse() throws IOException {
        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
        CDDModelParser parser = new CDDModelParser();
        parser.setSignatureLibrary(TEST_LIBRARY);
        parser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test model file: " + modelFileResource.getFilename());
        }
        parser.setModelFiles(modelFileResource);
        SignatureLibraryRelease release = parser.parse();

        assertEquals(TEST_LIBRARY, release.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, release.getVersion());
        final Set<Signature> signatures = release.getSignatures();
        assertNotNull(signatures);
        assertEquals(3, signatures.size());
        for (final Signature signature : signatures) {
            assertNotNull(signature);
            final Map<String, Model> models = signature.getModels();
            assertNotNull(models);
            assertEquals(1, models.size());

            // Detailed signature checks
            final String accession = signature.getAccession();
            final String name = signature.getName();
            final String description = signature.getDescription();
            if (!(accession.equals("cd00004") || accession.equals("cd00011") || accession.equals("sd00038"))) {
                fail("Unexpected accession: " + accession);
            }
            if (!(name.equals("Sortase") || name.equals("BAR_Arfaptin_like") || name.equals("Kelch"))) {
                fail("Unexpected name: " + name);
            }
            if (!(description.equals(name))) {
                fail("Unexpected description: " + description);
            }
        }
    }

}
