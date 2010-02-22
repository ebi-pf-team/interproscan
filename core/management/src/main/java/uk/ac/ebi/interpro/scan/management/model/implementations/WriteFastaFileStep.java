package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.persistence.Transient;
import java.io.IOException;
import java.util.List;

/**
 * This Step write Fasta files for the range of proteins requested.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteFastaFileStep.class);

    @Transient
    private final WriteFastaFile fastaFile = new WriteFastaFile();

    private String fastaFilePathTemplate;

    private ProteinDAO proteinDAO;

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    public String getFastaFilePathTemplate() {
        return fastaFilePathTemplate;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance containing the parameters for executing.
     */
    @Override
    public void execute(StepInstance stepInstance) throws IOException, WriteFastaFile.FastaFileWritingException, InterruptedException {
        String fastaFilePathName = stepInstance.filterFileNameProteinBounds(
                this.getFastaFilePathTemplate()
        );
        List<Protein> proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
        fastaFile.writeFastaFile(proteins, fastaFilePathName);
        Thread.sleep(2000); // Have a snooze to allow NFS to catch up.
    }
}

