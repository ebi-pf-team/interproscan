package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * Test class for {@link TMHMMRawResultParser}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMRawResultParserTest {

    private TMHMMRawResultParser parser;

    @Before
    public void setUp() {
        parser = new TMHMMRawResultParser(new SignatureLibraryRelease(SignatureLibrary.TMHMM, "2.5.1"));
    }

    @Test
    public void testParse() throws IOException {
        Resource fileResource = new ClassPathResource("uk/ac/ebi/interpro/scan/io/tmhmm/rawResultOutputFile.txt");
        assertNotNull("File resource is NULL!", fileResource);
        assertTrue("File resource does not exist!", fileResource.exists());
        InputStream is = fileResource.getInputStream();
        assertNotNull("Input stream is NULL!", is);
        Set<TMHMMProtein> proteins = parser.parse(is);
        assertEquals("Size of protein set differs from the expected one!", 4, proteins.size());
        for (TMHMMProtein protein : proteins) {
            if (protein.getMatches().size() == 3) {
                for (TMHMMMatch match : protein.getMatches()) {
                    //Testing signature properties
                    Signature signature = match.getSignature();
                    assertNotNull("Signature of protein with Id " + protein.getProteinIdentifier() + " shouldn't be Null!", signature);
                    assertEquals("Signature accession of protein with Id " + protein.getProteinIdentifier() + " differs from the expected one!", "TMhelix", signature.getAccession());
                    assertEquals("Signature description of protein with Id " + protein.getProteinIdentifier() + " differs from the expected one!", "transmembrane helix", signature.getDescription());
                    assertNotNull("Signature library release shouldn't be Null!", signature.getSignatureLibraryRelease());
                    assertEquals("Signature library release version of protein with Id " + protein.getProteinIdentifier() + " differs from the expected one!", "2.5.1", signature.getSignatureLibraryRelease().getVersion());
                    assertEquals("Signature library of protein with Id " + protein.getProteinIdentifier() + " differs from the expected one!", SignatureLibrary.TMHMM, signature.getSignatureLibraryRelease().getLibrary());
                    //Testing match locations
                    checkMatchLocations(match.getLocations(), protein.getProteinIdentifier(), Arrays.asList(20, 62, 201), Arrays.asList(42, 84, 223));
                }
            } else if (protein.getMatches().size() == 7) {
                for (TMHMMMatch match : protein.getMatches()) {
                    //Testing match locations
                    checkMatchLocations(match.getLocations(), protein.getProteinIdentifier(), Arrays.asList(35, 69, 106, 148, 193, 228, 270), Arrays.asList(57, 91, 128, 170, 215, 250, 287));
                }
            } else if (protein.getMatches().size() == 8) {
                for (TMHMMMatch match : protein.getMatches()) {
                    //Testing match locations
                    checkMatchLocations(match.getLocations(), protein.getProteinIdentifier(), Arrays.asList(200, 234, 271, 314, 356, 391, 412, 438), Arrays.asList(222, 256, 293, 336, 378, 408, 431, 455));
                }
            } else {
                assertTrue("Unexpected protein with " + protein.getMatches().size() + " match(es) found!", false);
            }
        }
    }

    private void checkMatchLocations(Set<TMHMMMatch.TMHMMLocation> locations, String proteinIdentifier, List<Integer> startPositions, List<Integer> endPositions) {
        assertNotNull("Locations of protein with Id " + proteinIdentifier + " shouldn't be Null!", locations);
        assertEquals("Size of location set of protein with Id " + proteinIdentifier + " differs from the expected one!", 1, locations.size());
        for (TMHMMMatch.TMHMMLocation location : locations) {
            assertTrue("Start position " + location.getStart() + " is not part of the set of expected start positions!", startPositions.contains(location.getStart()));
            assertTrue("End position " + location.getEnd() + " is not part of the set of expected end positions!", endPositions.contains(location.getEnd()));
        }
    }
}
