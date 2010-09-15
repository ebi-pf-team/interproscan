package uk.ac.ebi.interpro.scan.management.model.implementations.tigrfam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.HmmPfamParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TigrFamRawMatch;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This class parses and persists the output from hmmpfam for TIGRFAM
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseTigrFamHmmpfamOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseTigrFamHmmpfamOutputStep.class.getName());

    private String hmmerOutputFilePathTemplate;

    private HmmPfamParser<TigrFamRawMatch> parser = new HmmPfamParser<TigrFamRawMatch>();

    private RawMatchDAO<TigrFamRawMatch> tigrfamRawMatchDAO;

    @Required
    public void setHmmerOutputFileNameTemplate(String hmmerOutputFilePathTemplate) {
        this.hmmerOutputFilePathTemplate = hmmerOutputFilePathTemplate;
    }

    @Required
    public void setTigrfamRawMatchDAO(RawMatchDAO<TigrFamRawMatch> tigrfamRawMatchDAO) {
        this.tigrfamRawMatchDAO = tigrfamRawMatchDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        LOGGER.debug("Running Parser HMMER3 Output Step for proteins " + stepInstance.getBottomProtein() + " to " + stepInstance.getTopProtein());
        InputStream is = null;
        try {
            final String hmmerOutputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, hmmerOutputFilePathTemplate);
            is = new FileInputStream(hmmerOutputFilePath);
            final Set<RawProtein<TigrFamRawMatch>> parsedResults = parser.parse(is);
            tigrfamRawMatchDAO.insertProteinMatches(parsedResults);
        }

        catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to read " + hmmerOutputFilePathTemplate, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Duh - parsed OK, but can't close the input stream?", e);
                }
            }
        }
    }
}
