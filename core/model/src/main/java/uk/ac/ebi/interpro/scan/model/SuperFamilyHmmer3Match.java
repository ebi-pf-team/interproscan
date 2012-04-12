package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * SuperFamily filtered match.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@XmlType(name = "SuperFamilyHmmer3MatchType")
public class SuperFamilyHmmer3Match extends Match<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> {

    @Column(nullable = false)
    private double evalue;

    protected SuperFamilyHmmer3Match() {
    }

    public SuperFamilyHmmer3Match(Signature signature, double evalue, Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations) {
        super(signature, locations);
        setEvalue(evalue);
    }

    @XmlAttribute(required = true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SuperFamilyHmmer3Match))
            return false;
        final SuperFamilyHmmer3Match m = (SuperFamilyHmmer3Match) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals() &&
                PersistenceConversion.equivalent(evalue, m.evalue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 49)
                .appendSuper(super.hashCode())
                .append(evalue)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Location of a SuperFamily match on a protein sequence
     */
    @Entity
    @Table(name = "superfamilyhmmer3_location")
    @XmlType(name = "SuperFamilyHmmer3LocationType")
    public static class SuperFamilyHmmer3Location extends Location {

        protected SuperFamilyHmmer3Location() {
        }

        public SuperFamilyHmmer3Location(int start, int end) {

            super(start, end);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SuperFamilyHmmer3Location))
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
    }

}
