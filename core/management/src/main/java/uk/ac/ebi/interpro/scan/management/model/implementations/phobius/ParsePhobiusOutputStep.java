package uk.ac.ebi.interpro.scan.management.model.implementations.phobius;

import uk.ac.ebi.interpro.scan.management.model.implementations.ParseAndPersistStep;
import uk.ac.ebi.interpro.scan.model.PhobiusMatch;
import uk.ac.ebi.interpro.scan.model.raw.PhobiusRawMatch;

/**
 * Parses the output of phobius and stores the results to the database
 * (as filtered results - there is currently no second filtering step.)
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class ParsePhobiusOutputStep extends ParseAndPersistStep<PhobiusRawMatch, PhobiusMatch> {

}

/*

    private static final Logger LOGGER = LogManager.getLogger(ParsePhobiusOutputStep.class.getName());

    private String phobiusOutputFileNameTemplate;

    private PhobiusFilteredMatchDAO phobiusMatchDAO;

    private PhobiusMatchParser parser;

    @Required
    public void setPhobiusOutputFileNameTemplate(String phobiusOutputFileNameTemplate) {
        this.phobiusOutputFileNameTemplate = phobiusOutputFileNameTemplate;
    }

    @Required
    public void setPhobiusMatchDAO(PhobiusFilteredMatchDAO phobiusMatchDAO) {
        this.phobiusMatchDAO = phobiusMatchDAO;
    }

    @Required
    public void setParser(PhobiusMatchParser parser) {
        this.parser = parser;
    }

    **/


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

    /**
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
            phobiusMatchDAO.persist(phobiusProteins);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0) {
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog(1100, "represantiveRawMatch :" + represantiveRawMatch.toString());
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                } else {
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog(1100, "Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog(1100, "ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
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


     */
