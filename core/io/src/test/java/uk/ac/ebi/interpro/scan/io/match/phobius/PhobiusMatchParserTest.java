package uk.ac.ebi.interpro.scan.io.match.phobius;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;
import uk.ac.ebi.interpro.scan.model.raw.PhobiusRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * Tests the PhobiusMatchParser, specifically looking at memory usage.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusMatchParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(PhobiusMatchParserTest.class.getName());

    private static final String TEST_FILE_PATH = "data/phobius/10k.phobius.out";

    /**
     * Parses a (largish) file and outputs memory usage at the end of the parse.
     */
    @Test
    public void testParserEfficiency() throws IOException {
        logMemUsage("Before parse: ");
        PhobiusMatchParser parser = new PhobiusMatchParser();
        Set<RawProtein<PhobiusRawMatch>> results;
        try (InputStream is = PhobiusMatchParserTest.class.getClassLoader().getResourceAsStream(TEST_FILE_PATH)) {
            results = parser.parse(is);
        }
        logMemUsage("After parse: ");
        LOGGER.debug("Protein count: " + results.size());
        for (RawProtein<PhobiusRawMatch> protein : results) {
            assertNotNull(protein.getProteinIdentifier());
            Collection<PhobiusRawMatch> matches = protein.getMatches();
            assertNotNull(matches);
            assertNotNull(protein.getProteinIdentifier());
            assertTrue(matches.size() > 0);
            for (PhobiusRawMatch match : matches) {
                assertTrue("The protein should be one or both of TM and SP.", match.isSP() || match.isTM());
                // Now test that those two methods work properly!
                // Determine that all the included proteins contain
                // valid features only.
                PhobiusFeatureType type = match.getFeatureType();
                boolean isSignal = false;
                boolean isTM = false;
                if (PhobiusFeatureType.SIGNAL_PEPTIDE_C_REGION == type ||
                        PhobiusFeatureType.SIGNAL_PEPTIDE_N_REGION == type ||
                        PhobiusFeatureType.SIGNAL_PEPTIDE_H_REGION == type) {
                    isSignal = true;
                }
                if (PhobiusFeatureType.TRANSMEMBRANE == type) {
                    isTM = true;
                }
                assertTrue("The methods PhobiusProtein.isSP and / or PhobiusProtein.isTM are not returning expected results.", isSignal || isTM);
            }
        }
    }

    private void logMemUsage(String prefix) {
        if (LOGGER.isDebugEnabled()) {
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            LOGGER.debug(prefix + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB.");
        }
    }
}
