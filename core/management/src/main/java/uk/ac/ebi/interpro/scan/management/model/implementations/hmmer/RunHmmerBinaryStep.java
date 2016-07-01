package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This class runs HMMER 2 or HMMER 3 and reports any errors
 * spat out if the exit status != 0.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHmmerBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunHmmerBinaryStep.class.getName());

    private String fullPathToBinary;

    private String fullPathToHmmScanBinary;

    private String fullPathToHmmFile;

    private String fastaFileNameTemplate;

    private boolean useTbloutFormat = false;

    private String outputFileNameTbloutTemplate;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getFullPathToHmmScanBinary() {
        return fullPathToHmmScanBinary;
    }

    @Required
    public void setFullPathToHmmScanBinary(String fullPathToHmmScanBinary) {
        this.fullPathToHmmScanBinary = fullPathToHmmScanBinary;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public boolean isUseTbloutFormat() {
        return useTbloutFormat;
    }

    /**
     * We don't need to use TbloutFormat for every hmmer3 run
     *
     */
    public void setUseTbloutFormat(boolean useTbloutFormat) {
        this.useTbloutFormat = useTbloutFormat;
    }

    public String getOutputFileNameTbloutTemplate() {
        return outputFileNameTbloutTemplate;
    }

    public void setOutputFileNameTbloutTemplate(String outputFileNameTbloutTemplate) {
        this.outputFileNameTbloutTemplate = outputFileNameTbloutTemplate;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {

        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());

        List<String> command = new ArrayList<String>();

//        if (isSingleSeqMode()){
        if (Utilities.isRunningInSingleSeqMode()){
            Utilities.verboseLog("SINGLE_SEQUENCE_MODE: use  " + getFullPathToHmmScanBinary());
            command.add(this.getFullPathToHmmScanBinary());
        }else{
            command.add(this.getFullPathToBinary());
        }
        command.addAll(this.getBinarySwitchesAsList());
        // output file option
        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }

        if(useTbloutFormat) {
            final String tblOutputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTbloutTemplate());
            command.add("--tblout");
            command.add(tblOutputFilePathName);
        }

        command.add(this.getFullPathToHmmFile());
        command.add(fastaFilePathName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
