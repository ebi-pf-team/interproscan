package uk.ac.ebi.interpro.scan.model.raw;

import org.hibernate.annotations.Index;
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
@javax.persistence.Table(name = SmartRawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = SmartRawMatch.TABLE_NAME, indexes = {
        @Index(name = "SMART_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "SMART_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "SMART_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "SMART_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "SMART_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class SmartRawMatch extends Hmmer2RawMatch {

    public static final String TABLE_NAME = "SMART_RAW_MATCH";

    protected SmartRawMatch() {

    }

    public SmartRawMatch(String sequenceIdentifier, String model, SignatureLibrary signatureLibrary, String signatureLibraryRelease, int locationStart, int locationEnd, double evalue, double score, int hmmStart, int hmmEnd, String hmmBounds, double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, SignatureLibrary.SMART, signatureLibraryRelease, locationStart, locationEnd, evalue, score, hmmStart, hmmEnd, hmmBounds, locationEvalue, locationScore);
    }
}
