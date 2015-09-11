package uk.ac.ebi.interpro.scan.io.signalp.match;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * Test the SignalP binary output file parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPMatchParserTest extends TestCase {

    @Test
    public void testParse() throws IOException {

        String type = SignalPOrganismType.EUK.getTypeShortName();
        SignatureLibraryRelease signatureLibraryRelease = new SignatureLibraryRelease(SignatureLibrary.SIGNALP_EUK, "4.1");
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/signalp/signalp_binary_output.txt");
        SignalPMatchParser parser = new SignalPMatchParser(type, signatureLibraryRelease);
        Set<RawProtein<SignalPRawMatch>> proteins = parser.parse(is);
        assertNotNull(proteins);
        assertTrue(proteins.size() == 2);

        for (RawProtein<SignalPRawMatch> protein : proteins) {
            Collection<SignalPRawMatch> matches = protein.getMatches();
            assertNotNull(matches);
            assertTrue(matches.size() == 1);
        }
    }

    @Test
    public void testInvalidOrganismType() throws IOException {

        SignatureLibraryRelease signatureLibraryRelease = new SignatureLibraryRelease(SignatureLibrary.SIGNALP_EUK, "4.1");
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/signalp/signalp_binary_output.txt");
        SignalPMatchParser parser = new SignalPMatchParser("invalid", signatureLibraryRelease);
        try {
            Set<RawProtein<SignalPRawMatch>> proteins = parser.parse(is);
            if (proteins == null) {
                fail("Expected exception was not thrown, but retrieved no proteins.");
            }
            else {
                fail("Expected exception was not thrown, retrieved " + proteins.size() + " proteins.");
            }
        }
        catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testDifferentOrganismType() throws IOException {

        SignatureLibraryRelease signatureLibraryRelease = new SignatureLibraryRelease(SignatureLibrary.SIGNALP_EUK, "4.1");
        String type = SignalPOrganismType.GRAM_NEGATIVE.getTypeShortName();
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/signalp/signalp_binary_output.txt");
        SignalPMatchParser parser = new SignalPMatchParser(type, signatureLibraryRelease);
        try {
            Set<RawProtein<SignalPRawMatch>> proteins = parser.parse(is);
            if (proteins == null) {
                fail("Expected exception was not thrown, but retrieved no proteins.");
            }
            else {
                fail("Expected exception was not thrown, retrieved " + proteins.size() + " proteins.");
            }
        }
        catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

}
