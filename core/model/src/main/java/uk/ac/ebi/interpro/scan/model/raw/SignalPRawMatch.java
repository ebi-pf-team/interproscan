package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * SignalP raw match.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = SignalPRawMatch.TABLE_NAME, indexes = {
        @Index(name = "SIGNALP_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "SIGNALP_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "SIGNALP_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "SIGNALP_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "SIGNALP_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class SignalPRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "SIGNALP_RAW_MATCH";

    /*
     * Example SignalP binary output :
     *
     * # SignalP-4.0 gram+ predictions
     * # Measure  Position  Value
     * #  max. C    23       0.166
     * #  max. Y    42       0.257
     * #  max. S    38       0.526
     * #  mean S     1-41    0.250
     * #       D     1-41    0.254
     * # 1	SP= 'No' D= 0.254 D-cutoff= 0.450 Networks= SignalP-TM
     * # Measure  Position  Value
     * #  max. C    18       0.207
     * #  max. Y    18       0.374
     * #  max. S     1       0.765
     * #  mean S     1-17    0.693
     * #       D     1-17    0.499
     * # 2	SP= 'Yes' Cleavage site between pos. 17 and 18: ASA-VP D= 0.499 D-cutoff= 0.450 Networks= SignalP-TM
     */

    @Column
    private SignalPOrganismType organismType;

    @Column
    private double dScore;

    @Column
    private double dCutoff;

    protected SignalPRawMatch() {}

    public SignalPRawMatch(String sequenceIdentifier, String model,
                           SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                           int locationStart, int locationEnd,
                           SignalPOrganismType organismType, double dScore, double dCutoff) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        this.organismType = organismType;
        this.dScore = dScore;
        this.dCutoff = dCutoff;
        if (!(signatureLibrary.equals(SignatureLibrary.SIGNALP_EUK) ||
                signatureLibrary.equals(SignatureLibrary.SIGNALP_GRAM_POSITIVE) ||
                signatureLibrary.equals(SignatureLibrary.SIGNALP_GRAM_NEGATIVE))) {
            throw new IllegalStateException("Trying to construct a SignalP raw match with invalid signature library: " + signatureLibrary.getName());
        }
    }

    public SignalPOrganismType getOrganismType() {
        return organismType;
    }

    public void setOrganismType(SignalPOrganismType organismType) {
        this.organismType = organismType;
    }

    public double getdScore() {
        return dScore;
    }

    public void setdScore(double dScore) {
        this.dScore = dScore;
    }

    public double getdCutoff() {
        return dCutoff;
    }

    public void setdCutoff(double dCutoff) {
        this.dCutoff = dCutoff;
    }

}
