package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Model;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 11:28:24
 *
 * @author Phil Jones, EMBL-EBI
 */
public class ModelDAOImpl extends GenericDAOImpl<Model, Long> implements ModelDAO{

    /**
     * Default constructor that initialises the GenericDAOImpl to handle Model objects.
     */
    public ModelDAOImpl(){
        super (Model.class);
    }
}
