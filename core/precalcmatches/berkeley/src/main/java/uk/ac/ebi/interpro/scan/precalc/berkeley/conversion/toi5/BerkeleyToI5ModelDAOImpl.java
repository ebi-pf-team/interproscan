package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

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
    public void populateProteinMatches(Protein nonPersistedProtein, List<BerkeleyMatch> berkeleyMatches, String analysisJobNames) {
        populateProteinMatches(Collections.singleton(nonPersistedProtein), berkeleyMatches, analysisJobNames);
    }

    @Transactional(readOnly = true)
    public void populateProteinMatches(Set<Protein> preCalculatedProteins, List<BerkeleyMatch> berkeleyMatches, String analysisJobNames) {

        final Map<String, Protein> md5ToProteinMap = new HashMap<String, Protein>(preCalculatedProteins.size());
        // Populate the lookup map.
        for (Protein protein : preCalculatedProteins) {
            md5ToProteinMap.put(protein.getMd5().toUpperCase(), protein);
        }

        //Mapping between SignatureLibrary and the version number, e.g key=PIRSF,value=2.84
        Map<SignatureLibrary, String> librariesToAnalyse = null;

        //Populate map with data
        if (analysisJobNames != null) {
            librariesToAnalyse = new HashMap<SignatureLibrary, String>();
            for (String analysisJobName : analysisJobNames.split(",")) {
                // Strip off "job" and version number
                analysisJobName = analysisJobName.substring(3);
                String analysisJob = null;
                String versionNumber = null;
                if (analysisJobName != null && analysisJobName.contains("-")) {
                    analysisJob = analysisJobName.substring(0, analysisJobName.lastIndexOf('-'));
                    versionNumber = analysisJobName.substring(analysisJobName.lastIndexOf('-') + 1);
                    //Check for Gene3D and modify the version number. Onion version number for Gene3D is 3.5 while InterProScan uses 3.5.0
                    //Temp fix, it will not be necessary when Onion is retired
                    if(analysisJob.toLowerCase().equals("gene3d") && versionNumber.equals("3.5.0")){
                       versionNumber = "3.5";
                    }
                    LOGGER.debug("Job: " + analysisJobName + " :- analysisJob: " + analysisJob + " versionNumber: " + versionNumber);
                } else {
                    throw new IllegalStateException("Analysis job name is in an unexpected format: " + analysisJobName);
                }
                final SignatureLibrary matchingLibrary = SignatureLibraryLookup.lookupSignatureLibrary(analysisJob);
                if (matchingLibrary != null) {
                    librariesToAnalyse.put(matchingLibrary, versionNumber);
                }
            }
        }
        // Collection of BerkeleyMatches of different kinds.
        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
            String signatureLibraryReleaseVersion = berkeleyMatch.getSignatureLibraryRelease();
            final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
            // Check to see if the signature library is required for the analysis.
            // First check: librariesToAnalyse == null -> -appl option hasn't been set
            // Second check: Analysis library has been request with the right release version -> -appl PIRSF-2.84
            if (librariesToAnalyse == null || (librariesToAnalyse.containsKey(sigLib) && librariesToAnalyse.get(sigLib).equals(signatureLibraryReleaseVersion))) {
                // Retrieve Signature to match
                LOGGER.debug("Check match for : " + sigLib + "-" + signatureLibraryReleaseVersion);
                Query sigQuery = entityManager.createQuery("select distinct s from Signature s where s.accession = :sig_ac and s.signatureLibraryRelease.library = :library and s.signatureLibraryRelease.version = :version");
                sigQuery.setParameter("sig_ac", berkeleyMatch.getSignatureAccession());
                sigQuery.setParameter("library", sigLib);
                //TEMP Solution - deal with Gene3d versioning
                if(sigLib.getName().toLowerCase().equals("gene3d")){
                    signatureLibraryReleaseVersion = "3.5.0";
                }
                sigQuery.setParameter("version", signatureLibraryReleaseVersion);

                @SuppressWarnings("unchecked") List<Signature> signatures = sigQuery.getResultList();
                Signature signature = null;
                LOGGER.debug("signatures size: " +signatures.size());

                if (signatures.size() == 0) {   // This Signature is not in I5, so cannot store this one.
                    continue;
                } else if (signatures.size() > 1) {
                    throw new IllegalStateException("Data inconsistency issue. This distribution appears to contain the same signature multiple times: " + berkeleyMatch.getSignatureAccession());
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
