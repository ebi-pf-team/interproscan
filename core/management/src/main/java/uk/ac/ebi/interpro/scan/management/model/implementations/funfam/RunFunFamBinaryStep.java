package uk.ac.ebi.interpro.scan.management.model.implementations.funfam;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;


public class RunFunFamBinaryStep extends RunBinaryStep {
    private static final Logger LOGGER = LogManager.getLogger(RunFunFamBinaryStep.class.getName());
    private String fullPathToPython;
    private String fullPathToBinary;
    private String fullPathToModelsDirectory;
    private String fullPathToHmmsearchBinary;
    private String fastaFileNameTemplate;
    private String inputFileTemplate;
    private String outputFileTemplate;

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    @Required
    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFullPathToBinary() {
        return fullPathToBinary;
    }

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public String getFullPathToModelsDirectory() {
        return fullPathToModelsDirectory;
    }

    @Required
    public void setFullPathToModelsDirectory(String fullPathToModelsDirectory) {
        this.fullPathToModelsDirectory = fullPathToModelsDirectory;
    }

    public String getFullPathToHmmsearchBinary() {
        return fullPathToHmmsearchBinary;
    }

    @Required
    public void setFullPathToHmmsearchBinary(String fullPathToHmmsearchBinary) {
        this.fullPathToHmmsearchBinary = fullPathToHmmsearchBinary;
    }

    public String getInputFileTemplate() {
        return inputFileTemplate;
    }

    @Required
    public void setInputFileTemplate(String inputFileTemplate) {
        this.inputFileTemplate = inputFileTemplate;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    @Required
    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
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
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileTemplate());
        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileTemplate());

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToPython());
        command.add(this.getFullPathToBinary());
        command.add(fastaFilePath);
        command.add(inputFilePath);
        command.add(this.getFullPathToModelsDirectory());

        command.add("--binary");
        command.add(this.getFullPathToHmmsearchBinary());

        String binarySwitches = this.getBinarySwitches();
        if (binarySwitches != null && !binarySwitches.isEmpty()) {
            command.add("--flags");
            command.add('"' + binarySwitches + '"');
        }

        command.add("-o");
        command.add(outputFilePath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
