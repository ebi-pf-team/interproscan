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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

/**
 * Protein sequence match to model.
 *
 * @author  Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlTransient
public abstract class Match<T extends Location> implements Serializable {

    // TODO: IMPACT XML: Add evidence, e.g. "HMMER 2.3.2 (Oct 2003)" [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]
    // TODO: See http://www.ebi.ac.uk/seqdb/confluence/x/DYAg#ND3.3StandardXMLformatforallcommondatatypes-SMART


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private MatchableEntity sequence;

    @Transient
    private Set<T> locations = new LinkedHashSet<T>();

    protected Match()  { }

    protected Match(Set<T> locations)  {
        setLocations(locations);
    }


    @XmlTransient                                                                          
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlTransient
    public MatchableEntity getSequence()  {
        return sequence;
    }

    public void setSequence(MatchableEntity sequence) {
        this.sequence = sequence;

    }

    @Transient    
    @XmlJavaTypeAdapter(Location.LocationAdapter.class)
    public Set<T> getLocations() {
        return Collections.unmodifiableSet(locations);
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    // Doh - changed to public for JPA annotations.
    private void setLocations(Set<T> locations) {
        for (T t : locations)    {
            addLocation(t);
        }
    }

    public T addLocation(T location) {
        if (location == null) {
            throw new IllegalArgumentException("'Location' is null");
        }
        if (location.getMatch() != null) {
            // This cast is correct because in sub-classes, for example RawHmmMatch, we only allow a single 
            // location type, for example HmmLocation, so the type we remove is the same type we added.            
            @SuppressWarnings("unchecked") Match<T> match = location.getMatch();
            match.removeLocation(location);
        }
        location.setMatch(this);
        locations.add(location);
        return location;
    }

    public void removeLocation(T location)   {
        locations.remove(location);
        location.setMatch(null);
    }

    /**
     * Returns key to use in, for example, HashMap.
     *
     * @return Key to use in, for example, HashMap.
     */
    @Transient
    public abstract String getKey();

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Match))
            return false;
        final Match m = (Match) o;
        return new EqualsBuilder()
                .append(locations, m.locations)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 51)
                .append(locations)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
