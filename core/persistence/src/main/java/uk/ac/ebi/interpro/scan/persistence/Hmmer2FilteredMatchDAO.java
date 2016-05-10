package uk.ac.ebi.interpro.scan.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Sep 16, 2010
 */

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Map;

abstract class Hmmer2FilteredMatchDAO<T extends Hmmer2RawMatch>
        extends FilteredMatchDAOImpl<T, Hmmer2Match>
        implements FilteredMatchDAO<T, Hmmer2Match> {

    public Hmmer2FilteredMatchDAO() {
        super(Hmmer2Match.class);
    }


    @Override
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }
            // Convert raw matches to filtered matches
            Collection<Hmmer2Match> filteredMatches =
                    Hmmer2RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
                        @Override
                        public Signature getSignature(String modelAccession,
                                                      SignatureLibrary signatureLibrary,
                                                      String signatureLibraryRelease) {
                            return modelAccessionToSignatureMap.get(modelAccession);
                        }
                    }
                    );
            // Add matches to protein
            for (Hmmer2Match m : filteredMatches) {
                protein.addMatch(m);  // Adds protein to match (yes, I know it doesn't look that way!)
                entityManager.persist(m);
            }
        }
    }
}
