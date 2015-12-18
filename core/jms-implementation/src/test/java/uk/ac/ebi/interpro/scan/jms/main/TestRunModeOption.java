package uk.ac.ebi.interpro.scan.jms.main;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the ./interproscan.sh "-mode X" command-line input option.
 */
public class TestRunModeOption {

    /**
     * Default option.
     */
    @Test
    public void testDefault() {
        Mode mode = Run.getMode(null);
        Assert.assertEquals(Mode.STANDALONE, mode);
    }

    /**
     * Valid option: lowercase
     */
    @Test
    public void testConvertLowercase() {
        Mode mode = Run.getMode("convert");
        Assert.assertEquals(Mode.CONVERT, mode);
    }

    /**
     * Valid option: uppercase
     */
    @Test
    public void testConvertUppercase() {
        Mode mode = Run.getMode("CONVERT");
        Assert.assertEquals(Mode.CONVERT, mode);
    }

    /**
     * Invalid option.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOption() {
        Mode mode = Run.getMode("thismodedoesnotexist");
        Assert.fail("Invalid option should have failed, but didn't, found: " + (mode.name() == null ? "NULL" : mode.name()));
    }

}
