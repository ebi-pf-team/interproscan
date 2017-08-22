package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the PantherScore on the domtbloutput file provided .
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class RunPantherScoreStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunPantherScoreStep.class.getName());

    private String fullPathToBinary;

    private String fullPathToPython;


    private String inputFileNameDomTbloutTemplate;

    private String inputFilePantherFamilyNames;


    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }


    public String getInputFileNameDomTbloutTemplate() {
        return inputFileNameDomTbloutTemplate;
    }

    @Required
    public void setInputFileNameDomTbloutTemplate(String inputFileNameDomTbloutTemplate) {
        this.inputFileNameDomTbloutTemplate = inputFileNameDomTbloutTemplate;
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

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final List<String> command = new ArrayList<String>();
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());

        final String inputFileNameDomTblout = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFileNameDomTbloutTemplate());

        //final String inputFilePantherFamilyNames = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getInputFilePantherFamilyNames());


        command.add(fullPathToPython);
        command.add(fullPathToBinary);

        command.add("-d");
        command.add(inputFileNameDomTblout);
        command.add("-m");
        if (Utilities.getUseHmmsearch() ) {
            command.add("hmmsearch");
        }else{
            command.add("hmmscan");
        }

        command.add("-n");
        command.add(inputFilePantherFamilyNames);

        command.addAll(this.getBinarySwitchesAsList());

        // output file option
        if(this.isUsesFileOutputSwitch()){
            command.add("-o");
            command.add(outputFilePathName);
        }

        Utilities.verboseLog("binary cmd to run: " + command.toString());
        return command;

    }

}
