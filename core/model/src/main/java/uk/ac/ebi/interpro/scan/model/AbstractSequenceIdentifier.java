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
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Represents a sequence identifier, for example a UniProtKB accession, UniParc ID or MD5 checksum.
 *
 * @author  Phil Jones 
 * @author  Antony Quinn
 * @version $Id: AbstractSequenceIdentifier.java,v 1.9 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 */
@XmlTransient
@Entity
@Table (name = "sequence_identifier")
@DiscriminatorValue(value = "S")
abstract class AbstractSequenceIdentifier
        extends AbstractMatchableEntity
        implements SequenceIdentifier, MatchableEntity, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    /**
     * Instance member holding the MD5 digest.
     * TODO consider column name again...  (not urgent as does not affect functionality)
     */
    @Column (name = "identifier", nullable = false, unique = false, updatable = false)
    private String identifier;

    AbstractSequenceIdentifier(String identifier){
        this.identifier = identifier;
    }

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected AbstractSequenceIdentifier() {
    }

    /**
     * Returns the unique identifier for this Entity.
     * @return the unique identifier for this Entity.
     */
    @XmlTransient
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
    }

    /**
     * Returns sequence identifier (MD5 hex digest of sequence)
     *
     * @return Sequence identifier
     */
    @Override public String getIdentifier() {
        return identifier;
    }

    /**
     * TODO Sets the sequence identifier.  Required only for JPA - will try to remove.
     *
     * @param identifier DO NOT USE - added only for JPA.
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceIdentifier that = (SequenceIdentifier) o;
        return identifier.equals(that.getIdentifier());
    }

    @Override public int hashCode() {
        return identifier.hashCode();
    }

    @Override public String toString() {
        return identifier;
    }
}
