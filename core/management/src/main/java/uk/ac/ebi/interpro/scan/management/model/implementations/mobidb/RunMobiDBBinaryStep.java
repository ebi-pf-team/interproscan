package uk.ac.ebi.interpro.scan.management.model.implementations.mobidb;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the MobiDB binary on the fasta file provided to the output file provided.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class RunMobiDBBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunMobiDBBinaryStep.class.getName());

    private String fullPathToPython;

    private String fullPathToBinary;

    private String fullPathToBinDirectory;

    private String fastaFileNameTemplate;

    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public String getFullPathToBinDirectory() {
        return fullPathToBinDirectory;
    }

    @Required
    public void setFullPathToBinDirectory(String fullPathToBinDirectory) {
        this.fullPathToBinDirectory = fullPathToBinDirectory;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final List<String> command = new ArrayList<String>();
        final String fastaFilePath
                = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, getOutputFileNameTemplate());


        if(this.getFullPathToPython().trim().isEmpty()){
            command.add("python");
        }else{
            command.add(this.getFullPathToPython());
        }

        command.add(fullPathToBinary);
        command.addAll(getBinarySwitchesAsList());
        command.add("-bin");
        String absoluteBinxDirectoryPath = new File(fullPathToBinDirectory).getAbsolutePath();
        command.add(absoluteBinxDirectoryPath);
        //command.add("-o");
        //String absolutePathToOutputFileName = new File(outputFileName).getAbsolutePath();
        //command.add(absolutePathToOutputFileName);
        command.add(fastaFilePath);
        return command;
    }
}
