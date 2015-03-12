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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Model, for example SuperFamily 0035188 (part of signature SSF53098)
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */

@Entity
@XmlType(name = "ModelType")
public class Model implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    /**
     * id as unique identifier of the Model record (e.g. for JPA persistence)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MOD_IDGEN")
    @TableGenerator(name = "MOD_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "model", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    @Index(name = "model_ac_idx")
    private String accession;

    @Column(length = 4000, name = "model_name")
    @Index(name = "model_name_idx")
    private String name;

    //TODO: Hibernate annotation issue: Switched to lazy loading
    @ElementCollection
    @JoinTable(name = "model_description_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(name = "description_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE, name = "description_first_chunk")
    @XmlTransient
    private String descriptionFirstChunk;

    @Transient
    private String description;

    @ManyToOne(optional = true)
    private Signature signature;

    /**
     * TODO - Mucking about with Hibernate specific annotation - mapping
     * the pieces of the definition to a CollectionOfElements.
     * <p/>
     * This field holds the contents of the model file for this particular Model.  The
     * length of this is indeterminate, so stored in a LOB field.
     */
//    @Column (nullable = true, length=100000)
    //    TODO: Hibernate annotation issue: Switched to lazy loading
    @ElementCollection
    @JoinTable(name = "model_definition_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(name = "definition_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> definitionChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE, name = "definition_first_chunk")
    @XmlTransient
    private String definitionFirstChunk;

    @Transient
    private String definition;

    @Index(name = "model_md5_idx")
    private String md5;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Model() {
    }

    public Model(String accession) {
        setAccession(accession);
    }

    public Model(String accession, String name, String description) {
        setAccession(accession);
        setName(name);
        setDescription(description);
    }


    public Model(String accession, String name, String description, String definition) {
        setAccession(accession);
        setName(name);
        setDescription(description);
        setDefinition(definition);
    }

    /**
     * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
     *
     * @author Antony Quinn
     */
    @XmlTransient
    public static class Builder {

        // Required parameters
        private final String accession;

        // Optional parameters
        private String name;
        private String description;
        private Signature signature;
        private String definition;
        private String md5;

        public Builder(String accession) {
            this.accession = accession;
        }

        public Model build() {
            Model model = new Model(accession, name, description, definition);
            model.setSignature(signature);
            model.setMd5(md5);
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

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

    }

    /**
     * Returns model accession, for example PF00001.
     *
     * @return Model accession
     */
    @XmlAttribute(name = "ac", required = true)
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
    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public String getDefinition() {
        if (definition == null) {
            definition = CHUNKER.concatenate(definitionFirstChunk, definitionChunks);
        }
        return definition;
    }

    private void setDefinition(String definition) {
        this.definition = definition;
        List<String> chunks = CHUNKER.chunkIntoList(definition);
        definitionFirstChunk = CHUNKER.firstChunk(chunks);
        definitionChunks = CHUNKER.latterChunks(chunks);
    }

    /**
     * Returns model description, for example "7 transmembrane receptor (rhodopsin family)".
     *
     * @return Model description
     */
    @XmlAttribute(name = "desc")
    public String getDescription() {
        if (description == null) {
            description = CHUNKER.concatenate(descriptionFirstChunk, descriptionChunks);
        }
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
        List<String> chunks = CHUNKER.chunkIntoList(description);
        descriptionFirstChunk = CHUNKER.firstChunk(chunks);
        descriptionChunks = CHUNKER.latterChunks(chunks);
    }

    @XmlTransient
    public Signature getSignature() {
        return signature;
    }

    void setSignature(Signature signature) {
        this.signature = signature;
    }

    @XmlAttribute
    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
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

    private String getSafeMd5(String md5) {
        return (md5 == null ? "" : md5.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Model))
            return false;
        final Model m = (Model) o;
        return new EqualsBuilder()
                .append(accession, m.accession)
                .append(name, m.name)
                .append(getSafeMd5(md5), getSafeMd5(m.md5))
                .append(getDescription(), m.getDescription())
                .append(getDefinition(), m.getDefinition())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 41)
                .append(accession)
                .append(name)
                .append(getSafeMd5(md5))
                .append(getDescription())
                .append(getDefinition())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accession", accession)
                .append("name", name)
                .append("description", getDescription())
                .append("definition", getDefinition())
                .append("signature-ac", (signature == null ? null : signature.getAccession()))
                .append("md5", md5)
                .toString();
    }

}
