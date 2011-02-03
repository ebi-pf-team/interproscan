package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * <a href="http://prodom.prabi.fr/">ProDom</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@javax.persistence.Table(name = ProDomRawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = ProDomRawMatch.TABLE_NAME, indexes = {
        @Index(name = "PRODOM_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "PRODOM_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "PRODOM_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "PRODOM_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "PRODOM_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class ProDomRawMatch extends RawMatch implements Serializable {

    public static final String TABLE_NAME = "PRODOM_RAW_MATCH";

    @Column
    private double score;   // location.score

    protected ProDomRawMatch() {
    }

    public ProDomRawMatch(String sequenceIdentifier, String model,
                          String signatureLibraryRelease,
                          int locationStart, int locationEnd, double score) {
        super(sequenceIdentifier, model, SignatureLibrary.PRODOM, signatureLibraryRelease, locationStart, locationEnd);
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProDomRawMatch))
            return false;
        final ProDomRawMatch m = (ProDomRawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(score, m.score)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 81)
                .appendSuper(super.hashCode())
                .append(score)
                .toHashCode();
    }

}
