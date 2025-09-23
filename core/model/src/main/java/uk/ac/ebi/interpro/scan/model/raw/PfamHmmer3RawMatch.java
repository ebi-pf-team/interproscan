package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Index;
import javax.persistence.Table;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;

/**
 * <a href="http://pfam.xfam.org/">Pfam</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Table(name = PfamHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "PFAM_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PFAM_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PFAM_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PFAM_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PFAM_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PfamHmmer3RawMatch extends Hmmer3RawMatch implements Comparable<PfamHmmer3RawMatch> {

    public static final String TABLE_NAME = "PFAM_HMMER3_RAW_MATCH";

    protected PfamHmmer3RawMatch() {
    }

    public PfamHmmer3RawMatch(String sequenceIdentifier, String model,
                              SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                              int locationStart, int locationEnd,
                              double evalue, double score,
                              int hmmStart, int hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than , equal to, or greater than that object.
     * <p/>
     * The required ordering for Pfam A post processing is:
     * <p/>
     * domain I evalue ASC, Score DESC
     *
     * @param that being the PfamHmmer3RawMatch to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(PfamHmmer3RawMatch that) {
        if (this == that) return 0;

        // Compare ievalue (ascending)
        int result = Double.compare(this.getDomainIeValue(), that.getDomainIeValue());
        if (result != 0) return result;

        // Compare score (descending)
        result = Double.compare(that.getLocationScore(), this.getLocationScore());
        if (result != 0) return result;

        // Compare length (descending)
        int thisLength = this.getLocationEnd() - this.getLocationStart();
        int thatLength = that.getLocationEnd() - that.getLocationStart();
        result = Integer.compare(thatLength, thisLength);
        if (result != 0) return result;

        // Compare start and end positions (ascending)
        result = Integer.compare(this.getLocationStart(), that.getLocationStart());
        if (result != 0) return result;

        result = Integer.compare(this.getLocationEnd(), that.getLocationEnd());
        if (result != 0) return result;

        // Final fallback to ensure consistency with equals
        return Integer.compare(this.hashCode(), that.hashCode());
    }
}
