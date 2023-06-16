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

    public SuperFamilyHmmer3Match(Signature signature, String signatureModels, double evalue, Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations) {
        super(signature, signatureModels, locations);
        setEvalue(evalue);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<SuperFamilyHmmer3Location> clonedLocations = new HashSet<SuperFamilyHmmer3Location>(this.getLocations().size());
        for (SuperFamilyHmmer3Location location : this.getLocations()) {
            clonedLocations.add((SuperFamilyHmmer3Location) location.clone());
        }
        return new SuperFamilyHmmer3Match(this.getSignature(), this.getSignatureModels(), this.getEvalue(), clonedLocations);
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
        return new HashCodeBuilder(29, 53)
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
    @XmlType(name = "SuperFamilyHmmer3LocationType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
    public static class SuperFamilyHmmer3Location extends Location {

        @Column(nullable = false, name = "hmm_length")
        private int hmmLength;

        protected SuperFamilyHmmer3Location() {
        }

        public SuperFamilyHmmer3Location(int start, int end, Set locationFragments, int hmmLength) {
            super(start, end, locationFragments);
            this.hmmLength = hmmLength;
        }

        public SuperFamilyHmmer3Location(SuperFamilyHmmer3LocationFragment fragment, int hmmLength) {
            super(fragment);
            setHmmLength(hmmLength);
        }

        public SuperFamilyHmmer3Location(Set<SuperFamilyHmmer3LocationFragment> fragments, int hmmLength) {
            super(fragments);
            setHmmLength(hmmLength);
        }

        @XmlAttribute(name = "hmm-length", required = true)
        public int getHmmLength() {
            return hmmLength;
        }

        private void setHmmLength(int hmmLength) {
            this.hmmLength = hmmLength;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SuperFamilyHmmer3Location)) return false;
            if (!super.equals(o)) return false;

            SuperFamilyHmmer3Location that = (SuperFamilyHmmer3Location) o;

            return hmmLength == that.hmmLength;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + hmmLength;
            return result;
        }

        public Object clone() throws CloneNotSupportedException {
            return new SuperFamilyHmmer3Location(this.getLocationFragments(), this.getHmmLength());
        }


        /**
         * Location fragment of a SuperFamily match on a protein sequence
         */
        @Entity
        @Table(name = "superfam_location_fragment")
        @XmlType(name = "SuperFamilyHmmer3LocationFragmentType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        public static class SuperFamilyHmmer3LocationFragment extends LocationFragment {

            protected SuperFamilyHmmer3LocationFragment() {
            }

            public SuperFamilyHmmer3LocationFragment(int start, int end, DCStatus dcStatus) {
                super(start, end, dcStatus);
            }

            public SuperFamilyHmmer3LocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof SuperFamilyHmmer3LocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(129, 153)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new SuperFamilyHmmer3LocationFragment(this.getStart(), this.getEnd(), this.getDcStatus());
            }
        }

    }

}
