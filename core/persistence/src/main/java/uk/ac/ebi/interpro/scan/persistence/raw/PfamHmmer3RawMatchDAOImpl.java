package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DAO implementation for PfamHmmer3RawMatchDAO objects.
 *
 * @author Manjula Thimma
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 */
public class PfamHmmer3RawMatchDAOImpl
        extends RawMatchDAOImpl<PfamHmmer3RawMatch>
        implements PfamHmmer3RawMatchDAO {

    public PfamHmmer3RawMatchDAOImpl() {
        super(PfamHmmer3RawMatch.class);
    }

    /**
     * Returns a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for Pfam-A HMMER3 post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PfamHmmer3RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PfamHmmer3RawMatch>> getRawMatchesForProteinIdsInRange(
            long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToMatchMap =
                new HashMap<String, RawProtein<PfamHmmer3RawMatch>>();
        Query query = entityManager
                .createQuery("select p from PfamHmmer3RawMatch p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease")
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<PfamHmmer3RawMatch> resultList = query.getResultList();
        for (PfamHmmer3RawMatch match : resultList) {
            String id = match.getSequenceIdentifier();
            RawProtein<PfamHmmer3RawMatch> rawProtein = proteinIdToMatchMap.get(id);
            if (rawProtein == null) {
                rawProtein = new RawProtein<PfamHmmer3RawMatch>(id);
                proteinIdToMatchMap.put(id, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }

}
