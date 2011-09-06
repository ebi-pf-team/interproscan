package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Set;

/**
 * SignalP filtered match.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name="signalp_match")
@XmlType(name="SignalPMatchType")
public class SignalPMatch extends Match<SignalPMatch.SignalPLocation> {

    protected SignalPMatch() {}

    public SignalPMatch(Signature signature, Set<SignalPLocation> locations) {
        // Only ever 1 Signal Peptide location
        super(signature, locations);
    }

    /**
     * Location of signal peptide on protein sequence
     */
    @Entity
    @Table(name="signalp_location")
    @XmlType(name="SignalPLocationType")
    public static class SignalPLocation extends Location {

        @Column(nullable = false)
        private double score;

        /**
         * Protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected SignalPLocation() {}

        public SignalPLocation(int start, int end, double score) {
            super(start, end);
            setScore(score);
        }

        @XmlAttribute(required=true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        @Override public boolean equals(Object o) {
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

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 21)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .toHashCode();
        }

    }

}
