package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.binary.BinaryRunner;
import uk.ac.ebi.interpro.scan.business.binary.SimpleBinaryRunner;
import uk.ac.ebi.interpro.scan.io.cli.CommandLineConversationImpl;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: matthew
 * Date: 18-Apr-2011
 * Time: 15:18:35
 * To change this template use File | Settings | File Templates.
 */
public class BlastProgramTest  extends TestCase{

    @Test
    @Ignore
    public void testBlastProgram()
    {
        //TODO Finish this test!
        
        final StringBuilder additionalArguments = new StringBuilder();
        File outFile = null;
        try {
            Resource inputFileResource = new ClassPathResource("test_proteins.fasta");
            outFile = File.createTempFile("blastout-", ".out");
            additionalArguments
                    .append("-p blastp -F F -e 0.001 -a 1 -b1000 -m 8 -d data/pirsf/2.74/sf.seq ")
                    .append("-i ")
                    .append(inputFileResource.getFile().getAbsolutePath())
                    .append(' ')
                    .append("-o ")
                    .append(outFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        BinaryRunner runner = new SimpleBinaryRunner();
        runner.setBinary("blastall");
        runner.setDeleteTemporaryFiles(true);
        runner.setCommandLineConversation(new CommandLineConversationImpl());
        if(outFile!=null)
            runner.setTemporaryFilePath(outFile.getPath());

        // Run command but don't capture output (DF3 writes to a file, not stdout)
        try {
            runner.run(additionalArguments.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
