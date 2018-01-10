package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.phobius.PhobiusMatchParser;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.PhobiusMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.persistence.PhobiusFilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.SimpleProteinFilteredMatchKVDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

/**
 * Parses the output of phobius and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class ParsePhobiusOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePhobiusOutputStep.class.getName());

    private String phobiusOutputFileNameTemplate;

    private PhobiusFilteredMatchDAO phobiusMatchDAO;

    private SimpleProteinFilteredMatchKVDAO<PhobiusProtein, PhobiusMatch>  phobiusMatchKVDAO;

    private PhobiusMatchParser parser;

    @Required
    public void setPhobiusOutputFileNameTemplate(String phobiusOutputFileNameTemplate) {
        this.phobiusOutputFileNameTemplate = phobiusOutputFileNameTemplate;
    }

    @Required
    public void setPhobiusMatchDAO(PhobiusFilteredMatchDAO phobiusMatchDAO) {
        this.phobiusMatchDAO = phobiusMatchDAO;
    }

    public void setPhobiusMatchKVDAO(SimpleProteinFilteredMatchKVDAO<PhobiusProtein, PhobiusMatch> phobiusMatchKVDAO) {
        this.phobiusMatchKVDAO = phobiusMatchKVDAO;
    }

    @Required
    public void setParser(PhobiusMatchParser parser) {
        this.parser = parser;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory being the directory in which the raw file is being stored.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        delayForNfs();
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, phobiusOutputFileNameTemplate);
        InputStream is = null;
        PhobiusFeature represantiveRawMatch = null;
        int count = 0;
        try {
            is = new FileInputStream(fileName);
            Set<PhobiusProtein> phobiusProteins = parser.parse(is, fileName);
            for (PhobiusProtein phobiusProtein : phobiusProteins) {
                if (phobiusProtein.getFeatures().size() > 0) {
                    count += phobiusProtein.getFeatures().size();
                    if (represantiveRawMatch == null) {
                        PhobiusFeature feature = phobiusProtein.getFeatures().iterator().next();
                        represantiveRawMatch = feature;
                    }
//                Set<PhobiusMatch.PhobiusLocation> locations = Collections.singleton(
//                        new PhobiusMatch.PhobiusLocation(feature.getStart(), feature.getStop())
//                );
//                PhobiusMatch match = new PhobiusMatch(signature, locations);
                }
            }
            phobiusMatchKVDAO.persist(phobiusProteins);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0) {
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                } else {
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Phobius file " + fileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Phobius output file located at " + fileName, e);
                    throw new IllegalStateException("IOException thrown when attempting to close the InputStream from the Phobius output file.", e);
                }
            }
        }
    }
}
