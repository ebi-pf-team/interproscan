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
 * HMMER3 match with sites.
 */
@Entity
@Table(name = "hmmer3_match_with_sites")
@XmlType(name = "Hmmer3MatchWithSitesType")
public class Hmmer3MatchWithSites extends HmmerMatchWithSites<Hmmer3MatchWithSites.Hmmer3LocationWithSites> implements Serializable {


    private String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    protected Hmmer3MatchWithSites() {
    }

    public Hmmer3MatchWithSites(Signature signature, String signatureModels, double score, double evalue, Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations) {
        super(signature, signatureModels, score, evalue, locations);
    }

    public Hmmer3MatchWithSites(Signature signature, String signatureModels, double score, double evalue, Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations, String scope) {
        super(signature, signatureModels, score, evalue, locations);
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hmmer3MatchWithSites)) return false;
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
        final Set<Hmmer3LocationWithSites> clonedLocations = new HashSet<>(this.getLocations().size());
        for (Hmmer3LocationWithSites location : this.getLocations()) {
            clonedLocations.add((Hmmer3LocationWithSites) location.clone());
        }
        return new Hmmer3MatchWithSites(this.getSignature(), this.getSignatureModels(), this.getScore(), this.getEvalue(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "hmmer3_location_with_sites")
    @XmlType(name = "Hmmer3LocationWithSitesType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
    public static class Hmmer3LocationWithSites extends HmmerLocationWithSites<Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment> {

        @Column(name = "envelope_start", nullable = false)
        private int envelopeStart;

        @Column(name = "envelope_end", nullable = false)
        private int envelopeEnd;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected Hmmer3LocationWithSites() {
        }

        public Hmmer3LocationWithSites(int start, int end, double score, double evalue,
                              int hmmStart, int hmmEnd, int hmmLength, HmmBounds hmmBounds,
                              int envelopeStart, int envelopeEnd, Set<HmmerSite> sites) {
            super(new Hmmer3LocationWithSitesFragment(start, end), score, evalue, hmmStart, hmmEnd, hmmLength, hmmBounds, sites);
            setEnvelopeStart(envelopeStart);
            setEnvelopeEnd(envelopeEnd);
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
            if (this == o) return true;
            if (!(o instanceof Hmmer3LocationWithSites)) return false;
            Hmmer3LocationWithSites l = (Hmmer3LocationWithSites) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(envelopeStart, l.envelopeStart)
                    .append(envelopeEnd, l.envelopeEnd)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(39, 59)
                    .appendSuper(super.hashCode())
                    .append(envelopeStart)
                    .append(envelopeEnd)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public Object clone() throws CloneNotSupportedException {
            final Set<HmmerSite> clonedSites = new HashSet<>(this.getSites().size());
            for (HmmerSite site : this.getSites()) {
                clonedSites.add((HmmerSite) site.clone());
            }
            final Hmmer3LocationWithSites clone = new Hmmer3LocationWithSites(this.getStart(), this.getEnd(), this.getScore(), this.getEvalue(), this.getHmmStart(), this.getHmmEnd(), this.getHmmLength(), this.getHmmBounds(), this.getEnvelopeStart(), this.getEnvelopeEnd(), clonedSites);
            return clone;
        }

        /**
         * Location fragment of a HMMER3 match on a protein sequence
         */
        @Entity
        @Table(name = "hmmer3_locn_frag_with_sites")
        @XmlType(name = "Hmmer3LocationFragmentWithSitesType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        public static class Hmmer3LocationWithSitesFragment extends LocationFragment {

            protected Hmmer3LocationWithSitesFragment() {
            }

            public Hmmer3LocationWithSitesFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Hmmer3LocationWithSitesFragment))
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
                return new Hmmer3LocationWithSitesFragment(this.getStart(), this.getEnd());
            }
        }

        @Entity
        @Table(name = "hmmer3_site")
        @XmlType(name = "Hmmer3SiteType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        public static class Hmmer3Site extends HmmerSite {


            @Column (name = "SITE_LABEL")
            private String label;

            @Column (name = "SITE_GROUP")
            private int group;

            @Column //(name = "HMMSTART")
            private int hmmStart;

            @Column //(name = "HMMEND")
            private int hmmEnd;

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public int getGroup() {
                return group;
            }

            public void setGroup(int group) {
                this.group = group;
            }

            public int getHmmStart() {
                return hmmStart;
            }

            public void setHmmStart(int hmmStart) {
                this.hmmStart = hmmStart;
            }

            public int getHmmEnd() {
                return hmmEnd;
            }

            public void setHmmEnd(int hmmEnd) {
                this.hmmEnd = hmmEnd;
            }

            protected Hmmer3Site() {
            }

            public Hmmer3Site(String description, Set<SiteLocation> siteLocations) {
                super(description, siteLocations);
                this.label = null;
                this.hmmStart = 0;
                this.hmmEnd = 0;
                this.group = 0;
            }

            public Hmmer3Site(String description,   String label, int hmmStart,
                              int hmmEnd,
                              int group, Set<SiteLocation> siteLocations) {
                super(description, siteLocations);
                this.label = label;
                this.hmmStart = hmmStart;
                this.hmmEnd = hmmEnd;
                this.group = group;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Hmmer3Site))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(41, 79)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                final Set<SiteLocation> clonedSiteLocations = new HashSet<>(this.getSiteLocations().size());
                for (SiteLocation sl : this.getSiteLocations()) {
                    clonedSiteLocations.add((SiteLocation) sl.clone());
                }
                return new Hmmer3Site(this.getDescription(), clonedSiteLocations);
            }

        }


    }
}