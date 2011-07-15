package uk.ac.ebi.interpro.scan.persistence.installer;

import uk.ac.ebi.interpro.scan.model.PathwayXref;

import java.util.Collection;
import java.util.Map;

/**
 * Represents the data access object interface for
 * {@link Entry2PathwayDAOImpl}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface Entry2PathwayDAO {
    /**
     * Retrieves a collection of pathway cross references by the specified entry accessions.
     *
     * @param entryAc Entry accession.
     * @return A collection of pathway cross references for the specified accession.
     */
    Collection<PathwayXref> getPathwayXrefsByEntryAc(String entryAc);

    /**
     * Retrieves all pathway cross references mapped by entry accession.
     *
     * @return
     */
    Map<String, Collection<PathwayXref>> getAllPathwayXrefs();

    /**
     * Retrieves a sub set of pathway cross references mapped by the specified entry accessions.
     *
     * @return
     */
    Map<String, Collection<PathwayXref>> getPathwayXrefs(Collection<String> entryAccessions);
}