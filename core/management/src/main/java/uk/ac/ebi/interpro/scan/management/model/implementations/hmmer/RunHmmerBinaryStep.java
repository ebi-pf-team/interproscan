package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

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

    private String fullPathToHmmFile;

    private String fastaFileNameTemplate;

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
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

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {

        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToBinary());
        command.addAll(this.getBinarySwitchesAsList());
        // output file option
        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }

        command.add(this.getFullPathToHmmFile());
        command.add(fastaFilePathName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
