package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;


import java.util.*;

/**
 * SuperFamily filtered match data access object.
 *
 * @author Matthew Fraser
 * @author Gift Nuka
 * @version $Id$
 */
public class SuperFamilyHmmer3FilteredMatchDAOImpl extends FilteredMatchDAOImpl<SuperFamilyHmmer3RawMatch, SuperFamilyHmmer3Match> implements SuperFamilyHmmer3FilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(SuperFamilyHmmer3FilteredMatchDAOImpl.class.getName());

    private String superFamilyReleaseVersion;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public SuperFamilyHmmer3FilteredMatchDAOImpl(String version) {
        super(SuperFamilyHmmer3Match.class);
        this.superFamilyReleaseVersion = version;
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins      being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of model IDs to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Transactional
    public void persist(Collection<RawProtein<SuperFamilyHmmer3RawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        int proteinCount = 0;
        int matchCount = 0;
        int sfBatchSize = 3000;
        Utilities.verboseLog(1100, "SuperFamilyHmmer3FilteredMatchDAO: Start to persist " + filteredProteins.size() + " filteredProteins,");

        for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein : filteredProteins) {
            proteinCount++;
            final Map<UUID, SuperFamilyHmmer3Match> splitGroupToMatch = new HashMap<>();

            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Protein: " + protein);
            }
            // Each line in the Superfamily ass3.pl Perl script output represents a Superfamily match for a given sequence
            // and model with e-value. We'd never have more than one location against a Superfamily match, but a location
            // could have multiple location fragments (so are in the same split group). Each Superfamily raw match
            // represents a Superfamily location fragment.
            for (SuperFamilyHmmer3RawMatch rawMatch : rawProtein.getMatches()) {
                final SignatureModelHolder holder = modelIdToSignatureMap.get(rawMatch.getModelId());
                Model model = holder.getModel();
                int hmmLength = model == null ? 0 : model.getLength();

                SuperFamilyHmmer3Match match = splitGroupToMatch.get(rawMatch.getSplitGroup());

                SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment locationFragment = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(
                        rawMatch.getLocationStart(),
                        rawMatch.getLocationEnd(),
                        DCStatus.parseSymbol(rawMatch.getLocFragmentDCStatus()));

                if (match == null) {
                    // This raw match is not part of an existing split group
                    final Signature currentSignature = holder.getSignature();
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find model " + rawMatch.getModelId() + " in the database.");
                    }
                    match = new SuperFamilyHmmer3Match(
                            currentSignature,
                            rawMatch.getModelId(),
                            rawMatch.getEvalue(),
                            null);
                    splitGroupToMatch.put(rawMatch.getSplitGroup(), match);

                    SuperFamilyHmmer3Match.SuperFamilyHmmer3Location location = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                            locationFragment, hmmLength);
                    match.addLocation(location);
                }
                else {
                    // Add model to the match
                    if (! match.getSignatureModels().contains(rawMatch.getModelId())) {
                        match.addSignatureModel(rawMatch.getModelId());
                    }
                    else {
                        Utilities.verboseLog(25, "Model " + rawMatch.getModelId() + " already in list: "
                                + match.getSignatureModels());
                    }

                    // This raw match is part of an existing split group, so add this fragment to the existing
                    // match locations
                    Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = match.getLocations();
                    if (locations == null || locations.size() != 1) {
                        throw new IllegalStateException("Superfamily match did not have one location as expected, but had " + (locations == null ? "NULL" : locations.size()));
                    }
                    Utilities.verboseLog(25,"locations: " + locations.toString());
                    SuperFamilyHmmer3Match.SuperFamilyHmmer3Location location = locations.iterator().next();
                    Utilities.verboseLog(25,"locationFragment: " + locationFragment.toString());
                    for (Object objFragment: location.getLocationFragments()){
                        LocationFragment cmprLocationFragment = (LocationFragment)  objFragment;
                        locationFragment.updateDCStatus(cmprLocationFragment);
                        cmprLocationFragment.updateDCStatus(locationFragment);
                    }
                    location.addLocationFragment(locationFragment);
                }
                matchCount++;
            }
            Set<Match> proteinMatches = new HashSet();
            String signatureLibraryKey = null;
            for (SuperFamilyHmmer3Match match : splitGroupToMatch.values()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("superfamily match: " + match);
                    LOGGER.debug("Protein with match: " + protein);
                }
//                Utilities.verboseLog(1100, "SuperFamilyHmmer3FilteredMatchDAO:" + "superfamily match: " + match.getSignature()
//                        + "locations size: " + match.getLocations().size()
//                        + " \nProtein with match: " + protein.getId());

                //protein.addMatch(match);
                proteinMatches.add(match);
                //entityManager.persist(match);
                if(signatureLibraryKey == null) {
                    signatureLibraryKey = match.getSignature().getSignatureLibraryRelease().getLibrary().getName();
                }
            }
            if(! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
                for(Match i5Match: proteinMatches){
                    //try update with cross refs etc
                    updateMatch(i5Match);
                }
                matchDAO.persist(dbKey, proteinMatches);
            }
        }
    }
}
