package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Release;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 30/07/12
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public class NoOpReleaseDAOImpl extends GenericDAOImpl<Release, Long> implements ReleaseDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public NoOpReleaseDAOImpl() {
        super(Release.class);
    }

    public Release getReleaseByVersion(String version) {
        return null;  //No-op
    }
}
