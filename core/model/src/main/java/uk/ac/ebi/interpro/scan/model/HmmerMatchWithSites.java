/*
 * Copyright 2009-2010 the original author or authors.
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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Set;

/**
 * HMMER match.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "HmmerMatchWithSitesType")
abstract class HmmerMatchWithSites<T extends HmmerLocationWithSites> extends Match<T> implements Serializable {

    @Column(nullable = false)
    private double evalue;

    @Column(nullable = false)
    private double score;

    protected HmmerMatchWithSites() {
    }

    public HmmerMatchWithSites(Signature signature, double score, double evalue, Set<T> locations) {
        super(signature, locations);
        setScore(score);
        setEvalue(evalue);
    }

    @XmlAttribute(required = true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    private void setScore(double score) {
        this.score = score;
    }

    @XmlAttribute(name = "score", required = true)
    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HmmerMatchWithSites))
            return false;
        final HmmerMatchWithSites m = (HmmerMatchWithSites) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(score, m.score)
                .isEquals() &&
                PersistenceConversion.equivalent(evalue, m.evalue);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 49)
                .appendSuper(super.hashCode())
                .append(evalue)
                .append(score)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
