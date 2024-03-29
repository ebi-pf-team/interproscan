package uk.ac.ebi.interpro.scan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



/**
 * Tests cases for {@link PersistenceConversion}.
 *
 * @author  Antony Quinn
 * @author Gift Nuka
 *
 * @version $Id$
 */
public class PersistenceConversionTest {

    private static final double E_VALUE     = 2.8E-25;
    private static final double LOG_E_VALUE = -24.55284196865778; // http://www.google.co.uk/search?q=log+2.8E-25

    @Test
    public void testGet()  {
        final double expected = E_VALUE;
        final double actual = PersistenceConversion.get(LOG_E_VALUE);
        assertEquals(expected, actual);
        assertTrue(PersistenceConversion.equivalent(expected, actual));
    }    

    @Test
    public void testSet()  {
        final double expected = LOG_E_VALUE;
        final double actual = PersistenceConversion.set(E_VALUE);
        assertEquals(expected, actual);
        assertTrue(PersistenceConversion.equivalent(expected, actual));
    }

}
