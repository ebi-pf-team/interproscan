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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;

/**
 * BlastProDom filtered match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table(name="blast_prodom_match")
@XmlType(name="BlastProDomMatchType")
public class BlastProDomMatch extends Match<BlastProDomMatch.BlastProDomLocation> {

    protected BlastProDomMatch() {}

    public BlastProDomMatch(Signature signature, Set<BlastProDomLocation> locations) {
        super(signature, locations);
    }

    /**
     * Location(s) of match on protein sequence
     *
     * @author  Antony Quinn
     */
    @Entity
    @Table(name="blast_prodom_location")
    @XmlType(name="BlastProDomLocationType")
    public static class BlastProDomLocation extends Location {

        @Column(nullable = false)
        private double score;

        /**
         * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
         */
        protected BlastProDomLocation() {}

        public BlastProDomLocation(int start, int end, double score) {
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
            if (!(o instanceof BlastProDomLocation))
                return false;
            final BlastProDomLocation f = (BlastProDomLocation) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(score, f.score)
                    .isEquals();
        }

        @Override public int hashCode() {
            return new HashCodeBuilder(19, 21)
                    .appendSuper(super.hashCode())
                    .append(score)
                    .toHashCode();
        }

    }

}
