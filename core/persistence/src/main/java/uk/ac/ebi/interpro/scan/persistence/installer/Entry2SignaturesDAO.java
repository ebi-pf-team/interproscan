package uk.ac.ebi.interpro.scan.persistence.installer;

import uk.ac.ebi.interpro.scan.model.PathwayXref;
import uk.ac.ebi.interpro.scan.model.Signature;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents the data access object interface.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface Entry2SignaturesDAO {
    /**
     * Retrieves a collection of signatures for the specified entry.
     *
     * @param entryAc Entry accession.
     * @return A collection of signatures for the specified entry accession.
     */
    Collection<String> getSignaturesByEntryAc(String entryAc);

    /**
     * Retrieves all signatures.
     *
     * @return
     */
    Map<String, Collection<String>> getAllSignatures();

    /**
     * Retrieves signatures for the specified entry accessions.
     *
     * @return
     */
    Map<String, Collection<String>> getSignatures(Collection<String> entryAccessions);
}
