/*
 * Copyright 2011 the original author or authors.
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an InterPro entry match on a protein sequence.
 *
 * @author  Antony Quinn
 * @version $Id$
 */

// TODO: This is a hack for the IMPACT review (June 2011)
// TODO: Need to refactor data model so that SuperMatch is a sub-class of Match (lot of work!)

@XmlType(name = "SuperMatchType")//, propOrder = {"entry", "locations"})
public class SuperMatch implements Serializable {

    private Protein protein;
    private Entry entry;
    private final Set<SuperMatch.Location> locations = new LinkedHashSet<SuperMatch.Location>();

    protected SuperMatch() {
    }

    public SuperMatch(Entry entry) {
        this.entry = entry;
    }

    @XmlTransient
    public Protein getProtein() {
        return protein;
    }

    void setProtein(Protein protein) {
        this.protein = protein;

    }

    @XmlElement(required = true)
    public Entry getEntry() {
        return entry;
    }

    private void setEntry(Entry entry) {
        this.entry = entry;
    }

    @XmlElement(name="location", required = true)
    public Set<SuperMatch.Location> getLocations() {
        return locations;
    }

    protected void setLocations(final Set<SuperMatch.Location> locations) {
        if (locations.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one location for the match");
        }
        for (SuperMatch.Location location : locations) {
            location.setSuperMatch(this);
            locations.add(location);
        }
    }

    public SuperMatch.Location addLocation(SuperMatch.Location location) throws IllegalArgumentException {
        if (location == null) {
            throw new IllegalArgumentException("'location' must not be null");
        }
        if (location.getSuperMatch() != null) {
            location.getSuperMatch().removeLocation(location);
        }
        location.setSuperMatch(this);
        locations.add(location);
        return location;
    }

    public void removeLocation(SuperMatch.Location location) {
        locations.remove(location);
        location.setSuperMatch(null);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SuperMatch))
            return false;
        final SuperMatch m = (SuperMatch) o;
        return new EqualsBuilder()
                .append(locations, m.locations)
                .append(entry, m.entry)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(67, 51)
                .append(locations)
                .append(entry)
                .toHashCode();
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Location of super-match on protein sequence.
     *
     * @author Antony Quinn
     * @version $Id$
     */
    public static class Location implements Serializable {

        private int start;
        private int end;
        private SuperMatch match;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected Location() {
        }

        public Location(int start, int end) {
            setStart(start);
            setEnd(end);
        }

        @XmlAttribute(required = true)
        public int getStart() {
            return start;
        }

        private void setStart(int start) {
            this.start = start;
        }

        @XmlAttribute(required = true)
        public int getEnd() {
            return end;
        }

        private void setEnd(int end) {
            this.end = end;
        }

        @XmlTransient
        public SuperMatch getSuperMatch() {
            return match;
        }

        void setSuperMatch(SuperMatch match) {
            this.match = match;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Location))
                return false;
            final Location h = (Location) o;
            return new EqualsBuilder()
                    .append(start, h.start)
                    .append(end, h.end)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(63, 55)
                    .append(start)
                    .append(end)
                    .toHashCode();
        }

        @Override public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }
}
