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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.List;

/**
 * Signature library, for example Pfam or PRINTS.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@Table(name="signature_library")
@XmlType(name="SignatureLibraryType")
public class SignatureLibrary implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    // select upper(dbshort)||'("'||dbcode||'", "'||dbshort||'", "'||dbname||'"),'
    // from interpro.cv_database order by dbshort;

    /**
     * Unique ID for persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="name", length=255)
    private String name;

    @CollectionOfElements(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable (name="sig_lib_description_chunk")
    @IndexColumn(name="chunk_index")
    @Column (name="description_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks;

    @Transient
    private String description;
    

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SignatureLibrary() { }

    public SignatureLibrary(String name) {
        setName(name);
    }

    public SignatureLibrary(String name, String description) {
        setName(name);
        setDescription(description);
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
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

    /**
     * Map SignatureLibrary to and from XML representation
     */
    @XmlTransient
    static final class SignatureLibraryAdapter extends XmlAdapter<String, SignatureLibrary> {
        /* Map Java to XML type */
        @Override public String marshal(SignatureLibrary library) {
            return library.getName();
        }
        /* Map XML type to Java */
        @Override public SignatureLibrary unmarshal(String name) {
            return new SignatureLibrary(name);
        }
    }      

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SignatureLibrary))
            return false;
        final SignatureLibrary s = (SignatureLibrary) o;
        return new EqualsBuilder()
                .append(name, s.name)
                .append(getDescription(), s.getDescription())
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 43)
                .append(name)
                .append(getDescription())
                .toHashCode();
    }

    /**
     * TODO might will not work for the chunked description - Needs to use the getter method instead.
     * 
     * @return String representation of this.
     */
    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
