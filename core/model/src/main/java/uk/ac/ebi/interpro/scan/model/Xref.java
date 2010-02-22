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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Protein cross-reference.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@XmlType(name="XrefType")
public class Xref implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator="XREF_IDGEN")
    @TableGenerator(name="XREF_IDGEN", table="KEYGEN", pkColumnValue="xref", initialValue = 0, allocationSize = 50)
    protected Long id;

    // TODO consider column name again...  (not urgent as does not affect functionality)
    @Column (name = "identifier", nullable = false, unique = false, updatable = false)
    private String identifier;    

    /**
     * Reference to the protein that this Xref is an annotation of.
     */
    @ManyToOne (optional = false)
    private Protein protein;

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected Xref() { }

    /**
     * Constructor for an Xref that takes the xref String as an argument.
     *
     * @param identifier the Xref String.
     */
    public Xref(String identifier){
        this.identifier = identifier;
    }

    // TODO Really need to be able to optionally store database name here.

    /**
     * Returns the unique identifier for this Entity.
     *
     * @return the unique identifier for this Entity.
     */
    @XmlTransient
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns sequence identifier
     *
     * @return Sequence identifier
     */
    @XmlAttribute(name="id")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets sequence identifier.
     *
     * @param identifier Sequence identifier
     */
    private void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the Protein that this accession / ID cross reference annotates.
     *
     * @return the Protein that this accession / ID cross reference annotates.
     */
    @XmlTransient
    public Protein getProtein() {
        return protein;
    }

    /**
     * Package private setter used by the Protein class to create a reference to the annotated protein.
     *
     * @param protein the Protein that this accession / ID cross reference annotates.
     */
    void setProtein(Protein protein) {
        this.protein = protein;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Xref))
            return false;
        final Xref x = (Xref) o;
        return new EqualsBuilder()
                .append(identifier, x.identifier)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(15, 51)
                .append(identifier)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
