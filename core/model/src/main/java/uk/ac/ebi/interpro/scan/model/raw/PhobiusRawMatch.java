package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gift Nuka
 *
 */

@Entity
@Table(name = PhobiusRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PHOBIUS_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "PHOBIUS_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "PHOBIUS_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "PHOBIUS_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "PHOBIUS_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class PhobiusRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "PHOBIUS_RAW_MATCH";

    private boolean isSP = false,
            isTM = false;

    private PhobiusFeatureType featureType;

    public PhobiusRawMatch() {
    }

    public PhobiusRawMatch(String sequenceIdentifier, String model,
                           SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                           int locationStart, int locationEnd, PhobiusFeatureType featureType, boolean isSP, boolean isTM) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        setFeatureType(featureType);
        setSP(isSP);
        setTM(isTM);
    }

    public boolean isSP() {
        return isSP;
    }

    public void setSP(boolean SP) {
        isSP = SP;
    }

    public boolean isTM() {
        return isTM;
    }

    public void setTM(boolean TM) {
        isTM = TM;
    }

    public PhobiusFeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(PhobiusFeatureType featureType) {
        this.featureType = featureType;
    }

    @Override
    public String toString() {
        return "PhobiusRawMatch{" +
                "seqId: " + getSequenceIdentifier() +
                ", modelID: " + getModelId() +
                ", start: " + getLocationStart() +
                ", end: " + getLocationEnd() +
                ", isSP=" + isSP +
                ", isTM=" + isTM +
                ", featureType=" + featureType +
                '}';
    }
}
