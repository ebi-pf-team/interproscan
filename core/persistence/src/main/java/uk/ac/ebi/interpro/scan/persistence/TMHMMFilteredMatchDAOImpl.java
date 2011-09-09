package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMProtein;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

import java.util.Set;

/**
 * Implements the persistence method for TMHMMProtein objects
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class TMHMMFilteredMatchDAOImpl extends GenericDAOImpl<TMHMMMatch, Long> implements TMHMMFilteredMatchDAO {

    private static final Logger LOGGER = Logger.getLogger(TMHMMFilteredMatchDAOImpl.class.getName());

    /**
     * The version number of TMHMM being run.
     */
    private String modelVersion;


    public TMHMMFilteredMatchDAOImpl(String modelVersion) {
        super(TMHMMMatch.class);
        this.modelVersion = modelVersion;
    }

    @Transactional
    public void persist(Set<TMHMMProtein> proteins) {
        // Retrieve (or create and persist) PhobiusSignatures.
//        Map<PhobiusFeatureType, Signature> featureTypeToSignatureMap = loadPersistedSignatures();
//        Map<String, Protein> proteinIdToProteinMap = getProteinIdToProteinMap(phobiusProteins);
//        for (PhobiusProtein phobiusProtein : phobiusProteins) {
//            final Protein persistentProtein = proteinIdToProteinMap.get(phobiusProtein.getProteinIdentifier());
//            if (persistentProtein == null) {
//                throw new IllegalArgumentException("Attempting to store a Phobius match for a protein with id " + phobiusProtein.getProteinIdentifier() + ", however this does not exist in the database.");
//            }
//            for (PhobiusFeature feature : phobiusProtein.getFeatures()) {
//                final Signature signature = featureTypeToSignatureMap.get(feature.getFeatureType());
//                Set<PhobiusMatch.PhobiusLocation> locations = Collections.singleton(
//                        new PhobiusMatch.PhobiusLocation(feature.getStart(), feature.getStop())
//                );
//                PhobiusMatch match = new PhobiusMatch(signature, locations);
//                persistentProtein.addMatch(match);
//            }
//            entityManager.persist(persistentProtein);
//        }
//        entityManager.flush();
    }
}
