package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

/**
 * Signature library DAO
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public interface SignatureLibraryReleaseDAO extends GenericDAO<SignatureLibraryRelease, Long> {

    /**
     * This method returns true if the particular release is already persisted
     * to the database.  This method is used to ensure that repeated runs of
     * InterProScan 5 in 'installer' mode do not load the same Signatures and Models
     * twice.
     *
     * @param library being the specific SignatureLibrary to check for
     * @param version being the specific version to check for
     * @return true if the version of the SignatureLibrary passed in as arguments
     *         is already in the database.
     */
    @Transactional(readOnly = true)
    public boolean isReleaseAlreadyPersisted(SignatureLibrary library, String version);
}
