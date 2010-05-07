package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.IOException;


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

    private org.springframework.core.io.Resource fastaFile;

    private ProteinDAO proteinDAO;

    @javax.annotation.Resource (name="loader")
    public void setLoader(LoadFastaFile loader) {
        this.loader = loader;
    }

    @javax.annotation.Resource (name="fastaFile")
    public void setFastaFile(org.springframework.core.io.Resource fastaFile) {
        this.fastaFile = fastaFile;
    }

    @javax.annotation.Resource (name="proteinDAO")
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Test
    public void testLoader() throws IOException {
        System.out.println("Loader:" + loader);
        System.out.println("FastaFile: " + fastaFile);        
        loader.loadSequences(fastaFile.getInputStream(),new ProteinLoadListener(){
            @Override
            public void createStepInstances(Long bottomProteinId, Long topProteinId) {
                System.out.println("Loaded:"+bottomProteinId+"-"+topProteinId);
            }
        });
        System.out.println("Proteins loaded: " + proteinDAO.count());
    }
}
