package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;

import java.io.InputStream;
import java.io.Serializable;

/**
 * This interface defines the methods required to load a fasta file...
 * ...either protein or nucleic acid.  (See implementations)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public interface LoadFastaFile extends Serializable {

    /**
     * Set the SequenceLoader implementation (Spring config)
     *
     * @param sequenceLoader
     */
    @Required
    void setSequenceLoader(SequenceLoader sequenceLoader);

    /**
     * Load the sequences into the database.  Optionally, the caller
     * can specify if an available match lookup service should be used
     *
     * @param fastaFileInputStream  to read
     * @param sequenceLoadListener  to respond to sequence loading - e.g. loading sequence
     *                              data into the database
     * @param analysisJobNames      to be run
     * @param useMatchLookupService if it is available.
     */
    @Transactional
    void loadSequences(InputStream fastaFileInputStream, SequenceLoadListener sequenceLoadListener, String analysisJobNames, boolean useMatchLookupService);
}
