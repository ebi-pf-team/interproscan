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
import uk.ac.ebi.interpro.scan.model.raw.alignment.AlignmentEncoder;
import uk.ac.ebi.interpro.scan.model.raw.alignment.CigarAlignmentEncoder;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ProfileScan filtered match.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
@XmlType(name = "ProfileScanMatchType")
public class ProfileScanMatch extends Match<ProfileScanMatch.ProfileScanLocation> {

    protected ProfileScanMatch() {
    }

    public ProfileScanMatch(Signature signature, String signatureModels, Set<ProfileScanLocation> locations) {
        super(signature, signatureModels, locations);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<ProfileScanLocation> clonedLocations = new HashSet<ProfileScanLocation>(this.getLocations().size());
        for (ProfileScanLocation location : this.getLocations()) {
            clonedLocations.add((ProfileScanLocation) location.clone());
        }
        return new ProfileScanMatch(this.getSignature(), this.getSignatureModels(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "profile_scan_location")
    @XmlType(name = "ProfileScanLocationType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
    @JsonIgnoreProperties({"id", "cigarAlignment"}) // cigarAlignment is not output i the json
    public static class ProfileScanLocation extends Location {

        @Column(nullable = false)
        private double score;

        @Column(nullable = false, length = 4000, name = "cigar_align")
        private String cigarAlignment;

        @Enumerated(EnumType.ORDINAL)
        @Column(nullable = false, name = "location_level")
        private ProfileScanMatch.ProfileScanLocation.Level level;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected ProfileScanLocation() {
        }

        public ProfileScanLocation(int start, int end, double score, String cigarAlignment, Level level) {
            super(new ProfileScanLocationFragment(start, end));
            setScore(score);
            setCigarAlignment(cigarAlignment);
            setLevel(level);
        }

        @XmlAttribute(required = true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
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
            if (alignment != null) {
                AlignmentEncoder encoder = new CigarAlignmentEncoder();
                setCigarAlignment(encoder.encode(alignment));
            }
        }

        public String getCigarAlignment() {
            return cigarAlignment;
        }

        private void setCigarAlignment(String cigarAlignment) {
            this.cigarAlignment = cigarAlignment;
        }

        @XmlAttribute(required = true)
        public ProfileScanMatch.ProfileScanLocation.Level getLevel() {
            return level;
        }

        private void setLevel(ProfileScanMatch.ProfileScanLocation.Level level) {
            this.level = level;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ProfileScanLocation))
                return false;
            final ProfileScanLocation f = (ProfileScanLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(score, f.score)
                    .append(cigarAlignment, f.cigarAlignment)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 81)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .append(cigarAlignment)
                    .toHashCode();
        }

        public Object clone() throws CloneNotSupportedException {
            return new ProfileScanLocation(this.getStart(), this.getEnd(), this.getScore(), this.getCigarAlignment(), this.getLevel());
        }

        public enum Level {
            MINUS_ONE("-1"),
            ZERO("0"),
            ONE("1");

            private static final Map<String, Level> STRING_TO_LEVEL = new HashMap<>(Level.values().length);

            static {
                for (Level level : Level.values()) {
                    STRING_TO_LEVEL.put(level.levelValue, level);
                }
            }

            String levelValue;

            Level(String levelValue) {
                this.levelValue = levelValue;
            }

            public static Level byLevelString(String levelString) {
                return STRING_TO_LEVEL.get(levelString);
            }
        }

        @XmlType(name = "LevelType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
        public enum LevelType {

            STRONG("(0)", "!"),
            WEAK("(-1)", "?"),
            NONE(null, "?");

            private static final Map<String, LevelType> TAG_TO_LEVEL = new HashMap<>(Level.values().length);

            static {
                for (LevelType level : LevelType.values()) {
                    // Note that HashMap DOES support null keys, as required for the NONE Level.
                    TAG_TO_LEVEL.put(level.tag, level);
                }
            }

            private final String tag;
            private final String symbol;

            LevelType(String tag, String symbol) {
                this.tag = tag;
                this.symbol = symbol;
            }

            public String getTag() {
                return tag;
            }

            public String getTagNumber(){
                if (tag.equals("(0)")){
                    return "0";
                }else if (tag.equals("(-1)")){
                    return "-1";
                }
                return null;
            }
            public String getSymbol() {
                return symbol;
            }

            @Override
            public String toString() {
                return symbol;
            }

            /**
             * Returns enum corresponding to tag.
             *
             * @param tag Tag, for example null, (0) or (-1).
             * @return Enum corresponding to tag
             */
            public static LevelType getLevelByTag(String tag) {
                return TAG_TO_LEVEL.get(tag);
            }
        }

        /**
         * Location fragment of a Prosite Profile match on a protein sequence
         */
        @Entity
        @Table(name = "profile_scan_location_fragment")
        @XmlType(name = "ProfileScanLocationFragmentType", namespace = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas")
        public static class ProfileScanLocationFragment extends LocationFragment {

            protected ProfileScanLocationFragment() {
            }

            public ProfileScanLocationFragment(int start, int end) {
                super(start, end);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof ProfileScanLocationFragment))
                    return false;
                return new EqualsBuilder()
                        .appendSuper(super.equals(o))
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(119, 181)
                        .appendSuper(super.hashCode())
                        .toHashCode();
            }

            public Object clone() throws CloneNotSupportedException {
                return new ProfileScanLocationFragment(this.getStart(), this.getEnd());
            }
        }


    }
}
