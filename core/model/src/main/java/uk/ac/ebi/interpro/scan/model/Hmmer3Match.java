/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * HMMER3 match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name = "hmmer3_match")
@XmlType(name = "Hmmer3MatchType")
public class Hmmer3Match extends HmmerMatch<Hmmer3Match.Hmmer3Location> implements Serializable {

    protected Hmmer3Match() {
    }

    public Hmmer3Match(Signature signature, String signatureModels, double score, double evalue, Set<Hmmer3Match.Hmmer3Location> locations) {
        super(signature, signatureModels, score, evalue, locations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hmmer3Match)) return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(39, 59)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<Hmmer3Location> clonedLocations = new HashSet<Hmmer3Location>(this.getLocations().size());
        for (Hmmer3Location location : this.getLocations()) {
            clonedLocations.add((Hmmer3Location) location.clone());
        }
        return new Hmmer3Match(this.getSignature(), this.getSignatureModels(), this.getScore(), this.getEvalue(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "hmmer3_location")
    @XmlType(name = "Hmmer3LocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class Hmmer3Location extends HmmerLocation<Hmmer3Location.Hmmer3LocationFragment> {

        @Column(name = "envelope_start", nullable = false)
        private int envelopeStart;

        @Column(name = "envelope_end", nullable = false)
        private int envelopeEnd;

        @Column(name = "post_processed", nullable = false)
        private boolean postProcessed; // Is this a native HMMER3 result or has it been post processed in some way?

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected Hmmer3Location() {
        }

        /**
         * Create a {@Hmmer3Location} consisting of one fragment with the same start/stop (initially)
         * @param start
         * @param end
         * @param score
         * @param evalue
         * @param hmmStart
         * @param hmmEnd
         * @param hmmLength
         * @param hmmBounds
         * @param envelopeStart
         * @param envelopeEnd
         * @param postProcessed
         * @param dcStatus
         */
        public Hmmer3Location(int start, int end, double score, double evalue,
                              int hmmStart, int hmmEnd, int hmmLength, HmmBounds hmmBounds,
                              int envelopeStart, int envelopeEnd, boolean postProcessed, DCStatus dcStatus) {
            super(new Hmmer3LocationFragment(start, end, dcStatus), score, evalue, hmmStart, hmmEnd, hmmLength, hmmBounds);
            setEnvelopeStart(envelopeStart);
            setEnvelopeEnd(envelopeEnd);
            setPostProcessed(postProcessed);
        }

        public Hmmer3Location(int start, int end, double score, double evalue,
                              int hmmStart, int hmmEnd, int hmmLength, HmmBounds hmmBounds,
                              int envelopeStart, int envelopeEnd, boolean postProcessed, Set<Hmmer3LocationFragment> locationFragments) {
            super(start, end, score, evalue, hmmStart, hmmEnd, hmmLength, hmmBounds, locationFragments);
            setEnvelopeStart(envelopeStart);
            setEnvelopeEnd(envelopeEnd);
            setPostProcessed(postProcessed);
        }

        @XmlAttribute(name = "env-start", required = true)
        public int getEnvelopeStart() {
            return envelopeStart;
        }

        private void setEnvelopeStart(int envelopeStart) {
            this.envelopeStart = envelopeStart;
        }

        @XmlAttribute(name = "env-end", required = true)
        public int getEnvelopeEnd() {
            return envelopeEnd;
        }

        private void setEnvelopeEnd(int envelopeEnd) {
            this.envelopeEnd = envelopeEnd;
        }

        @XmlAttribute(name = "post-processed", required = true)
        public boolean isPostProcessed() {
            return postProcessed;
        }

        public void setPostProcessed(boolean postProcessed) {
            this.postProcessed = postProcessed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Hmmer3Location)) return false;
            Hmmer3Location l = (Hmmer3Location) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(envelopeStart, l.envelopeStart)
                    .append(envelopeEnd, l.envelopeEnd)
                    .append(postProcessed, l.postProcessed)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(39, 59)
                    .appendSuper(super.hashCode())
                    .append(envelopeStart)
                    .append(envelopeEnd)
                    .append(postProcessed)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public Object clone() throws CloneNotSupportedException {
            final Hmmer3Location clone = new Hmmer3Location(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue(), this.getHmmStart(), this.getHmmEnd(), this.getHmmLength(),
                    this.getHmmBounds(), this.getEnvelopeStart(), this.getEnvelopeEnd(), this.isPostProcessed(), this.getLocationFragments());
            return clone;
        }

        /**
         * Location fragment of a HMMER3 match on a protein sequence
         */
        @Entity
        @Table(name = "hmmer3_location_fragment")
        @XmlType(name = "Hmmer3LocationFragmentType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class Hmmer3LocationFragment extends LocationFragment {

            protected Hmmer3LocationFragment() {
            }

            public Hmmer3LocationFragment(int start, int end) {
                super(start, end);
            }

            public Hmmer3LocationFragment(int start, int end, DCStatus dcStatus) {
                super(start, end, dcStatus);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Hmmer3LocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }


            @Override
            public int hashCode() {
                return new HashCodeBuilder(139, 159)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new Hmmer3LocationFragment(this.getStart(), this.getEnd(), this.getDcStatus());
            }
        }


    }
}
