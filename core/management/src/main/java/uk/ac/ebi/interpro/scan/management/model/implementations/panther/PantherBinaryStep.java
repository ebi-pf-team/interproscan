package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * @author Gift Nuka
 * @version $Id$
 */
public final class PantherBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(PantherBinaryStep.class.getName());

    private String scriptPath;
    private String modelDirectory;
    private String blastPath;
    private String hmmerPath;
    private boolean forceHmmsearch = false;
    private String fastaFileNameTemplate;
    private String perlCommand;
    private String perlLibrary;
    private String perlScriptTempDir;
    private String  userDir;

    final private String PANTHER_TEMP_DIR_SUFFIX = "panther_tmp_files";

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

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
     * Returns command line for runPanther
     * <p/>
     * Example:
     * perl -I data/panther/7.0/lib bin/panther/7.0/pantherScore.pl
     * -l path to the Panther model directory
     * -D I
     * -E 1e-3
     * -B bin/blast/2.2.6/blastall
     * -H bin/hmmer/hmmer2/2.3.2/hmmsearch
     * -T temp/
     * -n
     * -i UPI000000004D.fasta
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath
                = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());


        List<String> command = new ArrayList<>();
        //Add command
        command.add(this.getPerlCommand());
        //Add Perl parameter
        command.add("-I");
        command.add(this.getPerlLibrary());
        // Panther script
        command.add(this.getScriptPath());
        // Models
        command.add("-l");
        command.add(this.getModelDirectory());

        // Arguments
        command.addAll(this.getBinarySwitchesAsList());
        // BLAST
        //command.add("-B");
        //command.add(this.getBlastPath());
        // HMMER
        command.add("-H");
        command.add(this.getHmmerPath());
        //if sequences less than 10 use hmmerscan

        if (forceHmmsearch && Utilities.getSequenceCount() > 10) {
            command.add("-s");
        }
        // FASTA file
        command.add("-i");
        command.add(fastaFilePath);
        // output file option
        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }
        // Temporary directory
        command.add("-T");
        command.add(getAbsolutePantherTempDirPath(temporaryFileDirectory));

        LOGGER.debug("panther command is :" + command.toString());
        Utilities.verboseLog(1100, "panther command is :" + command.toString());

        return command;
    }
}
