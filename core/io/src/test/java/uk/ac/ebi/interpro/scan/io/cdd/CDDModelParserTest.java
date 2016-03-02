package uk.ac.ebi.interpro.scan.io.cdd;


import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.cdd.CDDModelParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
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
            String name = signature.getName();
            String description = signature.getDescription();
            if (!(accession.equals("cd00011") || accession.equals("sd00038"))) {
                fail("Unexpected accession" + accession);
            }
            if (!(name.equals("BAR_Arfaptin_like") || name.equals("Kelch"))) {
                fail("Unexpected name");
            }
            if (!(description.equals("The Bin/Amphiphysin/Rvs (BAR) domain of Arfapt...")
                    || description.equals("Kelch repeat. Kelch repeats are 44 to 56 amino..."))) {
                fail("Unexpected description");
            }
        }
    }

}
