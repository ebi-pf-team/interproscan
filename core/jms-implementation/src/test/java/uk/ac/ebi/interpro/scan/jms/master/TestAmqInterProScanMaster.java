package uk.ac.ebi.interpro.scan.jms.master;

import org.junit.Test;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test for DistributedBlackBoxMaster.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TestAmqInterProScanMaster {

    private BlackBoxMaster master;

    @Test
    public void processOutputFormatsForProteinsTest() {
        //Set up the master
        master = new StandaloneBlackBoxMaster(new DefaultMessageListenerContainer());
        master.setSequenceType("p");
        //Run the test
        Map<String, String> params = new HashMap<String, String>();
        String[] outputFormats = new String[]{"tsv", "html", "gff3"};
        String[] expectedOutputFormats = outputFormats;
        master.processOutputFormats(params, outputFormats);
        String[] actualOutputFormats = params.get(WriteOutputStep.OUTPUT_FILE_FORMATS).split(",");
        //We sort the list so we shouldn't care about the order of the formats in the list
        Arrays.sort(expectedOutputFormats);
        Arrays.sort(actualOutputFormats);
        assertArrayEquals(expectedOutputFormats,actualOutputFormats);
        //Run the test for an empty parameter
        outputFormats = null;
        master.processOutputFormats(params, outputFormats);
        actualOutputFormats = params.get(WriteOutputStep.OUTPUT_FILE_FORMATS).split(",");
        expectedOutputFormats = new String[]{"tsv", "xml", "gff3"};
        Arrays.sort(expectedOutputFormats);
        Arrays.sort(actualOutputFormats);
        assertArrayEquals(expectedOutputFormats, actualOutputFormats);
        outputFormats = new String[]{};
        master.processOutputFormats(params, outputFormats);
        Arrays.sort(expectedOutputFormats);
        Arrays.sort(actualOutputFormats);
        assertArrayEquals(expectedOutputFormats, actualOutputFormats);

    }

    @Test
    public void processOutputFormatsForNucleicAcidsTest() {
        //Set up the master
        master = new StandaloneBlackBoxMaster(new DefaultMessageListenerContainer());
        master.setSequenceType("n");
        //Run the test
        Map<String, String> params = new HashMap<String, String>();
        String[] outputFormats = new String[]{"gff3"};
        master.processOutputFormats(params, outputFormats);
        assertEquals("gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        //Run the test for an empty parameter
        outputFormats = null;
        master.processOutputFormats(params, outputFormats);
        assertEquals("tsv,xml,gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        outputFormats = new String[]{};
        master.processOutputFormats(params, outputFormats);
        assertEquals("tsv,xml,gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
    }
}
