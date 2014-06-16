package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer2.OverlapPostProcessor}.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

public class OverlapPostProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(OverlapPostProcessorTest.class.getName());

    private OverlapPostProcessor instance;

    @Before
    public void init() {
        instance = new OverlapPostProcessor();
    }

    @Test
    public void testCheckOverlapCriterion() {
        PIRSFHmmer2RawMatch pirsfRawMatch = getDefaultRawMatchObj();
        PirsfDatRecord pirsfDatRecord = getDefaultPirsfDatRecordObj();
        int proteinLength = 122;
        //should be passed
        assertTrue("Overlap criterion should be passed!", instance.checkOverlapCriterion(proteinLength, pirsfRawMatch, pirsfDatRecord));

        //shouldn't be passed - overlap < 0.8f, because of a larger protein length
        proteinLength = 300;
        assertFalse("Overlap criterion shouldn't be passed!", instance.checkOverlapCriterion(proteinLength, pirsfRawMatch, pirsfDatRecord));

        //shouldn't be passed - lenDifference > 50.0d, because mean seq length is 200
        pirsfDatRecord.setMeanSeqLen(200.0d);
        assertFalse("Overlap criterion shouldn't be passed!", instance.checkOverlapCriterion(proteinLength, pirsfRawMatch, pirsfDatRecord));

        //shouldn't be passed - because locationScore < minScore
        pirsfRawMatch = new PIRSFHmmer2RawMatch("sequenceIdentifier", "model",
                null, "signatureLibraryRelease", 4, 113, 0.0d,
                0.0d, 0, 0, "hmmBounds", 0.0d, 20.0d);
        pirsfDatRecord = getDefaultPirsfDatRecordObj();
        assertFalse("Overlap criterion shouldn't be passed!", instance.checkOverlapCriterion(proteinLength, pirsfRawMatch, pirsfDatRecord));
    }

    private PirsfDatRecord getDefaultPirsfDatRecordObj() {
        String meanSeqLength = "110.136452241715";
        String stdDevSeqLength = "9.11541109440914";
        String minScore = "20.3";
        String[] values = new String[]{meanSeqLength, stdDevSeqLength, minScore, "0.0", "0.0"};
        return new PirsfDatRecord("model accession", "model name", values, false);
    }

    private PIRSFHmmer2RawMatch getDefaultRawMatchObj() {
        int locationStart = 4;
        int locationEnd = 113;
        double locationScore = 100.5d;
        return new PIRSFHmmer2RawMatch("sequenceIdentifier", "model",
                null, "signatureLibraryRelease", locationStart, locationEnd, 0.0d,
                0.0d, 0, 0, "hmmBounds", 0.0d, locationScore);
    }
}