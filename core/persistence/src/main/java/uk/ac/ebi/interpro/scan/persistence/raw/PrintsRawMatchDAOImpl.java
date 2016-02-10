package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author John Maslen
 * @author Phil Jones
 *         Date: Jun 23, 2010
 *         Time: 11:21:43 AM
 */

public class PrintsRawMatchDAOImpl extends RawMatchDAOImpl<PrintsRawMatch> implements PrintsRawMatchDAO {
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public PrintsRawMatchDAOImpl() {
        super(PrintsRawMatch.class);
    }

    /**
     * Returns a Map of sequence identifiers to a List of
     * PrintsRawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for PRINTS post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PrintsRawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PrintsRawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<PrintsRawMatch>> proteinIdToMatchMap = new HashMap<String, RawProtein<PrintsRawMatch>>();
        Query query = entityManager
                .createQuery("select p from PrintsRawMatch p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease")
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<PrintsRawMatch> resultList = query.getResultList();
        for (PrintsRawMatch match : resultList) {
            final String proteinId = match.getSequenceIdentifier();
            RawProtein<PrintsRawMatch> rawProtein = proteinIdToMatchMap.get(proteinId);
            if (rawProtein == null) {
                rawProtein = new RawProtein<PrintsRawMatch>(proteinId);
                proteinIdToMatchMap.put(proteinId, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }


}
