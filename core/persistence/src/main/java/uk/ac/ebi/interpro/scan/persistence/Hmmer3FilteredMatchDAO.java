package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

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

    private static final Logger LOGGER = Logger.getLogger(Hmmer3FilteredMatchDAO.class.getName());

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
    @Transactional
    public void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, Signature> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");
            }
//            Utilities.verboseLog("modelAccessionToSignatureMap: " + modelAccessionToSignatureMap);
            // Convert raw matches to filtered matches
            Collection<Hmmer3Match> filteredMatches =
                    Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
                        @Override
                        public Signature getSignature(String modelAccession,
                                                      SignatureLibrary signatureLibrary,
                                                      String signatureLibraryRelease) {
                            Signature signature = modelAccessionToSignatureMap.get(modelAccession);
                            if (signature == null) {
                                throw new IllegalStateException("Attempting to persist a match to " + modelAccession + " however this has not been found in the database.");
                            }
                            Utilities.verboseLog("signature: " + signature + " from - " + modelAccession );
                            //why not return just signature
                            return modelAccessionToSignatureMap.get(modelAccession);
                        }
                    }
                    );

            int matchLocationCount = 0;
            for (Hmmer3Match match : filteredMatches) {
                for (T rawMatch: rp.getMatches()){
                    if (! isLocationWithinRange(protein, rawMatch)){
                        LOGGER.error("Location coordinates Error - sequenceLength: " + protein.getSequenceLength()
                                +  " Location : " + rawMatch.getLocationStart() + "-" +  rawMatch.getLocationEnd());
                        throw new IllegalStateException("Attempting to persist a match location outside sequence range " +
                        rawMatch.toString() + "\n" + protein.toString());
                    }
                }
                protein.addMatch(match); // Adds protein to match (yes, I know it doesn't look that way!)
                entityManager.persist(match);
                matchLocationCount += match.getLocations().size();
            }
            //TODO use a different utitlity function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }
    }
}
