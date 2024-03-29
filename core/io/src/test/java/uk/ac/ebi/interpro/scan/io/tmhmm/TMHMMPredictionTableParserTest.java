package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of the PantherModelDirectoryParser class.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TMHMMPredictionTableParserTest {

    private TMHMMPredictionTableParser parser;

    @BeforeEach
    public void setUp() {
        parser = new TMHMMPredictionTableParser(new SignatureLibraryRelease(SignatureLibrary.TMHMM, "2.5.1"));
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