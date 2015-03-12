package uk.ac.ebi.interpro.scan.genericjpadao;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO Description of class...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public interface ModelObjectDAO extends GenericDAO<ModelObject, Long> {

    void nestedTransaction(boolean shouldFail) throws Exception;
}
