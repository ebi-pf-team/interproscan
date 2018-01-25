package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

/**
 * TMHMM match.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @since 1.0
 */
@Entity
@Table(name = "tmhmm_match")
@XmlType(name = "TMHMMMatchType")
public class TMHMMMatch extends Match<TMHMMMatch.TMHMMLocation> {

    protected TMHMMMatch() {
    }

    public TMHMMMatch(Signature signature, Set<TMHMMLocation> locations) {
        super(signature, locations);
        if (!TMHMMSignature.isValidSignature(signature)) {
            throw new IllegalArgumentException("The Signature object being used for this TMHMM does not appear to be a valid TMHMM signature.");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<TMHMMLocation> clonedLocations = new HashSet<TMHMMLocation>(this.getLocations().size());
        for (TMHMMLocation location : this.getLocations()) {
            clonedLocations.add((TMHMMLocation) location.clone());
        }
        return new TMHMMMatch(this.getSignature(), clonedLocations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TMHMMMatch))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
//        final TMHMMMatch m = (TMHMMMatch) o;
//        return new EqualsBuilder()
//                .appendSuper(super.equals(o))
//                .isEquals()
//                &&
//                PersistenceConversion.equivalent(evalue, m.evalue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 71)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     * @author Maxim Scheremetjew, EMBL-EBI, InterPro
     */
    @Entity
    @Table(name = "tmhmm_location")
    @XmlType(name = "TMHMMLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class TMHMMLocation extends Location {

        @Column(nullable = false)
        private String prediction;

        @Column(nullable = false)
        private float score;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected TMHMMLocation() {
        }

        public TMHMMLocation(int start, int end) {
            this(start, end, null, -1f);
        }

        public TMHMMLocation(int start, int end, String prediction, float score) {
            super(new TMHMMLocationFragment(start, end));
            this.prediction = prediction;
//            TODO: Take care of magnitude of score
            this.score = score;
        }

        /**
         * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
         *
         * @author Antony Quinn
         */
        @XmlTransient
        public static class Builder {

            private int start;

            private int end;

            private String prediction;

            private float score;


            public Builder(int start, int end) {
                this.start = start;
                this.end = end;
            }

            public TMHMMLocation build() {
                TMHMMLocation location = new TMHMMLocation(start, end);
                location.setPrediction(prediction);
                location.setScore(score);
                return location;
            }

            public Builder prediction(String prediction) {
                this.prediction = prediction;
                return this;
            }

            public Builder score(float score) {
                this.score = score;
                return this;
            }
        }

        @XmlTransient
        public String getPrediction() {
            return prediction;
        }

        public void setPrediction(String prediction) {
            this.prediction = prediction;
        }

        @XmlTransient
        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TMHMMLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 71)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new TMHMMLocation(this.getStart(), this.getEnd(), this.getPrediction(), this.getScore());
        }

        /**
         * Location fragment of a TMHMM match on a protein sequence
         */
        @Entity
        @Table(name = "tmhmm_location_fragment")
        @XmlType(name = "TMHMMLocationFragmentType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class TMHMMLocationFragment extends LocationFragment {

            protected TMHMMLocationFragment() {
            }

            public TMHMMLocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof TMHMMLocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(119, 171)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new TMHMMLocationFragment(this.getStart(), this.getEnd());
            }
        }

    }
}
