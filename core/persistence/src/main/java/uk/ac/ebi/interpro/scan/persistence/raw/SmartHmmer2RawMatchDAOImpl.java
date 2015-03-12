package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Sep 16, 2010
 * Time: 11:18:18 AM
 */

public class SmartHmmer2RawMatchDAOImpl extends RawMatchDAOImpl<SmartRawMatch> implements SmartHmmer2RawMatchDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public SmartHmmer2RawMatchDAOImpl() {
        super(SmartRawMatch.class);
    }

    /**
     * Returns a Map of sequence identifiers to a List of
     * SmartRawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         SmartRawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<SmartRawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<SmartRawMatch>> proteinIdToMatchMap = new HashMap<String, RawProtein<SmartRawMatch>>();
        Query query = entityManager
                .createQuery("select p from SmartRawMatch p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease")
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<SmartRawMatch> resultList = query.getResultList();
        for (SmartRawMatch match : resultList) {
            final String proteinId = match.getSequenceIdentifier();
            RawProtein<SmartRawMatch> rawProtein = proteinIdToMatchMap.get(proteinId);
            if (rawProtein == null) {
                rawProtein = new RawProtein<SmartRawMatch>(proteinId);
                proteinIdToMatchMap.put(proteinId, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }
}
