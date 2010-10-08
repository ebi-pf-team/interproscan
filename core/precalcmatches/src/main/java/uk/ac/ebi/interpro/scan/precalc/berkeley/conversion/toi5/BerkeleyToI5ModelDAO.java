package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class BerkeleyToI5ModelDAO {

    private Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter;

    @Required
    public void setSignatureLibraryToMatchConverter(Map<SignatureLibrary, BerkeleyMatchConverter> signatureLibraryToMatchConverter) {
        this.signatureLibraryToMatchConverter = signatureLibraryToMatchConverter;
    }

    @Transactional
    public void populateProteinMatches(Protein protein, Set<BerkeleyMatch> berkeleyMatches) {
        // Collection of BerkeleyMatches of different kinds.
        // Iterate over them,
        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
            // determine the type or the match currently being observed
            // Retrieve the appropriate converter to turn the BerkeleyMatch into an I5 match
            // Type is based upon the member database type.
            SignatureLibrary sigLib = SignatureLibraryLookup.lookupSignatureLibrary(berkeleyMatch.getSignatureLibraryName());
            BerkeleyMatchConverter matchConverter = signatureLibraryToMatchConverter.get(sigLib);
            Match i5Match = matchConverter.convertMatch(berkeleyMatch);
            protein.addMatch(i5Match);
        }
    }

}
