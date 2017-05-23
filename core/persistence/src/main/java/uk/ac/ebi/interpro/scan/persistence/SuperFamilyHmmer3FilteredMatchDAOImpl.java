package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;

import java.util.*;

/**
 * SuperFamily filtered match data access object.
 *
 * @author Matthew Fraser
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
    public void persist(Collection<RawProtein<SuperFamilyHmmer3RawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein : filteredProteins) {
            final Map<UUID, SuperFamilyHmmer3Match> splitGroupToMatch = new HashMap<>();

            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
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
                SuperFamilyHmmer3Match match = splitGroupToMatch.get(rawMatch.getSplitGroup());

                SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment locationFragment = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(
                        rawMatch.getLocationStart(),
                        rawMatch.getLocationEnd());

                if (match == null) {
                    // This raw match is not part of an existing split group
                    final Signature currentSignature = modelIdToSignatureMap.get(rawMatch.getModelId());
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find model " + rawMatch.getModelId() + " in the database.");
                    }
                    match = new SuperFamilyHmmer3Match(
                            currentSignature,
                            rawMatch.getEvalue(),
                            null);
                    splitGroupToMatch.put(rawMatch.getSplitGroup(), match);

                    SuperFamilyHmmer3Match.SuperFamilyHmmer3Location location = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                            locationFragment);
                    match.addLocation(location);
                }
                else {
                    // This raw match is part of an existing split group, so add this fragment to the existing
                    // match location
                    Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = match.getLocations();
                    if (locations == null || locations.size() != 1) {
                        throw new IllegalStateException("Superfamily match did not have one location as expected, but had " + (locations == null ? "NULL" : locations.size()));
                    }
                    SuperFamilyHmmer3Match.SuperFamilyHmmer3Location location = locations.iterator().next();
                    location.addLocationFragment(locationFragment);
                }
            }

            for (SuperFamilyHmmer3Match match : splitGroupToMatch.values()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Superfamily match: " + match);
                    LOGGER.debug("Protein with match: " + protein);
                }
                protein.addMatch(match);
                entityManager.persist(match);
            }
        }
    }
}
