package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversation;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This abstract class factors out the functionality required to run a binary.
 * The only assumption made is that some output from the binary needs to be consumed
 * and is routed to the file described by 'outputFileNameTemplate'.
 * <p/>
 * It also allows binary switches to be passed in as a white-space separated String.
 * <p/>
 * Implementations just need to build the command line in the form of a List<String>.
 *
 * @author John Maslen
 * @author Phil Jones
 *         Date: May 25, 2010
 *         Time: 3:04:26 PM
 */

abstract public class RunBinaryStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(RunBinaryStep.class.getName());

    private String outputFileNameTemplate;

    private List<String> binarySwitchesInList = Collections.emptyList();

    private String binarySwitches;

    private InputStream commandInputStream;

    private boolean usesFileOutputSwitch = false;

    private boolean singleSeqMode = false;

    final private String ANALYSIS_TEMP_DIR_SUFFIX = "tmp_files";

    public void setUsesFileOutputSwitch(boolean usesFileOutputSwitch) {
        this.usesFileOutputSwitch = usesFileOutputSwitch;
    }

    public boolean isUsesFileOutputSwitch() {
        return usesFileOutputSwitch;
    }

    public boolean isSingleSeqMode() {
        return singleSeqMode;
    }

    public void setSingleSeqMode(boolean singleSeqMode) {
        this.singleSeqMode = singleSeqMode;
    }

    public String getOutputFileNameTemplate() {
        return outputFileNameTemplate;
    }

    //    @Required
    public void setOutputFileNameTemplate(String hmmerOutputFilePathTemplate) {
        this.outputFileNameTemplate = hmmerOutputFilePathTemplate;
    }


    /**
     * Allows binary switches to be passed in as a white-space separated String
     * (for ease of configuration).
     *
     * @param binarySwitches binary switches to be passed in as a white-space separated String
     *                       (for ease of configuration).
     */
    public void setBinarySwitches(String binarySwitches) {
        this.binarySwitches = binarySwitches;
        if (binarySwitches == null) {
            this.binarySwitchesInList = Collections.emptyList();
        } else {
            this.binarySwitchesInList = Arrays.asList(binarySwitches.split("\\s+"));
        }
    }

    public String getBinarySwitches() {
        return binarySwitches;
    }

    public final List<String> getBinarySwitchesAsList() {
        return binarySwitchesInList;
    }



    /**
     * create an absolute path to the temporary directory file for this analysis
     *
     * @param temporaryFileDirectory
     * @return
     */
    public String getAbsoluteAnalysisTempDirPath(String temporaryFileDirectory, String fileNameTemplate){
        String absoluteTempDirPath =  temporaryFileDirectory
                + File.separator
                + fileNameTemplate
                + "_"
                + ANALYSIS_TEMP_DIR_SUFFIX;


        File dir = new File(absoluteTempDirPath);
        try {
            boolean dirCreated = dir.mkdirs();
            LOGGER.debug("The directory (-" + absoluteTempDirPath + " is created");
        } catch (SecurityException e) {
            LOGGER.error("Directory creation . Cannot create the specified directory !\n" +
                    "Specified directory path (absolute): " + dir.getAbsolutePath(), e);
            throw new IllegalStateException("The directory (-" + absoluteTempDirPath + ")  you specified cannot be written to:", e);
        }
        LOGGER.debug(" TempDir - after update: " + absoluteTempDirPath);
        return absoluteTempDirPath;
    }


    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        LOGGER.info("Starting step with Id " + this.getId());
        LOGGER.debug("About to run binary... some output should follow.");
        delayForNfs();
        String outputFileName;
        if (this.getOutputFileNameTemplate() == null) {
            outputFileName = "/dev/null";
        } else {
            outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
        }
        List<String> command = createCommand(stepInstance, temporaryFileDirectory);
        if (command != null && command.size() > 0) {
            LOGGER.debug("Running the following command: " + command);

            CommandLineConversation clc = new CommandLineConversationImpl();
            try {
                //handle binaries that use -o or similar switch for output
                if (this.usesFileOutputSwitch){
                    outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, "devnull.txt");
                    outputFileName = "/dev/null";
                    clc.setOutputPathToFile(outputFileName, true, false);
                }else{
                    clc.setOutputPathToFile(outputFileName, true, false);
                }
                clc.setCommandInputStream(createCommandInputStream(stepInstance, temporaryFileDirectory));
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when attempting to configure binary", e);
            }

            int exitStatus;
            try {

                clc.setStepInstanceStepId(stepInstance.getStepId());
                LOGGER.debug("Now Running  : " + stepInstance.getStepId());
                exitStatus = clc.runCommand(false, command);
            } catch (IOException e) {
                throw new IllegalStateException("IOException thrown when attempting to run binary", e);
            } catch (InterruptedException e) {
                throw new IllegalStateException("InterruptedException thrown when attempting to run binary", e);
            }
            if (exitStatus == 0) {
                LOGGER.debug("binary finished successfully!");
            } else {
                StringBuffer failureMessage = new StringBuffer();
                failureMessage.append("Command line failed with exit code: ")
                        .append(exitStatus)
                        .append("\nCommand: ");
                for (String element : command) {
                    failureMessage.append(element).append(' ');
                }
                failureMessage.append("\nError output from binary:\n");
                failureMessage.append(clc.getErrorMessage());
                LOGGER.error(failureMessage);
                // TODO Look for a more specific Exception to throw here...
                throw new IllegalStateException(failureMessage.toString());
            }
        } else {
            LOGGER.info("Command list is NULL or empty! Skipping this step...");
        }
        LOGGER.info("Step with Id " + this.getId() + " finished.");
    }

    /**
     * Implementations of this method should return a List<String> containing all the components of the command line to be called
     * including any arguments. The StepInstance and temporary file are provided to allow parameters to be built. Use
     * stepInstance.buildFullyQualifiedFilePath to assist building paths.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return elements of the command in a list.
     */
    protected abstract List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory);

    /**
     * Implementations of RunBinaryStep may optionally override this method to
     * return an InputStream that can be piped into the command.
     *
     * @param stepInstance           containing the parameters for execution (e.g. fasta file template)
     * @param temporaryFileDirectory provides methods to create file paths from file templates
     * @return either null or an InputStream to be piped into the command
     * @throws java.io.IOException during creation of the InputStream
     */
    protected InputStream createCommandInputStream(StepInstance stepInstance, String temporaryFileDirectory) throws IOException {
        return null;
    }

}
