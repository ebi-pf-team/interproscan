package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatRecord;

import static org.junit.Assert.*;

/**
 * JUnit test for class {@link PirsfDatRecord}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfDatRecordTest {

    private PirsfDatRecord instance1;

    private PirsfDatRecord instance2;

    @Before
    public void init() {
        //initialization of instance 1
        instance1 = new PirsfDatRecord(
                "PIRSF000077",
                "Thioredoxin",
                new String[]{"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"},
                false);
        //initialization of instance 2
        instance2 = new PirsfDatRecord(
                "PIRSF000729",
                "Glutamate 5-kinase",
                new String[]{"358.270731707317", "40.8471702485214", "47.1", "486.230487804878", "168.540809659098"},
                true);
    }

    @Test
    public void testEquals() {
        assertFalse("Instances should be unequal!", instance1.equals(instance2));
        assertNotSame(instance1, instance2);

        PirsfDatRecord testInstance = new PirsfDatRecord(
                "PIRSF000077",
                "Thioredoxin",
                new String[]{"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"},
                false);

        assertTrue("Instances should be equal!", instance1.equals(testInstance));
        assertEquals(instance1, testInstance);
    }

    @Test
    public void testHashCode() {
        assertNotSame("Instances should have different hash codes!", instance1.hashCode(), instance2.hashCode());

        PirsfDatRecord testInstance = new PirsfDatRecord(
                "PIRSF000077",
                "Thioredoxin",
                new String[]{"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"},
                false);

        assertEquals("Instances should have the same hash code!", instance1, testInstance);
    }

    @Test
    public void testSetValues() {
        PirsfDatRecord instance = new PirsfDatRecord("PIRSF000077");
        //
        assertEquals("Unexpected result value!", 0.0d, instance.getMeanSeqLen(), 0.0d);
        assertEquals("Unexpected result value!", 0.0d, instance.getStdDevSeqLen(), 0.0d);
        assertEquals("Unexpected result value!", 0.0d, instance.getMinScore(), 0.0d);
        assertEquals("Unexpected result value!", 0.0d, instance.getMeanScore(), 0.0d);
        assertEquals("Unexpected result value!", 0.0d, instance.getStdDevScore(), 0.0d);
        String[] values = new String[]{"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"};
        instance.setValues(values);
        //
        assertEquals("Unexpected result value!", 110.136452241715d, instance.getMeanSeqLen(), 0.0d);
        assertEquals("Unexpected result value!", 9.11541109440914d, instance.getStdDevSeqLen(), 0.0d);
        assertEquals("Unexpected result value!", 20.3d, instance.getMinScore(), 0.0d);
        assertEquals("Unexpected result value!", 167.482261208577d, instance.getMeanScore(), 0.0d);
        assertEquals("Unexpected result value!", 57.6586203540026d, instance.getStdDevScore(), 0.0d);
    }
}
