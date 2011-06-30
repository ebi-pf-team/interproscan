package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
* SuperFamily filtered match data access object.
*
* @author  Matthew Fraser
* @version $Id$
*/
public class SuperFamilyHmmer3FilteredMatchDAOImpl extends FilteredMatchDAOImpl<SuperFamilyHmmer3RawMatch, SuperFamilyHmmer3Match> implements SuperFamilyHmmer3FilteredMatchDAO {

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
     * @param modelIdToSignatureMap a Map of signature accessions to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Transactional
    public void persist(Collection<RawProtein<SuperFamilyHmmer3RawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = null;
            String currentSignatureAc = null;
            Double currentEvalue = null;
            Signature currentSignature = null;
            SuperFamilyHmmer3RawMatch lastRawMatch = null;
            for (SuperFamilyHmmer3RawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) continue;

                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null && currentEvalue != null) {
                        // Not the first...
                        protein.addMatch(new SuperFamilyHmmer3Match(currentSignature, currentEvalue, locations));
                    }
                    // Reset everything
                    locations = new HashSet<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>();
                    currentSignatureAc = rawMatch.getModelId();
                    currentEvalue = rawMatch.getEvalue();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find signature " + currentSignatureAc + " in the database.");
                    }
                }
                locations.add(
                        new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd()
                        )
                );
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                protein.addMatch(new SuperFamilyHmmer3Match(currentSignature, currentEvalue, locations));
            }
            entityManager.persist(protein);
            entityManager.flush();
        }
    }

}
