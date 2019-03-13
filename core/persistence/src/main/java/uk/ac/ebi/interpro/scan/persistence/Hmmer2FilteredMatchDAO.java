package uk.ac.ebi.interpro.scan.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Sep 16, 2010
 */

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class Hmmer2FilteredMatchDAO<T extends Hmmer2RawMatch>
        extends FilteredMatchDAOImpl<T, Hmmer2Match>
        implements FilteredMatchDAO<T, Hmmer2Match> {

    public Hmmer2FilteredMatchDAO() {
        super(Hmmer2Match.class);
    }


    @Override
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, SignatureModelHolder> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
        SignatureLibrary signatureLibrary = null;
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            Set<Match> proteinMatches = new HashSet<>();
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }
            // Convert raw matches to filtered matches
            Collection<Hmmer2Match> filteredMatches =
                    Hmmer2RawMatch.getMatches(rp.getMatches(), modelAccessionToSignatureMap);
            // Add matches to protein
            for (Hmmer2Match match : filteredMatches) {
                //protein.addMatch(match);  // Adds protein to match (yes, I know it doesn't look that way!)
                //proteinMatches.add(match);
                if(signatureLibrary == null) {
                    signatureLibrary = match.getSignature().getSignatureLibraryRelease().getLibrary();
                }
                proteinMatches.add(match);
                //entityManager.persist(match);
            }
            if(! filteredMatches.isEmpty()) {
                final String dbKey = Long.toString(protein.getId()) + signatureLibrary.getName();
                matchDAO.persist(dbKey, proteinMatches);
            }
        }
    }
}
