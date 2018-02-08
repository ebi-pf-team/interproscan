package uk.ac.ebi.interpro.scan.business.postprocessing.panther;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherPostProcessorTest {

    private PantherPostProcessor processor;

    @Before
    public void init() {
        processor = new PantherPostProcessor(10e-11);
    }

    @Test
    public void testPantherPostProcessor() {
        //Build a set of raw protein matches to process
        Set<RawProtein<PantherRawMatch>> rawMatches = new HashSet<RawProtein<PantherRawMatch>>();
        //Build a raw protein with a set of raw matches
        RawProtein<PantherRawMatch> rawProtein = new RawProtein<PantherRawMatch>("1");
        //Build raw matches and add them to the raw protein
        PantherRawMatch rawMatch1 = getDefaultPantherRawMatchObj("1");
        rawMatch1.setEvalue(10e-12);
        rawProtein.addMatch(rawMatch1);
        //
        PantherRawMatch rawMatch3 = getDefaultPantherRawMatchObj("2");
        rawMatch3.setEvalue(10e-11);
        rawProtein.addMatch(rawMatch3);
        //
        PantherRawMatch rawMatch2 = getDefaultPantherRawMatchObj("3");
        rawMatch2.setEvalue(10e-9);
        rawProtein.addMatch(rawMatch2);
        //
        rawMatches.add(rawProtein);
        assertEquals("Actual match size is different to the expected match size!", 3, rawProtein.getMatches().size());
        //Filter raw matches
        Set<RawProtein<PantherRawMatch>> filteredMatches = processor.process(rawMatches);
        assertEquals(1, filteredMatches.size());
        for (RawProtein<PantherRawMatch> item : filteredMatches) {
            assertEquals("Actual match size is different to the expected match size!", 2, item.getMatches().size());
            assertTrue("Raw match 1 should be part of the result set!", item.getMatches().contains(rawMatch1));
            assertTrue("Raw match 3 should be part of the result set!", item.getMatches().contains(rawMatch3));
            assertFalse("Raw match 2 shouldn't be part of the result set!", item.getMatches().contains(rawMatch2));
        }
    }

    private PantherRawMatch getDefaultPantherRawMatchObj(String sequenceIdentifier) {
        return new PantherRawMatch(sequenceIdentifier, "model", "signatureLibraryRelease", 0, 0, 0.0d, 0.0d, "familyName", 0, 0, 0,"[]",0,0);
    }
}