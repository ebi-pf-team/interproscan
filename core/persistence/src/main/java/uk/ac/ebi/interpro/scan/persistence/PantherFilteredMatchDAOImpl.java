package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */

public class PantherFilteredMatchDAOImpl extends FilteredMatchDAOImpl<PantherRawMatch, PantherMatch> implements PantherFilteredMatchDAO{

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public PantherFilteredMatchDAOImpl() {
        super(PantherMatch.class);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins      being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of signature accessions to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Override
    public void persist(Collection<RawProtein<PantherRawMatch>> filteredProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<PantherRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<PantherMatch.PantherLocation> locations = null;
            String currentSignatureAc = null;
            Signature currentSignature = null;
            PantherRawMatch lastRawMatch = null;
            for (PantherRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) {
                    continue;
                }
                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        protein.addMatch(new PantherMatch(currentSignature, locations, lastRawMatch.getEvalue(),
                                lastRawMatch.getFamilyName(), lastRawMatch.getScore()));
                    }
                    // Reset everything
                    locations = new HashSet<PantherMatch.PantherLocation>();
                    currentSignatureAc = rawMatch.getModelId();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PANTHER signature " + currentSignatureAc + " in the database.");
                    }
                }
                locations.add(new PantherMatch.PantherLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd()));
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                protein.addMatch(new PantherMatch(currentSignature, locations, lastRawMatch.getEvalue(),
                        lastRawMatch.getFamilyName(), lastRawMatch.getScore()));
            }
            entityManager.persist(protein);
            entityManager.flush();
        }
    }
}
