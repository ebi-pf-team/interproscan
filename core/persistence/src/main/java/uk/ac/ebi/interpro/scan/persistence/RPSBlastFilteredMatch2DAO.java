package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RPSBlastRawSite;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements the persistence method for RPSBlast matches (as filtered matches).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */

abstract class RPSBlastFilteredMatch2DAO<T extends RPSBlastRawMatch>
        extends FilteredMatchDAOImpl<T, RPSBlastMatch>
        implements FilteredMatchDAO<T, RPSBlastMatch> {

    private static final Logger LOGGER = Logger.getLogger(RPSBlastFilteredMatch2DAO.class.getName());


    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public RPSBlastFilteredMatch2DAO() {
        super(RPSBlastMatch.class);
//        this.releaseVersion = version;
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
    public void persist(Collection<RawProtein<T>> rawProteins, Map<String, Signature> modelIdToSignatureMap, Map<String, Protein> proteinIdToProteinMap) {
        for (RawProtein<T> rawProtein : rawProteins) {
            Protein protein = proteinIdToProteinMap.get(rawProtein.getProteinIdentifier());
            if (protein == null) {
                throw new IllegalStateException("Cannot store match to a protein that is not in database " +
                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
            }

            //LOGGER.debug("Protein: " + protein);
            Collection<T> rawMatches = rawProtein.getMatches();
//            if (rawMatches == null su|| rawMatches.size() != 1) {
//                throw new IllegalStateException("Protein did not have only one RPSBlast match! " +
//                        "[protein ID= " + rawProtein.getProteinIdentifier() + "]");
//            }
            for (T rawMatch : rawMatches) {
                // RPSBlast matches consist of a YES/NO result, therefore there will
                // only ever be 1 raw match for each protein, but never mind!
                if (rawMatch == null) continue;

                Signature signature = modelIdToSignatureMap.get(rawMatch.getModelId());
                LOGGER.debug("rpsBlast match model id:" + rawMatch.getModelId() + " signature: " + signature);
                LOGGER.debug("modelIdToSignatureMap: " + modelIdToSignatureMap);

                if (rawMatch.getModelId().startsWith("cl")) {
                    LOGGER.debug("this is a superfamily match, ignore for now ...");
                    continue;
                }
                Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<RPSBlastMatch.RPSBlastLocation>();
                locations.add(
                        new RPSBlastMatch.RPSBlastLocation(
                                rawMatch.getLocationStart(),
                                rawMatch.getLocationEnd(),
                                rawMatch.getBitScore(),
                                rawMatch.getEvalue()
                        )
                );
                RPSBlastMatch match = new RPSBlastMatch(signature, locations);
                LOGGER.debug("rpsBlast match: " + match);
                protein.addMatch(match);
                //LOGGER.debug("Protein with match: " + protein);
                entityManager.persist(match);
            }
        }
    }

}