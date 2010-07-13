package uk.ac.ebi.interpro.scan.io.model;

import org.apache.log4j.Logger;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link Hmmer3ModelLoader}.
 *
 * @author Phil Jones
 * @version $Id$
 */
public class Hmmer3ModelLoaderTest {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3ModelLoaderTest.class.getName());

    @Test
    public void testParse() throws IOException {
        URL url = Hmmer3ModelLoaderTest.class.getClassLoader().getResource("data/hmmer3/library/pfam-small.hmm");
        Hmmer3ModelLoader loader = new Hmmer3ModelLoader(SignatureLibrary.PFAM, "24.0");
        SignatureLibraryRelease release = loader.parse(url.getPath());
        assertEquals(SignatureLibrary.PFAM, release.getLibrary());
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
