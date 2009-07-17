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
 * FingerPRINTS match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlType(name="FilteredFingerPrintsMatchType", propOrder={"signature", "locations"})
public class FilteredFingerPrintsMatch
        extends AbstractFilteredMatch<FingerPrintsLocation>
        implements FilteredMatch<FingerPrintsLocation>, Serializable {

    @Column
    private double evalue;

    protected FilteredFingerPrintsMatch() {}

    public FilteredFingerPrintsMatch(Signature signature, double evalue) {
        super(signature);
        this.evalue = evalue;
    }

    @XmlAttribute(required=true)
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

    @Override public FingerPrintsLocation addLocation(FingerPrintsLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(FingerPrintsLocation location) {
        super.removeLocation(location);
    }

}
