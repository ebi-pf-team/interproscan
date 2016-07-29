package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawMatch;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawSite;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * Implements the persistence method for RPSBlast matches (as filtered matches).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */
abstract class RPSBlastFilteredMatchDAO<T extends RPSBlastRawMatch, R extends RPSBlastRawSite>
        extends FilteredMatchAndSiteDAOImpl<T,RPSBlastMatch, R, RPSBlastMatch.RPSBlastLocation.RPSBlastSite> {

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
    public void persist(Collection<RawProtein<T>> rawProteins, Collection<R> rawSites,
                        Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {

        // Map seqId to raw sites for that sequence
        Map<String, List<R>> seqIdToRawSitesMap = new HashMap<>();
        if (rawSites != null) {
            for (R rawSite : rawSites) {
                String seqId = rawSite.getSequenceIdentifier();
                if (seqIdToRawSitesMap.containsKey(seqId)) {
                    seqIdToRawSitesMap.get(seqId).add(rawSite);
                } else {
                    List<R> s = new ArrayList<>();
                    s.add(rawSite);
                    seqIdToRawSitesMap.put(seqId, s);
                }
            }
        }

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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("rpsBlast match model id:" + rawMatch.getModelId() + " signature: " + signature);
                    LOGGER.debug("modelIdToSignatureMap: " + modelIdToSignatureMap);
                }

                if (rawMatch.getModelId().startsWith("cl")){
                    LOGGER.debug("this is a superfamily match, ignore for now ...");
                    continue;
                }
                Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<>();

                rawMatch.getSequenceIdentifier();
                rawMatch.getModelId();
                rawMatch.getSessionNumber();
                rawMatch.getLocationStart();
                rawMatch.getLocationStart();
                rawMatch.getSignatureLibrary();
                rawMatch.getSignatureLibraryRelease();

                //for this location find the sites
                Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = getSites(rawMatch, seqIdToRawSitesMap.get(rawMatch.getSequenceIdentifier()));

                Utilities.verboseLog("filtered sites: " + rpsBlastSites);
                locations.add(
                        new RPSBlastMatch.RPSBlastLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getBitScore(),
                                rawMatch.getEvalue(),
                                rpsBlastSites
                        )
                );

                RPSBlastMatch match = new RPSBlastMatch(signature, locations);


                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("rpsBlast match: " + match);
                }
                protein.addMatch(match);
                //LOGGER.debug("Protein with match: " + protein);
                entityManager.persist(match);
            }
        }
    }

    private Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> getSites(T rawMatch, Collection<R> rawSites){
        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new HashSet<>();
        if (rawSites != null) {
            for (R rawSite : rawSites) {
                if (rawMatch.getPssmId().equalsIgnoreCase(rawSite.getPssmId())) {
                    if (siteInLocationRange(rawMatch, rawSite)) {
                        final String siteTitle = rawSite.getTitle();
                        final String[] residueCoordinateList = rawSite.getResidues().split(",");
                        Set<SiteLocation> siteLocations = new HashSet<>();
                        for (String residueAnnot : residueCoordinateList) {
                            String residue = residueAnnot.substring(0, 1);
                            int position = Integer.parseInt(residueAnnot.substring(1));
                            SiteLocation siteLocation = new SiteLocation(residue, position, position);
                            siteLocations.add(siteLocation);
                        }
                        if (rawSite.getMappedSize() != rawSite.getCompleteSize()) {
                            LOGGER.debug("Raw site " + siteTitle + " mapped size " + rawSite.getMappedSize() + " did not match complete size " + rawSite.getCompleteSize());
                        }
                        RPSBlastMatch.RPSBlastLocation.RPSBlastSite site = new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(siteTitle, siteLocations);
                        if (site.getNumLocations() != rawSite.getMappedSize()) {
                            throw new IllegalStateException("Found " + site.getNumLocations() + " site locations for raw site " + siteTitle + " with residues " + rawSite.getResidues() + " when expected " + rawSite.getMappedSize());
                        }

                        rpsBlastSites.add(site);
                    }
                }
            }
        }
        return rpsBlastSites;
    }

}

