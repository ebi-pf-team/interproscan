package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jun-2010
 * Time: 17:37:52
 * To change this template use File | Settings | File Templates.
 */
public class SignatureLibraryReleaseDAOImpl extends GenericDAOImpl<SignatureLibraryRelease, Long> implements SignatureLibraryReleaseDAO {
    /**
     * No-argument constructor
     * that calls the super constructor with the appropriate class.
     */
    public SignatureLibraryReleaseDAOImpl() {
        super(SignatureLibraryRelease.class);
    }

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
    public boolean isReleaseAlreadyPersisted(SignatureLibrary library, String version) {
        Query query = entityManager.createQuery(
                "select r from SignatureLibraryRelease r " +
                        "where r.library = :library and r.version = :version"
        );
        query.setParameter("library", library);
        query.setParameter("version", version);
        List results = query.getResultList();
        return results != null && results.size() > 0;
    }
}
