package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.FileContentChecker;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Phobius binary on the fasta file provided to the output file provided.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */

public class RunPhobiusBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = LogManager.getLogger(RunPhobiusBinaryStep.class.getName());

    private String fullPathToBinary;

    private String fastaFileNameTemplate;

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
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFileNameTemplate);
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, getOutputFileNameTemplate());
        //Check if the input file for Phobius binary is empty
        //If so create an empty raw result output file and return NULL
        FileContentChecker fileContentChecker = new FileContentChecker(new File(fastaFilePath));
        if (!fileContentChecker.isFileEmpty()) {
            final List<String> command = new ArrayList<String>();
            command.add(fullPathToBinary);
            command.addAll(getBinarySwitchesAsList());
            command.add(fastaFilePath);
            return command;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating empty raw result out put file...");
        }
        File file = new File(outputFileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            LOGGER.warn("Couldn't create empty raw result output file.", e);
        }
        return null;
    }
}
