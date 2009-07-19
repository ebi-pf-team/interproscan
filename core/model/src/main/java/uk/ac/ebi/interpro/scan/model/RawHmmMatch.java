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
@XmlType(name="RawHmmMatchType")//, propOrder={"model", "locations"})
public class RawHmmMatch
        extends AbstractRawMatch<HmmLocation>
        implements RawMatch<HmmLocation>, Serializable {

    // TODO: Most of this code is the same as FilteredHmmMatch - can we share code, eg. by composition or generics? Same is true for FingerPrints 

    @Column(nullable = false)
    private double evalue;
    
    @Column (nullable = false)
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

    @XmlAttribute(required=true)
    public double getEvalue() {
        return evalue;
    }

    /**
     * Private setter required by JPA
     * TODO - see if this can be removed - do not understand why it is required as the @Column annotation is on the field.
     * 
     * @param evalue
     */
    private void setEvalue(double evalue){
        this.evalue = evalue;
    }    

    @XmlAttribute(name="score", required=true)
    public double getScore() {
        return score;
    }

    /**
     * Private setter required by JPA
     * TODO - see if this can be removed - do not understand why it is required as the @Column annotation is on the field.
     * @param score
     */
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

}
