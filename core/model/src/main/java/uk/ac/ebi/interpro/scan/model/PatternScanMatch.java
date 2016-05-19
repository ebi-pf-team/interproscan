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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    public PatternScanMatch(Signature signature, Set<PatternScanLocation> locations) {
        super(signature, locations);
    }

    public Object clone() throws CloneNotSupportedException {
        final Set<PatternScanLocation> clonedLocations = new HashSet<PatternScanLocation>(this.getLocations().size());
        for (PatternScanLocation location : this.getLocations()) {
            clonedLocations.add((PatternScanLocation) location.clone());
        }
        return new PatternScanMatch(this.getSignature(), clonedLocations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author Antony Quinn
     */
    @Entity
    @Table(name = "pattern_scan_location")
    @XmlType(name = "PatternScanLocationType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    public static class PatternScanLocation extends Location {

        @Enumerated(EnumType.ORDINAL)   // Using ordinal to keep the database size down.
        @Column(nullable = false, name = "location_level")
        private Level level;

        @Column(nullable = false, name = "cigar_align")
        private String cigarAlignment;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected PatternScanLocation() {
        }

        public PatternScanLocation(int start, int end, Level level, String cigarAlignment) {
            super(start, end);
            setLevel(level);
            setCigarAlignment(cigarAlignment);
        }

        @XmlAttribute(required = true)
        public Level getLevel() {
            return level;
        }

        private void setLevel(Level level) {
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

        /**
         * ProSite cut-off level
         * (see <a href="http://www.expasy.ch/prosite/prosuser.html#convent37">PROSITE User Manual</a>)
         *
         * @author Antony Quinn
         * @author Phil Jones
         */
        @XmlType(name = "LevelType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
        @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
        public enum Level {

            STRONG("(0)", "!"),
            WEAK("(-1)", "?"),
            NONE(null, "?");

            private static final Map<String, Level> TAG_TO_LEVEL = new HashMap<String, Level>(Level.values().length);

            static {
                for (Level level : Level.values()) {
                    // Note that HashMap DOES support null keys, as required for the NONE Level.
                    TAG_TO_LEVEL.put(level.tag, level);
                }
            }

            private final String tag;
            private final String symbol;

            Level(String tag, String symbol) {
                this.tag = tag;
                this.symbol = symbol;
            }

            public String getTag() {
                return tag;
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
            public static Level getLevelByTag(String tag) {
                return TAG_TO_LEVEL.get(tag);
            }

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

    }
}
