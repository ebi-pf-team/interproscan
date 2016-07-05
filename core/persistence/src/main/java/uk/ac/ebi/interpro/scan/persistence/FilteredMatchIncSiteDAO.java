//package uk.ac.ebi.interpro.scan.persistence;
//
//import org.apache.log4j.Logger;
//import org.springframework.transaction.annotation.Transactional;
//import uk.ac.ebi.interpro.scan.model.*;
//import uk.ac.ebi.interpro.scan.model.raw.*;
//import uk.ac.ebi.interpro.scan.util.Utilities;
//
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * HMMER3 filtered match data access object.
// *
// * @author Antony Quinn
// * @author Phil Jones
// * @version $Id$
// */
////
////abstract class RPSBlastFilteredMatchDAO<T extends , R extends RPSBlastRawSite>
////        extends FilteredMatchAndSiteDAO<T,RPSBlastMatch, R, RPSBlastMatch.RPSBlastLocation.RPSBlastSite>
////{
//public abstract class FilteredMatchIncSiteDAO<T extends RPSBlastRawMatch>
//        extends FilteredMatchDAOImpl<T, RPSBlastMatch>
//        implements FilteredMatchDAO<T, RPSBlastMatch> {
//
//    private static final Logger LOGGER = Logger.getLogger(FilteredMatchIncSiteDAO.class.getName());
//
//    public FilteredMatchIncSiteDAO() {
//        super(RPSBlastMatch.class);
//    }
//
//    @Transactional
//    public void persist(Collection<RawProtein<T>> filteredProteins, Collection<CDDRawSite> sites) {
//        if (filteredProteins == null || filteredProteins.size() == 0) {
//            LOGGER.debug("No RawProtein objects have been passed into the persistFilteredMatches method, so exiting.");
//            return;
//        }
//
//        //dummy
//    }
//
//    /**
//     * Persists filtered protein matches.
//     *
//     * @param filteredProteins Filtered protein matches.
//     */
////    @Transactional
////    public void persist(Collection<RawProtein<T>> filteredProteins, Collection<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites) {
////        if (filteredProteins == null || filteredProteins.size() == 0) {
////            LOGGER.debug("No RawProtein objects have been passed into the persistFilteredMatches method, so exiting.");
////            return;
////        }
////
////        String signatureLibraryRelease = null;
////        SignatureLibrary signatureLibrary = null;
////        int rawMatchCount = 0;
////        for (RawProtein<T> rawProtein : filteredProteins) {
////            for (T rawMatch : rawProtein.getMatches()) {
////                rawMatchCount++;
////                if (signatureLibraryRelease == null) {
////                    signatureLibraryRelease = rawMatch.getSignatureLibraryRelease();
////                    if (signatureLibraryRelease == null) {
////                        throw new IllegalStateException("Found a raw match record that does not include the release version");
////                    }
////                } else if (!signatureLibraryRelease.equals(rawMatch.getSignatureLibraryRelease())) {
////                    throw new IllegalStateException("Attempting to persist a collection of filtered matches for more than one SignatureLibraryRelease.   Not implemented.");
////                }
////                if (signatureLibrary == null) {
////                    signatureLibrary = rawMatch.getSignatureLibrary();
////                    if (signatureLibrary == null) {
////                        throw new IllegalStateException("Found a raw match record that does not include the SignatureLibrary.");
////                    }
////                } else if (signatureLibrary != (rawMatch.getSignatureLibrary())) {
////                    throw new IllegalStateException("Attempting to persist a Collection of filtered matches for more than one SignatureLibrary.");
////                }
////            }
////        }
////        if (LOGGER.isDebugEnabled()) {
////            LOGGER.debug(rawMatchCount + " filtered matches have been passed in to the persistFilteredMatches method");
////        }
////        if (signatureLibraryRelease == null) {
////            LOGGER.debug("There are no raw matches to filter.");
////            return;
////        }
////
////        final Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(filteredProteins);
////        final Map<String, Signature> modelIdToSignatureMap = getModelAccessionToSignatureMap(signatureLibrary, signatureLibraryRelease, filteredProteins);
////
////        LOGGER.debug("signatureLibrary: " +  signatureLibrary
////                + " signatureLibraryRelease: "     + signatureLibraryRelease
////                + " filteredProteins: " + filteredProteins.size()
////                + " modelIdToSignatureMap size: " + modelIdToSignatureMap.size());
////
////
////        StringBuilder signatureList = new StringBuilder();
////        for (Signature signature:   modelIdToSignatureMap.values()){
////            signatureList.append(signature.getModels().toString());
////        }
////
////        persist(filteredProteins, sites, modelIdToSignatureMap, proteinIdToProteinMap);
////
////    }
//
//
//    /**
//     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
//     * persist filtered matches.
//     *
//     * @param rawProteins           being the Collection of filtered RawProtein objects to persist
//     * @param modelIdToSignatureMap a Map of model IDs to Signature objects.
//     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
//     */
////    @Override
////    @Transactional
////    public void persist(Collection<RawProtein<T>> rawProteins, Collection<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites,
////                        Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
////        for (RawProtein<T> rawProtein : rawProteins) {
////            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
////            if (protein == null) {
////                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
////                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
////            }
////
////            //LOGGER.debug("Protein: " + protein);
////            Collection<T> rawMatches = rawProtein.getMatches();
//////            if (rawMatches == null su|| rawMatches.size() != 1) {
//////                throw new IllegalStateException("Protein did not have only one RPSBlast match! " +
//////                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
//////            }
////            for (T rawMatch : rawMatches) {
////                // RPSBlast matches consist of a YES/NO result, therefore there will
////                // only ever be 1 raw match for each protein, but never mind!
////                if (rawMatch == null) continue;
////
////                Signature signature = modelIdToSignatureMap.get(rawMatch.getModelId());
////                LOGGER.debug("rpsBlast match model id:" + rawMatch.getModelId() + " signature: " + signature);
////                LOGGER.debug("modelIdToSignatureMap: " + modelIdToSignatureMap);
////
////                if (rawMatch.getModelId().startsWith("cl")){
////                    LOGGER.debug("this is a superfamily match, ignore for now ...");
////                    continue;
////                }
////                //TODO add Sites??
////                Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<RPSBlastMatch.RPSBlastLocation>();
////
////                //for this location find the sites
////                rawMatch.getSequenceIdentifier();
////                rawMatch.getModelId();
////                rawMatch.getAessionNumber();
////                rawMatch.getLocationStart();
////                rawMatch.getLocationStart();
////                rawMatch.getSignatureLibrary();
////                rawMatch.getSignatureLibraryRelease();
////
////                Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = getSites(rawMatch, sites);
////
////                Utilities.verboseLog("filtered sites: " + rpsBlastSites);
////                locations.add(
////                        new RPSBlastMatch.RPSBlastLocation(
////                                rawMatch.getLocationStart(),
////                                rawMatch.getLocationEnd(),
////                                rawMatch.getBitScore(),
////                                rawMatch.getEvalue()
////                        )
////                );
////                RPSBlastMatch match = new RPSBlastMatch(signature, locations);
////                LOGGER.debug("rpsBlast match: " + match);
////                protein.addMatch(match);
////                //LOGGER.debug("Protein with match: " + protein);
////                entityManager.persist(match);
////            }
////        }
////    }
//
//
//
//    /**
//     *  check if site is in the location range [start,end]
//     * @param rawMatch
//     * @param rawSite
//     * @return
//     */
////    boolean siteInLocationRange(T rawMatch, R rawSite){
////        if (rawSite.getSiteStart() >= rawMatch.getLocationStart() && rawSite.getSiteEnd() <= rawMatch.getLocationEnd()){
////            return true;
////        }
////        return false;
////    }
//
////    Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> getSites(T rawMatch, Collection<R> rawSites){
////        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new HashSet<>();
////        for (R rawSite: rawSites){
////            if (siteInLocationRange(rawMatch, rawSite)){
////                rpsBlastSites.add(new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(rawSite.getResidue(), rawSite.getSiteStart(), rawSite.getSiteEnd()));
////            }
////        }
////        return rpsBlastSites;
////    }
//
//
//}
