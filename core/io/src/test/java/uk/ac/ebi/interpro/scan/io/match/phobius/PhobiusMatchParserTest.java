package uk.ac.ebi.interpro.scan.io.match.phobius;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class PhobiusMatchParserTest  {

    private static final Logger LOGGER = LogManager.getLogger(PhobiusMatchParserTest.class.getName());

    private static final String TEST_FILE_PATH = "data/phobius/100.phobius.out";

    /**
     * Parses a (largish) file and outputs memory usage at the end of the parse.
     */
    @Test
    public void testParserEfficiency() throws IOException {
        logMemUsage("BeforeAll parse: ");
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
            boolean isSignal = false;
            boolean isTM = false;
            StringBuilder sPnTMWarningMessage = new StringBuilder();
            for (PhobiusRawMatch match : matches) {
                //LOGGER.warn("match check : " + match.toString());
                if (match.isSP() || match.isTM()) {

                    assertTrue( (match.isSP() || match.isTM()), "The protein should be one or both of TM and SP.");
                } else {
                    sPnTMWarningMessage.append("match not SP or TM: " + match.toString()).append("\n");
                    assertFalse( match.isSP() || match.isTM(), "The protein should be one or both of TM and SP.");
                }
                // Now test that those two methods work properly!
                // Determine that all the included proteins contain
                // valid features only. //
                //actually
                //other features are also included, just that it must has a signalp and or a transmembrane feature as well
                PhobiusFeatureType type = match.getFeatureType();

                if (PhobiusFeatureType.SIGNAL_PEPTIDE_C_REGION == type ||
                        PhobiusFeatureType.SIGNAL_PEPTIDE_N_REGION == type ||
                        PhobiusFeatureType.SIGNAL_PEPTIDE_H_REGION == type) {
                    isSignal = true;
                }
                if (PhobiusFeatureType.TRANSMEMBRANE == type) {
                    isTM = true;
                }
                //LOGGER.warn("PhobiusFeatureType: + " + type.toString());
            }
            if (isSignal || isTM) {
                assertTrue(isSignal || isTM, "The methods PhobiusProtein.isSP and / or PhobiusProtein.isTM are not returning expected results.");
            } else {
                assertFalse(isSignal || isTM, "The methods PhobiusProtein.isSP and / or PhobiusProtein.isTM are FALSE.. need to check if this is okay.");
                LOGGER.warn("The methods PhobiusProtein.isSP and / or PhobiusProtein.isTM are FALSE.. need to check if this is okay. \n"
                        + sPnTMWarningMessage.toString());
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
