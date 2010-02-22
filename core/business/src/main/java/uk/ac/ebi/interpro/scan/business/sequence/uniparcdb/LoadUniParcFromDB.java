package uk.ac.ebi.interpro.scan.business.sequence.uniparcdb;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoader;

/**
 * Interface for UniParc protein loader.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public interface LoadUniParcFromDB {

    /**
     * Sets the maximum number of proteins loaded
     * from UniParc in a single transaction.
     * @param maximumProteins the maximum number of proteins loaded
     * from UniParc in a single transaction.
     */
    @Required
    void setMaximumProteins(Integer maximumProteins);

    /**
     * Check for new protein sequences in the UniParc
     * database and load them (up to a maximum number)
     */
    @Transactional
    void loadNewSequences ();
}
