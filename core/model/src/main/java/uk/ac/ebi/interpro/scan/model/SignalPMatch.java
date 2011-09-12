package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * SignalP filtered match.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = "signalp_match")
@XmlType(name = "SignalPMatchType")
public class SignalPMatch extends Match<SignalPMatch.SignalPLocation> {

    protected SignalPMatch() {
    }

    private SignalPOrganismType orgType;

    public SignalPMatch(Signature signature, SignalPOrganismType orgType, Set<SignalPLocation> locations) {
        // Only ever 1 Signal Peptide location
        super(signature, locations);
        this.orgType = orgType;
    }

    @Enumerated(EnumType.ORDINAL)   // Using ordinal to keep the database size down.
    @Column(nullable = false)
    public SignalPOrganismType getOrgType() {
        return orgType;
    }

    private void setOrgType(SignalPOrganismType orgType) {
        this.orgType = orgType;
    }

    /**
     * Location of signal peptide on protein sequence
     */
    @Entity
    @Table(name = "signalp_location")
    @XmlType(name = "SignalPLocationType")
    public static class SignalPLocation extends Location {

        @Column
        private Double score;

        /**
         * Protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected SignalPLocation() {
        }

        public SignalPLocation(int start, int end) {
            this(start, end, null);
        }

        public SignalPLocation(int start, int end, Double score) {
            super(start, end);
            setScore(score);
        }

        @XmlAttribute(required = false)
        public Double getScore() {
            return score;
        }

        private void setScore(Double score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SignalPLocation))
                return false;
            final SignalPLocation f = (SignalPLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(score, f.score)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 21)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .toHashCode();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SignalPMatch that = (SignalPMatch) o;

        if (orgType != that.orgType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + orgType.hashCode();
        return result;
    }
}
