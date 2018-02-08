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

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
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
@Table(indexes = { @Index(columnList = "IDENTIFIER") })
public class GoXref extends Xref implements Serializable {

    @ManyToMany(mappedBy = "goXRefs",
            targetEntity = Entry.class)
    @JsonBackReference
    private Set<Entry> entries = new HashSet<Entry>();

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

    @XmlAttribute(name = "category")
    public GoCategory getCategory() {
        return category;
    }

    public void setCategory(GoCategory category) {
        this.category = category;
    }

    @XmlTransient
    public Set<Entry> getEntries() {
        return (entries == null ? new HashSet<Entry>() : entries);
    }

    public void setEntries(Set<Entry> entries) {
        for (Entry e : entries) {
            addEntry(e);
        }
    }

    protected void addEntry(Entry entry) {
        if (entries == null) {
            entries = new HashSet<Entry>();
        }
        entries.add(entry);
    }

    public void removeEntry(Entry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null!");
        }
        entry.removeGoXRef(this);
        if (entries != null) {
            entries.remove(entry);
        }
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
        final GoXref goXref = (GoXref) o;
        return new EqualsBuilder()
                .append(getIdentifier(), goXref.getIdentifier())
                .append(getName(), goXref.getName())
                .append(getDatabaseName(), goXref.getDatabaseName())
                .append(getCategory(), goXref.getCategory())
                .append(getEntries(), goXref.getEntries())
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
                .append(getIdentifier())
                .append(getName())
                .append(getDatabaseName())
                .append(getCategory())
                .append(getEntries())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
