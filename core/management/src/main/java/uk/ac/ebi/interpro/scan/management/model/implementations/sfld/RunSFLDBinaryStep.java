package uk.ac.ebi.interpro.scan.management.model.implementations.sfld;

import org.apache.commons.collections.functors.ExceptionClosure;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the SFLD binary on the fasta file provided to the output file provided.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class RunSFLDBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunSFLDBinaryStep.class.getName());

    private String fullPathToBinary;

    private String fastaFileNameTemplate;

    private String outputFileNameTbloutTemplate;


    @Required
    public void setFullPathToBinary(String fullPathToBinary) {
        this.fullPathToBinary = fullPathToBinary;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFileNameTemplate) {
        this.fastaFileNameTemplate = fastaFileNameTemplate;
    }

    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final List<String> command = new ArrayList<String>();
        final String outputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getOutputFileNameTemplate());
        final String exampleFile =  "/ebi/production/interpro/programmers/nuka/projects/git/interproscan5/github/interproscan/core/jms-implementation/target/interproscan-5-dist/data/sfld/201606_27/sfld.example.raw.out";

        //simulate the sfld post processing binary run
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(exampleFile));
            File outputFile = new File(outputFilePathName);
            // if File doesnt exists, then create it
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileWriter filewriter = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputStream = new BufferedWriter(filewriter);
            String count;
            while ((count = inputStream.readLine()) != null) {
                outputStream.write(count);
                outputStream.newLine();
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }catch (Exception e){

        }

        command.add("ls");
//        command.add(outputFilePathName);
        Utilities.verboseLog("binary cmd to run: " + command.toString());
        return command;

        // output file option
//        if(this.isUsesFileOutputSwitch()){
//            command.add("-o");
//            command.add(outputFilePathName);
//        }
//        command.addAll(getBinarySwitchesAsList());

    }

}
