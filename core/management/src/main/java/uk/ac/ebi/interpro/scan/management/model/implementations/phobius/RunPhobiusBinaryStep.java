package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

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

    private static final Logger LOGGER = Logger.getLogger(RunPhobiusBinaryStep.class.getName());

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
        final List<String> command = new ArrayList<String>();
        command.add(fullPathToBinary);
        command.addAll(getBinarySwitchesAsList());
        command.add(fastaFilePath);
        return command;
    }
}
