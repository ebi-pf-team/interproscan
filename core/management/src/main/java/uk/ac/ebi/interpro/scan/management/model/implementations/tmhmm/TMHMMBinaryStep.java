package uk.ac.ebi.interpro.scan.management.model.implementations.tmhmm;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.FileContentChecker;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs TMHMM (Prediction of transmembrane helices in proteins) binary.
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public final class TMHMMBinaryStep extends RunBinaryStep {

    private static final Logger LOGGER = Logger.getLogger(TMHMMBinaryStep.class.getName());

    private String fastaFileNameTemplate;

    private String pathToTmhmmBinary;

    private String binaryBackgroundSwitch;

    private String pathToTmhmmModel;

    public String getFastaFileNameTemplate() {
        return fastaFileNameTemplate;
    }

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathNameTemplate) {
        this.fastaFileNameTemplate = fastaFilePathNameTemplate;
    }

    @Required
    public void setPathToTmhmmBinary(String pathToTmhmmBinary) {
        this.pathToTmhmmBinary = pathToTmhmmBinary;
    }

    public void setBinaryBackgroundSwitch(String binaryBackgroundSwitch) {
        this.binaryBackgroundSwitch = binaryBackgroundSwitch;
    }

    @Required
    public void setPathToTmhmmModel(String pathToTmhmmModel) {
        this.pathToTmhmmModel = pathToTmhmmModel;
    }

    /**
     * Returns command line for run TMHMM
     * <p/>
     * Example:
     * support-mini-x86-32/bin/tmhmm/2.0c/decodeanhmm
     * -N 1
     * -PostLabProb
     * -PrintNumbers
     * -background '0.081 0.015 0.054 0.061 0.040 0.068 0.022 0.057 0.056 0.093 0.025 0.045 0.049 0.039 0.057 0.068 0.058 0.067 0.013 0.032'
     * support-mini-x86-32/data/tmhmm/2.0/TMHMM2.0.model
     * support-mini-x86-32/data/tmhmm/test_seqs.fasta
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory is the relative path in which files are stored.
     * @return Command line.
     */
    @Override
    protected List<String> createCommand(StepInstance stepInstance, String temporaryFileDirectory) {
        final String fastaFilePath
                = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getFastaFileNameTemplate());
        final String outputFileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, getOutputFileNameTemplate());
        FileContentChecker fileContentChecker = new FileContentChecker(new File(fastaFilePath));
        if (!fileContentChecker.isFileEmpty()) {
            final List<String> command = new ArrayList<String>();
            //Add command
            command.add(this.pathToTmhmmBinary);
            // Add TMHMM model
            command.add(this.pathToTmhmmModel);
            // FASTA file
            command.add(fastaFilePath);
            // Arguments
            command.addAll(this.getBinarySwitchesAsList());
            //Add background argument
            command.add("-background");
            command.add(this.binaryBackgroundSwitch);
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
