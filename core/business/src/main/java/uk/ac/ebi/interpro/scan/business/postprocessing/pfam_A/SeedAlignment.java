package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import java.io.Serializable;

/**
 * Simple immutable POJO holding the details of a single Seed alignment
 * Use the basic SeedAlignment class for retrieval from the database for
 * post processing.
 *
 * @author Phil Jones
 * @version $Id: SeedAlignment.java,v 1.1 2009/10/23 15:05:00 pjones Exp $
 * @since 1.0
 */
public class SeedAlignment implements Serializable {

    private final String modelAccession;

    private final int alignmentStart;

    private final int alignmentEnd;

    SeedAlignment(final String modelAccession,
                             final int alignmentStart,
                             final int alignmentEnd){
        this.modelAccession = modelAccession;
        this.alignmentStart = alignmentStart;
        this.alignmentEnd = alignmentEnd;
    }

    public String getModelAccession() {
        return modelAccession;
    }

    public int getAlignmentStart() {
        return alignmentStart;
    }

    public int getAlignmentEnd() {
        return alignmentEnd;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
              sb.append(modelAccession)
                .append('_')
                .append(alignmentStart)
                .append('_')
                .append(alignmentEnd);
        return sb.toString();
    }

    /**
     * Simple immutable POJO holding the details of a single Seed alignment.
     * Use this class to persist seed alignment data to the database.
     *
     * @author Phil Jones
     * @version $Id: SeedAlignment.java,v 1.1 2009/10/23 15:05:00 pjones Exp $
     * @since 1.0
     */
    public static class ForPersistence extends SeedAlignment {

        private final String uniprotAccession;

        private final int proteinVersionNumber;

        private final String releaseNumber;

        ForPersistence(final String modelAccession,
                             final String uniprotAccession,
                             final int proteinVersionNumber,
                             final int alignmentStart,
                             final int alignmentEnd,
                             final String releaseNumber) {
            super(modelAccession, alignmentStart, alignmentEnd);
            this.uniprotAccession = uniprotAccession;
            this.proteinVersionNumber = proteinVersionNumber;
            this.releaseNumber = releaseNumber;
        }

        public String getUniProtAccession() {
            return uniprotAccession;
        }

        public int getProteinVersionNumber() {
            return proteinVersionNumber;
        }

        public String getReleaseNumber() {
            return releaseNumber;
        }
    }
}
