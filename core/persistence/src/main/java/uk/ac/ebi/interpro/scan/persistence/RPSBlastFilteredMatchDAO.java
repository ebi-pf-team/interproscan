package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawMatch;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawSite;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements the persistence method for RPSBlast matches (as filtered matches).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */

//T extends RawMatch, U extends Match,  R extends RawSite, S extends Site

abstract class RPSBlastFilteredMatchDAO<T extends RPSBlastRawMatch, R extends RPSBlastRawSite>
        extends FilteredMatchAndSiteDAO<T,RPSBlastMatch, R, RPSBlastMatch.RPSBlastLocation.RPSBlastSite>
        {

    private static final Logger LOGGER = Logger.getLogger(RPSBlastFilteredMatchDAO.class.getName());


    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public RPSBlastFilteredMatchDAO() {
        super(RPSBlastMatch.class);
//        this.releaseVersion = version;
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param rawProteins           being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of model IDs to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Override
    @Transactional
    public void persist(Collection<RawProtein<T>> rawProteins, Collection<R> sites,
                        Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<T> rawProtein : rawProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }

            //LOGGER.debug("Protein: " + protein);
            Collection<T> rawMatches = rawProtein.getMatches();
//            if (rawMatches == null su|| rawMatches.size() != 1) {
//                throw new IllegalStateException("Protein did not have only one RPSBlast match! " +
//                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
//            }
            for (T rawMatch : rawMatches) {
                // RPSBlast matches consist of a YES/NO result, therefore there will
                // only ever be 1 raw match for each protein, but never mind!
                if (rawMatch == null) continue;

                Signature signature = modelIdToSignatureMap.get(rawMatch.getModelId());
                LOGGER.debug("rpsBlast match model id:" + rawMatch.getModelId() + " signature: " + signature);
                LOGGER.debug("modelIdToSignatureMap: " + modelIdToSignatureMap);

                if (rawMatch.getModelId().startsWith("cl")){
                    LOGGER.debug("this is a superfamily match, ignore for now ...");
                    continue;
                }
                //TODO add Sites??
                Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<RPSBlastMatch.RPSBlastLocation>();

                //for this location find the sites
                rawMatch.getSequenceIdentifier();
                rawMatch.getModelId();
                rawMatch.getAessionNumber();
                rawMatch.getLocationStart();
                rawMatch.getLocationStart();
                rawMatch.getSignatureLibrary();
                rawMatch.getSignatureLibraryRelease();

                Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = getSites(rawMatch, sites);

                Utilities.verboseLog("filtered sites: " + rpsBlastSites);
                locations.add(
                        new RPSBlastMatch.RPSBlastLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getBitScore(),
                                rawMatch.getEvalue()
                        )
                );
                RPSBlastMatch match = new RPSBlastMatch(signature, locations, rpsBlastSites);
                LOGGER.debug("rpsBlast match: " + match);
                protein.addMatch(match);
                //LOGGER.debug("Protein with match: " + protein);
                entityManager.persist(match);
            }
        }
    }



    /**
     *  check if site is in the location range [start,end]
     * @param rawMatch
     * @param rawSite
     * @return
     */
    boolean siteInLocationRange(T rawMatch, R rawSite){
        if (rawSite.getSiteStart() >= rawMatch.getLocationStart() && rawSite.getSiteEnd() <= rawMatch.getLocationEnd()){
            return true;
        }
        return false;
    }

    Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> getSites(T rawMatch, Collection<R> rawSites){
        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new HashSet<>();
        for (R rawSite: rawSites){
            if (siteInLocationRange(rawMatch, rawSite)){
                rpsBlastSites.add(new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(rawSite.getResidue(), rawSite.getSiteStart(), rawSite.getSiteEnd()));
            }
        }
        return rpsBlastSites;
    }

}

//public class CDDFilteredMatchDAOImpl2 extends FilteredMatchDAOImpl<CDDRawMatch, CDDMatch> implements CDDFilteredMatchDAO {
//
//    private String cddReleaseVersion;
//
//    /**
//     * Sets the class of the model that the DAO instance handles.
//     * Note that this has been set up to use constructor injection
//     * because it makes it easy to sub-class GenericDAOImpl in a robust
//     * manner.
//     * <p/>
//     * Model class specific sub-classes should define a no-argument constructor
//     * that calls this constructor with the appropriate class.
//     */
//    public CDDFilteredMatchDAOImpl2(String version) {
//        super(CDDMatch.class);
//        this.cddReleaseVersion = version;
//    }
//
//    /**
//     * Persists a set of ParseCDDMatch objects as filtered matches:
//     * there is no filtering step with CDD.
//     *
//     * @param cddMatches being a Set of ParseCDDMatch objects to be persisted.
//     */
//    @Transactional
//    public void persist(Collection<RawProtein<CDDRawMatch>> rawProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap){
//        Signature cddSignature = null; //was loadPersistedSignature()
//        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(cddMatches);
//        for (ParseCDDMatch parseCDDMatch : cddMatches) {
//            final Protein persistentProtein = proteinIdToProteinMap.get(parseCDDMatch.getProteinDatabaseIdentifier());
//            if (persistentProtein == null) {
//                throw new IllegalArgumentException("Attempting to store a CDD match for a protein with id " + parseCDDMatch.getProteinDatabaseIdentifier() + ", however this does not exist in the database.");
//            }
//            // Signature currentSignatureAc = parseCDDMatch.getModelId();
//            cddSignature = modelIdToSignatureMap.get(currentSignatureAc);
//            if (cddSignature == null) {
//                throw new IllegalStateException("Cannot find PANTHER signature " + currentSignatureAc + " in the database.");
//            }
//
//            Set<CDDMatch.CDDLocation> locations = Collections.singleton(
//                    new CDDMatch.CDDLocation(parseCDDMatch.getStartCoordinate(), parseCDDMatch.getEndCoordinate())
//            );
//            CDDMatch match = new CDDMatch(cddSignature, locations);
//            persistentProtein.addMatch(match);
//            entityManager.persist(match);
//        }
//    }
//
//}