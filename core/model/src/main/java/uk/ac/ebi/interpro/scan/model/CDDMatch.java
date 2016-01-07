package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * Models a match based upon the CDD algorithm against a
 * protein sequence.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.16
 */
@Entity
@Table(name = "cdd_match")
@XmlType(name = "CDDMatchType")
public class CDDMatch extends Match<CDDMatch.CDDLocation> {

    protected CDDMatch() {
    }

    public CDDMatch(Signature signature, Set<CDDLocation> locations) {
        super(signature, locations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CDDMatch))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 49)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<CDDLocation> clonedLocations = new HashSet<CDDLocation>(this.getLocations().size());
        for (CDDLocation location : this.getLocations()) {
            clonedLocations.add((CDDLocation) location.clone());
        }
        return new CDDMatch(this.getSignature(), clonedLocations);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Location of CDD match on a protein sequence
     *
     * @author Gift Nuka
     */
    @Entity
    @Table(name = "cdd_location")
    @XmlType(name = "CDDLocationType")
    //@XmlType(name = "CDDLocationType", propOrder = { "start", "end", "score", "evalue"})
    public static class CDDLocation extends Location {

        @Column(nullable = false, name = "evalue")
        private double evalue;

        @Column(nullable = false, name = "score")
        private double score;

        protected CDDLocation() {
        }

        public CDDLocation(int start, int end, double score, double evalue) {
            super(start, end);
            setScore(score);
            setEvalue(evalue);
        }

        @XmlAttribute(required = true)
        public double getEvalue() {
            return evalue;
        }

        private void setEvalue(double evalue) {
            this.evalue = evalue;
        }

        @XmlAttribute(required = true)
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
            if (!(o instanceof CDDLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(29, 53)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new CDDLocation(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue());
        }
    }

}
