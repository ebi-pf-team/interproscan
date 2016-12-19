package uk.ac.ebi.interpro.scan.management.model.implementations.gene3d;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.gene3d.Gene3DPostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 08/09/11
 * Time: 14:10
 */
public class Gene3DPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(Gene3DPostProcessingStep.class.getName());

    private String ssfOutputFileTemplate;

    private RawMatchDAO<Gene3dHmmer3RawMatch> rawMatchDAO;

    private FilteredMatchDAO<Gene3dHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;

    private String signatureLibraryRelease;

    private Gene3DPostProcessing postProcessor;

    @Required
    public void setSsfOutputFileTemplate(String ssfOutputFileTemplate) {
        this.ssfOutputFileTemplate = ssfOutputFileTemplate;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<Gene3dHmmer3RawMatch, Hmmer3Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
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
    public void setPostProcessor(Gene3DPostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    public String getSsfOutputFileTemplate() {
        return ssfOutputFileTemplate;
    }

    public FilteredMatchDAO<Gene3dHmmer3RawMatch, Hmmer3Match> getFilteredMatchDAO() {
        return filteredMatchDAO;
    }

    public RawMatchDAO<Gene3dHmmer3RawMatch> getRawMatchDAO() {
        return rawMatchDAO;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public Gene3DPostProcessing getPostProcessor() {
        return postProcessor;
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
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, this.getSsfOutputFileTemplate());
        // Get raw matches
        final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins = this.getRawMatchDAO().getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                getSignatureLibraryRelease()
        );
        // Check we have correct data
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("In execute() method of Gene3dHmmer3FilterStep.java (Gene3D Post Processing.)");
            LOGGER.debug("DAO returned " + rawProteins.size() + " raw proteins to filter.");
        }
        Utilities.verboseLog("PostProcess Gene3d matches: protein-range : "
                + stepInstance.getBottomProtein() + " - " + stepInstance.getTopProtein());
        // Filter and Persist
        filteredMatchDAO.persist(this.getPostProcessor().filter(rawProteins, outputFilePath));
    }
}
