package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
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
public class PirsfHmmer3RawMatchDAOImpl
        extends RawMatchDAOImpl<PirsfHmmer3RawMatch>
        implements PirsfHmmer3RawMatchDAO {

    public PirsfHmmer3RawMatchDAOImpl() {
        super(PirsfHmmer3RawMatch.class);
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
    public Map<String, RawProtein<PirsfHmmer3RawMatch>> getRawMatchesForProteinIdsInRange(
            long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<PirsfHmmer3RawMatch>> proteinIdToMatchMap =
                new HashMap<String, RawProtein<PirsfHmmer3RawMatch>>();
        Query query = entityManager
                .createQuery("select p from PirsfHmmer3RawMatch p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease")
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<PirsfHmmer3RawMatch> resultList = query.getResultList();
        for (PirsfHmmer3RawMatch match : resultList) {
            String id = match.getSequenceIdentifier();
            RawProtein<PirsfHmmer3RawMatch> rawProtein = proteinIdToMatchMap.get(id);
            if (rawProtein == null) {
                rawProtein = new RawProtein<PirsfHmmer3RawMatch>(id);
                proteinIdToMatchMap.put(id, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }

}
