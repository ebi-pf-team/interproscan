package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 23, 2010
 * Time: 11:21:43 AM
 */

public class PrintsRawMatchDAOImpl extends RawMatchDAOImpl<PrintsRawMatch> implements PrintsRawMatchDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     */
    public PrintsRawMatchDAOImpl() {
        super(PrintsRawMatch.class);
    }

}
