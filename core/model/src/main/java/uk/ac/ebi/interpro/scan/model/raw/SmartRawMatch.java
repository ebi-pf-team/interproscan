package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Index;
import javax.persistence.Table;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;

/**
 * Class to capture raw match data for SMART
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = SmartRawMatch.TABLE_NAME,  indexes = {
        @Index(name = "SMART_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "SMART_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "SMART_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "SMART_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "SMART_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class SmartRawMatch extends Hmmer2RawMatch implements Comparable<SmartRawMatch> {

    public static final String TABLE_NAME = "SMART_RAW_MATCH";

    protected SmartRawMatch() {

    }

    public SmartRawMatch(String sequenceIdentifier, String model, SignatureLibrary signatureLibrary, String signatureLibraryRelease, int locationStart, int locationEnd, double evalue, double score, int hmmStart, int hmmEnd, String hmmBounds, double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, SignatureLibrary.SMART, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationEvalue, locationScore);
    }

    @Override
    public int compareTo(SmartRawMatch that) {
        if (this == that) return 0;
        if (this.getEvalue() < that.getEvalue()) return -1;     // First, sort by ievalue ASC
        if (this.getEvalue() > that.getEvalue()) return 1;
        if (this.getScore() > that.getScore()) return -1;                     // then by score ASC
        if (this.getScore() < that.getScore()) return 1;
        if (this.hashCode() > that.hashCode())
            return -1;                     // then by hashcode to be consistent with equals.
        if (this.hashCode() < that.hashCode()) return 1;
        return 0;
    }
}
