package uk.ac.ebi.interpro.scan.io.installer.interprodao;

import uk.ac.ebi.interpro.scan.model.PathwayXref;

import java.util.Collection;

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
     * @param entryAc Entry accession.
     * @return A collection of pathway cross references for the specified accession.
     */
    Collection<PathwayXref> getPathwayXrefsByEntryAc(String entryAc);
}
