package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import java.io.Serializable;
import java.util.*;

/**
 * Used in post-processing to load seed alignment data into memory
 * for the batch of proteins currently being analysed.
 *
 * @author Phil Jones
 * @version $Id: SeedAlignmentDataRetriever.java,v 1.5 2009/11/04 16:24:31 craigm Exp $
 * @since 1.0
 */
public class SeedAlignmentDataRetriever implements Serializable {

    /**
     * Retrieves a Map of protein id to List<SeedAlignment> for use in
     * post processing.
     *
     * @param proteinIds for which to return the seed alignment data.
     * @return a Map of protein id to List<SeedAlignment> for use in
     *         post processing.
     */
    public SeedAlignmentData retrieveSeedAlignmentData(Set<String> proteinIds) {
        SeedAlignmentData seedAlignmentData = null;

        // TODO - Get the seed alignment data from somewhere...
        // TODO this can wait until after the first milestone however.

        return seedAlignmentData;
    }

    public class SeedAlignmentData implements Serializable {

        /**
         * Map of protein ID to a List of SeedAlignment objects.
         */
        private final Map<String, List<SeedAlignment>> data = new HashMap<String, List<SeedAlignment>>();

        private SeedAlignmentData() {
        }

        private void put(final String upi, final SeedAlignment seedAlignment) {
            List<SeedAlignment> seedAlignmentList = data.get(upi);
            if (seedAlignmentList == null) {
                seedAlignmentList = new ArrayList<SeedAlignment>();
                seedAlignmentList.add(seedAlignment);
                data.put(upi, seedAlignmentList);
            } else {
                seedAlignmentList.add(seedAlignment);
            }
        }

        public List<SeedAlignment> getSeedAlignments(final String proteinId) {
            List<SeedAlignment> seedAlignmentList = data.get(proteinId);
            if (seedAlignmentList == null) {
                seedAlignmentList = Collections.emptyList();
            }
            return seedAlignmentList;
        }
    }
}
