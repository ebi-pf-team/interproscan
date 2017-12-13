package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

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

public class PantherFilteredMatchDAOImpl extends FilteredMatchDAOImpl<PantherRawMatch, PantherMatch> implements PantherFilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(PantherFilteredMatchDAOImpl.class.getName());

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
    public void persist(Collection<RawProtein<PantherRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<PantherRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            Set<PantherMatch.PantherLocation> locations = null;
            String currentSignatureAc = null;
            SignatureModelHolder holder = null;
            Signature currentSignature = null;
            PantherRawMatch lastRawMatch = null;
            PantherMatch match = null;
            for (PantherRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) {
                    continue;
                }
                // If the first raw match, or moved to a different match...
                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        if (match != null) {
                            entityManager.persist(match);  // Persist the previous one.
                        }
                        match = new PantherMatch(
                                currentSignature,
                                locations,
                                lastRawMatch.getEvalue(),
                                lastRawMatch.getFamilyName(),
                                lastRawMatch.getScore()
                        );
                        protein.addMatch(match);
                    }
                    // Reset everything
                    locations = new HashSet<PantherMatch.PantherLocation>();
                    currentSignatureAc = rawMatch.getModelId();
                    holder = modelIdToSignatureMap.get(currentSignatureAc);
                    currentSignature = holder.getSignature();
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PANTHER signature " + currentSignatureAc + " in the database.");
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(" protein length = " + protein.getSequenceLength()
                            + " start location of raw match : " + rawMatch.getLocationStart() + " end location of raw match : " + rawMatch.getLocationEnd());
                }
                if (!pantherLocationWithinRange(protein, rawMatch)) {
                    LOGGER.error("PANTHER match is out of range: "
                            + " protein length = " + protein.getSequenceLength()
                            + " raw match : " + rawMatch.toString());
                    throw new IllegalStateException("PANTHER match location is out of range " + currentSignatureAc
                            + " protein length = " + protein.getSequenceLength()
                            + " raw match : " + rawMatch.toString());
                }
                locations.add(new PantherMatch.PantherLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd()));
                lastRawMatch = rawMatch;
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                match = new PantherMatch(
                        currentSignature,
                        locations,
                        lastRawMatch.getEvalue(),
                        lastRawMatch.getFamilyName(),
                        lastRawMatch.getScore()
                );
                protein.addMatch(match);
                entityManager.persist(match);       // Persist the last one
            }
        }
    }

    /**
     * check if the location is withing the sequence length
     *
     * @param protein
     * @param rawMatch
     * @return
     */
    public boolean pantherLocationWithinRange(Protein protein, RawMatch rawMatch){
        if (protein.getSequenceLength() < rawMatch.getLocationEnd() || protein.getSequenceLength() < rawMatch.getLocationStart()){
            return false;
        }
        return true;
    }

}
