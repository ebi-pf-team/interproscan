package uk.ac.ebi.interpro.scan.io.cli;

import org.apache.log4j.Logger;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: Add class description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class CommandLineConversationImplTest {

    private static final Logger LOGGER = Logger.getLogger(CommandLineConversationImplTest.class.getName());

    /**
     * System property that returns the location of the current users home directory.
     */
    private static final String USER_HOME = "user.home";

    /**
     * This method calls the java binary with `java -version`.
     * This test requires the JAVA_HOME environment variable to be
     * set correctly and JAVA_HOME/bin to be on the PATH.
     */
    @Test
    public void testCommandLineRunCommand() {
        //String[] testCommand = {"java", "-version"};
        List<String> testCommand = new ArrayList<String>();
        testCommand.add("java");
        testCommand.add("-version");
        CommandLineConversation clc = new CommandLineConversationImpl();
        try {
            clc.setWorkingDirectory(System.getProperty(USER_HOME));
            int outcome = clc.runCommand(false, testCommand);
            assertEquals( 0, outcome,"Outcome of " + testCommand.toString() + " should be 0.");
            LOGGER.debug(clc.getOutput());
            LOGGER.debug(clc.getErrorMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error("Check you have set JAVA_HOME and added JAVA_HOME/bin to the PATH environment variable.", e);
            fail(e.toString());
        } catch (IOException e) {
            LOGGER.error("Check you have set JAVA_HOME and added JAVA_HOME/bin to the PATH environment variable.", e);
            fail(e.toString());
        } catch (InterruptedException e) {
            LOGGER.error(e);
            fail(e.toString());
        }
    }

    @Test
    public void testCommandWithFileOutput() {
        //String[] testCommand = {"java", "-version"};
        List<String> testCommand = new ArrayList<String>();
        testCommand.add("java");
        testCommand.add("-version");
        CommandLineConversation clc = new CommandLineConversationImpl();
        try {
            clc.setOutputPathToFile("target/command_output", false, true);
            clc.setErrorPathToFile("target/command_error", false, true);
            clc.setWorkingDirectory(System.getProperty(USER_HOME));
            int outcome = clc.runCommand(false, testCommand);
            assertEquals(0, outcome, "Outcome of " + testCommand.toString() + " should be 0.");
            LOGGER.debug(clc.getOutput());
            LOGGER.debug(clc.getErrorMessage());
        } catch (FileNotFoundException e) {
            LOGGER.error("Check you have set JAVA_HOME and added JAVA_HOME/bin to the PATH environment variable.", e);
            fail(e.toString());
        } catch (IOException e) {
            LOGGER.error("Check you have set JAVA_HOME and added JAVA_HOME/bin to the PATH environment variable.", e);
            fail(e.toString());
        } catch (InterruptedException e) {
            LOGGER.error(e);
            fail(e.toString());
        }
    }

    /**
     * This method deliberately calls the java binary with nonsense parameters, to simulate
     * a command line error.  This test requires the JAVA_HOME environment variable to be
     * set correctly and JAVA_HOME/bin to be on the PATH.
     */
    @Test
    public void testCommandLineErrorReporting() {
        final String[] testCommand = {"java", "-utter", "-nonsense"};
        CommandLineConversation clc = new CommandLineConversationImpl();
        try {
            int outcome = clc.runCommand(false, testCommand);
            assertNotSame( 0, outcome, "Outcome of '" + Arrays.toString(testCommand) + "' should not be 0 as command is nonsense.");
            assertNotNull( clc.getErrorMessage(), "Error message expected in error output.");
            assertNotSame( "", clc.getErrorMessage(), "Error message expected in error output.");
            LOGGER.debug(clc.getOutput());
            LOGGER.debug(clc.getErrorMessage());
            outcome = clc.runCommand(true, testCommand);
            assertNotSame( 0, outcome, "Outcome of '" + Arrays.toString(testCommand) + "' should not be 0 as command is nonsense.");
            assertNull( clc.getErrorMessage(), "Error message not expected in error output.");
            assertNotNull( clc.getOutput(), "Error message expected in the merged output.");
            assertNotSame( "", clc.getOutput(), "Error message expected in the merged output.");
            LOGGER.debug(clc.getOutput());
            LOGGER.debug(clc.getErrorMessage());
        } catch (IOException e) {
            LOGGER.error(e);
            fail(e.toString());
        } catch (InterruptedException e) {
            LOGGER.error(e);
            fail(e.toString());
        }
    }
}
