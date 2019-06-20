package uk.ac.ebi.interpro.scan.io.gene3d;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class DomainFinderRecordTest {

    // TODO: Find out what these dummy values should really be
    private static final Integer DUMMY_INT = 0;
    private static final Double DUMMY_DBL  = 0.0;

    // Query:       2hxsA00  [M=178]
    // Scores for complete sequences (score includes all domains):
    //    --- full sequence ---   --- best 1 domain ---    -#dom-
    //     E-value  score  bias    E-value  score  bias    exp  N  Sequence              Description
    //     ------- ------ -----    ------- ------ -----   ---- --  --------              -----------
    //     2.6e-21   79.0   0.1    8.6e-10   41.5   0.0    2.2  2  HP0834
    //
    // Domain and alignment annotation for each sequence:

    // >> HP0834
    //    #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
    //  ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
    //    1 !   41.5   0.0   1.7e-11   8.6e-10       8     171 ..      10     167 ..       3     171 .. 0.83    
    private static final String MODEL_ID    = "2hxsA00";
    private static final String HIT_MODEL_NAME = "2hxsA00-i1";
    private static final String CATH_FAMILY_ID = "3.30.300.20";
    private static final String SIG_LIB_REL = "4.2.0";
    private static final String SEQ_ID      = "HP0834";
    private static final double SEQ_EVALUE  = 2.6e-21;
    private static final double SEQ_SCORE   = 79.0;
    private static final double SEQ_BIAS    = 0.1;
    private static final double DOMAIN_SCORE = 41.5;
    private static final double DOMAIN_BIAS = 0.0;
    private static final double DOMAIN_C_EVALUE = 1.7e-11;
    private static final double DOMAIN_I_EVALUE = 8.6e-10;
    private static final int MODEL_START    = 8;
    private static final int MODEL_END      = 171;
    private static final String HMM_BOUNDS  = "..";
    private static final int SEQ_START      = 10;
    private static final int SEQ_END        = 167;
    private static final int ENV_START      = 3;
    private static final int ENV_END        = 171;
    private static final double EXPECTED_ACCURACY = 0.83;
    private static final String ALIGNMENT   = "24M2I9M1D9";
    private static final String REGION = null;

    private static final Integer MATCHED_SEQ_COUNT = 1;
    private static final String SEGMENT_BOUNDARIES = "10:44";



    // See http://www.uniprot.org/uniprot/O25505
    private final static DomainFinderRecord EXPECTED_RECORD =
            new DomainFinderRecord (SEQ_ID, MODEL_ID, SEQ_START, SEQ_END,
                                    MODEL_START, MODEL_END, DOMAIN_I_EVALUE, DOMAIN_SCORE, ALIGNMENT, SEQ_EVALUE);

    private final static String EXPECTED_LINE =
            SEQ_ID + "\t" + MODEL_ID + "\t" +
            DUMMY_INT + "\t" + DUMMY_INT + "\t" + DUMMY_INT + "\t" + DUMMY_INT + "\t" +
            SEQ_START + "\t" + SEQ_END + "\t" + MODEL_START + "\t" + MODEL_END + "\t" +
            DOMAIN_I_EVALUE + "\t" + DOMAIN_SCORE + "\t" +
            DUMMY_DBL + "\t" + MATCHED_SEQ_COUNT + "\t" + SEGMENT_BOUNDARIES;

    private final static Gene3dHmmer3RawMatch EXPECTED_RAW_MATCH
            = new Gene3dHmmer3RawMatch(SEQ_ID, MODEL_ID, CATH_FAMILY_ID, HIT_MODEL_NAME, SIG_LIB_REL,
                                       SEQ_START, SEQ_END, SEQ_EVALUE, SEQ_SCORE,
                                       MODEL_START, MODEL_END, HMM_BOUNDS, DOMAIN_SCORE,
                                       ENV_START, ENV_END, EXPECTED_ACCURACY, SEQ_BIAS,
                                       DOMAIN_C_EVALUE, DOMAIN_I_EVALUE, DOMAIN_BIAS, ALIGNMENT, REGION);

    @Test
    public void testSegmentAndBoundaries() {
        DomainFinderRecord.SegmentRecord expected = 
                new DomainFinderRecord.SegmentRecord(MATCHED_SEQ_COUNT, SEGMENT_BOUNDARIES);
        assertEquals(expected, DomainFinderRecord.getSegmentAndBoundaries(ALIGNMENT, SEQ_START));
    }

    @Test
    public void testToLine() {
        assertEquals(EXPECTED_LINE, DomainFinderRecord.toLine(EXPECTED_RECORD));
    }

    @Test
    public void testValueOf() {
        assertEquals(EXPECTED_RECORD, DomainFinderRecord.valueOf(EXPECTED_LINE));
    }
        
    @Test
    public void testValueOfRawMatch() {
        assertEquals(EXPECTED_RECORD, DomainFinderRecord.valueOf(EXPECTED_RAW_MATCH));
    }

}