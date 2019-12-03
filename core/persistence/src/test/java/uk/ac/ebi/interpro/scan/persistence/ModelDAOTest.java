package uk.ac.ebi.interpro.scan.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import javax.annotation.Resource;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 21-Jul-2009
 * Time: 09:33:10
 *
 * @author Phil Jones, EMBL-EBI
 * @author Gift Nuka
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class ModelDAOTest {

    @Resource(name= "modelDAO")
    private GenericDAO dao;

    public void setDao(GenericDAO dao) {
        this.dao = dao;
    }

    @Test public void empty(){
        
    }
}
