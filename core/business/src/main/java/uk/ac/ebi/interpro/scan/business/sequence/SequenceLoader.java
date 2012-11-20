package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 22/06/11
 * <p/>
 * Common interface for classes the manage the loading of sequences (Protein or Nucleotide)
 * into the database.
 */
public interface SequenceLoader extends Serializable {

    void store(String sequence, String analysisJobNames, String... crossReferences);

    void persist(SequenceLoadListener sequenceLoadListener, String analysisJobNames);

    /**
     * If a match lookup service is available, set flag to determine if it is used.
     *
     * @param useMatchLookupService flag to indicate if an attempt should be made
     *                              to use this service.
     */
    void setUseMatchLookupService(boolean useMatchLookupService);

    /**
     * Persists proteins that have been collapsed and annotated with ProteinXrefs
     * by a separate process, e.g. the fasta file loader.
     *
     * @param parsedProteins   being a Collection of non-redundant Proteins and Xrefs.
     * @param analysisJobNames to be included in analysis.
     */
    void storeAll(Set<Protein> parsedProteins, String analysisJobNames);
}
