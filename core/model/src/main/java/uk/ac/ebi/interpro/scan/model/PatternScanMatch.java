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

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * PatternScan filtered match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table(name="pattern_scan_match")
@XmlType(name="PatternScanMatchType")
public class PatternScanMatch extends Match<PatternScanMatch.PatternScanLocation> {

    protected PatternScanMatch() {}

    public PatternScanMatch(Signature signature, Set<PatternScanLocation> locations) {
        super(signature, locations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author  Antony Quinn
     */
    @Entity
    @Table(name="pattern_scan_location")
    @XmlType(name="PatternScanLocationType")
    public static class PatternScanLocation extends Location {

        @Column(nullable = false, name="location_level")
        private Level level;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected PatternScanLocation() {}

        public PatternScanLocation(int start, int end, Level level) {
            super(start, end);
            setLevel(level);
        }

        @XmlAttribute(required=true)
        public Level getLevel() {
            return level;
        }

        private void setLevel(Level level) {
            this.level = level;
        }

        /**
         * ProSite cut-off level
         * (see <a href="http://www.expasy.ch/prosite/prosuser.html#convent37">PROSITE User Manual</a>)
         *
         * @author Antony Quinn
         */
        @XmlType(name="LevelType")
        public static enum Level {

            STRONG(0, "!"),
            WEAK(-1, "?");

            private final int    tag;
            private final String symbol;

            private Level(int tag, String symbol) {
                this.tag    = tag;
                this.symbol = symbol;
            }

            public int getTag() {
                return tag;
            }

            public String getSymbol() {
                return symbol;
            }

            @Override public String toString() {
                return symbol;
            }

            /**
             * Returns enum corresponding to tag.
             *
             * @param   tag Tag, for example 0 or -1.
             * @return  Enum corresponding to tag
             */
            // TODO: This needs testing
            public static Level parseTag(int tag)  {
                for (Level level : Level.values()) {
                    if (tag == level.getTag())   {
                        return level;
                    }
                }
                throw new IllegalArgumentException("Unrecognised tag: " + String.valueOf(tag));
            }

        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PatternScanLocation))
                return false;
            final PatternScanLocation f = (PatternScanLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(level, f.level)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 85)
                    .appendSuper(super.hashCode())
                    .append(level)
                    .toHashCode();
        }

    }
}
