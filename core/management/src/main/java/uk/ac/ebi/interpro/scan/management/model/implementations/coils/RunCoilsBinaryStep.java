package uk.ac.ebi.interpro.scan.management.model.implementations.coils;

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
 * Runs the Coils binary on the fasta file provided to the output file provided.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */

public class RunCoilsBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(RunCoilsBinaryStep.class.getName());

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
        final List<String> command = new ArrayList<String>();
        command.add(fullPathToBinary);
        command.addAll(getBinarySwitchesAsList());
        return command;
    }

    /**
     * Implementations of RunBinaryStep may optionally override this method to
     * return an InputStream that can be piped into the command.
     * <p/>
     * Coils overrides this method as the ncoils binary expects the fasta file to be piped in
     * to the command.
     */
    @Override
    protected InputStream createCommandInputStream(StepInstance stepInstance, String temporaryFileDirectory) throws IOException {
        final String fastaFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFileNameTemplate);
        return new FileInputStream(new File(fastaFilePath));
    }
}
