package uk.ac.ebi.interpro.scan.management.model.implementations.hamap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
public class RunPFSearchStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunPFSearchStep.class.getName());

    private String fullPathToPFsearch;

    private String fullPathToPython;

    private String fullPathToPfsearchWrapper;

    private String fastaFileNameTemplate;

    private String modelDir;

    private String outputFileTemplate;


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

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    public void setOutputFileTemplate(String outputFileTemplate) {
        this.outputFileTemplate = outputFileTemplate;
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

    /**
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        // final String statsFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameFilteredTemplate());
        // final String fileNameTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileNameTbloutTemplate);
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileTemplate);

        isFilePathLengthReasonable(fastaFilePathName);
        isFilePathLengthReasonable(outputFileName);

        List<String> command = new ArrayList<String>();
        if(this.getFullPathToPython().trim().isEmpty()){
            command.add("python3");
        }else{
            command.add(this.getFullPathToPython());
        }


        command.add(this.fullPathToPfsearchWrapper);
        // command.add(fileNameTblout);
        command.add(fastaFilePathName);
        // command.add(statsFilePathName);
        command.add(outputFileName);
        command.add(this.getModelDir());
        command.add(this.getFullPathToPFsearch());
        command.addAll(this.getBinarySwitchesAsList());

//        command.add(fastaFilePathName);
        LOGGER.debug("binary command: " + command.toString());
        Utilities.verboseLog(110,  "binary command: " + command.toString());
        return command;
    }
}
