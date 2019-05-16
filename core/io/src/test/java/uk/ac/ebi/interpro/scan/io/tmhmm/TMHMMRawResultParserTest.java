package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TMHMMRawMatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
        parser = new TMHMMRawResultParser(SignatureLibrary.TMHMM, "2.5.1");
    }

    @Test
    public void testParse() throws IOException {
        Resource fileResource = new ClassPathResource("uk/ac/ebi/interpro/scan/io/tmhmm/rawResultOutputFile.txt");
        assertNotNull("File resource is NULL!", fileResource);
        assertTrue("File resource does not exist!", fileResource.exists());
        InputStream is = fileResource.getInputStream();
        assertNotNull("Input stream is NULL!", is);
        Set<RawProtein<TMHMMRawMatch>> proteins = parser.parse(is);
        assertEquals("Size of protein set differs from the expected one!", 4, proteins.size());
        for (RawProtein<TMHMMRawMatch> protein : proteins) {
            Collection<TMHMMRawMatch> matches = protein.getMatches();
            assertNotNull(matches);
            assertNotNull(protein.getProteinIdentifier());
            assertTrue(matches.size() > 0);
            if (matches.size() == 3) {
                checkMatches(matches, protein.getProteinIdentifier(), Arrays.asList(20, 62, 201), Arrays.asList(42, 84, 223));
            }
            else if (matches.size() == 7) {
                checkMatches(matches, protein.getProteinIdentifier(), Arrays.asList(35, 69, 106, 148, 193, 228, 270), Arrays.asList(57, 91, 128, 170, 215, 250, 287));
            }
            else if (matches.size() == 8) {
                checkMatches(matches, protein.getProteinIdentifier(), Arrays.asList(200, 234, 271, 314, 356, 391, 412, 438), Arrays.asList(222, 256, 293, 336, 378, 408, 431, 455));
            }
            else {
                assertTrue("Unexpected protein with " + protein.getMatches().size() + " match(es) found!", false);
            }
        }
    }

    private void checkMatches(Collection<TMHMMRawMatch> matches, String proteinIdentifier, List<Integer> startPositions, List<Integer> endPositions) {
        for (TMHMMRawMatch match : matches) {
            assertTrue("Start position " + match.getLocationStart() + " is not part of the set of expected start positions!", startPositions.contains(match.getLocationStart()));
            assertTrue("End position " + match.getLocationEnd() + " is not part of the set of expected end positions!", endPositions.contains(match.getLocationEnd()));
            assertEquals("TMhelix", match.getModelId());
            assertEquals(SignatureLibrary.TMHMM, match.getSignatureLibrary());
            assertEquals(proteinIdentifier, match.getSequenceIdentifier());
        }
    }

}
