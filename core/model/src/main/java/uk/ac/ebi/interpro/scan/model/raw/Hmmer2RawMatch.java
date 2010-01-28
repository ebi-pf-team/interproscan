package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;

import uk.ac.ebi.interpro.scan.model.PersistenceConversion;

/**
 * <a href="http://hmmer.janelia.org/">HMMER 2</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
public abstract class Hmmer2RawMatch extends HmmerRawMatch {

    private double locationEvalue;    

    protected Hmmer2RawMatch() { }    

    protected Hmmer2RawMatch(String sequenceIdentifier, String model,
                             String signatureLibraryName, String signatureLibraryRelease,
                             int locationStart, int locationEnd,
                             double evalue, double score,
                             int hmmStart, int hmmEnd, String hmmBounds,
                             double locationEvalue, double locationScore) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd,
              evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore);
        setLocationEvalue(locationEvalue);
    }

    public double getLocationEvalue() {
        return PersistenceConversion.get(locationEvalue);
    }

    private void setLocationEvalue(double locationEvalue) {
        this.locationEvalue = PersistenceConversion.set(locationEvalue);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Hmmer2RawMatch))
            return false;
        final Hmmer2RawMatch m = (Hmmer2RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(locationEvalue, m.locationEvalue)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(53, 57)
                .appendSuper(super.hashCode())
                .append(locationEvalue)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }
    
}