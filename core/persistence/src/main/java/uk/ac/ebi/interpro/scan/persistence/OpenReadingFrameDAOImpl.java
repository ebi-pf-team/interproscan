package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

/**
 * @author Phil Jones
 *         Date: 21/06/11
 */
public class OpenReadingFrameDAOImpl extends GenericDAOImpl<OpenReadingFrame, Long> implements OpenReadingFrameDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public OpenReadingFrameDAOImpl() {
        super(OpenReadingFrame.class);
    }
}
