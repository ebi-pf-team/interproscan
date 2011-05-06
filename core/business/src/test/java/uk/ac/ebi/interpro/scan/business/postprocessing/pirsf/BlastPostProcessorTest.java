package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link BlastPostProcessor}.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

public class BlastPostProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(BlastPostProcessorTest.class.getName());

    private BlastPostProcessor instance;

    @Before
    public void init() {
        instance = new BlastPostProcessor();
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
        assertFalse("BLAST criterion shouldn't be passed!", instance.checkBlastCriterion(sfTbMap, blastResultMap, pirsfModelID + "1"));
        //should be passed - because numberOfBlastHits / sfTbValue > 0.3334f
        assertTrue("BLAST criterion should be passed!", instance.checkBlastCriterion(sfTbMap, blastResultMap, pirsfModelID + "2"));
        //should be passed - because numberOfBlastHits > 9
        assertTrue("BLAST criterion should be passed!", instance.checkBlastCriterion(sfTbMap, blastResultMap, pirsfModelID + "3"));
    }
}