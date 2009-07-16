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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Hidden Markov Model match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlType(name="FilteredHmmMatchType", propOrder={"signature", "locations"})
public class FilteredHmmMatch
        extends AbstractFilteredMatch<HmmLocation>
        implements FilteredMatch<HmmLocation>, Serializable {

    @Column (nullable = false)
    private double evalue;
    
    @Column (nullable = false)
    private double score;

    protected FilteredHmmMatch() {}

    // TODO: Make Location(s) a required argument?
    public FilteredHmmMatch(Signature signature, double score, double evalue) {
        super(signature);
        this.score  = score;
        this.evalue = evalue;
    }
    
    @XmlAttribute(name="evalue", required=true)
    public double getEvalue() {
        return evalue;
    }

    /**
     * setter required by JPA. (private as not needed for any other reason)
     * @param evalue
     */
    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    /**
     * setter required by JPA. (private as not needed for any other reason)
     * @param score
     */
    private void setScore(double score) {
        this.score = score;
    }

    @XmlAttribute(name="score", required=true)
    public double getScore() {
        return score;
    }

    @Override public HmmLocation addLocation(HmmLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(HmmLocation location) {
        super.removeLocation(location);
    }

}
