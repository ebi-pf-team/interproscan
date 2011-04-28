package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfBlastResultParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests {@link PirsfPostProcessing}.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

public class PirsfPostProcessingTest {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessingTest.class.getName());

    private PirsfPostProcessing instance;

    @Before
    public void init() {
        instance = new PirsfPostProcessing();
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

    @Test
    public void testCheckBlastCriterion() {
        String pirsfModelID = "PIRSF0000";

        Map<String, Integer> blastResultMap = new HashMap<String, Integer>();
        blastResultMap.put(pirsfModelID.substring(3) + "1", 1);
        blastResultMap.put(pirsfModelID.substring(3) + "2", 8);
        blastResultMap.put(pirsfModelID.substring(3) + "3", 10);
        Map<String, Integer> sfTbMap = new HashMap<String, Integer>();
        sfTbMap.put(pirsfModelID.substring(3) + "1", 400);
        sfTbMap.put(pirsfModelID.substring(3) + "2", 20);

        //shouldn't be passed - because numberOfBlastHits < 9 and numberOfBlastHits / sfTbValue < 0.3334f
        assertFalse("BLAST criterion shouldn't be passed!", instance.checkBlastCriterion(blastResultMap, sfTbMap, pirsfModelID + "1"));
        //should be passed - because numberOfBlastHits / sfTbValue > 0.3334f
        assertTrue("BLAST criterion should be passed!", instance.checkBlastCriterion(blastResultMap, sfTbMap, pirsfModelID + "2"));
        //should be passed - because numberOfBlastHits > 9
        assertTrue("BLAST criterion should be passed!", instance.checkBlastCriterion(blastResultMap, sfTbMap, pirsfModelID + "3"));
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
