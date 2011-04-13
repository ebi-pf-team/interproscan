package uk.ac.ebi.interpro.scan.management.model.implementations.test;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import javax.persistence.Transient;
import java.io.IOException;
import java.util.List;

/**
 * This Step simply writes out all raw matches to the system.out (e.g. command line).
 * Useful for developer testing, but does not add any functionality so best not to use in production!
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class TestRawDataStoredStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(TestRawDataStoredStep.class.getName());

    GenericDAO<RawMatch, Long> rawMatchDAO;

    @Required
    public void setRawMatchDAO(GenericDAO<RawMatch, Long> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        List<RawMatch> rawMatches = rawMatchDAO.retrieveAll();

        System.out.println("\n****** START: RAW MATCHES ******");
        if (rawMatches.size() < 1) {
            System.out.println("None");
        }
        else {
            for (RawMatch rawMatch : rawMatches) {
                System.out.println(rawMatch);
            }
        }
        System.out.println(rawMatches.size() + " raw matches");
        System.out.println("****** END: RAW MATCHES ******\n");
    }
}

