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

import org.apache.commons.lang3.SerializationUtils;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * HMMER3 filtered match data access object.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 */
abstract class Hmmer3FilteredMatchKVDAO<T extends Hmmer3RawMatch>
        extends FilteredMatchKVDAOImpl<Hmmer3Match, T>
        implements FilteredMatchKVDAO<Hmmer3Match, T> {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3FilteredMatchKVDAO.class.getName());


    public Hmmer3FilteredMatchKVDAO() {
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
        List<Protein> completeProteins = new ArrayList();

        Hmmer3Match repMatch = null;
        Hmmer3Match theMatch = null;

        //SignatureLibraryRelease signatureLibraryRelease = null; //getSignatureLibraryRelease();
        SignatureLibrary signatureLibraryRep = null;
        Long timeNow = System.currentTimeMillis();

        int matchLocationCount = 0;
        for (RawProtein<T> rp : filteredProteins) {
            Protein protein = proteinIdToProteinMap.get(rp.getProteinIdentifier());
            if (signatureLibraryRep == null) {
                Hmmer3RawMatch repRawMatch = (Hmmer3RawMatch) new ArrayList(rp.getMatches()).get(0);
                signatureLibraryRep = repRawMatch.getSignatureLibrary();
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("persist protein: " + protein.getId() + " md5:" + protein.getMd5());
            }
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rp.getProteinIdentifier() + "]");
            }
//            Utilities.verboseLog("modelAccessionToSignatureMap: " + modelAccessionToSignatureMap);
            // Convert raw matches to filtered matches
            Set<Hmmer3Match> filteredMatches = (HashSet<Hmmer3Match>)
                    Hmmer3RawMatch.getMatches(rp.getMatches(), new RawMatch.Listener() {
                                @Override
                                public Signature getSignature(String modelAccession,
                                                              SignatureLibrary signatureLibrary,
                                                              String signatureLibraryRelease) {
                                    Signature signature = modelAccessionToSignatureMap.get(modelAccession);

                                    if (signature == null) {
//                                TODO remove this temp check when gene3d  4.3.0 is stable
                                        LOGGER.error("this accession doesnt have a family or Signature acc:  " + modelAccession);
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
                for (T rawMatch : rp.getMatches()) {
                    if (!isLocationWithinRange(protein, rawMatch)) {
                        LOGGER.error("Location coordinates Error - sequenceLength: " + protein.getSequenceLength()
                                + " Location : " + rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd());
                        throw new IllegalStateException("Attempting to persist a match location outside sequence range " +
                                rawMatch.toString() + "\n" + protein.toString());
                    }
                }
                if (repMatch == null) {
                    repMatch = match;
                    Utilities.verboseLog("repMatch: " + repMatch);
                }
                matchLocationCount += match.getLocations().size();
            }
            String key = Long.toString(protein.getId()) + signatureLibraryRep.getName();
            //String key = ((String) pair.getKey()).replace(signatureLibrary.getName(), "").trim();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatches = SerializationUtils.serialize((HashSet<Hmmer3Match>) filteredMatches);
            //byteKeyToMatchMap.put(byteKey,byteMatches);
            persist(byteKey, byteMatches);

            //TODO use a different utitlity function
            //System.out.println(" Filtered Match locations size : - " + matchLocationCount);
        }

        Long timeTaken = System.currentTimeMillis() - timeNow;
        Long timeTakenSecs = timeTaken / 1000;
        Long timeTakenMins = timeTakenSecs / 60;
        System.out.println("Time taken to persist " + signatureLibraryRep.getName() + " matches to  levelDb : " + timeTakenSecs + " seconds ("
                + timeTakenMins + " minutes)");

        if (signatureLibraryRep != null) {
            //System.out.println("addSignatureLibraryName: " + signatureLibraryRep.getName());
            addSignatureLibraryName(signatureLibraryRep.getName());
        }
    }

    /**
     * Check if the location is withing the sequence length
     *
     * @param protein
     * @param rawMatch
     * @return
     */
    public boolean isLocationWithinRange(Protein protein, RawMatch rawMatch) {
        if (protein.getSequenceLength() < rawMatch.getLocationEnd() || protein.getSequenceLength() < rawMatch.getLocationStart()) {
            return false;
        }
        return true;
    }

}
