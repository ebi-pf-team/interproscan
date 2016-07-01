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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Location of match on protein sequence.
 *
 * @version $Id$
 * @since 1.0
 */

@Entity
@XmlType(name = "ResidueLocationType", propOrder = {"residue", "start", "end"})
@Table(name = "residue_location")
public class SiteLocation implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RESIDUE_LOCN_IDGEN")
    @TableGenerator(name = "RESIDUE_LOCN_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "residue_location", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(name = "loc_start", nullable = false)
    // to match start - 'start' is reserved word in SQL.
    private int start;

    @Column(name = "loc_end", nullable = false)
    // 'end' is reserved word in SQL.
    private int end;

    @Column(name = "residue", nullable = false)
    private String residue;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    private Site site;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SiteLocation() {
    }

    public SiteLocation(String residue, int start, int end) {
        setResidue(residue);
        setStart(start);
        setEnd(end);
    }

    /**
     * @return the persistence unique identifier for this object.
     */
    @XmlTransient
    public Long getId() {
        return null;
    }

    /**
     * @param id being the persistence unique identifier for this object.
     */
    private void setId(Long id) {
    }

    /**
     * Returns the start coordinate of this Location.
     *
     * @return the start coordinate of this Location.
     */
    @XmlAttribute(required = true)
    public int getStart() {
        return start;
    }

    /**
     * Start coordinate of this Location.
     *
     * @param start Start coordinate of this Location
     */
    private void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end coordinate of this Location.
     *
     * @return the end coordinate of this Location.
     */
    @XmlAttribute(required = true)
    public int getEnd() {
        return end;
    }

    /**
     * End coordinate of this Location.
     *
     * @param end End coordinate of this Location.
     */
    private void setEnd(int end) {
        this.end = end;
    }

    /**
     * Returns the residue of this Location.
     *
     * @return the residue of this Location.
     */
    @XmlAttribute(required = true)
    public String getResidue() {
        return residue;
    }

    /**
     * Residue of this Location.
     *
     * @param residue Residue of this Location.
     */
    private void setResidue(String residue) {
        this.residue = residue;
    }


    /**
     * This method is called by Site, upon the addition of a residue location to a site.
     *
     * @param site to which this residue location is related.
     */
    void setSite(Site site) {
        this.site = site;
    }

    /**
     * Returns the Match that this Location is related to.
     *
     * @return
     */
    @XmlTransient
    public Site getSite() {
        return site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SiteLocation))
            return false;
        final SiteLocation h = (SiteLocation) o;
        return new EqualsBuilder()
                .append(residue, h.residue)
                .append(start, h.start)
                .append(end, h.end)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 57)
                .append(residue)
                .append(start)
                .append(end)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Object clone() throws CloneNotSupportedException {
        return new SiteLocation(this.getResidue(), this.getStart(), this.getEnd());
    }

}
