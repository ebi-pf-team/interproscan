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
 * FingerPRINTS match.
 *
 * @author  Antony Quinn
 * @version $Id: RawFingerPrintsMatch.java,v 1.2 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 */
@Entity
@XmlType(name="RawFingerPrintsMatchType", propOrder={"model", "locations"})
public class RawFingerPrintsMatch
        extends AbstractRawMatch<FingerPrintsLocation>
        implements RawMatch<FingerPrintsLocation>, Serializable {

    @Column
    private double evalue;

    protected RawFingerPrintsMatch() {}

    public RawFingerPrintsMatch(Model model, double evalue) {
        super(model);
        this.evalue = evalue;        
    }

    @XmlAttribute(name="evalue", required=true)
    public double getEValue() {
        return evalue;
    }

    /**
     * Private setter required by JPA
     * TODO - see if this can be removed - do not understand why it is required as the @Column annotation is on the field.
     * @param evalue
     */
    private void setEValue(double evalue){
        this.evalue = evalue;
    }        

    @Override public FingerPrintsLocation addLocation(FingerPrintsLocation location) {
        return super.addLocation(location);
    }

    @Override public void removeLocation(FingerPrintsLocation location) {
        super.removeLocation(location);
    }

    @OneToMany(targetEntity = FingerPrintsLocation.class)
    @Override public Set<FingerPrintsLocation> getLocations() {
        return super.getLocations();
    }


    @Override public void setLocations(Set<FingerPrintsLocation> locations){
        super.setLocations(locations);
    }

}
