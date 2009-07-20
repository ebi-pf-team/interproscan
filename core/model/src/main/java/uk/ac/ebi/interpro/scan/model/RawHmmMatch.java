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

import javax.xml.bind.annotation.*;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.Set;

/**
 * Hidden Markov Model match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlTransient
public class RawHmmMatch
        extends AbstractRawMatch<HmmLocation>
        implements RawMatch<HmmLocation>, Serializable {

    // TODO: Most of this code is the same as FilteredHmmMatch - can we share code, eg. by composition or generics? Same is true for FingerPrints 

    @Column
    private double evalue;
    
    @Column
    private double score;

    protected RawHmmMatch() {}

    // TODO: Make Location(s) a required argument?
    public RawHmmMatch(Model model, double score, double evalue) {
        super(model);
        setScore(score);
        setEvalue(evalue);
    }

    public RawHmmMatch(Model model, double score, double evalue, Set<HmmLocation> locations) {
        super(model, locations);
        setScore(score);
        setEvalue(evalue);
    }

    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue){
        this.evalue = evalue;
    }    

    public double getScore() {
        return score;
    }

    private void setScore (double score){
        this.score = score;
    }

    @Override public HmmLocation addLocation(HmmLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(HmmLocation location) {
        super.removeLocation(location);
    }

    @OneToMany(targetEntity = HmmLocation.class)
    @Override public Set<HmmLocation> getLocations() {
        return super.getLocations ();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawHmmMatch))
            return false;
        final RawHmmMatch m = (RawHmmMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(evalue, m.evalue)
                .append(score, m.score)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 77)
                .appendSuper(super.hashCode())
                .append(evalue)
                .append(score)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
