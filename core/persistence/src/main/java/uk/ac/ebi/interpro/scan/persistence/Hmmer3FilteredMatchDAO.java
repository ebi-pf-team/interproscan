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
import java.util.List;
import java.util.ArrayList;

/**
 * HMMER3 filtered match data access object.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 */
abstract class Hmmer3FilteredMatchDAO<T extends Hmmer3RawMatch>
        extends FilteredMatchDAOImpl<T, Hmmer3Match>
        implements FilteredMatchDAO<T, Hmmer3Match> {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3FilteredMatchDAO.class.getName());

    LevelDBStore levelDBStore;
    
    public Hmmer3FilteredMatchDAO() {
        super(Hmmer3Match.class);
    }

/*    public Hmmer3FilteredMatchDAO(LevelDBStore levelDBStore) {
        this.levelDBStore = levelDBStore;
        super(Hmmer3Match.class);
    }
*/

    public void setLevelDBStore(LevelDBStore levelDBStore) {
        this.levelDBStore = levelDBStore;
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
        List<Protein> completeProteins = new ArrayList();

        Hmmer3Match repMatch = null;
        Hmmer3Match theMatch = null;

        int matchLocationCount = 0;
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("persist protein: " + protein.getId() + " md5:" + protein.getMd5());
            }
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
//                                TODO remove this temp check when gene3d  4.3.0 is stable
                                LOGGER.error("this accession doesnt have a family or Signature acc:  " + modelAccession );
                                throw new IllegalStateException("Attempting to persist a match to " + modelAccession + " however this has not been found in the database.");
                            }
                            //  Utilities.verboseLog("signature: " + signature + " from - " + modelAccession );
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("signature: " + signature + " from - " + modelAccession);
                            }
                            //why not return just signature, changed this tor eturn the signature
                            return signature;
//                            return modelAccessionToSignatureMap.get(modelAccession);
                        }
                    }
                    );

            matchLocationCount = 0;
            for (Hmmer3Match match : filteredMatches) {
                for (T rawMatch: rp.getMatches()){
                    if (! isLocationWithinRange(protein, rawMatch)){
                        LOGGER.error("Location coordinates Error - sequenceLength: " + protein.getSequenceLength()
                                +  " Location : " + rawMatch.getLocationStart() + "-" +  rawMatch.getLocationEnd());
                        throw new IllegalStateException("Attempting to persist a match location outside sequence range " +
                        rawMatch.toString() + "\n" + protein.toString());
                    }
                }
                if (repMatch == null) {
                    repMatch = match;
                    Utilities.verboseLog("repMatch: " + repMatch);
                }
                protein.addMatch(match); // Adds protein to match (yes, I know it doesn't look that way!)
                entityManager.persist(match);
                matchLocationCount += match.getLocations().size();
                completeProteins.add(protein);
                if (theMatch == null) {
                    theMatch = match;
                }
            }
            //TODO use a different utitlity function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }

        Utilities.verboseLog("theMatch:" + theMatch);
        //Utilities.verbose("Start persist to leveldb: " + completeProteins.size() + " proteins and " + matchLocationCount);
        System.out.println("Start persist to leveldb: " + completeProteins.size() + " proteins and " + matchLocationCount);

	Long timeNow = System.currentTimeMillis();
        for (Protein protein:completeProteins) {
            String key = String.valueOf(protein.getId());
            byte[] data = levelDBStore.serialize(protein);
	    levelDBStore.put(key, data);	   
	}
        Long   timeTaken = System.currentTimeMillis() - timeNow;
        Long    timeTakenSecs = timeTaken / 1000;
        Long    timeTakenMins = timeTakenSecs / 60;
        System.out.println("Time taken to persist to  levelDb : " + timeTakenSecs + " seconds ("
                    + timeTakenMins + " minutes)");
    }


}
