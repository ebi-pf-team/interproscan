package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs PANTHER binary.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public final class PantherScoreStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(PantherScoreStep.class.getName());


    private String inputFileNameRawOutTemplate;
    private String inputFileNameDomTbloutTemplate;

    private String scriptPath;
    private String modelDirectory;
    private String blastPath;
    private String hmmerPath;
    private String fullPathToPython;

    private boolean forceHmmsearch = false;

    private String perlCommand;
    private String perlLibrary;
    private String perlScriptTempDir;
    private String  userDir;

    final private String PANTHER_TEMP_DIR_SUFFIX = "panther_tmp_files";

    public String getScriptPath() {
        return scriptPath;
    }

    @Required
    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getModelDirectory() {
        return modelDirectory;
    }

    @Required
    public void setModelDirectory(String modelDirectory) {
        this.modelDirectory = modelDirectory;
    }

    public String getBlastPath() {
        return blastPath;
    }

    @Required
    public void setBlastPath(String blastPath) {
        this.blastPath = blastPath;
    }

    public String getHmmerPath() {
        return hmmerPath;
    }

    @Required
    public void setHmmerPath(String hmmerPath) {
        this.hmmerPath = hmmerPath;
    }

    public String getPerlCommand() {
        return perlCommand;
    }

    @Required
    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    public String getPerlLibrary() {
        return perlLibrary;
    }

    @Required
    public void setPerlLibrary(String perlLibrary) {
        this.perlLibrary = perlLibrary;
    }

    public String getPerlScriptTempDir() {
        return perlScriptTempDir;
    }

    public void setPerlScriptTempDir(String perlScriptTempDir) {
        this.perlScriptTempDir = perlScriptTempDir;
    }

    public String getUserDir() {
        return userDir;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public void setForceHmmsearch(boolean forceHmmsearch) {
        this.forceHmmsearch = forceHmmsearch;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getInputFileNameRawOutTemplate() {
        return inputFileNameRawOutTemplate;
    }

    public void setInputFileNameRawOutTemplate(String inputFileNameRawOutTemplate) {
        this.inputFileNameRawOutTemplate = inputFileNameRawOutTemplate;
    }

    public String getInputFileNameDomTbloutTemplate() {
        return inputFileNameDomTbloutTemplate;
    }

    public void setInputFileNameDomTbloutTemplate(String inputFileNameDomTbloutTemplate) {
        this.inputFileNameDomTbloutTemplate = inputFileNameDomTbloutTemplate;
    }

    /**
     * create an absolute path to the temporary directory file for panther
     *
     * @param temporaryFileDirectory
     * @return
     */
    public String getAbsolutePantherTempDirPath(String temporaryFileDirectory){
        String absolutePantherTempDirPath = this.getPerlScriptTempDir();
        LOGGER.debug("pantherTempDir - before update: " + absolutePantherTempDirPath);
        LOGGER.debug("temporaryFileDirectory: " + temporaryFileDirectory);
        if (! this.getPerlScriptTempDir().trim().isEmpty()) {
            if (new File(this.getPerlScriptTempDir()).isAbsolute()) {
                absolutePantherTempDirPath = this.getPerlScriptTempDir()
                        + File.separator
                        + PANTHER_TEMP_DIR_SUFFIX;
            }else{
                //add the user directory to the path
                absolutePantherTempDirPath = this.getUserDir()
                        + File.separator
                        + this.getPerlScriptTempDir()
                        + File.separator
                        + PANTHER_TEMP_DIR_SUFFIX;
            }
        } else {
            absolutePantherTempDirPath = temporaryFileDirectory + File.separator + PANTHER_TEMP_DIR_SUFFIX;
        }

        File dir = new File(absolutePantherTempDirPath);
        try {
            boolean dirCreated = dir.mkdirs();
            LOGGER.debug("The directory (-" + absolutePantherTempDirPath + " is created");
        } catch (SecurityException e) {
            LOGGER.error("Directory creation . Cannot create the specified directory !\n" +
                    "Specified directory path (absolute): " + dir.getAbsolutePath(), e);
            throw new IllegalStateException("The directory (-" + absolutePantherTempDirPath + ")  you specified cannot be written to:", e);
        }
        LOGGER.debug("pantherTempDir - after update: " + absolutePantherTempDirPath);
        return absolutePantherTempDirPath;
    }

    /**
     * Returns command line for runPantherScore
     * <p/>
     * Example:
     * pantherscore inputfile outputfile
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());

        final String inputFileNameRawOut = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameRawOutTemplate());
        final String inputFileNameDomTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameDomTbloutTemplate());


        List<String> command = new ArrayList<>();
        //Add command
        command.add(this.getFullPathToPython());
        // Panther script
        command.add(this.getScriptPath());
        // input domtblout file
        command.add(inputFileNameDomTblout);
        // output file option
        command.add(outputFilePathName);

        LOGGER.debug("pantherScore command is :" + command.toString());
        Utilities.verboseLog("pantherScore command is :" + command.toString());

        return command;
    }
}
