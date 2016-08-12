package uk.ac.ebi.interpro.scan.management.model.implementations.prosite;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * This step defines running the Prosite PFSearch using a python wrapper.
 *
 * @author Gift Nuka
 * @date today
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class RunHmmPfsearchStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunHmmPfsearchStep.class.getName());

    private String fullPathToPFsearch;

    private String fullPathToPython;

    private String fullPathToPfsearchWrapper;

    private String fastaFileNameTemplate;

    private String statsFileNameTemplate;

    private String fullPathToConfirmatoryProfiles;

    private String modelDir;

    private String filteredFastaInputFileNameTemplate;

    private String outputFileTemplate;

    private String outputFileNameTbloutTemplate;


    public String getFullPathToPFsearch() {
        return fullPathToPFsearch;
    }

    @Required
    public void setFullPathToPFsearch(String fullPathToPFsearch) {
        this.fullPathToPFsearch = fullPathToPFsearch;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public String getModelDir() {
        return modelDir;
    }

    @Required
    public void setModelDir(String modelDir) {
        this.modelDir = modelDir;
    }

    public String getFullPathToConfirmatoryProfiles() {
        return fullPathToConfirmatoryProfiles;
    }

    public void setFullPathToConfirmatoryProfiles(String fullPathToConfirmatoryProfiles) {
        this.fullPathToConfirmatoryProfiles = fullPathToConfirmatoryProfiles;
    }

    public String getFilteredFastaInputFileNameTemplate() {
        return filteredFastaInputFileNameTemplate;
    }

    public void setFilteredFastaInputFileNameTemplate(String filteredFastaInputFileNameTemplate) {
        this.filteredFastaInputFileNameTemplate = filteredFastaInputFileNameTemplate;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
    }

    public String getOutputFileNameTbloutTemplate() {
        return outputFileNameTbloutTemplate;
    }

    public void setOutputFileNameTbloutTemplate(String outputFileNameTbloutTemplate) {
        this.outputFileNameTbloutTemplate = outputFileNameTbloutTemplate;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFullPathToPfsearchWrapper() {
        return fullPathToPfsearchWrapper;
    }

    @Required
    public void setFullPathToPfsearchWrapper(String fullPathToPfsearchWrapper) {
        this.fullPathToPfsearchWrapper = fullPathToPfsearchWrapper;
    }

    public String getStatsFileNameTemplate() {
        return statsFileNameTemplate;
    }

    public void setStatsFileNameTemplate(String statsFileNameTemplate) {
        this.statsFileNameTemplate = statsFileNameTemplate;
    }

    /**
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String statsFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getStatsFileNameTemplate());
        final String fileNameTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileNameTbloutTemplate);
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileTemplate);

        List<String> command = new ArrayList<String>();
        if(this.getFullPathToPython().trim().isEmpty()){
            command.add("python");
        }else{
            command.add(this.getFullPathToPython());
        }


        command.add(this.fullPathToPfsearchWrapper);
        command.add(fileNameTblout);
        command.add(fastaFilePathName);
        command.add(statsFilePathName);
        command.add(outputFileName);
        command.add(this.getModelDir());
        command.add(this.getFullPathToPFsearch());
        command.addAll(this.getBinarySwitchesAsList());

//        command.add(fastaFilePathName);
        LOGGER.debug("binary command: " + command.toString());
        Utilities.verboseLog(10,  "binary command: " + command.toString());
        return command;
    }
}
