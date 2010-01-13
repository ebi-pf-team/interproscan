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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;
import java.util.Collections;

import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

/**
 * HMMER match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table(name="hmmer_match")
@XmlType(name="HmmerMatchType", propOrder={"signature", "locations"})
public class HmmerMatch extends Match<HmmerMatch.HmmerLocation> {

    @Column (nullable = false)
    private double evalue;

    @Column (nullable = false)
    private double score;

    protected HmmerMatch() {}

    public HmmerMatch(Signature signature, double score, double evalue, Set<HmmerLocation> locations) {
        super(signature, locations);
        setScore(score);
        setEvalue(evalue);
    }

    /**
     * Builds a new HmmerMatch object based upon a Signature object
     * and a PfamHmmer3RawMatch object
     * @param signature being the Signature to Match to.
     * @param rawMatch being the RawMatch representation of the Match.
     */
    // TODO: Should be other way around: add (static factory?) method to PfamHmmer3RawMatch to create HmmerMatch
    // TODO: Otherwise need to add constructors here for every type of Hmmer3RawMatch
    public HmmerMatch(Signature signature, PfamHmmer3RawMatch rawMatch){
        super(signature, Collections.singleton(new HmmerMatch.HmmerLocation(rawMatch)));
        setScore(rawMatch.getScore());
        setEvalue(rawMatch.getEvalue());
    }

    @XmlAttribute(required=true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    private void setScore(double score) {
        this.score = score;
    }

    @XmlAttribute(name="score", required=true)
    public double getScore() {
        return score;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HmmerMatch))
            return false;
        final HmmerMatch m = (HmmerMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(evalue, m.evalue)
                .append(score, m.score)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 49)
                .appendSuper(super.hashCode())
                .append(evalue)
                .append(score)
                .toHashCode();
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author  Antony Quinn
     */
    @Entity
    @Table(name="hmmer_location")
    @XmlType(name="HmmLocationType", propOrder={"start", "end"})
    public static class HmmerLocation extends Location {

        @Column (nullable = false)
        private int hmmStart;

        @Column (nullable = false)
        private int hmmEnd;

        @Column (nullable = false)
        private HmmBounds hmmBounds;

        @Column (nullable = false)
        private double evalue;

        @Column (nullable = false)
        private double score;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected HmmerLocation() {}

        // Don't use Builder pattern because all fields are required
        public HmmerLocation(int start, int end, double score, double evalue,
                             int hmmStart, int hmmEnd, HmmBounds hmmBounds) {
            super(start, end);
            setHmmStart(hmmStart);
            setHmmEnd(hmmEnd);
            setHmmBounds(hmmBounds);
            setEvalue(evalue);
            setScore(score);
        }

        /**
         * Constructor that builds a HmmerMatch object from a
         * PfamHmmer3RawMatch object.
         *
         * @param pfamRawMatchObject being the Raw representation of the Match.
         */
        public HmmerLocation(PfamHmmer3RawMatch pfamRawMatchObject){
            this(
                    pfamRawMatchObject.getLocationStart(),
                    pfamRawMatchObject.getLocationEnd(),
                    pfamRawMatchObject.getLocationScore(),
                    pfamRawMatchObject.getDomainIeValue(),
                    pfamRawMatchObject.getHmmStart(),
                    pfamRawMatchObject.getHmmEnd(),
                    HmmBounds.parseSymbol(pfamRawMatchObject.getHmmBounds())
            );
        }

        @XmlAttribute(name="hmm-start", required=true)
        public int getHmmStart() {
            return hmmStart;
        }

        private void setHmmStart(int hmmStart) {
            this.hmmStart = hmmStart;
        }

        @XmlAttribute(name="hmm-end", required=true)
        public int getHmmEnd() {
            return hmmEnd;
        }

        private void setHmmEnd(int hmmEnd) {
            this.hmmEnd = hmmEnd;
        }

        @XmlAttribute(name="hmm-bounds", required=true)
        public HmmBounds getHmmBounds() {
            return hmmBounds;
        }

        private void setHmmBounds(HmmBounds hmmBounds) {
            this.hmmBounds = hmmBounds;
        }

        @XmlAttribute(required=true)
        public double getEvalue() {
            return evalue;
        }

        private void setEvalue(double evalue) {
            this.evalue = evalue;
        }

        @XmlAttribute(required=true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        /**
         * HMMER output notation for model match
         *
         * @author  Antony Quinn
         */
        public static enum HmmBounds {

            COMPLETE("[]", "Complete"),
            N_TERMINAL_COMPLETE("[.", "N-terminal complete"),
            C_TERMINAL_COMPLETE(".]", "C-terminal complete"),
            INCOMPLETE("..", "Incomplete");

            private final String symbol;
            private final String description;

            private HmmBounds(String symbol, String description) {
                this.symbol = symbol;
                this.description = description;
            }

            public String getSymbol() {
                return symbol;
            }

            public String getDescription() {
                return description;
            }

            @Override public String toString() {
                return symbol;
            }

            /**
             * Returns enum corresponding to symbol, for example "[."
             *
             * @param   symbol  HmmBounds symbol, for example "[." or ".."
             * @return  Enum corresponding to symbol, for example "[."
             */
            public static HmmBounds parseSymbol(String symbol)  {
                for (HmmBounds hb : HmmBounds.values()) {
                    if (symbol.equals(hb.getSymbol()))   {
                        return hb;
                    }
                }
                throw new IllegalArgumentException("Unrecognised symbol: " + symbol);
            }

        }

        // TODO: Figure out which class to use (HmmerMatch replaced by RawHmmMatch and HmmerMatch)
        //@ManyToOne(targetEntity = HmmerMatch.class)
        @XmlTransient
        @Override public Match getMatch() {
            return super.getMatch();
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HmmerLocation))
                return false;
            final HmmerLocation h = (HmmerLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(hmmStart, h.hmmStart)
                    .append(hmmEnd, h.hmmEnd)
                    .append(hmmBounds, h.hmmBounds)
                    .append(score, h.score)
                    .append(evalue, h.evalue)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 53)
                    .appendSuper(super.hashCode())
                    .append(hmmStart)
                    .append(hmmEnd)
                    .append(hmmBounds)
                    .append(score)
                    .append(evalue)
                    .toHashCode();
        }
    }
}
