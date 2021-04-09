package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.persistence.MatchDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.awt.desktop.SystemSleepEvent;
import java.util.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */
public class LookupStoreToI5ModelDAOImpl implements LookupStoreToI5ModelDAO {

    private static final Logger LOGGER = LogManager.getLogger(LookupStoreToI5ModelDAOImpl.class.getName());

    private Map<SignatureLibrary, LookupMatchConverter> signatureLibraryToMatchConverter;

    protected EntityManager entityManager;

    protected MatchDAO matchDAO;

    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Required
    public void setSignatureLibraryToMatchConverter(Map<SignatureLibrary, LookupMatchConverter> signatureLibraryToMatchConverter) {
        this.signatureLibraryToMatchConverter = signatureLibraryToMatchConverter;
    }

    @Required
    public void setMatchDAO(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    /**
     * Method to store matches based upon lookup from the Berkeley match database of precalculated matches.
     *
     * @param nonPersistedProtein being a newly instantiated Protein object
     * @param berkeleyMatches     being a Set of BerkeleyMatch objects, retrieved / unmarshalled from
     *                            the Berkeley Match web service.
     */
    @Transactional(readOnly = true)
    public void populateProteinMatches(Protein nonPersistedProtein, List<KVSequenceEntry> kvSequenceEntries, List<KVSequenceEntry> kvSiteSequenceEntries, Map<String, SignatureLibraryRelease> analysisJobMap, boolean includeCDDorSFLD) {
        populateProteinMatches(Collections.singleton(nonPersistedProtein), kvSequenceEntries, kvSiteSequenceEntries, analysisJobMap, includeCDDorSFLD);
    }

    @Transactional(readOnly = true)
    public void populateProteinMatches(Set<Protein> preCalculatedProteins, List<KVSequenceEntry> kvSequenceEntries, List<KVSequenceEntry> kvSiteSequenceEntries, Map<String, SignatureLibraryRelease> analysisJobMap, boolean includeCDDorSFLD) {
        String debugString = "";
        String lookup2IdRunID = String.valueOf(preCalculatedProteins.hashCode());
        Utilities.verboseLog(30, lookup2IdRunID + " Start  populateProteinMatches:  preCalculatedProteins: " + preCalculatedProteins.size()); // +  " kvSequenceEntries: "  + kvSequenceEntries.size() );

        final Map<String, Protein> md5ToProteinMap = new HashMap<>(preCalculatedProteins.size());
        // Populate the lookup map.
        for (Protein protein : preCalculatedProteins) {
            md5ToProteinMap.put(protein.getMd5().toUpperCase(), protein);
        }

        //the following was the problem:
        //analysisJobMap = new HashMap<String, SignatureLibraryRelease>();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("analysisJobMap: " + analysisJobMap);
        }
        final Map<String, Set<Match>> protteinToMatchesMap = new HashMap<>();

        //Mapping between SignatureLibrary and the version number, e.g key=PIRSF,value=2.84
        Map<SignatureLibrary, String> librariesToAnalyse = null;

        //Populate map with data
        if (analysisJobMap != null) {
            librariesToAnalyse = new HashMap<>();
            for (String analysisJobName : analysisJobMap.keySet()) {
                String analysisJob = null;
                String versionNumber = null;
                if (analysisJobName != null) {
                    analysisJob = analysisJobName;
                    versionNumber = analysisJobMap.get(analysisJobName).getVersion();

                    debugString = "Job: " + analysisJobName + " :- analysisJob: " + analysisJob + " versionNumber: " + versionNumber;
//                    Utilities.verboseLog(110, debugString);
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
        if (LOGGER.isDebugEnabled()) {
            StringBuilder jobsToAnalyse = new StringBuilder();
            for (String job : analysisJobMap.keySet()) {
                jobsToAnalyse.append("job: " + job + " version: " + analysisJobMap.get(job).getVersion() + "\n");
            }
            LOGGER.debug("From analysisJobMap" + jobsToAnalyse);
            jobsToAnalyse = new StringBuilder();
            for (SignatureLibrary signatureLibrary : librariesToAnalyse.keySet()) {
                jobsToAnalyse.append("job: " + signatureLibrary.getName() + " version: " + librariesToAnalyse.get(signatureLibrary) + "\n");
            }
            LOGGER.debug("From librariesToAnalyse: " + jobsToAnalyse);

//        LOGGER.debug("From librariesToAnalyse: " + jobsToAnalyse);
        }

        Map<String, List<KVSequenceEntry>> mapKVSequenceEntryForSites = getMapKVSequenceEntry(kvSiteSequenceEntries);

        // Collection of BerkeleyMatches of different kinds.
        //Utilities.verboseLog(30, "Start comvert lookup matches to i5 matches:  kvSequenceEntries : " + kvSequenceEntries.size());
        Utilities.verboseLog(30, lookup2IdRunID + " Start convert lookup matches to i5 matches:  kvSequenceEntries : " + kvSequenceEntries.size());
        String exampleKey = null;
        long persistTime = 0;
        int totalMatches = 0;
        long totalSignatureQueryTime = 0;
        int sigQueriesCount = 0;
        for (KVSequenceEntry lookupMatch : kvSequenceEntries) {
            //now we ahave a list

            String proteinMD5 = lookupMatch.getProteinMD5();
            Set<String> sequenceHits = lookupMatch.getSequenceHits();

            //deal with cdd and sfld sites
            List<KVSequenceEntry> siteSequenceEntryList = mapKVSequenceEntryForSites.get(proteinMD5);
            Set<String> sequenceSiteHits = null;
            if (siteSequenceEntryList != null) {
                sequenceSiteHits = new HashSet<>();
                for (KVSequenceEntry siteSequenceEntry : siteSequenceEntryList) {
                    sequenceSiteHits.addAll(siteSequenceEntry.getSequenceHits());
                }
                Utilities.verboseLog(140, "SequenceSiteHits : " + sequenceSiteHits.size());
            }

            //Utilities.verboseLog(110, "consider proteinMD5:  " + proteinMD5 );
            // Convert list of matches for current protein into a Map of modelAc -> List of matches for that model on this protein
            Map<String, List<SimpleLookupMatch>> modelToMatchesMap = new HashMap<>();
            for (String sequenceHit : sequenceHits) {
                SimpleLookupMatch simpleMatch = new SimpleLookupMatch(proteinMD5, sequenceHit);
                String modelAc = simpleMatch.getModelAccession();
                if (modelToMatchesMap.containsKey(modelAc)) {
                    modelToMatchesMap.get(modelAc).add(simpleMatch);
                } else {
                    List<SimpleLookupMatch> matches = new ArrayList<>();
                    matches.add(simpleMatch);
                    modelToMatchesMap.put(modelAc, matches);
                }
            }

            Utilities.verboseLog(140, "modelToMatchesMap size:  " + modelToMatchesMap.values().size());

            //we have to get all the matches and not just the first match
            Utilities.verboseLog(140, "modelToMatchesMap size:  " + modelToMatchesMap.values().size());
            int simpleMatchCount = 0;

            Utilities.verboseLog(110, lookup2IdRunID + " modelToMatchesMap size after pre-process " + modelToMatchesMap.values().size());

            for (List<SimpleLookupMatch> matchesForModel : modelToMatchesMap.values()) {
                assert matchesForModel.size() > 0;

                Utilities.verboseLog(140, "Start matchesForModel - size:  " + matchesForModel.size());

                SimpleLookupMatch simpleMatch = matchesForModel.get(0); // Get first match with this modelAc for this protein

                //Utilities.verboseLog(110, "matchesForModel - first match:  " + simpleMatch.toString());

                String signatureLibraryReleaseVersion = simpleMatch.getSigLibRelease();

                //Utilities.verboseLog(110, "simpleMatch:  " + simpleMatch.toString() );
                final SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(simpleMatch.getSignatureLibraryName());
                //Quick Hack: deal with CDD and SFLD for now as they need to be calculated locally (since sites are not in Berkeley DB yet)
                if (sigLib.getName().equals(SignatureLibrary.CDD.getName())
                        || sigLib.getName().equals(SignatureLibrary.SFLD.getName())) {
                    Utilities.verboseLog(140, "SFLD or CDD match found: " + simpleMatch.toString());
                    ;
                }
                SignatureLibrary signatureLibraryKey = sigLib;
                if (LOGGER.isDebugEnabled() && analysisJobMap.containsKey(sigLib.getName().toUpperCase())) {
                    LOGGER.debug("Found Library : sigLib: " + sigLib + " version: " + signatureLibraryReleaseVersion);
                }
                debugString = "sigLib: " + sigLib + "vversion: " + signatureLibraryReleaseVersion;
                debugString += "\n librariesToAnalyse value: " + librariesToAnalyse.keySet().toString() + " version: " + librariesToAnalyse.get(sigLib);
                Utilities.verboseLog(140, debugString);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sigLib: " + sigLib + " version: " + signatureLibraryReleaseVersion);
                    LOGGER.debug("librariesToAnalyse value: " + librariesToAnalyse.keySet().toString() + " version: " + librariesToAnalyse.get(sigLib));
                }

                // Check to see if the signature library is required for the analysis.
                // First check: librariesToAnalyse == null -> -appl option hasn't been set
                // Second check: Analysis library has been request with the right release version -> -appl PIRSF-2.84
                if (librariesToAnalyse == null || (librariesToAnalyse.containsKey(sigLib) && librariesToAnalyse.get(sigLib).equals(signatureLibraryReleaseVersion))) {
                    // Retrieve Signature to match
                    debugString = "Check matches for : " + sigLib + "-" + signatureLibraryReleaseVersion;
                    Utilities.verboseLog(140, debugString);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(debugString);
                    }
                    long startSignatureQuery =  System.currentTimeMillis();

                    Query sigQuery = entityManager.createQuery("select distinct s from Signature s where s.accession = :sig_ac and s.signatureLibraryRelease.library = :library and s.signatureLibraryRelease.version = :version");

                    sigQuery.setParameter("sig_ac", simpleMatch.getSignatureAccession());
                    sigQuery.setParameter("library", sigLib);

                    sigQuery.setParameter("version", signatureLibraryReleaseVersion);

                    //Utilities.verboseLog(110, " parameters : - " + sigQuery.getParameters().toString());

                    debugString = "Execute sigQuery : " + sigQuery.toString();
                    //Utilities.verboseLog(110, debugString);
//                    List<Signature> signatures = null;
                    @SuppressWarnings("unchecked") List<Signature> signatures = sigQuery.getResultList();
//                        signatures = sigQuery.getResultList();

                    long getSignatureTime = System.currentTimeMillis() - startSignatureQuery;
                    totalSignatureQueryTime +=  getSignatureTime;
                    sigQueriesCount ++;

                    Signature signature = null;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("signatures size: " + signatures.size());
                    }

                    //Utilities.verboseLog(110, "Signatures size: " + signatures.size());
                    //what should be the behaviour here:
                    //
                    if (signatures.size() == 0) {   // This Signature is not in I5, so cannot store this one.
                        continue;
                    } else if (signatures.size() > 1) {
                        //try continue instead of exiting
                        String warning = "Data inconsistency issue. This distribution appears to contain the same signature multiple times: "
                                + " signature: " + simpleMatch.getSignatureAccession()
                                + " library name: " + simpleMatch.getSignatureLibraryName()
                                //+ " match id: " + simpleMatch.getMatchId()
                                + " sequence md5: " + proteinMD5;
                        LOGGER.warn(warning);
                        continue;
                        //throw new IllegalStateException("Data inconsistency issue. This distribution appears to contain the same signature multiple times: " + berkeleyMatch.getSignatureAccession());
                    } else {
                        signature = signatures.get(0);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("signatures size: " + signatures.get(0));
                        }
                    }

                    // determine the type or the match currently being observed
                    // Retrieve the appropriate converter to turn the BerkeleyMatch into an I5 match
                    // Type is based upon the member database type.

                    if (signatureLibraryToMatchConverter == null) {
                        throw new IllegalStateException("The match converter map has not been populated.");
                    }
                    //Utilities.verboseLog(110, "signatures size: " + signatures.get(0) );
                    LookupMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);

                    if (matchConverter != null) {
                        // Lookup up the right protein using the MD5
                        //Utilities.verboseLog(110, "matchConverter: is not null " );
                        final Protein prot = md5ToProteinMap.get(proteinMD5);
                        final String dbKey = Long.toString(prot.getId()) + signatureLibraryKey.getName();
                        exampleKey = dbKey;
//                        Utilities.verboseLog(1100, "dbKey: " + dbKey);
                        if (prot != null) {
                            // One or multiple locations for this match on a given protein for this modelAc
                            //Utilities.verboseLog(110, "consider dbKey:  " + dbKey + " matchesForModel: " + matchesForModel.size() );
                            simpleMatchCount++;
                            if (Utilities.verboseLogLevel >= 140 ) {
                                Utilities.verboseLog(140, "simpleMatchCount :  " + simpleMatchCount + " signatureAc: " + simpleMatch.getSignatureAccession() +
                                        " expected matches: " + matchesForModel.size());
                                for (SimpleLookupMatch simpleLookupMatchDisplay : matchesForModel) {
                                    Utilities.verboseLog(120, "simpleLookupMatchDisplay: [\n" +
                                            " sequence: " + prot.getId() +
                                            " md5: " + simpleLookupMatchDisplay.getProteinMD5() +
                                            " location: " + simpleLookupMatchDisplay.getSequenceStart() + " - " + simpleLookupMatchDisplay.getSequenceEnd() +
                                            " fragments: " + simpleLookupMatchDisplay.getFragments() +
                                            " \n ]");
                                }
                            }
                            if (matchesForModel.size() == 1) {
                                Utilities.verboseLog(130, "Convert match for:  " + simpleMatch.getProteinMD5());
                                Utilities.verboseLog(130, "simpleMatch : " + simpleMatch.toString() + " signature:  " + signature.getName());
                                if (sequenceSiteHits != null) {
                                    Utilities.verboseLog(130, " \n sequenceSiteHits  size: " + sequenceSiteHits.size());
                                    //Utilities.verboseLog(130, " \n sequenceSiteHits  :" + sequenceSiteHits.toString());
                                } else {
                                    Utilities.verboseLog(130, " \n sequenceSiteHits  is NULL:");
                                }

                                Utilities.verboseLog(110, "Lookup Match :-  " + simpleMatch.getProteinMD5() + "  "
                                        + simpleMatch.getSequenceStart() + " - " + simpleMatch.getSequenceEnd());
                                Match i5Match = matchConverter.convertMatch(simpleMatch, sequenceSiteHits, signature);
                                Utilities.verboseLog(140, "----");

                                if (i5Match != null) {
                                    Utilities.verboseLog(50, lookup2IdRunID + " i5 Lookup Converted Match :-  " + i5Match.getId()); //avoid using complete objects in verbose

                                    prot.addMatch(i5Match);

                                    Set<Match> matchSet = new HashSet<>();
                                    updateMatch(i5Match);
                                    matchSet.add(i5Match);
                                    String coordinates = getCoordinates(i5Match);
                                    Utilities.verboseLog(130, "Persist to kvMatchStore: key " + dbKey + " singleton match: " +
                                            coordinates + " match size: " + matchSet.size());
                                    //matchDAO.persist(dbKey, matchSet);
                                    long startPersist = System.currentTimeMillis();
                                    totalMatches += matchSet.size();
                                    persistMatch(matchSet, dbKey);
                                    persistTime  = persistTime + (System.currentTimeMillis() - startPersist);
                                } else {
                                    LOGGER.warn("i5 Lookup Converted Match is NULL");
                                    Utilities.verboseLog(1100, "i5 Lookup Converted Match is NULL");
                                }
                            } else {
                                Utilities.verboseLog(130, "Convert matches for:  " + simpleMatch.getProteinMD5() + " -- " + matchesForModel.size());
                                List<Match> i5Matches = matchConverter.convertMatches(matchesForModel, sequenceSiteHits, signature);
                                if (i5Matches != null) {

                                    for (Match i5Match : i5Matches) {
                                        prot.addMatch(i5Match);
                                        updateMatch(i5Match);
                                        String accession = i5Match.getSignature().getAccession();
                                        Utilities.verboseLog(110, "Converted Match dbKey : " + dbKey + " - " + accession);
                                        //i5Match.getLocations()
                                        String coordinates = getCoordinates(i5Match);
                                        Utilities.verboseLog(110, "i5 Lookup Converted Match (from a set) :-  " + accession + " " + coordinates);
                                        //prot.addMatch(i5Match); try to reverse the order in which the above two statments are being executed
                                    }
                                    Set<Match> matchSet = new HashSet<>(i5Matches);
                                    Utilities.verboseLog(130, "Persist to kvMatchStore: key " + dbKey + " matches : " + matchSet.size());
                                    long startPersist = System.currentTimeMillis();
                                    persistMatch(matchSet, dbKey);
                                    totalMatches += matchSet.size();
                                    persistTime  = persistTime + (System.currentTimeMillis() - startPersist);
                                }
                            }
                        } else {
                            LOGGER.warn("Attempted to store a match in a Protein, but cannot find the protein??? This makes no sense. Possible coding error.");
                        }
                        Utilities.verboseLog(130, "protein:  " + prot.getId() + " dbkey: " + dbKey);
                    } else {
                        Utilities.verboseLog(110, "matchConverter: is NULL ");
                        LOGGER.warn("Unable to persist match " + simpleMatch + " as there is no available conversion for signature libarary " + sigLib);
                    }
                }
                Utilities.verboseLog(110, "End matches for model ");
            }
        }
        if (totalMatches > 0) {
            Utilities.verboseLog(30, lookup2IdRunID + " Total persist time millis " +
                    (persistTime) + " and persist time per match  millis = " + ((persistTime ) / totalMatches) +
                    " for " + totalMatches +  " matches");
        }
        if (sigQueriesCount > 0) {
            Utilities.verboseLog(30, lookup2IdRunID + " totalSignatureQueryTime : " + totalSignatureQueryTime +
                    " totalSigQueries: " + sigQueriesCount + " avge sigquery per call : " + (totalSignatureQueryTime / sigQueriesCount));
        }
        Utilities.verboseLog(30, lookup2IdRunID + " Completed converting and persisting this batch exampleKey: " + exampleKey);
    }


    void persist(Protein protein, Match match) {
        matchDAO.persist(protein.getMd5(), match);
    }

    /**
     * deal with the case where matches for the sequence are already in the db
     *
     * @param matches
     * @param dbKey
     * @return
     */
    public boolean persistMatch(Set<Match> matches, String dbKey) {
        Utilities.verboseLog(120, "Start persistMatch  " + dbKey + " matches size : " + matches.size());
        Set<Match> matchSet = null;
        int tryCount = 0;
        int maxTryCount = 8;
        int totalWaitTime = 0;
        Long proteinsConsidered = 800l; //max proteins considered per time
        while (tryCount <= maxTryCount) {
            try {
                matchSet = matchDAO.getMatchSet(dbKey);
                break; //avoid infinite loop
            } catch (Exception exception) {
                //dont recover but sleep for a few seconds and try again
                Utilities.verboseLog(1100, "Exception type: " + exception.getClass());
                int waitTime = (proteinsConsidered.intValue() / 800) * 2 * 1000;
                String errorMessage = "Get MatchSet: There is a problem processing results for matchset internal Identifier: " + dbKey +
                        ". You should report the errors if this problem persists. match calculations will continue.";
                totalWaitTime = handleKVStoreExceptions(tryCount, maxTryCount, waitTime, totalWaitTime, exception, errorMessage);
            }
            tryCount++;
        }

        //StringBuffer superfamilyCheck = new StringBuffer("Superfamily check: ");
        if (matchSet == null) {
            Utilities.verboseLog(120, "set new matches " + dbKey + " matches size : " + matches.size());
            matchSet = matches;
            //superfamilyCheck.append(" matchset is Null - ");
        } else {
            int oldMatchSetSize = matchSet.size();
            Utilities.verboseLog(120, "Found matches for this key key " + dbKey + " oldMatchSetSize : " + oldMatchSetSize + " new matchSet size : " + matchSet.size());
            matchSet.addAll(matches);
            //superfamilyCheck.append(" matchset has " + oldMatchSetSize + " matches already");
            //superfamilyCheck.append(" matchset has " + oldMatchSetSize + " matches already");
        }
        //superfamilyCheck.append(" - added " + matches.size() + " matches");
        Utilities.verboseLog(130, "Persist to Converetd lkpup Matches to kvMatchStore: key " + dbKey + " match count:  : " + matchSet.size());
        matchDAO.persist(dbKey, matchSet);
        //Set<Match> matchSet2 = matchDAO.getMatchSet(dbKey);

        //uperfamilyCheck.append(" final matchCount = " + matchSet2.size());
        //Utilities.verboseLog(140, superfamilyCheck.toString());
        //Utilities.verboseLog(120, "End persistMatch  - kvMatchStore MatchSet: key " + dbKey + " match count:  : " + matchSet2.size());
        return true; // ??
    }

    public String getCoordinates(Match i5Match) {
        StringBuilder coordinates = new StringBuilder();
        Set<Location> locations = i5Match.getLocations();
        Integer start = null;
        Integer end = null;
        for (Location location : locations) {
            if (start == null) {
                start = location.getStart();
            } else if (start > location.getStart()) {
                start = location.getStart();
            }
            if (end == null) {
                end = location.getEnd();
            } else if (end < location.getEnd()) {
                end = location.getEnd();
            }
        }
        coordinates.append("[").
                append(start).
                append("-").
                append(end).
                append("]");
        return coordinates.toString();
    }

    public void updateMatch(Match match) {
        Entry matchEntry = match.getSignature().getEntry();
        if (matchEntry != null) {
            //check goterms
            //check pathways
            matchEntry.getGoXRefs();
            if (matchEntry.getGoXRefs() != null) {
                matchEntry.getGoXRefs().size();
            }
            //update only if pathways are requested
            matchEntry.getPathwayXRefs();
            if (matchEntry.getPathwayXRefs() != null) {
                matchEntry.getPathwayXRefs().size();
            }
        }
    }

    private Map<String, List<KVSequenceEntry>> getMapKVSequenceEntry(List<KVSequenceEntry> kvSiteSequenceEntries) {
        Map<String, List<KVSequenceEntry>> mapKVSequenceEntry = new HashMap<>();
        for (KVSequenceEntry kvSequenceEntry : kvSiteSequenceEntries) {
            List<KVSequenceEntry> kvSequenceEntryList = mapKVSequenceEntry.get(kvSequenceEntry.getProteinMD5());
            if (kvSequenceEntryList != null) {
                if (Utilities.verboseLogLevel >= 140) {
                    Utilities.verboseLog(140, "We already have the sites for : " + kvSequenceEntry.getProteinMD5());
                    Utilities.verboseLog(140, "kvSequenceEntry in the Map: " + mapKVSequenceEntry.get(kvSequenceEntry.getProteinMD5()).toString());
                }
            } else {
                kvSequenceEntryList = new ArrayList<>();
            }
            kvSequenceEntryList.add(kvSequenceEntry);
            mapKVSequenceEntry.put(kvSequenceEntry.getProteinMD5(), kvSequenceEntryList);
        }

        return mapKVSequenceEntry;
    }

    /**
     *  if the kvstore is returning some exception, its mostly due to file system problems,
     *  so allow to try again
     *
     * @param tryCount
     * @param maxTryCount
     * @param waitTime
     * @param totalWaitTime
     * @param exc
     * @param errorMessage
     * @return
     */
    private int handleKVStoreExceptions(int tryCount, int maxTryCount, int waitTime, int totalWaitTime, Exception exc, String errorMessage) {
        if (tryCount < maxTryCount - 1) {
            Utilities.sleep(waitTime);
            totalWaitTime += waitTime;
            Utilities.verboseLog(110, "  get matchset - wait for for at least " + waitTime + " millis");
        } else {
            exc.printStackTrace();
            LOGGER.warn(errorMessage);
        }

        return totalWaitTime;
    }

    @Override
    public void checkMatchDAO() {
        Utilities.verboseLog(140, matchDAO.getDbStore().toString());
        Utilities.verboseLog(20, " DBSTORE: " + matchDAO.getDbStore().getDbName()
                + ", DB Store type = " + matchDAO.getDbStore().getKVDBStore()
                + ", DB Store type = " + matchDAO.getDbStore().getKVDBType());
    }
}
