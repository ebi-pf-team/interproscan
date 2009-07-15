package uk.ac.ebi.interpro.scan.batch.item;

import static org.junit.Assert.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * Test generation of FASTA files from {@link Protein} instances.
 *
 * @author  Antony Quinn
 * @version $Id: FastaFileItemWriterTest.java,v 1.1 2009/06/18 10:53:08 aquinn Exp $
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FastaFileItemWriterTest {

    private final String[] SEQUENCES_1 = {
            "VAVFFGGLSIKKDEEVLKKNCPHIVVGTPGRILALARNKSLNLKHIKHFILDECDKMLEQLDMRRDVQEIFRMTPHEKQV",
            "MMFSATLSKEIRPVCRKFMQDPMEIFVDDETKLTLHGLQQYYVKLKDNEKNRKLFDLLDVLEFNQVVIFVKSVQRCIALA",
            "QLLVEQNFPAIAIHRGMPQEERLSRYQQFKDFQRRILVATNLFGRGMDIERVNIAFNYDMPEDSDTYLHRVARAGRFGTK",
            "MNAPLGGIWLWLPLLLTWLTPEVNSSWWYMRATGGSSRVMCDNVPGLVSSQRQLCHRHPDVMRAISQGVAEWTAECQHQF",
            "RQHRWNCNTLDRDHSLFGRVLLRSSRESAFVYAISSAGVVFAITRACSQGEVKSCSCDPKKMGSAKDSKGIFDWGGCSDN",
            "IDYGIKFARAFVDAKERKGKDARALMNLHNNRAGRKAVKRFLKQECKCHGVSGSCTLRTCWLAMADFRKTGDYLWRKYNG"
            };

    private final String[] SEQUENCES_2 = {
            "MAMFFHHLSIKKDEEMLKKNCPHIMMHTPHRILALARNKSLNLKHIKHFILDECDKMLEQLDMRRDMQEIFRMTPHEKQM",
            "MMFSATLSKEIRPMCRKFMQDPMEIFMDDETKLTLHHLQQYYMKLKDNEKNRKLFDLLDMLEFNQMMIFMKSMQRCIALA",
            "QLLMEQNFPAIAIHRHMPQEERLSRYQQFKDFQRRILMATNLFHRHMDIERMNIAFNYDMPEDSDTYLHRMARAHRFHTK",
            "MNAPLHHIWLWLPLLLTWLTPEMNSSWWYMRATHHSSRMMCDNMPHLMSSQRQLCHRHPDMMRAISQHMAEWTAECQHQF",
            "RQHRWNCNTLDRDHSLFHRMLLRSSRESAFMYAISSAHMMFAITRACSQHEMKSCSCDPKKMHSAKDSKHIFDWHHCSDN",
            "IDYHIKFARAFMDAKERKHKDARALMNLHNNRAHRKAMKRFLKQECKCHHMSHSCTLRTCWLAMADFRKTHDYLWRKYNH"
            };    

    @Autowired
    @Qualifier("singleWriter")
    private FlatFileItemWriter<Protein> singleWriter;
       
    @Autowired
    @Qualifier("singleResource")
    private Resource singleResource;

    @Autowired
    @Qualifier("multiWriter")
    private MultiResourceItemWriter<Protein> multiWriter;

    @Autowired
    @Qualifier("multiResource")
    private Resource multiResource;

    private List<Protein> proteins1;
    private List<Protein> proteins2;

    @Before public void setUp() {
        proteins1 = getProteins(SEQUENCES_1);
        proteins2 = getProteins(SEQUENCES_2);
    }

    @Test public void testSingleWrite() throws Exception {
        singleWriter.open(new ExecutionContext());
        singleWriter.write(proteins1);
        singleWriter.close();
        assertExists(singleResource);
        assertLineCount(proteins1, singleResource);
        // TODO: Check contents of file using org.springframework.batch.test.AssertFile.assertFileEquals
    }

    @Test public void testMutliWrite() throws Exception {
        multiWriter.open(new ExecutionContext());
        // Simulate tasklet.chunk=sequences.length (eg. commit every 6 reads)
        multiWriter.write(proteins1);
        multiWriter.write(proteins2);
        multiWriter.close();
        assertExists(multiResource, ".1");
        assertExists(multiResource, ".2");
        assertLineCount(proteins1, addPrefix(multiResource, ".1"));
        assertLineCount(proteins2, addPrefix(multiResource, ".2"));
        // TODO: Check contents of file using org.springframework.batch.test.AssertFile.assertFileEquals
    }

    private static void assertLineCount(List<Protein> proteins, Resource resource) throws Exception  {
        // TODO: Change expected value if we wrap sequence 
        int expected = proteins.size() * 2;  // MD5 + sequence
        AssertFile.assertLineCount(expected, resource);
    }

    private static Resource addPrefix(Resource resource, String prefix) throws IOException {
        return new UrlResource(resource.getURL() + prefix);
    }
    
    private static void assertExists(Resource resource, String prefix) throws IOException {
        assertExists(addPrefix(resource, prefix));
    }

    private static void assertExists(Resource resource) throws IOException {
        assertTrue(resource.getURI() +  " does not exist", resource.exists());
    }
    
    private List<Protein> getProteins(String[] sequences) {
        List<Protein> list = new ArrayList<Protein>();
        for (String sequence : sequences)   {
            list.add(new Protein(sequence));
        }
        return list;
    }

}
