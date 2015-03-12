package uk.ac.ebi.interpro.scan.genericjpadao;

import org.springframework.transaction.annotation.Transactional;

/**
 * Simple DAO with method that implements a nested transaction
 * for testing purposes. 
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class ModelObjectDAOImpl extends GenericDAOImpl<ModelObject, Long> implements ModelObjectDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     */
    public ModelObjectDAOImpl() {
        super(ModelObject.class);
    }

    @Transactional
    public void nestedTransaction(boolean shouldFail) throws Exception{
        insert(new ModelObject("Bob"));
        insert(new ModelObject("Geoff"));
        insert(new ModelObject(          // The field in ModelObject is 'not null'.
           (shouldFail) ? null : "Harry"
        ));
    }
}
