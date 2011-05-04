package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;
import uk.ac.ebi.interpro.scan.io.pirsf.BlastMatchesFileParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.annotation.Resource;
import javax.persistence.Transient;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This Step write Fasta files for the range of proteins requested.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileForBlastStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteFastaFileForBlastStep.class.getName());

    private String blastMatchesFileName;

    @Transient
    private final WriteFastaFile fastaFile = new WriteFastaFile();

    private String fastaFilePathTemplate;

    private ProteinDAO proteinDAO;

    @Required
    public void setBlastMatchesFileName(String blastMatchesFileName) {
        this.blastMatchesFileName = blastMatchesFileName;
    }

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        // Read in raw matches that need to be blasted
        String blastInputFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastMatchesFileName);
        Map<Long, String> proteinIdMap = null;
        try {
            proteinIdMap = BlastMatchesFileParser.parse(blastInputFilePathName);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing blast matches file " + blastInputFilePathName);
        }

        // Write FASTA file as output, ready for BLAST
        String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFilePathTemplate);
        List<Protein> proteins = proteinDAO.getProteinsByIds(proteinIdMap.keySet());
        try {
            fastaFile.writeFastaFile(proteins, fastaFilePathName);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        } catch (WriteFastaFile.FastaFileWritingException e) {
            throw new IllegalStateException("WriteFastaFile.FastaFileWritingException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        }
    }
}

