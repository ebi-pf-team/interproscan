package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Release;

/**
 * DAO Interface for data access to the InterPro Release table.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public interface ReleaseDAO extends GenericDAO<Release, Long> {
    Release getReleaseByVersion(String version);
}