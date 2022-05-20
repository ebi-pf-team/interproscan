package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @author Maxim Scheremetjew
 * @author Matthias Blum
 * @version $Id$
 * @since 1.0
 */

public class PantherFilteredMatchDAOImpl extends FilteredMatchDAOImpl<PantherRawMatch, PantherMatch> implements PantherFilteredMatchDAO {

    private static final Logger LOGGER = LogManager.getLogger(PantherFilteredMatchDAOImpl.class.getName());
    private String paintDirectory;

    public String getPaintDirectory() {
        return paintDirectory;
    }

    public void setPaintDirectory(String paintDirectory) {
        this.paintDirectory = paintDirectory;
    }

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
            String signatureLibraryKey = null;
            Set <Match> proteinMatches = new HashSet<>();
            for (PantherRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) {
                    continue;
                }
                // If the first raw match, or moved to a different match...
                if (currentSignatureAc == null || !currentSignatureAc.equals(rawMatch.getModelId())) {
                    if (currentSignatureAc != null) {
                        // Not the first...
                        match = new PantherMatch(
                                currentSignature,
                                currentSignatureAc,
                                locations,
                                lastRawMatch.getEvalue(),
                                lastRawMatch.getScore(),
                                lastRawMatch.getAnnotationsNodeId()
                        );
                        proteinMatches.add(match);
                    }
                    // Reset everything
                    locations = new HashSet<>();
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

                locations.add(new PantherMatch.PantherLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd(),
                        rawMatch.getHmmStart(), rawMatch.getHmmEnd(), rawMatch.getHmmLength(), HmmBounds.parseSymbol(rawMatch.getHmmBounds()),
                        rawMatch.getEnvelopeStart(), rawMatch.getEnvelopeEnd()));
                lastRawMatch = rawMatch;
                if(signatureLibraryKey == null){
                    signatureLibraryKey = currentSignature.getSignatureLibraryRelease().getLibrary().getName();
                }
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                match = new PantherMatch(
                        currentSignature,
                        currentSignatureAc,
                        locations,
                        lastRawMatch.getEvalue(),
                        lastRawMatch.getScore(),
                        lastRawMatch.getAnnotationsNodeId()
                );
                proteinMatches.add(match);
            }
            final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;

            if (!proteinMatches.isEmpty()) {
                for(Match i5Match: proteinMatches){
                    //try update with cross refs etc
                    updateMatch(i5Match);
                }
                matchDAO.persist(dbKey, proteinMatches);
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
