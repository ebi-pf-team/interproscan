package uk.ac.ebi.interpro.scan.io.model;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests {@link HmmerModelParser}.
 *
 * @author Phil Jones
 * @version $Id$
 */
public class Hmmer3ModelLoaderTest {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3ModelLoaderTest.class.getName());

    private static final SignatureLibrary TEST_LIBRARY = SignatureLibrary.PFAM;

    private static final String TEST_RELEASE_VERSION = "24.0";

    private static final String TEST_MODEL_FILE = "data/hmmer3/library/pfam-small.hmm";

    @Test
    public void testParse() throws IOException {
        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
        HmmerModelParser parser = new HmmerModelParser();
        parser.setSignatureLibrary(TEST_LIBRARY);
        parser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        parser.setModelFiles(modelFileResource);
        SignatureLibraryRelease release = parser.parse();
        assertEquals(TEST_LIBRARY, release.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, release.getVersion());
        assertNotNull(release.getSignatures());
        assertEquals(21, release.getSignatures().size());
        for (Signature signature : release.getSignatures()) {
            assertNotNull(signature);
            assertNotNull(signature.getModels());
            assertEquals(1, signature.getModels().size());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("signature.accession = " + signature.getAccession());
                LOGGER.debug("signature.name = " + signature.getName());
                LOGGER.debug("signature.description() = " + signature.getDescription());
            }
        }
    }
}
