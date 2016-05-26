package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.IOException;


/**
 * Created by IntelliJ IDEA.
 *
 * @author Phil Jones
 *         Date: 14-Nov-2009
 *         Time: 15:01:29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LoadFastaFileTest {

    private static final Logger LOGGER = Logger.getLogger(LoadFastaFileTest.class.getName());

    private LoadFastaFile loader;

    private org.springframework.core.io.Resource fastaFile;

    private ProteinDAO proteinDAO;

    @javax.annotation.Resource(name = "loader")
    public void setLoader(LoadFastaFile loader) {
        this.loader = loader;
    }

    @javax.annotation.Resource(name = "fastaFile")
    public void setFastaFile(org.springframework.core.io.Resource fastaFile) {
        this.fastaFile = fastaFile;
    }

    @javax.annotation.Resource(name = "proteinDAO")
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Test
    public void testLoader() throws IOException {
        LOGGER.debug("Loader:" + loader);
        LOGGER.debug("FastaFile: " + fastaFile);
        loader.loadSequences(fastaFile.getInputStream(), (bottomNewSequenceId, topNewSequenceId, bottomPrecalculatedSequenceId, topPrecalculatedSequenceId) -> {
            LOGGER.debug("Loaded New:" + bottomNewSequenceId + "-" + topNewSequenceId);
            LOGGER.debug("Loaded New:" + bottomPrecalculatedSequenceId + "-" + topPrecalculatedSequenceId);

        }, null, false);
        LOGGER.debug("Proteins loaded: " + proteinDAO.count());
    }
}
