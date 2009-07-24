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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * Represents a sequence identifier, for example a UniProtKB accession, UniParc ID or MD5 checksum.
 *
 * @author  Phil Jones 
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table (name = "sequence_identifier")
@DiscriminatorValue(value = "S")
@XmlTransient
public abstract class SequenceIdentifier extends MatchableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    /**
     * Instance member holding the MD5 digest.
     * TODO consider column name again...  (not urgent as does not affect functionality)
     */
    @Column (name = "identifier", nullable = false, unique = false, updatable = false)
    private String identifier;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SequenceIdentifier() {
    }

    SequenceIdentifier(String identifier){
        this.identifier = identifier;
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
    @XmlAttribute(name="id")
    public String getIdentifier() {
        return identifier;
    }

    /**
     * TODO Sets the sequence identifier.  Required only for JPA - will try to remove.
     *
     * @param identifier DO NOT USE - added only for JPA.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Use this factory method to create SequenceIdentifier objects based upon a String that may
     * be an accession, or an MD5, or any other way of identifying a protein in a FASTA file.
     */
    @XmlTransient
    public static class Factory{

        private Factory(){}

        /**
         * Returns an appropriate implementation of SequenceIdentifier based upon the contents
         * of the identifier parameter.
         *
         * @param  identifier the identifier retrieved from a flat file or FASTA file.
         * @return SequenceIdentifier of an appropriate class for the identifier type.
         */
        public static SequenceIdentifier createSequenceIdentifier (String identifier) {
            if (identifier == null){
                throw new IllegalArgumentException("'identifier' must not be null.");
            }
            return (MD5SequenceIdentifier.isMD5(identifier))
                ? new MD5SequenceIdentifier(identifier)
                : new XrefSequenceIdentifier(identifier);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SequenceIdentifier))
            return false;
        final SequenceIdentifier m = (SequenceIdentifier) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(identifier, m.identifier)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 73)
                .appendSuper(super.hashCode())
                .append(identifier)
                .toHashCode();
    }

}
