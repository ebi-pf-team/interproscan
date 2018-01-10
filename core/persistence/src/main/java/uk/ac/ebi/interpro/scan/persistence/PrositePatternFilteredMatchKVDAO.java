package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.ProSitePatternRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import org.apache.commons.lang3.SerializationUtils;

import java.util.*;

/**
 * @author Phil Jones, EMBL-EBI
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

public class PrositePatternFilteredMatchKVDAO
        extends FilteredMatchKVDAOImpl<PatternScanMatch, ProSitePatternRawMatch>
        implements FilteredMatchKVDAO<PatternScanMatch, ProSitePatternRawMatch> {

    public PrositePatternFilteredMatchKVDAO() {
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
    public void persist(Collection<RawProtein<ProSitePatternRawMatch>> filteredProteins, Map<String, Signature> modelAccessionToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        SignatureLibrary signatureLibraryRep = null;
        for (RawProtein<ProSitePatternRawMatch> rawProtein : filteredProteins) {
            if (signatureLibraryRep == null){
                ProSitePatternRawMatch repRawMatch =  (ProSitePatternRawMatch) new ArrayList(rawProtein.getMatches()).get(0);
                signatureLibraryRep = repRawMatch.getSignatureLibrary();
            }
            Set<PatternScanMatch> filteredMatches =  new HashSet<>();
            final Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            for (ProSitePatternRawMatch rawMatch : rawProtein.getMatches()) {
                Signature signature = modelAccessionToSignatureMap.get(rawMatch.getModelId());
                PatternScanMatch match = buildMatch(signature, rawMatch);
                filteredMatches.add(match);
            }
            String key =  Long.toString(protein.getId()) + signatureLibraryRep.getName();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatches = SerializationUtils.serialize((HashSet<PatternScanMatch>) filteredMatches);
            persist(byteKey,byteMatches);
        }
        if (filteredProteins.size() > 0){
            addSignatureLibraryName(signatureLibraryRep.getName());
	}
    }

    private PatternScanMatch buildMatch(Signature signature, ProSitePatternRawMatch rawMatch) {
        PatternScanMatch.PatternScanLocation location = new PatternScanMatch.PatternScanLocation(
                rawMatch.getLocationStart(),
                rawMatch.getLocationEnd(),
                rawMatch.getPatternLevel(),
                rawMatch.getCigarAlignment());
        return new PatternScanMatch(signature, Collections.singleton(location));
    }
}
