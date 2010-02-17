package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.Hmmer3SearchMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.PfamHmmer3RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This class parses and persists the output from HMMER 3 for Pfam A
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParsePfam_A_HMMER3OutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePfam_A_HMMER3OutputStep.class);

    private String fullPathToHmmFile;

    private String hmmerOutputFilePathTemplate;

    private Hmmer3SearchMatchParser<PfamHmmer3RawMatch> parser;

    private PfamHmmer3RawMatchDAO pfamRawMatchDAO;

    public Hmmer3SearchMatchParser<PfamHmmer3RawMatch> getParser() {
        return parser;
    }

    public void setParser(Hmmer3SearchMatchParser<PfamHmmer3RawMatch> parser) {
        this.parser = parser;
    }

    public String getFullPathToHmmFile() {
        return fullPathToHmmFile;
    }

    @Required
    public void setFullPathToHmmFile(String fullPathToHmmFile) {
        this.fullPathToHmmFile = fullPathToHmmFile;
    }

    public String getHmmerOutputFilePathTemplate() {
        return hmmerOutputFilePathTemplate;
    }

    @Required
    public void setHmmerOutputFilePathTemplate(String hmmerOutputFilePathTemplate) {
        this.hmmerOutputFilePathTemplate = hmmerOutputFilePathTemplate;
    }

    @Required
    public void setPfamRawMatchDAO(PfamHmmer3RawMatchDAO pfamRawMatchDAO) {
        this.pfamRawMatchDAO = pfamRawMatchDAO;
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     *
     * @param stepExecution record of execution
     */
    @Override
    public void execute(StepExecution stepExecution) {
        final StepInstance stepInstance = stepExecution.getStepInstance();
        LOGGER.debug("Running Parser HMMER3 Output Step for proteins " + stepInstance.getBottomProtein() + " to " + stepInstance.getTopProtein());
        stepExecution.setToRun();
        InputStream is = null;
        try{
            Thread.sleep(2000);  // Have a snooze to allow NFS to catch up.
            final String hmmerOutputFilePath = stepInstance.filterFileNameProteinBounds(this.getHmmerOutputFilePathTemplate());
            is = new FileInputStream(hmmerOutputFilePath);
            final Hmmer3SearchMatchParser<PfamHmmer3RawMatch> parser = this.getParser();
            final Set<RawProtein<PfamHmmer3RawMatch>> parsedResults = parser.parse(is);
            pfamRawMatchDAO.insertProteinMatches(parsedResults);
            stepExecution.completeSuccessfully();
        } catch (Exception e) {
            stepExecution.fail();
            LOGGER.error("Doh.  Hmmer output parsing failed.", e);
        }
        finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error ("Duh - parsed OK, but can't close the input stream?" , e);
                }
            }
        }
    }
}
