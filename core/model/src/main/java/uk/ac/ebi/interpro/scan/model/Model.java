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
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model, for example SuperFamily 0035188 (part of signature SSF53098)
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlType(name="ModelType")
public class Model implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    /**
     * id as unique identifier of the Model record (e.g. for JPA persistence)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 50, nullable = false)
    private String accession;

    @Column(length = 100)
    private String name;

    @CollectionOfElements (fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable (name="model_description_chunk")
    @IndexColumn (name="chunk_index")
    @Column (name="description_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks;

    @Transient
    private String description;

    @ManyToOne (optional = true)
    private Signature signature;

    /**
     * TODO - Mucking about with Hibernate specific annotation - mapping
     * the pieces of the definition to a CollectionOfElements.
     *
     * This field holds the contents of the model file for this particular Model.  The
     * length of this is indeterminate, so stored in a LOB field.
     */
//    @Column (nullable = true, length=100000)
    @CollectionOfElements (fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable (name="model_definition_chunk")
    @IndexColumn (name="chunk_index")
    @Column (name="definition_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> definitionChunks;

    @Transient
    private String definition;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Model() {}

    public Model(String accession) {
        setAccession(accession);
    }

    public Model(String accession, String name, String description) {
        setAccession(accession);
        setName(name);
        setDescription(description);
    }

    /**
     * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
     *
     * @author  Antony Quinn
     */
    @XmlTransient
    public static class Builder {

        // Required parameters
        private final String accession;

        // Optional parameters
        private String name;
        private String description;
        private Signature signature;

        public Builder(String accession) {
            this.accession = accession;
        }

        public Model build() {
            Model model = new Model(accession, name, description);
            model.setSignature(signature);
            return model;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder signature(Signature signature) {
            this.signature = signature;
            return this;
        }

    }

    /**
     * Returns model accession, for example PF00001.
     *
     * @return Model accession
     */
    @XmlAttribute(name="ac", required=true)
    public String getAccession() {
        return accession;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    private void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Returns model name, for example 7tm_1.
     *
     * @return Model name
     */
    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public String getDefinition() {
        if (definition == null){
            definition = CHUNKER.concatenate(definitionChunks);
        }
        return definition;
    }

    private void setDefinition(String definition) {
        this.definition = definition;
        definitionChunks = CHUNKER.chunkIntoList(definition);
    }

    /**
     * Returns model description, for example "7 transmembrane receptor (rhodopsin family)".
     *
     * @return Model description
     */
    @XmlAttribute(name="desc")
    public String getDescription() {
        if (description == null){
            description = CHUNKER.concatenate(descriptionChunks);
        }
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
        descriptionChunks = CHUNKER.chunkIntoList(description);
    }

    @XmlTransient
    public Signature getSignature()    {
        return signature;
    }

    void setSignature(Signature signature)  {
        this.signature = signature;
    }

    /**
     * Returns key to use in, for example, HashMap.
     * TODO - Check if it is the correct decision to make this transient.
     *
     * @return Key to use in, for example, HashMap.
     */
    @Transient
    @XmlTransient
    public String getKey() {
        // TODO: Use name, accession or MD5 as model key?
        return getAccession();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Model))
            return false;
        final Model m = (Model) o;
        return new EqualsBuilder()
                .append(accession, m.accession)
                .append(name, m.name)
                .append(getDescription(), m.getDescription())
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 41)
                .append(accession)
                .append(name)
                .append(getDescription())
                .toHashCode();
    }

    @Override public String toString()  {
        return new ToStringBuilder(this)
                .append("accession", accession)
                .append("name", name)
                .append("description", getDescription())
                .append("signature-ac", (signature == null ? null : signature.getAccession()))
                .toString();
    }

}
