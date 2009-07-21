package uk.ac.ebi.interpro.scan.persistence;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 21-Jul-2009
 * Time: 09:33:10
 *
 * @author Phil Jones, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/springconfig/spring-ModelDAOTest-config.xml"})
public class ModelDAOTest {

    @Resource(name= "modelDAO")
    private ProteinDAO dao;

    public void setDao(ProteinDAO dao) {
        this.dao = dao;
    }

    @Test public void empty(){
        
    }
}
