package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.FastaFileWriter;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfMatchTempParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.persistence.Transient;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private final FastaFileWriter fastaFileWriter = new FastaFileWriter();

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
        final String blastMatchesFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, blastMatchesFileName);
        Set<RawProtein<PIRSFHmmer2RawMatch>> rawMatches;
        try {
            rawMatches = PirsfMatchTempParser.parse(blastMatchesFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when parsing blast matches file " + blastMatchesFilePath);
        }

        // Build a list of protein Ids which will need to be included in the FASTA file to send to Blast
        Set<Long> proteinIds = new HashSet<Long>();
        Iterator<RawProtein<PIRSFHmmer2RawMatch>> i = rawMatches.iterator();
        RawProtein<PIRSFHmmer2RawMatch> rawProtein = null;
        Long proteinId;
        while (i.hasNext()) {
            rawProtein = i.next();
            proteinId = Long.parseLong(rawProtein.getProteinIdentifier());
            proteinIds.add(proteinId);
        }

        // Write FASTA file as output, ready for BLAST
        String fastaFilePathName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, fastaFilePathTemplate);
        List<Protein> proteins = proteinDAO.getProteinsByIds(proteinIds);
        try {
            fastaFileWriter.writeFastaFile(proteins, fastaFilePathName);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        } catch (FastaFileWriter.FastaFileWritingException e) {
            throw new IllegalStateException("FastaFileWriter.FastaFileWritingException thrown when attempting to write a fasta file to " + fastaFilePathName, e);
        }
    }
}

