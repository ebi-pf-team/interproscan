package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.PfamHMMER3PostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.PfamHmmer3RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class performs post-processing (including data persistence)
 * for Pfam A.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Pfam_A_PostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(Pfam_A_PostProcessingStep.class.getName());

    private PfamHMMER3PostProcessing postProcessor;

    private SignatureLibrary signatureLibrary;

    private String signatureLibraryRelease;

    private PfamHmmer3RawMatchDAO rawMatchDAO;

    private FilteredMatchDAO filteredMatchDAO;

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public PfamHMMER3PostProcessing getPostProcessor() {
        return postProcessor;
    }

    @Required
    public void setPostProcessor(PfamHMMER3PostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(PfamHmmer3RawMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     * @throws Exception could be anything thrown by the execute method.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        // Retrieve raw results for protein range.
        Map<String, RawProtein<PfamHmmer3RawMatch>> rawMatches = rawMatchDAO.getRawMatchesForProteinIdsInRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                getSignatureLibraryRelease()
        );
        Utilities.verboseLog(10, "Pfam_A_PostProcessingStep : stepinstance:" + stepInstance.toString());
        if(rawMatches.size() == 0){
            Long sequenceCout = stepInstance.getTopProtein() - stepInstance.getBottomProtein();
            Utilities.verboseLog(10, "Zero matches found: on " + sequenceCout + " proteins stepinstance:" + stepInstance.toString());
            int waitTimeFactor = 2;
            if (! Utilities.isRunningInSingleSeqMode()){
                waitTimeFactor = Utilities.getWaitTimeFactorLogE(10 * sequenceCout.intValue()).intValue();
            }
            Utilities.sleep(waitTimeFactor * 1000);
            //try again
            rawMatches = rawMatchDAO.getRawMatchesForProteinIdsInRange(
                    stepInstance.getBottomProtein(),
                    stepInstance.getTopProtein(),
                    getSignatureLibraryRelease()
            );
            Utilities.verboseLog(10, "matches after waitTimeFactor: " + waitTimeFactor + " - " + rawMatches.size());
        }
        int matchCount = 0;
        for (final RawProtein rawProtein : rawMatches.values()) {
            matchCount += rawProtein.getMatches().size();
        }
        Utilities.verboseLog(10, " PfamA: Retrieved " + rawMatches.size() + " proteins to post-process.");
        Utilities.verboseLog(10, " PfamA: A total of " + matchCount + " raw matches.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("PfamA: Retrieved " + rawMatches.size() + " proteins to post-process.");
            //int matchCount = 0;
            //for (final RawProtein rawProtein : rawMatches.values()) {
            //   matchCount += rawProtein.getMatches().size();
            //}
            LOGGER.debug("PfamA: A total of " + matchCount + " raw matches.");
        }

        // Post process
        try {
            Map<String, RawProtein<PfamHmmer3RawMatch>> filteredMatches = getPostProcessor().process(rawMatches);

            matchCount = 0;
            for (final RawProtein rawProtein : filteredMatches.values()) {
                matchCount += rawProtein.getMatches().size();
            }
            Utilities.verboseLog(10,  " PfamA: " + filteredMatches.size() + " proteins passed through post processing.");
            Utilities.verboseLog(10,  " PfamA: A total of " + matchCount + " matches PASSED.");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PfamA: " + filteredMatches.size() + " proteins passed through post processing.");
                //int matchCount = 0;
                //for (final RawProtein rawProtein : filteredMatches.values()) {
                //    matchCount += rawProtein.getMatches().size();
                //}
                LOGGER.debug("PfamA: A total of " + matchCount + " matches PASSED.");
            }
            filteredMatchDAO.persist(filteredMatches.values());
            Utilities.verboseLog(10,  " PfamA: filteredMatches persisted");
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered matches.", e);
        }
    }

    private int countMatches(Map<String, RawProtein<PfamHmmer3RawMatch>> matches) {
        int count = 0;
        for (RawProtein<PfamHmmer3RawMatch> protein : matches.values()) {
            if (protein.getMatches() != null) count += protein.getMatches().size();
        }
        return count;
    }

}
