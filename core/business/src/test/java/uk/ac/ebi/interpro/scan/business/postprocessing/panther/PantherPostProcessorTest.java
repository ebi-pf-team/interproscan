package uk.ac.ebi.interpro.scan.business.postprocessing.panther;


import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherPostProcessorTest {

    private PantherPostProcessor processor;

    @BeforeEach
    public void init() {
        processor = new PantherPostProcessor();

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
        assertEquals( 3, rawProtein.getMatches().size(), "Actual match size is different to the expected match size!");
        //Filter raw matches
        PantherPostProcessor  processor2 = new PantherPostProcessor();
        Set<RawProtein<PantherRawMatch>> filteredMatches = processor2.process(rawMatches);
        assertEquals(1, filteredMatches.size());
        for (RawProtein<PantherRawMatch> item : filteredMatches) {
            /*
                PANTHER 16.0: all matches are kept, and promoted: total result is 6
            */
            assertEquals( 6, item.getMatches().size(), "Actual match size is different to the expected match size!");
            assertTrue( item.getMatches().contains(rawMatch1), "Raw match 1 should be part of the result set!");
            assertTrue( item.getMatches().contains(rawMatch3), "Raw match 3 should be part of the result set!");
            assertTrue( item.getMatches().contains(rawMatch2), "Raw match 2 shouldn't be part of the result set!");
        }
    }

    private PantherRawMatch getDefaultPantherRawMatchObj(String sequenceIdentifier) {
        return new PantherRawMatch(sequenceIdentifier, "model", "subfamily", "annotNodeId", "signatureLibraryRelease", 0, 0, 0.0d, 0.0d, "familyName", 0, 0, 0,"[]",0,0, "annotation");
    }
}