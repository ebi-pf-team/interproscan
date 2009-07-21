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
 * FingerPRINTS match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlTransient
public class RawFingerPrintsMatch extends AbstractRawMatch<FingerPrintsLocation> {

    @Column
    private double evalue;

    @Column
    private String graphscan;

    @Column
    private int motifCount;

    protected RawFingerPrintsMatch() {}

    public RawFingerPrintsMatch(Model model, double evalue, String graphscan, int motifCount) {
        super(model);
        setEvalue(evalue);
        setGraphscan(graphscan);
        setMotifCount(motifCount);        
    }

    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue){
        this.evalue = evalue;
    }

    public String getGraphscan() {
        return graphscan;
    }

    private void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    public int getMotifCount() {
        return motifCount;
    }

    private void setMotifCount(int motifCount) {
        this.motifCount = motifCount;
    }    

    @OneToMany(targetEntity = FingerPrintsLocation.class)
    @Override public Set<FingerPrintsLocation> getLocations() {
        return super.getLocations();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RawFingerPrintsMatch))
            return false;
        final RawFingerPrintsMatch m = (RawFingerPrintsMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(evalue, m.evalue)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 75)
                .appendSuper(super.hashCode())
                .append(evalue)
                .toHashCode();
    }

}
