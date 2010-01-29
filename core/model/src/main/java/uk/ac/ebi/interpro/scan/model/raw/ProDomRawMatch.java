package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <a href="http://prodom.prabi.fr/">ProDom</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="prodom_raw_match")
public class ProDomRawMatch extends RawMatch implements Serializable {

    @Column
    private double score;   // location.score

    protected ProDomRawMatch() { }    

    public ProDomRawMatch(String sequenceIdentifier, String model,
                          String signatureLibraryName, String signatureLibraryRelease,
                          int locationStart, int locationEnd, double score) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd);
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }

    @Override public boolean equals(Object o) {
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

    @Override public int hashCode() {
        return new HashCodeBuilder(11, 81)
                .appendSuper(super.hashCode())
                .append(score)
                .toHashCode();
    }    

}
