package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.raw.ProfileScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public abstract class AbstractProfileFilteredMatchDAOImpl extends ProfileFilteredMatchDAO<ProfileScanRawMatch> {

    public void persistFilteredMatches(Collection<RawProtein<ProfileScanRawMatch>> filteredMatches) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
