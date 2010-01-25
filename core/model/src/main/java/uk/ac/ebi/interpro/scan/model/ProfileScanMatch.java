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
 * ProfileScan filtered match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table(name="profile_scan_match")
@XmlType(name="ProfileScanMatchType")
public class ProfileScanMatch extends Match<ProfileScanMatch.ProfileScanLocation> {

    protected ProfileScanMatch() {}

    public ProfileScanMatch(Signature signature, Set<ProfileScanLocation> locations) {
        super(signature, locations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author  Antony Quinn
     */
    @Entity
    @Table(name="profile_scan_location")
    @XmlType(name="ProfileScanLocationType")
    public static class ProfileScanLocation extends Location {

        @Column(nullable = false)
        private double score;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected ProfileScanLocation() {}

        public ProfileScanLocation(int start, int end, double score) {
            super(start, end);
            setScore(score);
        }

        @XmlAttribute(required=true)
        public double getScore() {
            return score;
        }

        private void setScore(double score) {
            this.score = score;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ProfileScanLocation))
                return false;
            final ProfileScanLocation f = (ProfileScanLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(score, f.score)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 81)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .toHashCode();
        }

    }
}
