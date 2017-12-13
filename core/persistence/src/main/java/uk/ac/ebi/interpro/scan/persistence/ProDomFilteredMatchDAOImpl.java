package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ProDom CRUD database operations.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProDomFilteredMatchDAOImpl extends FilteredMatchDAOImpl<ProDomRawMatch, BlastProDomMatch> implements ProDomFilteredMatchDAO {

    private final String proDomReleaseVersion;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public ProDomFilteredMatchDAOImpl(String version) {
        super(BlastProDomMatch.class);
        this.proDomReleaseVersion = version;
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
    public void persist(Collection<RawProtein<ProDomRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<ProDomRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<BlastProDomMatch.BlastProDomLocation> locations = null;
            String currentModelId = null;
            SignatureModelHolder holder = null;
            Signature currentSignature = null;
            ProDomRawMatch lastRawMatch = null;
            BlastProDomMatch match = null;
            for (ProDomRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) {
                    continue;
                }

                if (currentModelId == null || !currentModelId.equals(rawMatch.getModelId())) {
                    if (currentModelId != null) {

                        // Not the first (because the currentSignatureAc is not null)
                        if (match != null) {
                            entityManager.persist(match); // Persist the previous one...
                        }
                        match = new BlastProDomMatch(currentSignature, locations);
                        // Not the first...
                        protein.addMatch(match);
                    }
                    // Reset everything
                    locations = new HashSet<BlastProDomMatch.BlastProDomLocation>();
                    currentModelId = rawMatch.getModelId();
                    holder = modelIdToSignatureMap.get(currentModelId);
                    currentSignature = holder.getSignature();
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find ProDom model " + currentModelId + " in the database.");
                    }
                }
                locations.add(
                        new BlastProDomMatch.BlastProDomLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getScore(),
                                rawMatch.getEvalue()
                        )
                );
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                match = new BlastProDomMatch(currentSignature, locations);
                protein.addMatch(match);
                entityManager.persist(match);
            }
        }
    }
}
