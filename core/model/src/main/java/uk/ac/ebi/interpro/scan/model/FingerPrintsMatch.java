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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * FingerPRINTS match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlType(name="FingerPrintsMatchType", propOrder={"signature", "locations"})
public class FingerPrintsMatch extends Match<FingerPrintsMatch.FingerPrintsLocation> {

    @Column
    private double evalue;

    @Column
    private String graphscan;

    @Column
    private int motifCount;

    protected FingerPrintsMatch() {}

    public FingerPrintsMatch(Signature signature, double evalue, String graphscan, int motifCount, Set<FingerPrintsLocation> locations) {
        super(signature, locations);
        setEvalue(evalue);
        setGraphscan(graphscan);
        setMotifCount(motifCount);        
    }

    @XmlAttribute(required=true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @XmlAttribute(required=true)
    public String getGraphscan() {
        return graphscan;
    }

    private void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    @XmlAttribute(required=true)
    public int getMotifCount() {
        return motifCount;
    }

    private void setMotifCount(int motifCount) {
        this.motifCount = motifCount;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FingerPrintsMatch))
            return false;
        final FingerPrintsMatch m = (FingerPrintsMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(evalue, m.evalue)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 63)
                .appendSuper(super.hashCode())
                .append(evalue)
                .toHashCode();
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author  Antony Quinn
     */
    @Entity
    @XmlType(name="FingerPrintsLocationType")
    public static class FingerPrintsLocation extends Location {

        @Column (nullable = false)
        private double pvalue;

        @Column (nullable = false)
        private double score;

        @Column (nullable = false)
        private int motifNumber;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected FingerPrintsLocation() {}

        public FingerPrintsLocation(int start, int end, double pvalue, double score, int motifNumber) {
            super(start, end);
            setPvalue(pvalue);
            setScore(score);
            setMotifNumber(motifNumber);
        }

        @XmlAttribute(required=true)
        public double getPvalue() {
            return pvalue;
        }

        private void setPvalue(double pvalue) {
            this.pvalue = pvalue;
        }

        @XmlAttribute(required=true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        @XmlAttribute(required=true)
        public int getMotifNumber() {
            return motifNumber;
        }

        private void setMotifNumber(int motifNumber) {
            this.motifNumber = motifNumber;
        }

        // TODO: Figure out which class to use (FingerPrintsMatch replaced by RawFingerPrintsMatch and FingerPrintsMatch)
        //@ManyToOne(targetEntity = FingerPrintsMatch.class)
        @XmlTransient
        @Override public Match getMatch() {
            return super.getMatch();
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof FingerPrintsLocation))
                return false;
            final FingerPrintsLocation f = (FingerPrintsLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(pvalue, f.pvalue)
                    .append(score, f.score)
                    .append(motifNumber, f.motifNumber)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 61)
                    .appendSuper(super.hashCode())
                    .append(pvalue)
                    .append(score)
                    .append(motifNumber)
                    .toHashCode();
        }

    }
}
