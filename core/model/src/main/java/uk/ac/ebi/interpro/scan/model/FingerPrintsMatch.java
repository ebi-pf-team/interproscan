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
import java.util.HashSet;
import java.util.Set;

/**
 * FingerPRINTS match.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
//@Table(uniqueConstraints = @UniqueConstraint())
@XmlType(name = "FingerPrintsMatchType")
public class FingerPrintsMatch extends Match<FingerPrintsMatch.FingerPrintsLocation> {

    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false, length = 25)
    private String graphscan;

    protected FingerPrintsMatch() {
    }

    public FingerPrintsMatch(Signature signature, double evalue, String graphscan, Set<FingerPrintsLocation> locations) {
        super(signature, locations);
        setEvalue(evalue);
        setGraphscan(graphscan);
    }

    @XmlAttribute(required = true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @XmlAttribute(required = true)
    public String getGraphscan() {
        return graphscan;
    }

    private void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FingerPrintsMatch))
            return false;
        final FingerPrintsMatch m = (FingerPrintsMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals()
                &&
                PersistenceConversion.equivalent(evalue, m.evalue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 61)
                .appendSuper(super.hashCode())
                .append(evalue)
                .toHashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<FingerPrintsLocation> clonedLocations = new HashSet<FingerPrintsLocation>(this.getLocations().size());
        for (FingerPrintsLocation location : this.getLocations()) {
            clonedLocations.add((FingerPrintsLocation) location.clone());
        }
        return new FingerPrintsMatch(this.getSignature(), this.getEvalue(), this.getGraphscan(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "finger_prints_location")
    @XmlType(name = "FingerPrintsLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class FingerPrintsLocation extends Location {

        @Column(nullable = false)
        private double pvalue;

        @Column(nullable = false)
        private double score;

        @Column(nullable = false, name = "motif_number")
        private int motifNumber;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected FingerPrintsLocation() {
        }

        public FingerPrintsLocation(int start, int end, double pvalue, double score, int motifNumber) {
            super(start, end);
            setPvalue(pvalue);
            setScore(score);
            setMotifNumber(motifNumber);
        }

        @XmlAttribute(required = true)
        public double getPvalue() {
            return pvalue;
        }

        private void setPvalue(double pvalue) {
            this.pvalue = pvalue;
        }

        @XmlAttribute(required = true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        @XmlAttribute(required = true)
        public int getMotifNumber() {
            return motifNumber;
        }

        private void setMotifNumber(int motifNumber) {
            this.motifNumber = motifNumber;
        }

        @Override
        public boolean equals(Object o) {
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

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 61)
                    .appendSuper(super.hashCode())
                    .append(pvalue)
                    .append(score)
                    .append(motifNumber)
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new FingerPrintsLocation(this.getStart(), this.getEnd(), this.getPvalue(), this.getScore(), this.getMotifNumber());
        }

    }
}
