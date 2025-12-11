package uk.ac.ebi.interpro.scan.management.model.implementations.hamap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This step defines running the Prosite pfscanV3 script for hamap.
 *
 */
public class RunPFScanStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunPFScanStep.class.getName());

    private String fullPathToPFscan;

    private String fastaFileNameTemplate;

    private String modelFile;


    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    public String getModelFile() {
        return modelFile;
    }

    @Required
    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    public String getFullPathToPFscan() {
        return fullPathToPFscan;
    }

    @Required
    public void setFullPathToPFscan(String fullPathToPFscan) {
        this.fullPathToPFscan = fullPathToPFscan;
    }


    /**
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return
     */

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());

        isFilePathLengthReasonable(fastaFilePathName);

        List<String> command = new ArrayList<String>();
        command.add(this.getFullPathToPFscan());
        command.addAll(this.getBinarySwitchesAsList());
        command.add(this.getModelFile());
        command.add(fastaFilePathName);

        Utilities.verboseLog(30, "command: " + command.toString());
        return command;
    }
}
