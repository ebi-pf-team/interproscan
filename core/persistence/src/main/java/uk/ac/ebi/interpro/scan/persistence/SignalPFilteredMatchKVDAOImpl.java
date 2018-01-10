package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;

import org.apache.commons.lang3.SerializationUtils;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * DAO implementation for SignalP filtered matches.
 *
 * @author Gift Nuka
 * @since 1.0-SNAPSHOT
 */
public class SignalPFilteredMatchKVDAOImpl extends FilteredMatchKVDAOImpl<SignalPMatch,SignalPRawMatch>
        implements FilteredMatchKVDAO<SignalPMatch,SignalPRawMatch> {

    private static final Logger LOGGER = Logger.getLogger(SignalPFilteredMatchKVDAOImpl.class.getName());

    private final String signalPReleaseVersion;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public SignalPFilteredMatchKVDAOImpl(String version) {
        super(SignalPMatch.class);
        this.signalPReleaseVersion = version;
    }

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param rawProteins           being the Collection of filtered RawProtein objects to persist
     * @param modelIdToSignatureMap a Map of model IDs to Signature objects.
     * @param proteinIdToProteinMap a Map of Protein IDs to Protein objects
     */
    @Transactional
    public void persist(Collection<RawProtein<SignalPRawMatch>> rawProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        SignatureLibrary signatureLibraryRep = null;
        for (RawProtein<SignalPRawMatch> rawProtein : rawProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }

            Collection<SignalPRawMatch> rawMatches = rawProtein.getMatches();
            if (rawMatches == null || rawMatches.size() != 1) {
                throw new IllegalStateException("Protein did not have only one SignalP match! " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }
            if (signatureLibraryRep == null){
                SignalPRawMatch repRawMatch =  (SignalPRawMatch) new ArrayList(rawMatches).get(0);
                signatureLibraryRep = repRawMatch.getSignatureLibrary();
            }
            HashSet<SignalPMatch> proteinMatches = new HashSet<>();
            for (SignalPRawMatch rawMatch : rawMatches) {
                // SignalP matches consist of a YES/NO result, therefore there will
                // only ever be 1 raw match for each protein, but never mind!
                if (rawMatch == null) continue;

                Signature signature = modelIdToSignatureMap.get(rawMatch.getModelId());

                SignalPOrganismType organismType = rawMatch.getOrganismType();
                if (organismType == null) {
                    throw new IllegalStateException("SignalP match organism type was null! " +
                            "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
                }

                Set<SignalPMatch.SignalPLocation> locations = new HashSet<SignalPMatch.SignalPLocation>();
                locations.add(
                        new SignalPMatch.SignalPLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getdScore()
                        )
                );
                SignalPMatch match = new SignalPMatch(signature, organismType, locations);
                proteinMatches.add(match);
            }

            String key = Long.toString(protein.getId()) + signatureLibraryRep.getName();
            byte[] byteKey = SerializationUtils.serialize(key);
            byte[] byteMatch = SerializationUtils.serialize((HashSet<SignalPMatch>) proteinMatches);
            persist(byteKey, byteMatch);
        }
        if (rawProteins.size() > 0) {
            addSignatureLibraryName(signatureLibraryRep.getName());
        }
    }

}
