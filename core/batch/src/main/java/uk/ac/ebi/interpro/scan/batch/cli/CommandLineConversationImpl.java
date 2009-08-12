package uk.ac.ebi.interpro.scan.batch.cli;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Instances of this class can be instantiated to build a generic 'commmand line conversation'.
 * This allows any command to be called and captures both the console output and error output.
 *
 * This class is <b>NOT THREAD SAFE</b>, however a single instance of this class
 * can be reused synchronously in a single thread.
 *
 * @author  Phil Jones, EMBL-EBI
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class CommandLineConversationImpl implements CommandLineConversation{

	/**
	 * Logger for Junit logging. Log messages will be associated with the SessionImpl class.
	 */
	private static volatile Logger LOGGER = Logger.getLogger(CommandLineConversationImpl.class);

	private String output;

	private String error;

	private Integer exitStatus;

	private Map<String, String> environment;

	private boolean overrideAllEnvironment;

	private File workingDirectory;

	/**
	 * Inner class that reads in the output / error streams from the process.
	 * This is required because otherwise the stream buffers in the underlying OS
	 * may / will fill causing the process to hang.
	 *
	 * These readers operate in a separate thread, ensuring that the buffers
	 * for error / output are emptied in a timely manner.
 	 */


	class StreamGobbler extends Thread {
		InputStream inputStream;
		StringBuffer stringBuffer = new StringBuffer();

		StreamGobbler(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		/**
		 * Run method than consumes any ouput from the external process as it is produced.
		 */
		public void run(){

			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ( (line = bufferedReader.readLine()) != null){
					stringBuffer
							.append(line)
							.append('\n');
				}
			}
			catch (IOException ioe) {
				LOGGER.error("IOException thrown when attempting to read InputStream from external process.", ioe);
			}
			finally{
				if (bufferedReader != null){
					try {
						bufferedReader.close();
					} catch (IOException ioe) {
						LOGGER.error("IOException thrown when attempting to close BufferedReader from external process.", ioe);
					}
				}
			}
		}

		/**
		 * Accessor to retrieve the ouput from the process as a String.
		 * @return the ouput from the process as a String.
		 */
		public String getStreamContent(){
			return stringBuffer.toString();
		}
	}

	/**
	 * Runs a command on the command line synchronously.
	 *
	 * @param mergeErrorIntoOutput if true, error messages will be included in the
	 *                            output and can be accessed through the <code>getOutput()</code> method.  In this case,
	 *                            <code>getErrorMessage()</code> will return <code>null</code>.
	 * @param commands             being a set of recognised commands.  Note that compound commands (i.e. commands
	 * separated by spaces) need to be submitted as separate strings, e.g. `ls -l` is passed in as "ls", "-l"
	 * @return the return code from the command after it has completed.
	 * @throws java.io.IOException propagated from the RunTime.execute method.
	 */
	//@Override
	public int runCommand(boolean mergeErrorIntoOutput, String... commands) throws IOException, InterruptedException {
        return runCommand(mergeErrorIntoOutput, new ArrayList<String>(Arrays.asList(commands)));
    }


    /**
	 * Runs a command on the command line synchronously.
	 *
	 * @param mergeErrorIntoOutput if true, error messages will be included in the
	 *                            output and can be accessed through the <code>getOutput()</code> method.  In this case,
	 *                            <code>getErrorMessage()</code> will return <code>null</code>.
	 * @param commands             being a set of recognised commands.  Note that compound commands (i.e. commands
	 * separated by spaces) need to be submitted as separate strings, e.g. `ls -l` is passed in as "ls", "-l"
	 * @return the return code from the command after it has completed.
	 * @throws java.io.IOException propagated from the RunTime.execute method.
	 */
	//@Override
	public int runCommand(boolean mergeErrorIntoOutput, List<String> commands) throws IOException, InterruptedException {

		ProcessBuilder pb = new ProcessBuilder(commands);

		// Set error redirect as requested.
		pb.redirectErrorStream(mergeErrorIntoOutput);

		// Sort out the environment stuff.
		Map<String, String> retrievedEnvironment = pb.environment();
		if (overrideAllEnvironment){
			retrievedEnvironment.clear();
		}
		if (environment != null){
			retrievedEnvironment.putAll(environment);
		}

		// Set the working directory.  If workingDirectory as null,
		// uses the directory of the current process.  Not advised!
		pb.directory(workingDirectory);

		// Run the command
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Command Line: \n "+ pb);
		}


		Process process = pb.start();
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		// Retrieve status and output from the command.
		exitStatus = process.waitFor();

		output = outputGobbler.getStreamContent();

		if (mergeErrorIntoOutput){
			error = null;
		}
		else {
			error = errorGobbler.getStreamContent();
		}

		return exitStatus;
	}

	/**
	 * Allows the environment to be set, overriding any environment variables that
	 * are included, or clearing <b>all</b> environment variables and setting only those
	 * specified
	 *
	 * @param environmentVariables being a Map of environment variable name to value.
	 * @param overrideAll          if all preexisting environment variables should be cleared first.
	 */
	//@Override
	public void setEnvironment(Map<String, String> environmentVariables, boolean overrideAll) {
		this.environment = environmentVariables;
		this.overrideAllEnvironment = overrideAll;
	}

	/**
	 * Sets the working directory for subsequent commands.
	 *
	 * @param directoryPath being a valid path to a working directory
	 * @throws FileNotFoundException if the directory path given does not exist.
	 * @throws FileIsNotADirectoryException if the path exists, but does not resolve to a directory.
	 */
	//@Override
	public void setWorkingDirectory(String directoryPath) throws FileNotFoundException, FileIsNotADirectoryException {
		File file = new File (directoryPath);
		if (! file.exists()){
			throw new FileNotFoundException("An attempt has been made to set the working directory to " + directoryPath + ".  This directory does not exist.");
		}
		if (! file.isDirectory()){
			throw new FileIsNotADirectoryException("An attempt has been made to set the working directory to " + directoryPath + ".  This path exists, but inputStream not a directory.");
		}
		workingDirectory = file;
	}

	/**
	 * @return The output from the command, or null if no output was produced or no command has been run.
	 */
	//@Override
	public String getOutput() {
		return output;
	}

	/**
	 * @return The error message from the command, or null if no error message was generated or no command has been run.
	 */
	//@Override
	public String getErrorMessage() {
		return error;
	}

	/**
	 * @return The exit status from the last command run, or null if no command has been run yet.
	 */
	//@Override
	public Integer getExitStatus() {
		return exitStatus;
	}
}
