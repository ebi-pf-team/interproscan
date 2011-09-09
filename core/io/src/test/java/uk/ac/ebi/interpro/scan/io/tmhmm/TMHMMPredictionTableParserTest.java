package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of the PantherModelDirectoryParser class.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMPredictionTableParserTest {

    private TMHMMPredictionTableParser parser;

    @Before
    public void setUp() {
        parser = new TMHMMPredictionTableParser();
    }

    @Test
    public void testCheckLineForNewEntry() {
        //allowed
        assertTrue(parser.checkLineForNewEntry("# 1"));
        assertTrue(parser.checkLineForNewEntry("# UPI0000003CFF"));
        //not allowed
        assertFalse(parser.checkLineForNewEntry("# UPI0000003CFF "));
        assertFalse(parser.checkLineForNewEntry("#         i         O         o         M   "));
        assertFalse(parser.checkLineForNewEntry("M      0.46419   0.39130   0.14451   0.00000"));
    }
}