package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.sequence.XrefParser;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceDAO;
import uk.ac.ebi.interpro.scan.persistence.NucleotideSequenceXrefDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones
 * @author Gift Nuka
 *         Date: 22/06/11
 *
 *         Manages loading of nucleotide sequences.
 */
public class NucleotideSequenceLoader implements SequenceLoader<NucleotideSequence> {

    private static final Logger LOGGER = Logger.getLogger(NucleotideSequenceLoader.class.getName());

    private Set<NucleotideSequence> sequencesAwaitingInsertion;

    private NucleotideSequenceDAO nucleotideSequenceDAO;

    private NucleotideSequenceXrefDAO nucleotideSequenceXrefDAO;

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

    @Required
    public NucleotideSequenceXrefDAO getNucleotideSequenceXrefDAO() {
        return nucleotideSequenceXrefDAO;
    }

    public void setNucleotideSequenceXrefDAO(NucleotideSequenceXrefDAO nucleotideSequenceXrefDAO) {
        this.nucleotideSequenceXrefDAO = nucleotideSequenceXrefDAO;
    }

    public void store(String sequence, Map<String, SignatureLibraryRelease> analysisJobMap, String... crossReferences) {
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
            Utilities.verboseLog("Persisting " + sequencesAwaitingInsertion.size()  + " nucleotide sequences");

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
    public void persist(SequenceLoadListener sequenceLoadListener, Map<String, SignatureLibraryRelease> analysisJobMap) {
        persistBatch();
        Collection<String> nonUniqueIdentifiers = nucleotideSequenceXrefDAO.getNonUniqueXrefs();
        if (nonUniqueIdentifiers != null && nonUniqueIdentifiers.size() > 0) {
            System.out.println("Found " + nonUniqueIdentifiers.size() + " non unique identifier(s). These identifiers do have different sequences, within the FASTA nucleotide sequence input file.");
            System.out.println("Please find below a list of detected identifiers:");
            for (String nonUniqueIdentifier : nonUniqueIdentifiers) {
                System.out.println(nonUniqueIdentifier);
            }
            System.out.println("InterProScan will shutdown, because there is no way to map nucleic sequences and predicted proteins.");
            System.exit(0);
        }

    }

    public void setUseMatchLookupService(boolean useMatchLookupService) {
        // Currently a no-op, as there is no lookup service for nucleotide sequence matches.
    }

    /**
     * Persists proteins that have been collapsed and annotated with ProteinXrefs
     * by a separate process, e.g. the fasta file loader.
     *
     * @param parsedNucleotideSequences being a Collection of non-redundant Nucleotide Sequences and Xrefs.
     * @param analysisJobMa for analysisJobNames          to be included in analysis.
     */
    public void storeAll(Set<NucleotideSequence> parsedNucleotideSequences, Map<String, SignatureLibraryRelease> analysisJobMa) {
        if (parsedNucleotideSequences.size() > 2000){
            LOGGER.warn("You are analysing more than 2000 nucleotide sequences. " +
                    " Either use an external tool to translate the sequences or Chunk the input and then send the chunks to InterProScan. Refer to " +
                    " https://github.com/ebi-pf-team/interproscan/wiki/ScanNucleicAcidSeqs#improving-performance");
        }

        for (NucleotideSequence nucleotideSequence : parsedNucleotideSequences) {
            sequencesAwaitingInsertion.add(nucleotideSequence);
            if (sequencesAwaitingInsertion.size() > sequenceInsertBatchSize) {
                persistBatch();
            }
        }
    }

}
