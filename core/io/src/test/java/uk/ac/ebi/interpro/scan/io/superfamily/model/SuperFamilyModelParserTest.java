package uk.ac.ebi.interpro.scan.io.superfamily.model;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SuperFamily model parser test.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyModelParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(SuperFamilyModelParserTest.class.getName());

    private static final Pattern ACCESSION_PATTERN = Pattern.compile("^SSF\\d+$"); // E.g. "SSF81321"

    private static final SignatureLibrary TEST_LIBRARY = SignatureLibrary.SUPERFAMILY;

    private static final String TEST_MODEL_FILE = "data/superfamily/hmmlib_1.75";

    private static final String TEST_RELEASE_VERSION = "1.75";

    @Test
    public void testSuperFamilyModelParser() throws IOException {

        // Run test method
        SuperFamilyModelParser parser = new SuperFamilyModelParser();
        parser.setSignatureLibrary(TEST_LIBRARY);
        parser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
        parser.setModelFile(modelFileResource);
        SignatureLibraryRelease actualResult = parser.parse();

        // Compare actual result with expected result
        assertEquals(TEST_LIBRARY, actualResult.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, actualResult.getVersion());
        assertNotNull(actualResult.getSignatures());
        assertEquals(2, actualResult.getSignatures().size());

        for (Signature signature : actualResult.getSignatures()) {
            assertNotNull(signature);
            assertNotNull(signature.getModels());
            assertEquals(1, signature.getModels().size());

            // Check the signature accession is in the correct format
            String acc = signature.getAccession();
            Matcher accessionMatcher = ACCESSION_PATTERN.matcher(acc);
            if (acc == null || !accessionMatcher.find()) {
                fail("Signature accession " + acc + " was not in the expected format");
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Signature accession = " + signature.getAccession());
                LOGGER.debug("Signature name = " + signature.getName());
                LOGGER.debug("Signature description = " + signature.getDescription());
            }
        }

    }

}
