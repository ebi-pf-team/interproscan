package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignalPMatch;
import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;
import uk.ac.ebi.interpro.scan.model.helper.SignatureModelHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DAO implementation for SignalP filtered matches.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPFilteredMatchDAOImpl extends FilteredMatchDAOImpl<SignalPRawMatch, SignalPMatch> implements SignalPFilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(SignalPFilteredMatchDAOImpl.class.getName());

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
    public SignalPFilteredMatchDAOImpl(String version) {
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
    public void persist(Collection<RawProtein<SignalPRawMatch>> rawProteins, Map<String, SignatureModelHolder> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
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
            for (SignalPRawMatch rawMatch : rawMatches) {
                // SignalP matches consist of a YES/NO result, therefore there will
                // only ever be 1 raw match for each protein, but never mind!
                if (rawMatch == null) continue;

                SignatureModelHolder holder = modelIdToSignatureMap.get(rawMatch.getModelId());
                Signature signature = holder.getSignature();

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
                SignalPMatch match = new SignalPMatch(signature, rawMatch.getModelId(), organismType, locations);
                protein.addMatch(match);
                entityManager.persist(match);
            }
        }
    }

}
