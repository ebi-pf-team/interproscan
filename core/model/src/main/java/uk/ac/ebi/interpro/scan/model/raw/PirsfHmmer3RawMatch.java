package uk.ac.ebi.interpro.scan.model.raw;

import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;

/**
 * Specific raw match class for PIRSF.
 * Only required to ensure it gets it's own table.
 *
 * @author Matthew Fraser
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@javax.persistence.Table(name = PirsfHmmer3RawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = PirsfHmmer3RawMatch.TABLE_NAME, indexes = {
        @Index(name = "PIRSF3_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "PIRSF3_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "PIRSF3_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "PIRSF3_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "PIRSF3_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class PirsfHmmer3RawMatch extends Hmmer3RawMatch {

    public static final String TABLE_NAME = "PIRSF_HMMER3_RAW_MATCH";

    protected PirsfHmmer3RawMatch() {

    }

    /*
       The pirsf.pl with "--outfmt i5" shall return the following in TSV format:
       LOCATION_END
       LOCATION_START
       MODEL_ID
       SEQUENCE_ID
       EVALUE
       HMM_BOUNDS
       HMM_END
       HMM_START
       LOCATION_SCORE
       SCORE
       DOMAIN_BIAS
       DOMAIN_CE_VALUE
       DOMAIN_IE_VALUE
       ENVELOPE_END
       ENVELOPE_START
       EXPECTED_ACCURACY
       FULL_SEQUENCE_BIAS
     */

    public PirsfHmmer3RawMatch(int locationEnd, int locationStart, String model, String sequenceIdentifier, double evalue,
                               String hmmBounds, int hmmEnd, int hmmStart, double locationScore, double score,
                               double domainBias, double domainCeValue, double domainIeValue, int envelopeEnd,
                               int envelopeStart, double expectedAccuracy, double fullSequenceBias,
                               SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
                evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy,
                fullSequenceBias, domainCeValue, domainIeValue, domainBias);


    }

}