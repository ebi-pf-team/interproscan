package uk.ac.ebi.interpro.scan.batch.tasklet;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.core.io.Resource;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import uk.ac.ebi.interpro.scan.batch.cli.CommandLineConversation;

/**
 * Runs system commands and writes stdout to given file.
 *
 * @author  Antony Quinn
 * @version $Id: FileWritingSystemCommandTasklet.java,v 1.3 2009/06/18 10:53:08 aquinn Exp $
 * @since   1.0
 */
public class FileWritingSystemCommandTasklet
        extends StepExecutionListenerSupport
        implements Tasklet, InitializingBean {

    private static final int EXIT_CODE_SUCCESS = 0;

    private static Logger LOGGER = Logger.getLogger(FileWritingSystemCommandTasklet.class);

    // Can't use SystemCommandTasklet as-is because cannot get stdout and stderr -- is it worth modifying?
    //private SystemCommandTasklet systemCommandTasklet;
    private CommandLineConversation commandLineConversation;

    private final static String COMMAND_SEP = " ";
    
    private String command;
    private String switches;
    private String arguments;
    private Resource outputResource;
    private FlatFileItemWriter flatFileItemWriter;

    private String fullCommand;

    public void setOutputResource(Resource outputResource) {
        this.outputResource = outputResource;
    }

    public void setCommandLineConversation(CommandLineConversation commandLineConversation) {
        this.commandLineConversation = commandLineConversation;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setSwitches(String switches) {
        this.switches = switches;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public void afterPropertiesSet() throws Exception {
        // Check we have required properties
        Assert.notNull(commandLineConversation, "CommandLineConversation must be set");
        Assert.notNull(command, "Command must be set");
        Assert.notNull(outputResource, "OutputResource must be set");
        // Build command as single string
        fullCommand = command;
        if (switches.length() > 0)  {
            fullCommand += COMMAND_SEP + switches;
        }
        if (arguments.length() > 0)  {
            fullCommand += COMMAND_SEP + arguments;
        }
        fullCommand = removePrefixes(fullCommand);
    }    

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<String> commandList = new ArrayList<String>(Arrays.asList(fullCommand.split(COMMAND_SEP)));
        LOGGER.info("Running: " + fullCommand);
        Integer exitCode = commandLineConversation.runCommand(false, commandList);
        if (exitCode.equals(EXIT_CODE_SUCCESS)) {
            // Write to file            
            String path = removePrefixes(outputResource.getFile().getPath());
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(commandLineConversation.getOutput());
            writer.close();
            LOGGER.info("Wrote results to: " + path);
            return RepeatStatus.FINISHED;
        }
        throw new IOException("Exit code '" + exitCode + "' executing '" + fullCommand + "': " +
                commandLineConversation.getErrorMessage());
    }

    // Remove "file:" prefix (added by MultiResourcePartitioner)
    private String removePrefixes(String path) {
        return path.replace("file:", "");
    }

    @Override public String toString()  {
        return fullCommand;
    }

}
