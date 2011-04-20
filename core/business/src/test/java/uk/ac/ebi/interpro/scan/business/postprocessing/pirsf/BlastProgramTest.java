package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: matthew
 * Date: 18-Apr-2011
 * Time: 15:18:35
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BlastProgramTest extends TestCase {

    @javax.annotation.Resource
    private BinaryRunner binRunner;

    @Test
    @Ignore
    public void testBlastProgram() {
        //TODO Finish this test!

//        try {
//            Resource inputFileResource = new ClassPathResource("test_proteins.fasta");
//            File outFile = File.createTempFile("blastout-", ".out");
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }

        try {
            binRunner.run();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
