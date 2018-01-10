package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class PantherFilteredMatchKVDAO extends FilteredMatchKVDAOImpl<PantherMatch, PantherRawMatch>
        implements FilteredMatchKVDAO<PantherMatch, PantherRawMatch> {

    private static final Logger LOGGER = Logger.getLogger(PantherFilteredMatchKVDAO.class.getName());

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public PantherFilteredMatchKVDAO() {
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
        Utilities.verboseLog("Start Panther persist");

        String signatureLibraryName  = SignatureLibrary.PANTHER.getName();

        for (RawProtein<PantherRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            HashSet<PantherMatch> filteredMatches = new HashSet<>();
            Set<PantherMatch.PantherLocation> locations = null;
            String currentSignatureAc = null;
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
                            //entityManager.persist(match);  // Persist the previous one.
                        }
                        match = new PantherMatch(
                                currentSignature,
                                locations,
                                lastRawMatch.getEvalue(),
                                lastRawMatch.getFamilyName(),
                                lastRawMatch.getScore()
                        );
                        filteredMatches.add(match);
                    }
                    // Reset everything
                    locations = new HashSet<PantherMatch.PantherLocation>();
                    currentSignatureAc = rawMatch.getModelId();
                    currentSignature = modelIdToSignatureMap.get(currentSignatureAc);
                    if (currentSignature == null) {
                        throw new IllegalStateException("Cannot find PANTHER signature " + currentSignatureAc + " in the database.");
                    }
                }
                LOGGER.debug(" protein length = " + protein.getSequenceLength()
                        + " start location of raw match : " + rawMatch.getLocationStart() + " end location of raw match : " + rawMatch.getLocationEnd());
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
                filteredMatches.add(match);       // Persist the last one
            }
            //now persist the matches for this protein
            String key = Long.toString(protein.getId()) + signatureLibraryName;
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatches = SerializationUtils.serialize(filteredMatches);
            persist(byteKey, byteMatches);
        }
        if (filteredProteins.size() > 0) {
            Utilities.verboseLog("SignatureLibrary  to add: " + signatureLibraryName);
            addSignatureLibraryName(signatureLibraryName);
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
