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

    /**
     * TODO: METHOD NOT FINISHED!
     * <p/>
     * Note: This method needs access to the protein sequence to biuld the Protein objects.
     * It's possible that it could take a ProteinDAO as a parameter, to allow this to be looked up...
     * <p/>
     * ... more likely though that this will end up being a DAO in its own right, so it can store
     * the matches directly into the database.
     *
     * @param berkeleyMatches being a
     */
    @Transactional
    public void populateProteinMatches(Set<BerkeleyMatch> berkeleyMatches) {

        // Collection of BerkeleyMatches of different kinds.
        // Iterate over them,
        for (BerkeleyMatch berkeleyMatch : berkeleyMatches) {
            Protein protein = null;  // TODO - Retrieve the protein from the database.
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
