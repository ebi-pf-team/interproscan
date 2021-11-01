package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Panther binary Treegrafter on the domtbloutput file provided .
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class RunPantherTreeGrafterStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunPantherTreeGrafterStep.class.getName());

    private String fullPathToBinary;

    private String fullPathToEPANGBinary;

    private String fullPathToPython;

    private String pantherModelsDirectory;

    private String fastaFileNameTemplate;

    private String inputFileNameHmmerRawOutputTemplate;

    private String inputFilePantherFamilyNames;

    private boolean forceHmmsearch = true;


    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    public void setFullPathToEPANGBinary(String fullPathToEPANGBinary) {
        this.fullPathToEPANGBinary = fullPathToEPANGBinary;
    }

    public String getPantherModelsDirectory() {
        return pantherModelsDirectory;
    }

    public void setPantherModelsDirectory(String pantherModelsDirectory) {
        this.pantherModelsDirectory = pantherModelsDirectory;
    }

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    public String getInputFileNameHmmerRawOutputTemplate() {
        return inputFileNameHmmerRawOutputTemplate;
    }

    public void setInputFileNameHmmerRawOutputTemplate(String inputFileNameHmmerRawOutputTemplate) {
        this.inputFileNameHmmerRawOutputTemplate = inputFileNameHmmerRawOutputTemplate;
    }

    public String getInputFilePantherFamilyNames() {
        return inputFilePantherFamilyNames;
    }

    public void setInputFilePantherFamilyNames(String inputFilePantherFamilyNames) {
        this.inputFilePantherFamilyNames = inputFilePantherFamilyNames;
    }

    public String getFullPathToPython() {
        return fullPathToPython;
    }

    public void setFullPathToPython(String fullPathToPython) {
        this.fullPathToPython = fullPathToPython;
    }

    public boolean isForceHmmsearch() {
        return forceHmmsearch;
    }

    public void setForceHmmsearch(boolean forceHmmsearch) {
        this.forceHmmsearch = forceHmmsearch;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final List<String> command = new ArrayList<String>();
        final String treeGrafterTemplate =  this.getFastaFileNameTemplate().replace("fasta", "tg");
        final String treeGrafterTempdirectory = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, treeGrafterTemplate);
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
        final String inputFileNameHmmerRawOutput = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameHmmerRawOutputTemplate());

        command.add(fullPathToPython);
        command.add(fullPathToBinary);
        command.add(fastaFilePathName);
        command.add(inputFileNameHmmerRawOutput);
        command.add(pantherModelsDirectory);

        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }

        command.add("--epa-ng");
        command.add(fullPathToEPANGBinary);

        command.add("-T");
        command.add(treeGrafterTempdirectory);

        command.addAll(this.getBinarySwitchesAsList());

        Utilities.verboseLog(1100, "binary cmd to run: " + command.toString());
        return command;

    }

}
