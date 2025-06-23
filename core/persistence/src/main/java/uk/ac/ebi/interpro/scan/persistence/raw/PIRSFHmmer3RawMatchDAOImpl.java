package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DAO implementation for PirsfHmmer3RawMatchDAO objects.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public class PIRSFHmmer3RawMatchDAOImpl
        extends RawMatchDAOImpl<PIRSFHmmer3RawMatch>
        implements PIRSFHmmer3RawMatchDAO {

    public PIRSFHmmer3RawMatchDAOImpl() {
        super(PIRSFHmmer3RawMatch.class);
    }

    /**
     * Returns a Map of sequence identifiers to a List of
     * PirsfHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for PIRSF HMMER3 post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PirsfHmmer3RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PIRSFHmmer3RawMatch>> getRawMatchesForProteinIdsInRange(
            long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<PIRSFHmmer3RawMatch>> proteinIdToMatchMap =
                new HashMap<String, RawProtein<PIRSFHmmer3RawMatch>>();
        Query query = entityManager
                .createQuery("select p from PIRSFHmmer3RawMatch p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease")
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<PIRSFHmmer3RawMatch> resultList = query.getResultList();
        for (PIRSFHmmer3RawMatch match : resultList) {
            String id = match.getSequenceIdentifier();
            RawProtein<PIRSFHmmer3RawMatch> rawProtein = proteinIdToMatchMap.get(id);
            if (rawProtein == null) {
                rawProtein = new RawProtein<PIRSFHmmer3RawMatch>(id);
                proteinIdToMatchMap.put(id, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }

}
