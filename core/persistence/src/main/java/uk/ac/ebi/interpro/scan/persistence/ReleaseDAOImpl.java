package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Release;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;
import java.util.HashSet;
import java.util.List;

/**
 * Represents an implementation of {@link ReleaseDAO} interface.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public class ReleaseDAOImpl extends GenericDAOImpl<Release, Long> implements ReleaseDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in {@link Release} class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public ReleaseDAOImpl() {
        super(Release.class);
    }

    @Transactional(readOnly = true)
    public Release getReleaseByVersion(String version) {
        Query query = entityManager.createQuery("select r from Release r " +
                "where r.version = :version");
        query.setParameter("version", version);

        List<Release> results = query.getResultList();
        if (results != null && results.size() > 0) {
            return results.get(0);
        }
        return null;
    }
}