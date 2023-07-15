package uk.ac.ebi.interpro.scan.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public void persist(Collection<RawProtein<PantherRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap,
                        Map<String, Protein> proteinIdToProteinMap) {
        Map<String, Set<Match>> toPersist = new HashMap<>();
        Map<String, Set<PantherMatch>> signatureToMatches = new HashMap<>();

        for (RawProtein<PantherRawMatch> rawProtein : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }

            Set<PantherMatch.PantherLocation> locations = null;
            String matchId = null;
            Signature signature = null;
            PantherRawMatch lastRawMatch = null;
            PantherMatch match = null;
            String signatureLibraryKey = null;
            Set <Match> proteinMatches = new HashSet<>();
            for (PantherRawMatch rawMatch : rawProtein.getMatches()) {
                if (rawMatch == null) {
                    continue;
                }
                // If the first raw match, or moved to a different match...
                if (matchId == null || !matchId.equals(rawMatch.getModelId())) {
                    if (matchId != null) {
                        // Not the first...
                        match = new PantherMatch(
                                signature,
                                matchId,
                                locations,
                                lastRawMatch.getEvalue(),
                                lastRawMatch.getScore(),
                                lastRawMatch.getAnnotationsNodeId()
                        );

                        if (!signatureToMatches.containsKey(signature.getAccession())) {
                            signatureToMatches.put(signature.getAccession(), new HashSet<>());
                        }
                        signatureToMatches.get(signature.getAccession()).add(match);
                        proteinMatches.add(match);
                    }
                    // Reset everything
                    locations = new HashSet<>();
                    matchId = rawMatch.getModelId();
                    signature = modelIdToSignatureMap.get(matchId).getSignature();
                    if (signature == null) {
                        throw new IllegalStateException("Cannot find PANTHER signature " + matchId + " in the database.");
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
                    throw new IllegalStateException("PANTHER match location is out of range " + matchId
                            + " protein length = " + protein.getSequenceLength()
                            + " raw match : " + rawMatch.toString());
                }

                locations.add(new PantherMatch.PantherLocation(rawMatch.getLocationStart(), rawMatch.getLocationEnd(),
                        rawMatch.getHmmStart(), rawMatch.getHmmEnd(), rawMatch.getHmmLength(), HmmBounds.parseSymbol(rawMatch.getHmmBounds()),
                        rawMatch.getEnvelopeStart(), rawMatch.getEnvelopeEnd()));
                lastRawMatch = rawMatch;
                if(signatureLibraryKey == null){
                    signatureLibraryKey = signature.getSignatureLibraryRelease().getLibrary().getName();
                }
            }
            // Don't forget the last one!
            if (lastRawMatch != null) {
                match = new PantherMatch(
                        signature,
                        matchId,
                        locations,
                        lastRawMatch.getEvalue(),
                        lastRawMatch.getScore(),
                        lastRawMatch.getAnnotationsNodeId()
                );
                if (!signatureToMatches.containsKey(signature.getAccession())) {
                    signatureToMatches.put(signature.getAccession(), new HashSet<>());
                }
                signatureToMatches.get(signature.getAccession()).add(match);
                proteinMatches.add(match);
            }
            final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;

            if (!proteinMatches.isEmpty()) {
                for(Match i5Match: proteinMatches){
                    //try update with cross refs etc
                    updateMatch(i5Match);
                }

                toPersist.put(dbKey, proteinMatches);
            }
        }

        for (String accession: signatureToMatches.keySet()) {
            File file = new File(this.getPaintDirectory() + "/" + accession + ".json");

            if (file.isFile()) {
                Map<String, String[]> familyAnnotations;
                ObjectMapper mapper = new ObjectMapper();

                try {
                    familyAnnotations = mapper.readValue(file, new TypeReference<>() {});
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                signatureToMatches.get(accession).forEach(
                        match -> match.addAnnotations(familyAnnotations)
                );
            }
        }

        for (Map.Entry<String, Set<Match>> entry: toPersist.entrySet()) {
            matchDAO.persist(entry.getKey(), entry.getValue());
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
