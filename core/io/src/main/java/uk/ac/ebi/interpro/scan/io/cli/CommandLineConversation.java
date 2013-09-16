package uk.ac.ebi.interpro.scan.io.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Provides a generic interface for running operating system or shell commands on the command line and
 * returning the result.   The result may include output from the command, error messages, and
 * will normally return a status code to indicate success or failure (i.e. typically returning 0 to indicate
 * success).
 *
 * @author Phil Jones, EMBL-EBI
 * @author Antony Quinn
 * @version $Id: CommandLineConversation.java 19 2009-07-16 14:39:29Z aquinn.ebi $
 * @since 1.0
 */
public interface CommandLineConversation extends Serializable {

    /**
     * Runs a command on the command line synchronously.
     *
     * @param mergeOutputAndError if true, error messages will be included in the
     *                            output and can be accessed through the <code>getOutput()</code> method.  In this case,
     *                            <code>getErrorMessage()</code> will return <code>null</code>.
     * @param commands            being a set of recognised commands.  Note that compound commands (i.e. commands
     *                            separated by spaces) need to be submitted as separate strings, e.g. `ls -l` is passed in as "ls", "-l"
     *                            and `chmod g+w bob.txt` is passed in as "chmod", "g+w", "bob.txt"
     * @return the return code from the command after it has completed.
     * @throws IOException          propagated from the RunTime.execute method.
     * @throws InterruptedException If the thread is interrupted while waiting for the command to return.
     */
    int runCommand(boolean mergeOutputAndError, List<String> commands) throws IOException, InterruptedException;

    /**
     * Runs a command on the command line synchronously.
     *
     * @param mergeOutputAndError if true, error messages will be included in the
     *                            output and can be accessed through the <code>getOutput()</code> method.  In this case,
     *                            <code>getErrorMessage()</code> will return <code>null</code>.
     * @param commands            being a set of recognised commands.  Note that compound commands (i.e. commands
     *                            separated by spaces) need to be submitted as separate strings, e.g. `ls -l` is passed in as "ls", "-l"
     *                            and `chmod g+w bob.txt` is passed in as "chmod", "g+w", "bob.txt"
     * @return the return code from the command after it has completed.
     * @throws IOException          propagated from the RunTime.execute method.
     * @throws InterruptedException If the thread is interrupted while waiting for the command to return.
     */
    int runCommand(boolean mergeOutputAndError, String... commands) throws IOException, InterruptedException;

    /**
     * Runs command and returns exit status.
     *
     * @param command Command to run, for example "head -n 100 /tmp/example.txt"
     * @return Exit status
     * @throws IOException           if could not run command
     * @throws IllegalStateException if could not run command, or if command returns a failure flag
     */
    int runCommand(String command) throws IOException;

    /**
     * Allows the environment to be set, overriding any environment variables that
     * are included, or clearing <b>all</b> environment variables and setting only those
     * specified
     *
     * @param environmentVariables being a Map of environment variable name to value.
     * @param overrideAll          if all preexisting environment variables should be cleared first.
     */
    void setEnvironment(Map<String, String> environmentVariables, boolean overrideAll);

    /**
     * Redirects the output from the command directly to the specified file.
     * If a filePath is set, then the method 'getOutput' will return null.
     *
     * @param filePath the path of the file to which output should be redirected.
     */
    void setOutputPathToFile(String filePath, boolean overwriteIfExists, boolean append) throws IOException;

    /**
     * Redirects the error stream from the command directly to the specified file.
     * If a filePath is set, then the method 'getError' will return null.
     *
     * @param filePath the path of the file to which error output should be redirected.
     */
    void setErrorPathToFile(String filePath, boolean overwriteIfExists, boolean append) throws IOException, InterruptedException;

    /**
     * Sets the working directory for subsequent commands.
     *
     * @param directoryPath being a valid path to a working directory
     * @throws FileNotFoundException        if the directory path given does not exist.
     * @throws FileIsNotADirectoryException if the path exists, but does not resolve to a directory.
     */
    void setWorkingDirectory(String directoryPath) throws FileNotFoundException, FileIsNotADirectoryException;


    /**
     *
     * @param stepInstanceStepId
     */
    void setStepInstanceStepId(String stepInstanceStepId);

    /**
     *
     * @param verboseLog
     */
    void setVerboseLog(boolean verboseLog);


    /**
     * @return The output from the last command run, or null if no output was produced or no command has been run.
     */

    String getOutput();

    /**
     * @return The error message from the last command run, or null if no error message was generated or no command has been run.
     */
    String getErrorMessage();

    /**
     * @return The exit status from the last command run, or null if no command has been run yet.
     */
    Integer getExitStatus();

    /**
     * This optional method allows data to be piped into a command (i.e. on STDIN)
     *
     * @param commandInputStream data to be piped into the command.
     */
    void setCommandInputStream(InputStream commandInputStream);
}
