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
 * PANTHER match.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @since 1.0
 */
@Entity
@XmlType(name = "PantherMatchType")
public class PantherMatch extends Match<PantherMatch.PantherLocation> {

    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false, name = "family_name")
    private String familyName;

    @Column(nullable = false)
    private double score;


    protected PantherMatch() {
    }

    public PantherMatch(Signature signature, Set<PantherLocation> locations, double evalue, String familyName, double score) {
        super(signature, locations);
        setEvalue(evalue);
        this.familyName = familyName;
        this.score = score;
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<PantherLocation> clonedLocations = new HashSet<PantherLocation>(this.getLocations().size());
        for (PantherLocation location : this.getLocations()) {
            clonedLocations.add((PantherLocation) location.clone());
        }
        return new PantherMatch(this.getSignature(), clonedLocations, this.getEvalue(), this.getFamilyName(), this.getScore());
    }

    @XmlAttribute(required = true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @XmlAttribute(required = true)
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @XmlAttribute(required = true)
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PantherMatch))
            return false;
        final PantherMatch m = (PantherMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(familyName, m.familyName)
                .append(score, m.score)
                .isEquals()
                &&
                PersistenceConversion.equivalent(evalue, m.evalue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 63)
                .appendSuper(super.hashCode())
                .append(evalue)
                .append(familyName)
                .append(score)
                .toHashCode();
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     * @author Maxim Scheremetjew, EMBL-EBI, InterPro
     */
    @Entity
    @Table(name = "panther_location")
    @XmlType(name = "PantherLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class PantherLocation extends Location {

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected PantherLocation() {
        }

        public PantherLocation(int start, int end) {
            super(new PantherLocationFragment(start, end));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PantherLocation))
                return false;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 61)
                    .appendSuper(super.hashCode())
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new PantherLocation(this.getStart(), this.getEnd());
        }

        /**
         * Location fragment of a PANTHER match on a protein sequence
         */
        @Entity
        @Table(name = "panther_location_fragment")
        @XmlType(name = "PantherLocationFragmentType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        public static class PantherLocationFragment extends LocationFragment {

            protected PantherLocationFragment() {
            }

            public PantherLocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof PantherLocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(119, 161)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new PantherLocationFragment(this.getStart(), this.getEnd());
            }
        }

    }
}
