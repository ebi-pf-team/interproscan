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
    private String fullPathToPythonBinary;
    private String fastaFileNameTemplate;
    private String cathAssignedFileNameTemplace;
    private String fullPathToModelsDirectory;
    private String cathResolveHitsOutputFileNameTemplate;
    private String fullPathToHmmsearchBinary;
    private String hmmsearchBinarySwitches;
    private String fullPathToCathResolveBinary;
    private String cathResolveBinarySwitches;

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    @Required
    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFullPathToPythonBinary() {
        return fullPathToPythonBinary;
    }

    @Required
    public void setFullPathToPythonBinary(String fullPathToPythonBinary) {
        this.fullPathToPythonBinary = fullPathToPythonBinary;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getCathAssignedFileNameTemplace() {
        return cathAssignedFileNameTemplace;
    }

    @Required
    public void setCathAssignedFileNameTemplace(String cathAssignedFileNameTemplace) {
        this.cathAssignedFileNameTemplace = cathAssignedFileNameTemplace;
    }

    public String getFullPathToModelsDirectory() {
        return fullPathToModelsDirectory;
    }

    @Required
    public void setFullPathToModelsDirectory(String fullPathToModelsDirectory) {
        this.fullPathToModelsDirectory = fullPathToModelsDirectory;
    }

    public String getCathResolveHitsOutputFileNameTemplate() {
        return cathResolveHitsOutputFileNameTemplate;
    }

    @Required
    public void setCathResolveHitsOutputFileNameTemplate(String cathResolveHitsOutputFileNameTemplate) {
        this.cathResolveHitsOutputFileNameTemplate = cathResolveHitsOutputFileNameTemplate;
    }

    public String getFullPathToHmmsearchBinary() {
        return fullPathToHmmsearchBinary;
    }

    @Required
    public void setFullPathToHmmsearchBinary(String fullPathToHmmsearchBinary) {
        this.fullPathToHmmsearchBinary = fullPathToHmmsearchBinary;
    }

    public String getHmmsearchBinarySwitches() {
        return hmmsearchBinarySwitches;
    }

    public void setHmmsearchBinarySwitches(String hmmsearchBinarySwitches) {
        this.hmmsearchBinarySwitches = hmmsearchBinarySwitches;
    }

    public String getFullPathToCathResolveBinary() {
        return fullPathToCathResolveBinary;
    }

    @Required
    public void setFullPathToCathResolveBinary(String fullPathToCathResolveBinary) {
        this.fullPathToCathResolveBinary = fullPathToCathResolveBinary;
    }

    public String getCathResolveBinarySwitches() {
        return cathResolveBinarySwitches;
    }

    public void setCathResolveBinarySwitches(String cathResolveBinarySwitches) {
        this.cathResolveBinarySwitches = cathResolveBinarySwitches;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCathAssignedFileNameTemplace());
        final String hmmsearchOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
        final String cathResolveHitsOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getCathResolveHitsOutputFileNameTemplate());

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToPython());
        command.add(this.getFullPathToPythonBinary());

        command.add("--hmmsearch");
        command.add(this.getFullPathToHmmsearchBinary());

        String binarySwitches = this.getHmmsearchBinarySwitches();
        if (binarySwitches != null && !binarySwitches.isEmpty()) {
            command.add("--hmmsearch-flags");
            command.add('"' + binarySwitches + '"');
        }

        command.add("--cath-resolve-hits");
        command.add(this.getFullPathToCathResolveBinary());

        binarySwitches = this.getCathResolveBinarySwitches();
        if (binarySwitches != null && !binarySwitches.isEmpty()) {
            command.add("--cath-resolve-hits-flags");
            command.add('"' + binarySwitches + '"');
        }

        command.add(fastaFilePath);
        command.add(inputFilePath);
        command.add(this.getFullPathToModelsDirectory());
        command.add(hmmsearchOutputFilePath);
        command.add(cathResolveHitsOutputFilePath);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(command.toString());
        }

        return command;
    }
}
