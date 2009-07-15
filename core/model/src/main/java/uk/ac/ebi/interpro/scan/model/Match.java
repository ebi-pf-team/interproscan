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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Protein sequence match to model.
 *
 * @author  Antony Quinn
 * @version $Id: Match.java,v 1.9 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 */
@Entity
@Inheritance (strategy = InheritanceType.TABLE_PER_CLASS)
public interface Match<T extends Location> extends Serializable {

    // TODO: IMPACT XML: Add evidence, e.g. "HMMER 2.3.2 (Oct 2003)" [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]
    // TODO: See http://www.ebi.ac.uk/seqdb/confluence/x/DYAg#ND3.3StandardXMLformatforallcommondatatypes-SMART

    @Id
    Long getId();

    void setId (Long id);

    @Transient
    public Set<T> getLocations();

    /**
     * Made public for JPA, but should NOT be used in other circumstances.
     * @param locations DO NOT USE.
     */
    public void setLocations(Set<T> locations);
    public T addLocation(T location);
    public void removeLocation(T location);

    public MatchableEntity getSequence();
    public void setSequence(MatchableEntity sequence);

    /**
     * Returns key to use in, for example, HashMap.
     *
     * @return Key to use in, for example, HashMap.
     */
    @Transient
    public String getKey();

}
