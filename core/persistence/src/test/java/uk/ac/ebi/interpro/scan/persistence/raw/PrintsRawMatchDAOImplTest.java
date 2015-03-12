package uk.ac.ebi.interpro.scan.persistence.raw;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PrintsRawMatchDAOImplTest {

    private static final Logger LOGGER = Logger.getLogger(PrintsRawMatchDAOImplTest.class.getName());

    private static final String SIG_DB_VERSION = "40.0";

    private static final List<PrintsRawMatch> REFERENCE_MATCHES = Arrays.asList(
            // Note protein identifiers are integers in this example, to match the usage in I5.
            new PrintsRawMatch("100", "PF0001", SIG_DB_VERSION, 10, 20, 1.9e-18, "IIIIiiii", 4, 1, 0.0000123, 1.123),
            new PrintsRawMatch("100", "PF0001", SIG_DB_VERSION, 67, 92, 1.9e-18, "IIIIiiii", 4, 2, 0.0000456, 1.456),
            new PrintsRawMatch("100", "PF0001", SIG_DB_VERSION, 123, 167, 1.9e-18, "IIIIiiii", 4, 3, 0.0000678, 1.678),
            new PrintsRawMatch("100", "PF0001", SIG_DB_VERSION, 178, 192, 1.9e-18, "IIIIiiii", 4, 4, 0.0000789, 1.789),

            new PrintsRawMatch("2", "PF0999", SIG_DB_VERSION, 5, 31, 1.3e-11, "iiIIIIiiI", 5, 1, 3.0000123, 4.123),
            new PrintsRawMatch("2", "PF0999", SIG_DB_VERSION, 46, 87, 1.3e-11, "iiIIIIiiI", 5, 2, 3.0000456, 4.456),
            new PrintsRawMatch("2", "PF0999", SIG_DB_VERSION, 111, 148, 1.3e-11, "iiIIIIiiI", 5, 3, 3.0000678, 4.678),
            new PrintsRawMatch("2", "PF0999", SIG_DB_VERSION, 156, 211, 1.3e-11, "iiIIIIiiI", 5, 4, 3.0000789, 4.789),
            new PrintsRawMatch("2", "PF0999", SIG_DB_VERSION, 223, 297, 1.3e-11, "iiIIIIiiI", 5, 5, 3.0000789, 4.789)
    );

    @Resource
    PrintsRawMatchDAO dao;

    @Before
    @After
    public void deleteAll() {
        dao.deleteAll();
        assertEquals("Could not delete all PrintsRawMatch objects from the database.", Long.valueOf(0), dao.count());
    }

    @Test
    public void testRoundTripStoreRetrieve() {
        // Create matches.  Note - NOT persisting the REFERENCE_MATCHES - copying them and persisting the copies.
        // Will then retrieve and test that was is returned is correct.
        List<PrintsRawMatch> matchesToPersist = new ArrayList<PrintsRawMatch>(REFERENCE_MATCHES.size());
        long minProteinId = Long.MAX_VALUE;    // Needed later for retrieval.
        long maxProteinId = Long.MIN_VALUE;

        for (PrintsRawMatch referenceMatch : REFERENCE_MATCHES) {
            int currentSequenceIdAsInt = Integer.parseInt(referenceMatch.getSequenceIdentifier());
            if (minProteinId > currentSequenceIdAsInt) minProteinId = currentSequenceIdAsInt;
            if (maxProteinId < currentSequenceIdAsInt) maxProteinId = currentSequenceIdAsInt;
            matchesToPersist.add(cloneRawMatch(referenceMatch));
        }

        dao.insert(matchesToPersist);

        assertEquals("Unexpected number of stored PrintsRawMatch objects", new Long(matchesToPersist.size()), dao.count());

        // Retrieve the raw matches using the DAO method used in PRINTS post-processing and check they are the same.
        Map<String, RawProtein<PrintsRawMatch>> retrievedRawProteins = dao.getRawMatchesForProteinIdsInRange(minProteinId, maxProteinId, SIG_DB_VERSION);

        assertNotNull(retrievedRawProteins);
        assertEquals("Unexpected number of RawProtein objects returned", 2, retrievedRawProteins.size());

        boolean oneHundred = false, two = false;
        for (String key : retrievedRawProteins.keySet()) {
            oneHundred |= "100".equals(key);
            two |= "2".equals(key);
        }
        assertTrue("Expected protein keys not found in key set.", oneHundred && two);
        oneHundred = two = false;
        final List<PrintsRawMatch> unseenRawMatches = new ArrayList<PrintsRawMatch>(REFERENCE_MATCHES.size());
        unseenRawMatches.addAll(REFERENCE_MATCHES);
        for (RawProtein<PrintsRawMatch> retrievedRawProtein : retrievedRawProteins.values()) {
            oneHundred |= "100".equals(retrievedRawProtein.getProteinIdentifier());
            two |= "2".equals(retrievedRawProtein.getProteinIdentifier());

            for (PrintsRawMatch retrievedRawMatch : retrievedRawProtein.getMatches()) {
                assertTrue("One of the matches retrieved from the database is not recognised.", unseenRawMatches.contains(retrievedRawMatch));
                unseenRawMatches.remove(retrievedRawMatch);
            }
        }
        LOGGER.debug("unseenRawMatches = " + unseenRawMatches);
        assertTrue("Not all of the reference matches were retrieved from the database.", unseenRawMatches.isEmpty());
        assertTrue("Expected protein keys not found in value set.", oneHundred && two);

    }

    /**
     * Not relying on a clone method in the PrintsRawMatch class - clone method currently not
     * implemented, but who knows?  May be required in the future, and may look different to this.
     *
     * @param original to be cloned
     * @return the clone.
     */
    private PrintsRawMatch cloneRawMatch(PrintsRawMatch original) {
        return new PrintsRawMatch(
                original.getSequenceIdentifier(),
                original.getModelId(),
                original.getSignatureLibraryRelease(),
                original.getLocationStart(),
                original.getLocationEnd(),
                original.getEvalue(),
                original.getGraphscan(),
                original.getMotifCount(),
                original.getMotifNumber(),
                original.getPvalue(),
                original.getScore()
        );
    }


}
