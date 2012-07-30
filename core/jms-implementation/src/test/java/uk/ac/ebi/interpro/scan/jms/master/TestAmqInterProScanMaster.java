package uk.ac.ebi.interpro.scan.jms.master;

import org.junit.Test;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;

import java.util.HashMap;
import java.util.Map;

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
        master = new StandaloneBlackBoxMaster();
        master.setSequenceType("p");
        //Run the test
        Map<String, String> params = new HashMap<String, String>();
        String[] outputFormats = new String[]{"tsv", "html", "gff3"};
        master.processOutputFormats(params, outputFormats);
        assertEquals("tsv,html,gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        //Run the test for an empty parameter
        outputFormats = null;
        master.processOutputFormats(params, outputFormats);
        assertEquals("tsv,xml,gff3,html", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        outputFormats = new String[]{};
        master.processOutputFormats(params, outputFormats);
        assertEquals("tsv,xml,gff3,html", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
    }

    @Test
    public void processOutputFormatsForNucleicAcidsTest() {
        //Set up the master
        master = new StandaloneBlackBoxMaster();
        master.setSequenceType("n");
        //Run the test
        Map<String, String> params = new HashMap<String, String>();
        String[] outputFormats = new String[]{"gff3"};
        master.processOutputFormats(params, outputFormats);
        assertEquals("gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        //Run the test for an empty parameter
        outputFormats = null;
        master.processOutputFormats(params, outputFormats);
        assertEquals("xml,gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
        outputFormats = new String[]{};
        master.processOutputFormats(params, outputFormats);
        assertEquals("xml,gff3", params.get(WriteOutputStep.OUTPUT_FILE_FORMATS));
    }
}
