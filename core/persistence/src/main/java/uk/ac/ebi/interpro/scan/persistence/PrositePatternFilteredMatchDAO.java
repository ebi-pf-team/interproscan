package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.ProSitePatternRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrositePatternFilteredMatchDAO
        extends FilteredMatchDAOImpl<ProSitePatternRawMatch, PatternScanMatch>
        implements FilteredMatchDAO<ProSitePatternRawMatch, PatternScanMatch> {

    public PrositePatternFilteredMatchDAO() {
        super(PatternScanMatch.class);
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     * @param modelAccessionToSignatureMap a Map of model accessions to Signature objects.
     * @param proteinIdToProteinMap        a Map of Protein IDs to Protein objects
     */
    @Override
    public void persist(Collection<RawProtein<ProSitePatternRawMatch>> filteredProteins, Map<String, SignatureModelHolder> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        SignatureLibrary signatureLibrary = null;
        for (RawProtein<ProSitePatternRawMatch> rawProtein : filteredProteins) {
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());

            for (ProSitePatternRawMatch rawMatch : rawProtein.getMatches()) {

                SignatureModelHolder holder = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                Utilities.verboseLog("rawMatch.getModelId() : "+ rawMatch.getModelId() +  " SignatureModelHolder: " + holder);
                Signature signature = holder.getSignature();
                if(signatureLibrary == null) {
                    signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                }
                PatternScanMatch match = buildMatch(signature, rawMatch);
                //hibernateInitialise
                hibernateInitialise(match);
                protein.addMatch(match);
                entityManager.persist(match);

            }
            Set<Match> proteinMatches = protein.getMatches();
            if (! proteinMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibrary.getName();
                Set<Match> matchSet = new HashSet<>(proteinMatches);
                matchDAO.persist(dbKey, matchSet);
            }
        }

    }

    private PatternScanMatch buildMatch(Signature signature, ProSitePatternRawMatch rawMatch) {
        PatternScanMatch.PatternScanLocation location = new PatternScanMatch.PatternScanLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd(),
                rawMatch.getPatternLevel(),
                rawMatch.getCigarAlignment());
        return new PatternScanMatch(signature, rawMatch.getModelId(), Collections.singleton(location));
    }
}
