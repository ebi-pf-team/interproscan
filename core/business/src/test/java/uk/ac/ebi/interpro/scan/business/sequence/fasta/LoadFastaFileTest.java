package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 15:01:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LoadFastaFileTest {

    private LoadFastaFile loader;

    private ProteinDAO proteinDAO;

    @Resource (name="loader")
    public void setLoader(LoadFastaFile loader) {
        this.loader = loader;
    }
    @Resource (name="proteinDAO")
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Test
    public void testLoader(){
        loader.loadSequences();
        System.out.println("Proteins loaded: " + proteinDAO.count());
    }
}
