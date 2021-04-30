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
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());

        //final String inputFileNameDomTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameHmmerRawOutputTemplate());

        final String inputFileNameHmmerRawOutput = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameHmmerRawOutputTemplate());


        //final String inputFilePantherFamilyNames = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFilePantherFamilyNames());
        //treegrafter.py --out output/panther15/tg.v16.run1.out
        // -d /hps/nobackup/production/interpro/nuka/i5/panther/panther16/v16/data
        // -t temp/hx-noah-31-04.ebi.ac.uk_20210429_155843053_c3kq/
        // -ab bin/ -f /hps/nobackup/production/interpro/nuka/i5/release/interproscan-5.51-85.0/test_all_appl.fasta
        // -v DEBUG -k

        command.add(fullPathToPython);
        command.add(fullPathToBinary);
        command.add("-d");
        command.add(pantherModelsDirectory);
//        command.add("-m");
//        if (forceHmmsearch || Utilities.getSequenceCount() > 10) {
//            command.add("hmmsearch");
//        }else{
//            command.add("hmmscan");
//        }
        command.add("-ab");
        command.add(fullPathToEPANGBinary);
        command.add("-f");
        command.add(fastaFilePathName);
        command.add("-ho");
        command.add(inputFileNameHmmerRawOutput);

        command.addAll(this.getBinarySwitchesAsList());

        // output file option
        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }

        command.add("-v");
        command.add("DEBUG");
        command.add("-k");

        Utilities.verboseLog(1100, "binary cmd to run: " + command.toString());
        return command;

    }

}
