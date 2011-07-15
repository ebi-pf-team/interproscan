package uk.ac.ebi.interpro.scan.persistence.installer;

import uk.ac.ebi.interpro.scan.model.GoXref;

import java.util.Collection;
import java.util.Map;

/**
 * Represents the data access object interface for GO term cross references
 * (Entry to GO term mappings).
 * TODO: Need to rethink GO term integration in I5 because we do not store additional info like GO classes
 * TODO: and GO names at the moment
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface Entry2GoDAO {
    /**
     * Retrieves a collection of GO term cross references by the specified entry accessions.
     *
     * @param entryAc Entry accession.
     * @return A collection of GO term cross references for the specified accession.
     */
    Collection<GoXref> getGoXrefsByEntryAc(String entryAc);

    /**
     * Retrieves all GO terms cross references mapped by entry accession.
     *
     * @return
     */
    Map<String, Collection<GoXref>> getAllGoXrefs();

    /**
     * Retrieves a sub set of GO terms cross references mapped by the specified entry accessions.
     *
     * @return
     */
    Map<String, Collection<GoXref>> getGoXrefs(Collection<String> entryAccessions);
}