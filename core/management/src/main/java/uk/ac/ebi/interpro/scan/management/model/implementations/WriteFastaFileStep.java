package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.FastaFileWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
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

        try {
            if (doRunLocally || (!useMatchLookupService)) {
                Utilities.verboseLog(110, this.getId() + " GetAllSequences: doRunLocally: " + doRunLocally + " useMatchLookupService: " + useMatchLookupService);
                proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            } else {
                //TODO this is getting completed to filter nonlookup up proteins

                //final List<Protein> proteinsInRange = proteinDAO.getProteinsWithoutLookupHitBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
                //final List<Protein>  proteinsNotInLookup = proteinDAO.getProteinsNotInLookup();
                //System.out.println("proteinsNotInLookup size: " + proteinsNotInLookup.size());
                long bottomProtein = stepInstance.getBottomProtein();
                long topProtein = stepInstance.getTopProtein();
                proteins = new ArrayList<>();
                int count = 0;
                for (long proteinId = bottomProtein; proteinId <= topProtein; proteinId++) {
                    //Protein protein = proteinDAO.getProtein(Long.toString(proteinId));
                    Protein proteinNotInLookup = proteinDAO.getProteinNotInLookup(Long.toString(proteinId));
                    if (proteinNotInLookup != null) {
                        //System.out.println("write sequence id : " + proteinId + " real id from kv: " + proteinNotInLookup.getId());
                        proteins.add(proteinNotInLookup);
                        count++;
                    }
                }
                long maxProteins = topProtein - bottomProtein;

                Utilities.verboseLog(110, stepInstance.getStepId() + "[" + bottomProtein + "-" + topProtein + "]" + " Writen fasta sequence count : " + count + " of possible " + (maxProteins + 1));

                //deal with cases where there is no sequence in this range
                if (count == 0) {

                    //String stepID = stepInstance.getStepId();
                    String libraryName = job.getLibraryRelease().getLibrary().getName();
                    String range = "_" + bottomProtein + "-" + topProtein;
                    String key = libraryName + range;
                    job.addSkipRange(key);
                    LOGGER.warn("Protein sequences problem - for:  " + key + " - count of proteins to write to file: " + count);
                }

                 /*   for (Protein protein : proteinsNotInLookup) {
                        System.out.println("write sequence id : " + protein.getId());
                        Protein protein = proteinDAO.getProtein(protein.getId().toString());
                        if (protein.getId() >= stepInstance.getBottomProtein() && protein.getId() <= stepInstance.getTopProtein()) {
                            proteins.add(protein);
                        }
                    }
                  */

            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Writing " + proteins.size() + " proteins to FASTA file...");
            }
            fastaFileWriter.writeFastaFile(proteins, fastaFilePathName);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        } catch (FastaFileWriter.FastaFileWritingException e) {
            throw new IllegalStateException("FastaFileWriter.FastaFileWritingException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Step with Id " + this.getId() + " finished.");
        }
    }
}

