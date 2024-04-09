package uk.ac.ebi.interpro.scan.management.model.implementations.prosite;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.util.ArrayList;
import java.util.List;

public class RunPsScanStep extends RunBinaryStep {
    private String perlCommand;
    private String fullPathToPsScanPerlScript;
    private String fullPathToPfscanBinary;
    private String fullPathToPfsearchBinary;
    private String fastaFileNameTemplate;
    private String fullPathToConfirmatoryProfiles;
    private Boolean usePfsearch = false;
    private String modelFile;

    public String getPerlCommand() {
        return perlCommand;
    }

    public void setPerlCommand(String perlCommand) {
        this.perlCommand = perlCommand;
    }

    public String getFullPathToPsScanPerlScript() {
        return fullPathToPsScanPerlScript;
    }

    public void setFullPathToPsScanPerlScript(String fullPathToPsScanPerlScript) {
        this.fullPathToPsScanPerlScript = fullPathToPsScanPerlScript;
    }

    public String getFullPathToPfscanBinary() {
        return fullPathToPfscanBinary;
    }

    public void setFullPathToPfscanBinary(String fullPathToPfscanBinary) {
        this.fullPathToPfscanBinary = fullPathToPfscanBinary;
    }

    public String getFullPathToPfsearchBinary() {
        return fullPathToPfsearchBinary;
    }

    public void setFullPathToPfsearchBinary(String fullPathToPfsearchBinary) {
        this.fullPathToPfsearchBinary = fullPathToPfsearchBinary;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getFullPathToConfirmatoryProfiles() {
        return fullPathToConfirmatoryProfiles;
    }

    public void setFullPathToConfirmatoryProfiles(String fullPathToConfirmatoryProfiles) {
        this.fullPathToConfirmatoryProfiles = fullPathToConfirmatoryProfiles;
    }

    public Boolean getUsePfsearch() {
        return usePfsearch;
    }

    public void setUsePfsearch(Boolean usePfsearch) {
        this.usePfsearch = usePfsearch;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        List<String> command = new ArrayList<>();
        command.add(this.getPerlCommand());
        command.add(this.getFullPathToPsScanPerlScript());
        command.add("-d");
        command.add(this.getModelFile());
        if (this.getUsePfsearch() && this.getFullPathToPfsearchBinary() != null && !this.getFullPathToPfsearchBinary().isEmpty()){
            command.add("-w");
            command.add(this.getFullPathToPfsearchBinary());
        } else if (this.getFullPathToPfscanBinary() != null && !this.getFullPathToPfscanBinary().isEmpty()) {
            command.add("--pfscan");
            command.add(this.getFullPathToPfscanBinary());
        }
        if (this.getFullPathToConfirmatoryProfiles() != null && !this.getFullPathToConfirmatoryProfiles().isEmpty()) {
            command.add("-b");
            command.add(this.getFullPathToConfirmatoryProfiles());
        }
        command.addAll(this.getBinarySwitchesAsList());
        command.add(fastaFilePathName);
        return command;
    }
}
