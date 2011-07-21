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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * GO cross-reference.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@XmlType(name = "GoXrefType")
public class GoXref extends Xref implements Serializable {

    @ManyToMany(
            cascade = CascadeType.ALL,
            mappedBy = "goXRefs",
            targetEntity = Entry.class)
    private Set<Entry> entries;

    private GoCategory category;

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected GoXref() {
    }

    public GoXref(String identifier, String name, GoCategory category) {
        super("GO", identifier, name);
        this.category = category;
    }

    @XmlAttribute
    public GoCategory getCategory() {
        return category;
    }

    public void setCategory(GoCategory category) {
        this.category = category;
    }

    @XmlTransient
    public Set<Entry> getEntries() {
        return entries;
    }

    public void setEntries(Set<Entry> entries) {
        this.entries = entries;
    }

    public void addEntry(Entry entry) {
        if (entries == null) {
            entries = new HashSet<Entry>();
        }
        entries.add(entry);
    }

    /**
     * This equals method must not change - do NOT add the database id
     * to this equals method - otherwise the protein loader code will break.
     * <p/>
     * Only considers the natural key of the Xref object (as it should!)
     *
     * @param o ProteinXref to compare with.
     * @return true if the two ProteinXrefs have the same natural key.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GoXref))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    /**
     * This hashCode method must not change - do NOT add the database id
     * to this hashCode method - otherwise the protein loader code will break.
     * <p/>
     * Only considers the natural key of the Xref object (as it should!)
     *
     * @return hashcode for this Xref.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 51)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}