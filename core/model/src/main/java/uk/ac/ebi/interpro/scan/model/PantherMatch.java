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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"annotations"})
public class PantherMatch extends Match<PantherMatch.PantherLocation> {

    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false, name = "family_name")
    private String familyName;

    @Column(nullable = false)
    private double score;

    @Column
    private String annotationsNodeId;

    @Column (length = 8000)
    private String annotations;

    protected PantherMatch() {
    }

    public PantherMatch(Signature signature, String signatureModels, Set<PantherLocation> locations,
                        String annotationsNodeId, double evalue, String familyName, double score, String annotations) {
        super(signature, signatureModels, locations);
        setAnnotationsNodeId(annotationsNodeId);
        setEvalue(evalue);
        this.familyName = familyName;
        this.score = score;
        setAnnotations(annotations);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<PantherLocation> clonedLocations = new HashSet<PantherLocation>(this.getLocations().size());
        for (PantherLocation location : this.getLocations()) {
            clonedLocations.add((PantherLocation) location.clone());
        }
        return new PantherMatch(this.getSignature(), this.getSignatureModels(), clonedLocations,
                this.getAnnotationsNodeId(), this.getEvalue(), this.getFamilyName(), this.getScore(), this.getAnnotations());
    }

    @XmlAttribute(required = true)
    public String getAnnotationsNodeId() {
        return annotationsNodeId;
    }

    public void setAnnotationsNodeId(String annotationsNodeId) {
        this.annotationsNodeId = annotationsNodeId;
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

    public String getAnnotations() {
        return annotations;
    }

    //@XmlAttribute(required = true)
    public void setAnnotations(String annotations) {
        this.annotations = annotations;
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
    @XmlType(name = "PantherLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", propOrder = {"envelopeStart", "envelopeEnd", "hmmStart", "hmmEnd", "hmmLength", "hmmBounds"})
    public static class PantherLocation extends Location {

        @Column(nullable = false, name = "hmm_start")
        private int hmmStart;

        @Column(nullable = false, name = "hmm_end")
        private int hmmEnd;

        @Column(nullable = false, name = "hmm_length")
        private int hmmLength;

        @Column(nullable = false, name = "hmm_bounds", length = 2)
        private String hmmBounds;

        @Column(name = "envelope_start", nullable = false)
        private int envelopeStart;

        @Column(name = "envelope_end", nullable = false)
        private int envelopeEnd;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected PantherLocation() {
        }

        public PantherLocation(int start, int end, int hmmStart, int hmmEnd, int hmmLength, HmmBounds hmmBounds, int envelopeStart, int envelopeEnd) {
            super(new PantherLocationFragment(start, end));
            this.hmmStart = hmmStart;
            this.hmmEnd = hmmEnd;
            this.hmmLength = hmmLength;
            setHmmBounds(hmmBounds);
            this.envelopeStart = envelopeStart;
            this.envelopeEnd = envelopeEnd;
        }

        @XmlAttribute(name = "hmm-start", required = true)
        public int getHmmStart() {
            return hmmStart;
        }

        private void setHmmStart(int hmmStart) {
            this.hmmStart = hmmStart;
        }

        @XmlAttribute(name = "hmm-end", required = true)
        public int getHmmEnd() {
            return hmmEnd;
        }

        private void setHmmEnd(int hmmEnd) {
            this.hmmEnd = hmmEnd;
        }

        @XmlAttribute(name = "hmm-length", required = true)
        public int getHmmLength() {
            return hmmLength;
        }

        private void setHmmLength(int hmmLength) {
            this.hmmLength = hmmLength;
        }

        @XmlAttribute(name="hmm-bounds", required=true)
        public HmmBounds getHmmBounds() {
            return HmmBounds.parseSymbol(hmmBounds);
        }

        private void setHmmBounds(HmmBounds hmmBounds) {
            this.hmmBounds = hmmBounds.getSymbol();
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

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PantherLocation))
                return false;
            PantherLocation that = (PantherLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(hmmStart, that.hmmStart)
                    .append(hmmEnd, that.hmmEnd)
                    .append(hmmLength, that.hmmLength)
                    .append(hmmBounds, that.hmmBounds)
                    .append(envelopeStart, that.envelopeStart)
                    .append(envelopeEnd, that.envelopeEnd)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + hmmStart;
            result = 31 * result + hmmEnd;
            result = 31 * result + hmmLength;
            result = 31 * result + (hmmBounds != null ? hmmBounds.hashCode() : 0);
            result = 31 * result + envelopeStart;
            result = 31 * result + envelopeEnd;
            return result;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            final PantherLocation clone = new PantherLocation(this.getStart(), this.getEnd(), this.getHmmStart(), this.getHmmEnd(), this.getHmmLength(), this.getHmmBounds(), this.getEnvelopeStart(), this.getEnvelopeEnd());
            return clone;
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
