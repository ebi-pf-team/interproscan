package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class BerkeleyToI5ModelDAOImpl implements BerkeleyToI5ModelDAO {

    private static final Logger LOGGER = Logger.getLogger(BerkeleyToI5ModelDAOImpl.class.getName());

    private Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter;

    protected EntityManager entityManager;

    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Required
    public void setSignatureLibraryToMatchConverter(Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter) {
        this.signatureLibraryToMatchConverter = signatureLibraryToMatchConverter;
    }

    /**
     * Method to store matches based upon lookup from the Berkeley match database of precalculated matches.
     *
     * @param nonPersistedProtein being a newly instantiated Protein object
     * @param berkeleyMatches     being a Set of BerkeleyMatch objects, retrieved / unmarshalled from
     *                            the Berkeley Match web service.
     */
    @Transactional(readOnly = true)
    public void populateProteinMatches(Protein nonPersistedProtein, List<BerkeleyMatch> berkeleyMatches, Map<String, SignatureLibraryRelease> analysisJobMap) {
        populateProteinMatches(Collections.singleton(nonPersistedProtein), berkeleyMatches,analysisJobMap);
    }

    @Transactional(readOnly = true)
    public void populateProteinMatches(Set<Protein> preCalculatedProteins, List<BerkeleyMatch> berkeleyMatches, Map<String, SignatureLibraryRelease> analysisJobMap) {
        String debugString = "";
        final Map<String, Protein> md5ToProteinMap = new HashMap<String, Protein>(preCalculatedProteins.size());
        // Populate the lookup map.
        for (Protein protein : preCalculatedProteins) {
            md5ToProteinMap.put(protein.getMd5().toUpperCase(), protein);
        }

        //the following was the problem:
        //analysisJobMap = new HashMap<String, SignatureLibraryRelease>();
        LOGGER.debug("analysisJobMap: " + analysisJobMap);
        //Mapping between SignatureLibrary and the version number, e.g key=PIRSF,value=2.84
        Map<SignatureLibrary, String> librariesToAnalyse = null;

        //Populate map with data
        if (analysisJobMap != null) {
            librariesToAnalyse = new HashMap<SignatureLibrary, String>();
            for (String analysisJobName : analysisJobMap.keySet()) {
                String analysisJob = null;
                String versionNumber = null;
                if (analysisJobName != null) {
                    analysisJob = analysisJobName;
                    versionNumber = analysisJobMap.get(analysisJobName).getVersion();
                    debugString = "Job: " + analysisJobName + " :- analysisJob: " + analysisJob + " versionNumber: " + versionNumber;
//                    Utilities.verboseLog(10, debugString);
                    LOGGER.debug(debugString);
                } else {
                    throw new IllegalStateException("Analysis job name is in an unexpected format: " + analysisJobName);
                }
                final SignatureLibrary matchingLibrary = SignatureLibraryLookup.lookupSignatureLibrary(analysisJobName);
                if (matchingLibrary != null) {
                    librariesToAnalyse.put(matchingLibrary, versionNumber);
                }
            }
        }
        //Debug
        StringBuilder jobsToAnalyse = new StringBuilder();
        for (String job: analysisJobMap.keySet()){
            jobsToAnalyse.append("job: " + job + " version: " + analysisJobMap.get(job).getVersion() + "\n");
        }
        LOGGER.debug("From analysisJobMap" + jobsToAnalyse);
        jobsToAnalyse = new StringBuilder();
        for (SignatureLibrary signatureLibrary: librariesToAnalyse.keySet()){
            jobsToAnalyse.append("job: " + signatureLibrary.getName() + " version: " + librariesToAnalyse.get(signatureLibrary) + "\n");
        }
        LOGGER.debug("From librariesToAnalyse: " + jobsToAnalyse);

//        LOGGER.debug("From librariesToAnalyse: " + jobsToAnalyse);
        String temp;
        // Collection of BerkeleyMatches of different kinds.
        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
            String signatureLibraryReleaseVersion = berkeleyMatch.getSignatureLibraryRelease();
            final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
            if(analysisJobMap.containsKey(sigLib.getName().toUpperCase())){
                LOGGER.debug("Found Library : sigLib: " + sigLib + " version: " + signatureLibraryReleaseVersion);
            }
            debugString = "sigLib: " + sigLib + "version: " + signatureLibraryReleaseVersion;
            debugString +=  "\n librariesToAnalyse value: " + librariesToAnalyse.keySet().toString() + " version: " + librariesToAnalyse.get(sigLib);
//            Utilities.verboseLog(10, debugString);

            LOGGER.debug("sigLib: " + sigLib + "version: " + signatureLibraryReleaseVersion);
            LOGGER.debug("librariesToAnalyse value: " + librariesToAnalyse.keySet().toString() + " version: " + librariesToAnalyse.get(sigLib));

            // Check to see if the signature library is required for the analysis.
            // First check: librariesToAnalyse == null -> -appl option hasn't been set
            // Second check: Analysis library has been request with the right release version -> -appl PIRSF-2.84
            if (librariesToAnalyse == null || (librariesToAnalyse.containsKey(sigLib) && librariesToAnalyse.get(sigLib).equals(signatureLibraryReleaseVersion))) {
                // Retrieve Signature to match
                LOGGER.debug("Check match for : " + sigLib + "-" + signatureLibraryReleaseVersion);
                Query sigQuery = entityManager.createQuery("select distinct s from Signature s where s.accession = :sig_ac and s.signatureLibraryRelease.library = :library and s.signatureLibraryRelease.version = :version");
                sigQuery.setParameter("sig_ac", berkeleyMatch.getSignatureAccession());
                sigQuery.setParameter("library", sigLib);

                sigQuery.setParameter("version", signatureLibraryReleaseVersion);

                @SuppressWarnings("unchecked") List<Signature> signatures = sigQuery.getResultList();
                Signature signature = null;
                LOGGER.debug("signatures size: " + signatures.size());

                //what should be the behaviour here:
                //
                if (signatures.size() == 0) {   // This Signature is not in I5, so cannot store this one.
                    continue;
                } else if (signatures.size() > 1) {
                    //try continue instead of exiting
                    String warning = "Data inconsistency issue. This distribution appears to contain the same signature multiple times: "
                            + " signature: " + berkeleyMatch.getSignatureAccession()
                            + " library name: " + berkeleyMatch.getSignatureLibraryName()
                            + " match id: " + berkeleyMatch.getMatchId()
                            + " sequence md5: " + berkeleyMatch.getProteinMD5()
                            ;
                    LOGGER.warn(warning);
                    continue;
                    //throw new IllegalStateException("Data inconsistency issue. This distribution appears to contain the same signature multiple times: " + berkeleyMatch.getSignatureAccession());
                } else {
                    signature = signatures.get(0);
                    LOGGER.debug("signatures size: " +signatures.get(0));
                }

                // determine the type or the match currently being observed
                // Retrieve the appropriate converter to turn the BerkeleyMatch into an I5 match
                // Type is based upon the member database type.

                if (signatureLibraryToMatchConverter == null) {
                    throw new IllegalStateException("The match converter map has not been populated.");
                }
                BerkeleyMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);
                if (matchConverter != null) {
                    Match i5Match = matchConverter.convertMatch(berkeleyMatch, signature);
                    if (i5Match != null) {
                        // Lookup up the right protein
                        final Protein prot = md5ToProteinMap.get(berkeleyMatch.getProteinMD5().toUpperCase());
                        if (prot != null) {
                            prot.addMatch(i5Match);
                        } else {
                            LOGGER.warn("Attempted to store a match in a Protein, but cannot find the protein??? This makes no sense. Possible coding error.");
                        }
                    }
                } else {
                    LOGGER.warn("Unable to persist match " + berkeleyMatch + " as there is no available conversion for signature libarary " + sigLib);
                }
            }
        }
    }
}
