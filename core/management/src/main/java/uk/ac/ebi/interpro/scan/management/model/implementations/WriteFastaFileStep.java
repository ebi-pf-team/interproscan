package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.FastaFileWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.persistence.Transient;
import java.io.IOException;
import java.util.List;

/**
 * This Step write Fasta files for the range of proteins requested.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteFastaFileStep.class.getName());

    @Transient
    private FastaFileWriter fastaFileWriter = new FastaFileWriter();

    private String fastaFilePathTemplate;

    private ProteinDAO proteinDAO;

    @Required
    public void setFastaFileNameTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * If you need a custom fasta file writer, you can inject it here (e.g. for Phobius and TMHMM which
     * have picky requirements for amino acid alphabet).  Normally you can safely ignore this parameter
     * and the Step will use a default FastaFileWriter.
     *
     * @param fastaFileWriter a custom fasta file writer
     */
    public void setFastaFileWriter(FastaFileWriter fastaFileWriter) {
        this.fastaFileWriter = fastaFileWriter;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting step with Id " + this.getId());
        }
        final String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFilePathTemplate);
        final List<Protein> proteins;
        if (doRunLocally) {
            proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        }else {
            proteins = proteinDAO.getProteinsWithoutLookupHitBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Writing " + proteins.size() + " proteins to FASTA file...");
        }
        try {
            fastaFileWriter.writeFastaFile(proteins, fastaFilePathName);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        } catch (FastaFileWriter.FastaFileWritingException e) {
            throw new IllegalStateException("FastaFileWriter.FastaFileWritingException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Step with Id " + this.getId() + " finished.");
        }
    }
}

