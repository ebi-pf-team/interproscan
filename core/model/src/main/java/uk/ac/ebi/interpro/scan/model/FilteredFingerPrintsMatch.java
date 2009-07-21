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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
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
@XmlType(name="FilteredFingerPrintsMatchType", propOrder={"signature", "locations"})
public class FilteredFingerPrintsMatch extends AbstractFilteredMatch<FingerPrintsLocation> {

    @Column
    private double evalue;

    @Column
    private String graphscan;

    @Column
    private int motifCount;

    protected FilteredFingerPrintsMatch() {}

    public FilteredFingerPrintsMatch(Signature signature, double evalue, String graphscan, int motifCount) {
        super(signature);
        setEvalue(evalue);
        setGraphscan(graphscan);
        setMotifCount(motifCount);
    }

    public FilteredFingerPrintsMatch(Signature signature, double evalue, String graphscan, int motifCount, Set<FingerPrintsLocation> locations) {
        super(signature, locations);
        setEvalue(evalue);
        setGraphscan(graphscan);
        setMotifCount(motifCount);        
    }    

    @XmlAttribute(required=true)
    public double getEvalue() {
        return evalue;
    }

    private void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    @XmlAttribute(required=true)
    public String getGraphscan() {
        return graphscan;
    }

    private void setGraphscan(String graphscan) {
        this.graphscan = graphscan;
    }

    @XmlAttribute(required=true)
    public int getMotifCount() {
        return motifCount;
    }

    private void setMotifCount(int motifCount) {
        this.motifCount = motifCount;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FilteredFingerPrintsMatch))
            return false;
        final FilteredFingerPrintsMatch m = (FilteredFingerPrintsMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(evalue, m.evalue)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 63)
                .appendSuper(super.hashCode())
                .append(evalue)
                .toHashCode();
    }

}
