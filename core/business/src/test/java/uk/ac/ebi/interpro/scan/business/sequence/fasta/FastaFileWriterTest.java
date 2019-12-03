package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 15-Nov-2009
 * Time: 12:36:07
 *
 * @author Phil Jones
 * @author Gift Nuka
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class FastaFileWriterTest {

    private FastaFileWriter writer;

    private ProteinDAO proteinDAO;

    @Resource(name = "writer")
    public void setWriter(FastaFileWriter writer) {
        this.writer = writer;
    }

    @Resource(name = "proteinDAO")
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Test
    //@Ignore("Need to fix as sequences are now validated as amino acid.")
    public void testWriter() throws IOException, FastaFileWriter.FastaFileWritingException {
        List<Protein> proteinList = new ArrayList<Protein>();
        String[] proteins = {"ABCDEFGHIKLMNPQRSTUVWXYZ",
                "ASFYSTAFCYDASFYCTDFSYTCFYSDTCFYDSCFYSDCFSDTYCVHEVSDHSDVCYDVS" +
                        "CYTSDVSFDEYSVDCSDYVCSDYCVSDCYYVSDCYHCVSDYCVSDYVCYSDV" +
                        "CYSDVCYVDCYVSDYCVYSDCSDYCVYSCDHGCTYVASEYWCDECDXCASHDE" +
                        "CXYSCVYSDVCTYVDSCSDTYCVYSCSDVCYVSDYCTTCXATSVABWCFGDS" +
                        "CKNKSDVBIDSCNSDGYCVDUSBCTYSDFCUISDBCTYDGCIKSDBCYSDGC" +
                        "IBDCUGISDCBUSDCISDHNCIUGSDICHIDCGBUIDHCIDGCHNSICDGS" +
                        "DCBSDKCGYUSDLBCDKBSDCTYISDCBYISDHCBCDGUSDGVBCUSDG" +
                        "CVKYSDKCGHKSDCKSDBVCUISDGHCBVKYIDGCBVGDCUGSBDVCIKGSD" +
                        "ICBVKSDICBKSDUCBSDUKCKHSDBCKYYDGC",
                "CDHASDKLCHSDKLCHSDCHYSDTCVYSDTVCTURSDYGCBIKYCIKUSDVCUSDGYC" +
                        "VIKCUBCYIKGSDBCVKSDCSDC"};
        for (String sequence : proteins) {
            Protein protein = new Protein(sequence);
            protein = proteinDAO.insert(protein);
            proteinList.add(protein);
        }

        // Try writing with the default sequence line length
        writer.writeFastaFile(proteinList, "target/default_line_length.fasta");
        for (int i = 5; i <= 30; i++) {
            writer.setSequenceLineLength(i);
            String fileName = "target/lineLength" + i + ".fasta";
            File file = new File(fileName);
            // Make sure this works even if clean has not be run.
            if (file.exists()) {
                file.delete();
            }
            writer.writeFastaFile(proteinList, fileName);
        }

    }

}
