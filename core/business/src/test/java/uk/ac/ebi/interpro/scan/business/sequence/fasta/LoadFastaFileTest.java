package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.IOException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 *
 * @author Phil Jones
 * @author Gift Nuka
 *         Date: 14-Nov-2009
 *         Time: 15:01:29
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class LoadFastaFileTest {

    private static final Logger LOGGER = LogManager.getLogger(LoadFastaFileTest.class.getName());

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

       /* StepCreationSequenceLoadListener sequenceLoadListener =
                new StepCreationSequenceLoadListener(analysisJobs, completionJob, prepareOutputJob, matchLookupJob, finalInitialJob, initialSetupSteps, stepInstance.getParameters());
        sequenceLoadListener.setStepInstanceDAO(stepInstanceDAO);

        loader.loadSequences(fastaFile.getInputStream(),
                (bottomNewSequenceId, topNewSequenceId, bottomPrecalculatedSequenceId, topPrecalculatedSequenceId) -> {
                    LOGGER.debug("Loaded New:" + bottomNewSequenceId + "-" + topNewSequenceId);
                    LOGGER.debug("Loaded New:" + bottomPrecalculatedSequenceId + "-" + topPrecalculatedSequenceId);

                }, null, false);
        */

        loader.loadSequences(fastaFile.getInputStream(),
                new SequenceLoadListener() {
                    @Override
                    public void sequencesLoaded(Long bottomNewSequenceId, Long topNewSequenceId, Long bottomPrecalculatedSequenceId, Long topPrecalculatedSequenceId, boolean useMatchLookupService, List<Long> idsWithoutLookupHit) {
                        LOGGER.debug("Loaded New:" + bottomNewSequenceId + "-" + topNewSequenceId);
                        LOGGER.debug("Loaded New:" + bottomPrecalculatedSequenceId + "-" + topPrecalculatedSequenceId);

                    }
                }, null, false);

        LOGGER.debug("Proteins loaded: " + proteinDAO.count());
    }
}
