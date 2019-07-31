package uk.ac.ebi.interpro.scan.io.prints;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.util.Map;

/**
 * Runs tests of the KdatParser & PvalParser classes,
 * used to parse the files needed to create PRINTS Signatures and Models.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

public class PrintsParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PrintsParserTest.class.getName());

    private static final String kdatFileName = "data/prints/prints_10_only.kdat";
    private static final String pvalFileName = "data/prints/prints_10_only.pval";

    private static final String TEST_RELEASE_VERSION = "40";

    private static final String[] testAccessions = {"PR00439", "PR00305", "PR00916", "PR00159", "PR00551", "PR00352", "PR00003", "PR00353", "PR00512", "PR00513"};

    @Test
    public void testKdatParser() throws IOException {
        KdatParser kdatParser = new KdatParser();
        Resource kdatResource = new ClassPathResource(kdatFileName);
        kdatParser.setKdatFileResource(kdatResource);
        Map<String, String> accessionToAbstract = kdatParser.parse();
        assertEquals(testAccessions.length, accessionToAbstract.size());
        for (String testAccession : testAccessions) {
            assertTrue("Accession missing from final Map: " + testAccession, accessionToAbstract.keySet().contains(testAccession));
            String printsAbstract = accessionToAbstract.get(testAccession);
            assertNotNull(printsAbstract);
            assertNotNull(printsAbstract);
            assertTrue(printsAbstract.length() > 0);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Accession: " + testAccession);
                LOGGER.debug("Abstract: " + printsAbstract);
            }
        }
    }

    @Test
    public void testPvalParser() throws IOException {
        KdatParser kdatParser = new KdatParser();
        Resource kdatResource = new ClassPathResource(kdatFileName);
        kdatParser.setKdatFileResource(kdatResource);

        PvalParser pvalParser = new PvalParser();
        pvalParser.setSignatureLibrary(SignatureLibrary.PRINTS);
        pvalParser.setReleaseVersionNumber(TEST_RELEASE_VERSION);
        pvalParser.setKdatParser(kdatParser);
        Resource pvalResource = new ClassPathResource(pvalFileName);
        pvalParser.setModelFiles(pvalResource);
        SignatureLibraryRelease release = pvalParser.parse();

        assertNotNull(release);
        assertEquals(SignatureLibrary.PRINTS, release.getLibrary());
        assertEquals(TEST_RELEASE_VERSION, release.getVersion());
        assertNotNull(release.getSignatures());
        assertEquals(testAccessions.length, release.getSignatures().size());

        for (String testAccession : testAccessions) {
            boolean testAccessionFound = false;
            for (Signature signature : release.getSignatures()) {
                testAccessionFound |= testAccession.equals(signature.getAccession());
            }
            assertTrue(testAccessionFound);
        }
    }
}
