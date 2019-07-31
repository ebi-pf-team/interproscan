package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public void persist(Collection<RawProtein<T>> filteredProteins, final Map<String, SignatureModelHolder> modelAccessionToSignatureMap, final Map<String, Protein> proteinIdToProteinMap) {
        // Add matches to protein
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
            String signatureLibraryKey = null;

            // Convert raw matches to filtered matches
            Collection<T> rawMatches = rp.getMatches();
            for (T rawMatch: rawMatches){
                if (! isLocationWithinRange(protein, rawMatch)){
                    LOGGER.error("Location coordinates Error - sequenceLength: " + protein.getSequenceLength()
                            +  " Location : " + rawMatch.getLocationStart() + "-" +  rawMatch.getLocationEnd());
                    throw new IllegalStateException("Attempting to persist a match location outside sequence range " +
                            rawMatch.toString() + "\n" + protein.toString());
                }
                if(signatureLibraryKey == null){
                    signatureLibraryKey = rawMatch.getSignatureLibrary().getName();
                }

            }
            Collection<Hmmer3Match> filteredMatches = Hmmer3RawMatch.getMatches(rawMatches, modelAccessionToSignatureMap);

            if(! (filteredMatches == null && filteredMatches.isEmpty())) {
                Set<Match> proteinMatches = new HashSet(filteredMatches);
                for(Match i5Match: proteinMatches){
                    //try update with cross refs etc
                    updateMatch(i5Match);
                }
                final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
                matchDAO.persist(dbKey, proteinMatches);
            }
            /*
            for (Hmmer3Match match : filteredMatches) {

                //hibernateInitialise
                hibernateInitialise(match);
                protein.addMatch(match); // Adds protein to match (yes, I know it doesn't look that way!)

            }
            persist(protein, signatureLibraryKey);

            */

//            final String dbKey = Long.toString(protein.getId()) + signatureLibraryKey;
//            Utilities.verboseLog("persisted matches in kvstore for key: " + dbKey);
//            Set <Match> proteinMatches = protein.getMatches();
//            if (proteinMatches != null || proteinMatches.isEmpty()) {
//                Utilities.verboseLog("persisted matches in kvstore for key: " + dbKey + " : " + proteinMatches.size());
//                Set<Match> matchSet = new HashSet<>(); // the protein.get Matches is a persistentSet, but we want a hashset
//                matchSet.addAll(proteinMatches);
//                matchDAO.persist(dbKey, matchSet);
//            }

            //TODO use a different utility function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }
    }


}
