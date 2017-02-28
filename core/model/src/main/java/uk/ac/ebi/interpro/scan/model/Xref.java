/*
 * Copyright 2010 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Database cross-reference.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "XrefType")
@JsonIgnoreProperties({"id"})
abstract class Xref implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "XREF_IDGEN")
    @TableGenerator(name = "XREF_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "xref", initialValue = 0, allocationSize = 50)
    protected Long id;

    // TODO Put an index here instead of the sub-classes? Not currently possible with JPA 2.1?
    @Column(nullable = false, unique = false, updatable = false)
    private String identifier;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String databaseName;

    /**
     * Zero arguments constructor for Hibernate.
     */
    protected Xref() {
    }

    public Xref(String identifier) {
        this.identifier = identifier;
    }

    public Xref(String databaseName, String identifier, String name) {
        this.databaseName = databaseName;
        this.identifier = identifier;
        this.name = name;
    }


    @XmlTransient
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @XmlAttribute(name = "id", required = true)
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "db")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * This equals method must not change - do NOT add the database id
     * to this equals method - otherwise the protein loader code will break.
     * <p/>
     * Only considers the natural key of the Xref object (as it should!)
     *
     * @param o Xref to compare with.
     * @return true if the two objects have the same natural key.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Xref))
            return false;
        final Xref x = (Xref) o;
        return new EqualsBuilder()
                .append(identifier, x.identifier)
                .append(name, x.name)
                .append(databaseName, x.databaseName)
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
        return new HashCodeBuilder(15, 37)
                .append(identifier)
                .append(name)
                .append(databaseName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
