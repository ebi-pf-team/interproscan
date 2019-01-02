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
 * BlastProDom filtered match.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
@Table(name = "blast_prodom_match")
@XmlType(name = "BlastProDomMatchType")
public class BlastProDomMatch extends Match<BlastProDomMatch.BlastProDomLocation> {

    protected BlastProDomMatch() {
    }

    public BlastProDomMatch(Signature signature, String signatureModels,Set<BlastProDomLocation> locations) {
        super(signature, signatureModels, locations);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<BlastProDomLocation> clonedLocations = new HashSet<BlastProDomLocation>(this.getLocations().size());
        for (BlastProDomLocation location : this.getLocations()) {
            clonedLocations.add((BlastProDomLocation) location.clone());
        }
        return new BlastProDomMatch(this.getSignature(), this.getSignatureModels(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "blast_prodom_location")
    @XmlType(name = "BlastProDomLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class BlastProDomLocation extends Location {

        @Column(nullable = false)
        private double score;

        @Column(nullable = false)
        private double evalue;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected BlastProDomLocation() {
        }

        public BlastProDomLocation(int start, int end, double score, double evalue) {
            super(new BlastProDomLocationFragment(start, end));
            setScore(score);
            setEvalue(evalue);
        }

        @XmlAttribute(required = true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
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
            if (!(o instanceof BlastProDomLocation))
                return false;
            final BlastProDomLocation f = (BlastProDomLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(score, f.score)
                    .append(evalue, f.evalue)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 19)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .append(evalue)
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new BlastProDomLocation(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue());
        }

        /**
         * Location fragment of a ProDom match on a protein sequence
         */
        @Entity
        @Table(name = "blast_prodom_location_fragment")
        @XmlType(name = "ProDomLocationFragmentType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class BlastProDomLocationFragment extends LocationFragment {

            protected BlastProDomLocationFragment() {
            }

            public BlastProDomLocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof BlastProDomLocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(117, 119)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new BlastProDomLocationFragment(this.getStart(), this.getEnd());
            }
        }

    }
}
