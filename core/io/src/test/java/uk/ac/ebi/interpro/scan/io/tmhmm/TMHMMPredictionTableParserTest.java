package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.panther.PantherModelDirectoryParser;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.IOException;

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

    private TMHMMPredicationTableParser parser;

    @Before
    public void setUp() {
        parser = new TMHMMPredicationTableParser();
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