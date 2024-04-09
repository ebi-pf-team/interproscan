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
import uk.ac.ebi.interpro.scan.model.raw.alignment.AlignmentEncoder;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * PatternScan filtered match.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
@XmlType(name = "PatternScanMatchType")
public class PatternScanMatch extends Match<PatternScanMatch.PatternScanLocation> {

    protected PatternScanMatch() {
    }

    public PatternScanMatch(Signature signature, String signatureModels, Set<PatternScanLocation> locations) {
        super(signature, signatureModels, locations);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<PatternScanLocation> clonedLocations = new HashSet<PatternScanLocation>(this.getLocations().size());
        for (PatternScanLocation location : this.getLocations()) {
            clonedLocations.add((PatternScanLocation) location.clone());
        }
        return new PatternScanMatch(this.getSignature(), this.getSignatureModels(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "pattern_scan_location")
    @XmlType(name = "PatternScanLocationType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
    public static class PatternScanLocation extends Location {

        @Enumerated(EnumType.ORDINAL)   // Using ordinal to keep the database size down.
        @Column(nullable = false, name = "location_level")
        private ProfileScanMatch.ProfileScanLocation.LevelType level;

        @Column(nullable = false, length = 4000, name = "cigar_align")
        private String cigarAlignment;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected PatternScanLocation() {
        }

        public PatternScanLocation(int start, int end, ProfileScanMatch.ProfileScanLocation.LevelType level, String cigarAlignment) {
            super(new PatternScanLocationFragment(start, end));
            setLevel(level);
            setCigarAlignment(cigarAlignment);
        }

        @XmlAttribute(required = true)
        public ProfileScanMatch.ProfileScanLocation.LevelType getLevel() {
            return level;
        }

        private void setLevel(ProfileScanMatch.ProfileScanLocation.LevelType level) {
            this.level = level;
        }

        /**
         * Returns sequence alignment.
         *
         * @return Sequence alignment.
         */
        @XmlElement(required = true)
        public String getAlignment() {
            if (cigarAlignment == null) {
                return null;
            }
            AlignmentEncoder encoder = new CigarAlignmentEncoder();

            return encoder.decode(getMatch().getProtein().getSequence(), cigarAlignment, getStart(), getEnd());
        }

        public void setAlignment(String alignment) {
            AlignmentEncoder encoder = new CigarAlignmentEncoder();
            setCigarAlignment(encoder.encode(alignment));
        }

        public String getCigarAlignment() {
            return cigarAlignment;
        }

        private void setCigarAlignment(String cigarAlignment) {
            this.cigarAlignment = cigarAlignment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PatternScanLocation))
                return false;
            final PatternScanLocation f = (PatternScanLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(level, f.level)
                    .append(cigarAlignment, f.cigarAlignment)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 85)
                    .appendSuper(super.hashCode())
                    .append(level)
                    .append(cigarAlignment)
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new PatternScanLocation(this.getStart(), this.getEnd(), this.getLevel(), this.getCigarAlignment());
        }

        /**
         * Location fragment of a Prosite Pattern match on a protein sequence
         */
        @Entity
        @Table(name = "pattern_scan_location_fragment")
        @XmlType(name = "PatternScanLocationFragmentType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        public static class PatternScanLocationFragment extends LocationFragment {

            protected PatternScanLocationFragment() {
            }

            public PatternScanLocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof PatternScanLocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(119, 185)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new PatternScanLocationFragment(this.getStart(), this.getEnd());
            }
        }


    }
}
