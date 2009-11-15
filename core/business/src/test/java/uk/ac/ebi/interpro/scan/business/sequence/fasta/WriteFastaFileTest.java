package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.model.Protein;

import javax.annotation.Resource;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mumdad
 * Date: 15-Nov-2009
 * Time: 12:36:07
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WriteFastaFileTest {

    private WriteFastaFile writer;

    private ProteinDAO proteinDAO;

    @Resource(name="writer")
    public void setWriter(WriteFastaFile writer) {
        this.writer = writer;
    }

    @Resource (name="proteinDAO")
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Test
    public void testWriter() throws IOException, WriteFastaFile.FastaFileWritingException {
        List<Protein> proteinList = new ArrayList<Protein>();
        String[] proteins = {"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "ASFYSTAFCYDASFYCTDFSYTCFYSDTCFYDSCFYSDCFSDTYCVHEVSDHSDVCYDVS" +
                        "CYTSDVSFDEYSVDCSDYVCSDYCVSDCYYVSDCYHCVSDYCVSDYVCYSDV" +
                        "CYSDVCYVDCYVSDYCVYSDCSDYCVYSCDHGCTYVASEYWCDECDXCASHDE" +
                        "CXYSCVYSDVCTYVDSCSDTYCVYSCSDVCYVSDYCTTCXATSVABWJCFGDS" +
                        "JCKNKSDVBIDSCNSDGYCVDUSBCTYSDFCUISDBCTYDGCIKSDBCYSDGC" +
                        "IBDCUGISDCBUJSDCISDHNCIUGSDICHIDCGBUIDHCIDGOCHNSICDGS" +
                        "DJCBSDKCGYUSDLBCDKHJBSDCTYISDCBYISDHCBCDGUSDGVBCUSDG" +
                        "CVKYSDKCGHKSDCKJSDBVCUISDGHCBVKYIDGCBVGDCUGSBDVCIKGSD" +
                        "ICBVKSDICBKSDUCBSDUKCKHSDBCKYYDGC",
                "CDHASDJKLCHSDKLJCHSDCHYSDTCVYSDTVCTURSDYGCBIKYCIKUSDVCUJSDGYC" +
                        "VIKCUBCYIKGSDBCVKSDCSDC"};
        for (String sequence : proteins){
            Protein protein = new Protein(sequence);
            protein = proteinDAO.insert(protein);
            proteinList.add(protein);
        }

        // Try writing with the default sequence line length
        writer.writeFastaFile(proteinList, "target/default_line_length.fasta");
        for (int i = 5; i <= 30; i++){
            writer.setSequenceLineLength(i);
            String fileName = "target/lineLength" + i + ".fasta";
            File file = new File(fileName);
            // Make sure this works even if clean has not be run.
            if (file.exists()){
                file.delete();
            }
            writer.writeFastaFile(proteinList, fileName);
        }

    }

}
