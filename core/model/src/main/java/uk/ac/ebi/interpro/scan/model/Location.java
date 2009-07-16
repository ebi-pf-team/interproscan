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
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Location of match on protein sequence.
 *
 * @author  Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since   1.0
 */

@Entity
//@Inheritance (strategy = InheritanceType.TABLE_PER_CLASS)
public interface Location extends Serializable {

    @Id
    public Long getId();

    public void setId (Long id);

    /**
     * Returns the start coordinate of this Location.
     * @return the start coordinate of this Location.
     */
    @Column (name="location_start")    // to match end - 'end' is reserved word in SQL.
    public int getStart();

    /**
     * Required by JPA. The start coordinate of this Location.
     * @param start being the start coordinate of this Location
     */
    public void setStart(int start);

    /**
     * Returns the end coordinate of this Location.
     * @return  the end coordinate of this Location.
     */
    @Column (name="location_end")       // 'end' is reserved word in SQL.
    public int getEnd();

    /**
     * Required by JPA.  The end coordinate of this Location.
     * @param end being the end coordinate of this Location.
     */
    public void setEnd(int end);

    @ManyToOne
    public Match getMatch();
    
    // Must be public so can use from Match.addLocation() and Match.removeLocation()
    // (better as package-private but not possible to specify in interface)
    public void setMatch(Match match);

}
