package uk.ac.ebi.interpro.scan.jms.main;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Gift Nuka
 *
 * Unit tests for the ./interproscan.sh "-mode X" command-line input option.
 */
public class RunModeOptionTest {

    /**
     * Default option.
     */
    @Test
    public void testDefault() {
        Mode mode = Run.getMode(null);
        assertEquals(Mode.STANDALONE, mode);
    }

    /**
     * Valid option: lowercase
     */
    @Test
    public void testConvertLowercase() {
        Mode mode = Run.getMode("convert");
        assertEquals(Mode.CONVERT, mode);
    }

    /**
     * Valid option: uppercase
     */
    @Test
    public void testConvertUppercase() {
        Mode mode = Run.getMode("CONVERT");
        assertEquals(Mode.CONVERT, mode);
    }

    /**
     * Invalid option.
     */
    @Test //(expected = IllegalArgumentException.class)
    public void testInvalidOption() {
        //Mode mode = null;
        assertThrows(IllegalArgumentException.class,  () -> {
            Mode mode = Run.getMode("thismodedoesnotexist");
        }, "Invalid option should have failed, but didn't, found: " + "(mode.name() == null ? "+"NULL" + " : mode.name())");

    }

}
