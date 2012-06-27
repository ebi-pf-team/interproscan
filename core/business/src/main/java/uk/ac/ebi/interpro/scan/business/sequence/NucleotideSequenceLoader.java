package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 22/06/11
 *         Time: 18:08
 *         <p/>
 *         Manages loading of nucleotide sequences.
 */
public class NucleotideSequenceLoader implements SequenceLoader {

    private static final Logger LOGGER = Logger.getLogger(NucleotideSequenceLoader.class.getName());

    private Set<NucleotideSequence> sequencesAwaitingInsertion;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private int sequenceInsertBatchSize;

    @Required
    public void setSequenceInsertBatchSize(int sequenceInsertBatchSize) {
        this.sequenceInsertBatchSize = sequenceInsertBatchSize;
        sequencesAwaitingInsertion = new HashSet<NucleotideSequence>(sequenceInsertBatchSize);
    }

    @Required
    public void setNucleotideSequenceDAO(NucleotideSequenceDAO nucleotideSequenceDAO) {
        this.nucleotideSequenceDAO = nucleotideSequenceDAO;
    }

    public void store(String sequence, String analysisJobNames, String... crossReferences) {
        if (sequence != null && sequence.length() > 0) {
            NucleotideSequence nucleotideSequence = new NucleotideSequence(sequence);
            if (crossReferences != null) {
                for (String crossReference : crossReferences) {
                    NucleotideSequenceXref xref = XrefParser.getNucleotideSequenceXref(crossReference);
                    nucleotideSequence.addCrossReference(xref);
                }
            } else {
                LOGGER.error("Have a nucleotide sequence with no associated Xrefs.  There must be at least one xref associated with the nucleotide sequence");
            }
            sequencesAwaitingInsertion.add(nucleotideSequence);
            if (sequencesAwaitingInsertion.size() > sequenceInsertBatchSize) {
                persistBatch();
            }
        }
    }

    /**
     * Persists all of the proteins in the list of proteinsAwaitingPersistence and empties
     * this Collection, ready to be used again.
     */
    private void persistBatch() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("NucleotideSequenceLoader.persistBatch() method has been called.");
        }
        if (sequencesAwaitingInsertion.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + sequencesAwaitingInsertion.size() + " nucleotide sequences");
            }
            nucleotideSequenceDAO.insertNewNucleotideSequences(sequencesAwaitingInsertion);
            sequencesAwaitingInsertion.clear();
        }
    }

    /**
     * The nucleotide sequences are persisted in batches.  This method MUST be called
     * after the final call to the store method to ensure the final batch are persisted.
     *
     * @param sequenceLoadListener probably should be null, unless there is some reason
     *                             to listen to the addition of new nucleotide sequences.
     *                             <p/>
     *                             (This implementation certainly does not need one.)
     */
    public void persist(SequenceLoadListener sequenceLoadListener, String analysisJobNames) {
        persistBatch();
    }

    public void setUseMatchLookupService(boolean useMatchLookupService) {
        // Currently a no-op, as there is no lookup service for nucleotide sequence matches.
    }

}
