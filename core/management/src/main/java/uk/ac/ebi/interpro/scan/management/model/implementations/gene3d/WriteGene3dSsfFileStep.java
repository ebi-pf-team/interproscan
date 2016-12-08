package uk.ac.ebi.interpro.scan.management.model.implementations.gene3d;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.gene3d.SsfFileWriter;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 08/09/11
 * Time: 14:10
 */
public class WriteGene3dSsfFileStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(WriteGene3dSsfFileStep.class.getName());

    private String ssfInputFileTemplate;

    private RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDAO;

    private String signatureLibraryRelease;

    private SsfFileWriter ssfFileWriter;

    @Required
    public void setSsfInputFileTemplate(String ssfInputFileTemplate) {
        this.ssfInputFileTemplate = ssfInputFileTemplate;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setSsfFileWriter(SsfFileWriter ssfFileWriter) {
        this.ssfFileWriter = ssfFileWriter;
    }

    public String getSsfInputFileTemplate() {
        return ssfInputFileTemplate;
    }

    public RawMatchDAO<Gene3dHmmer3RawMatch> getRawMatchDAO() {
        return rawMatchDAO;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public SsfFileWriter getSsfFileWriter() {
        return ssfFileWriter;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method <b>MUST</b> throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     * <p/>
     * Implementations of this method MAY call <code>this.delayForNfs()</code> before starting, if, for example,
     * they are operating on file system resources.
     * <p/>
     * <h2>Notes:</h2>
     * <p/>
     * <p>The StepInstance parameter that is passed in provides the following useful methods that you may need to use
     * in your implementation:
     * <p/>
     * <p><code>stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate)</code>
     * <p>should be used to ensure that temporary files are written to the appropriate location, with file names
     * filtered for the range of proteins / models being analysed.  Note that the parameter to this method
     * temporaryFileDirectory is also passed in to executions of this method.
     * <p/>
     * <p>To determine the range of proteins or models being analysed, call any of:
     * <p/>
     * <ul>
     * <li><code>stepInstance.getBottomProtein()</code></li>
     * <li><code>stepInstance.getTopProtein()</code></li>
     * <li><code>stepInstance.getBottomModel()</code></li>
     * <li><code>stepInstance.getTopModel()</code></li>
     * </ul>
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     *                               above.
     * @param temporaryFileDirectory which can be passed into the
     *                               stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     *                               to build temporary file paths.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        final String inputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getSsfInputFileTemplate());

        int count = 0;
        int waitTimeFactor = 3;
        Long proteinCount = stepInstance.getTopProtein() - stepInstance.getBottomProtein();
        Long now = System.currentTimeMillis();
        Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = this.getRawMatchDAO().getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                getSignatureLibraryRelease()
        );
        if(rawProteins.size() > 0) {
            for (RawProtein<Gene3dHmmer3RawMatch> rawProtein : rawProteins) {
                count += rawProtein.getMatches().size();
            }
        }
        Long timeTaken = System.currentTimeMillis() - now;
        int retryCount = 0;
        Long allowedWaitTime = Long.valueOf(waitTimeFactor)  * 10 * 1000;
        long chunkSize =  stepInstance.getTopProtein() - stepInstance.getBottomProtein();
        while (count == 0 && chunkSize > 10) {
            retryCount ++;
            int matchesFound = 0;
            int countForWaitTime = proteinCount.intValue() * 200;
            waitTimeFactor = Utilities.getWaitTimeFactor(countForWaitTime).intValue();
            Utilities.sleep(waitTimeFactor * 1000);
            // Get raw matches
            rawProteins = this.getRawMatchDAO().getProteinsByIdRange(
                    stepInstance.getBottomProtein(),
                    stepInstance.getTopProtein(),
                    getSignatureLibraryRelease()
            );
            if(rawProteins.size() > 0) {
                for (RawProtein<Gene3dHmmer3RawMatch> rawProtein : rawProteins) {
                    count += rawProtein.getMatches().size();
                }
            }
            if (count == 0){
                String matchPersistWarning = "Possible db problem: failed to  get Gene3d matches for the domain finder:  proteins ("
                        + rawProteins.size()
                        + ") protein-range : " + stepInstance.getBottomProtein() + " - "
                        + stepInstance.getTopProtein()
                        + " library : " + getSignatureLibraryRelease()
                        + " matchesCount: " + count;
                if (retryCount == 1) {
                    LOGGER.debug(matchPersistWarning);
                }
                timeTaken = System.currentTimeMillis() - now;
                //we try three times then break
                if (chunkSize < 100 || timeTaken > allowedWaitTime || retryCount > 3) {
                    LOGGER.warn(matchPersistWarning);
                    Utilities.verboseLog(matchPersistWarning);
                    break;
                }
            }
        }

        Utilities.verboseLog("Raw proteins: " + rawProteins.size() + ", matches: " + count + ", timeTaken: " + timeTaken);

        // Check we have correct data
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("In execute() method of Gene3dHmmer3FilterStep.java (Gene3D Post Processing.)");
            LOGGER.debug("DAO returned " + rawProteins.size() + " raw proteins to filter.");
        }
        this.getSsfFileWriter().writeSsfFile(rawProteins, inputFilePath);
    }
}
