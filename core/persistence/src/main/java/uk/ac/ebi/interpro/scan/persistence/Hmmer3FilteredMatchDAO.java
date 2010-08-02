package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Map;

/**
 * HMMER3 filtered match data access object.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
abstract class Hmmer3FilteredMatchDAO<T extends Hmmer3RawMatch>
        extends FilteredMatchDAOImpl<T, Hmmer3Match>
        implements FilteredMatchDAO<T, Hmmer3Match> {

    public Hmmer3FilteredMatchDAO() {
        super(Hmmer3Match.class);
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
    protected void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");

            }
            // Convert raw matches to filtered matches
            Collection<Hmmer3Match> filteredMatches =
                    Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
                        @Override
                        public Signature getSignature(String modelAccession,
                                                      SignatureLibrary signatureLibrary,
                                                      String signatureLibraryRelease) {
                            return modelAccessionToSignatureMap.get(modelAccession);
                        }
                    }
                    );
            // Add matches to protein
            for (Hmmer3Match m : filteredMatches) {
                protein.addMatch(m);
            }
            // Store
            entityManager.persist(protein);
        }
    }
}
