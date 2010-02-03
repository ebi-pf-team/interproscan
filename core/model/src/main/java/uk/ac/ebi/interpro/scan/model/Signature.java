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
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

/**
 * Signature, for example SSF53098 [http://supfam.mrc-lmb.cam.ac.uk/SUPERFAMILY/cgi-bin/models_list.cgi?sf=53098]
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlRootElement(name="signature")
@XmlType(name="SignatureType")
public class Signature implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    // TODO: IMPACT XML: Handle Pfam Clans, FingerPrints Hierachiesm SMART thresholds ...etc [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]

    // TODO: Add xrefs (inc. GO terms) and InterPro entry [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]
    // TODO: See http://www.ebi.ac.uk/seqdb/confluence/x/DYAg#ND3.3StandardXMLformatforallcommondatatypes-SMART

    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column (name="accession", nullable = false)
    private String accession;

    @Column (name="name")
    private String name;

    @CollectionOfElements(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable (name="signature_description_chunk")
    @IndexColumn(name="chunk_index")
    @Column (name="description_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks = Collections.emptyList();

    @Transient
    private String description;

    /**
     * Member database specific category for the Signature
     */
    @Column (name="type")
    private String type;

    private Date created;
    private Date updated;
    private String md5;

    @CollectionOfElements(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable (name="signature_abstract_chunk")
    @IndexColumn(name="chunk_index")
    @Column (name="abstract_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> abstractChunks = Collections.emptyList();

    @Transient
    private String abstractText;

    @ManyToOne // TODO This needs to be ManyToMany so that a Signature can be re-used across releases.
    private SignatureLibraryRelease signatureLibraryRelease;

    // TODO: Decide whether to use Map or Set (see ChEBI team)
    // TODO: Use ConcurrentHashMap if need concurrent modification of signatures
    // TODO: Use Hashtable if want to disallow duplicate values
    @OneToMany (mappedBy = "signature", cascade = CascadeType.ALL)
    @MapKey (name= "accession")
    private Map<String, Model> models = new HashMap<String, Model>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Signature() {}

    public Signature(String accession) {
        setAccession(accession);
    }

    public Signature(String accession, String name) {
        setAccession(accession);
        setName(name);
    }

    // TODO: Add created, updated and md5 here?
    public Signature(String accession,
                     String name,
                     String type,                     
                     String description,
                     String abstractText,
                     SignatureLibraryRelease signatureLibraryRelease,
                     Set<Model> models) {
        setAccession(accession);
        setName(name);
        setDescription(description);
        setType(type);
        setAbstract(abstractText);
        setSignatureLibraryRelease(signatureLibraryRelease);
        setModels(models);
    }

    /**
     * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
     *
     * @author  Antony Quinn
     */
    @XmlTransient
    public static class Builder {

        private final String accession;
        private String name;
        private String description;
        private String type;
        private Date created;
        private Date updated;
        private String md5;
        private String abstractText;
        private SignatureLibraryRelease signatureLibraryRelease;
        Set<Model> models;

        public Builder(String accession) {
            this.accession = accession;
        }

        public Signature build() {
            Signature signature = new Signature(accession);
            signature.setName(name);
            signature.setDescription(description);
            signature.setType(type);
            signature.setAbstract(abstractText);
            signature.setSignatureLibraryRelease(signatureLibraryRelease);
            if (models != null) {
                signature.setModels(models);
            }
            return signature;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder abstractText(String text) {
            this.abstractText = text;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder created(Date created) {
            this.created = created;
            return this;
        }

        public Builder updated(Date updated) {
            this.updated = updated;
            return this;
        }

        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder signatureLibraryRelease(SignatureLibraryRelease signatureLibraryRelease) {
            this.signatureLibraryRelease = signatureLibraryRelease;
            return this;
        }

        public Builder models(Set<Model> models) {
            this.models = models;
            return this;
        }        
    }

    public Long getId() {
        return id;
    }

    /**
     * Returns signature accession, for example PF00001.
     *
     * @return Signature accession
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
     * Returns signature identifier, for example 7tm_1.
     *
     * @return Signature identifier
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /**
     * Returns signature description, for example "7 transmembrane receptor (rhodopsin family)".
     *
     * @return Signature description
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

    /**
     * Returns type of signature according to member database, for example:
     * - Gene3D signatures are all Domain
     * - ProDom and ProSite signatures are all Family
     * - Pfam signatures are Family, Domain, Motif or Repeat
     *
     * @return Type of signature according to member database
     */
    @XmlAttribute
    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    /**
     * Returns signature abstract.
     *
     * @return Signature abstract
     */
    @XmlElement(name="abstract")
    public String getAbstract() {
        if (abstractText == null){
            abstractText = CHUNKER.concatenate(abstractChunks);
        }
        return abstractText;
    }

    private void setAbstract(String text) {
        this.abstractText = text;
        abstractChunks = CHUNKER.chunkIntoList(abstractText);
    }

    public Date getCreated() {
        return created;
    }

    private void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    private void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
    }

    @XmlTransient
    public SignatureLibraryRelease getSignatureLibraryRelease()    {
        return signatureLibraryRelease;
    }

    void setSignatureLibraryRelease(SignatureLibraryRelease signatureLibraryRelease)  {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @XmlJavaTypeAdapter(ModelAdapter.class)
    public Map<String, Model> getModels() {
        return (models == null ? null : Collections.unmodifiableMap(models));
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setModels(Map<String, Model> models) {
        setModels(models.values());
    }

    private void setModels(Collection<Model> models) {
        // Ensure Signature reference is set ("this.models = models" won't do)
        for (Model m : models)  {
            addModel(m);
        }
    }

    public Model addModel(Model model) throws IllegalArgumentException {
        if (model == null) {
            throw new IllegalArgumentException("'Model' must not be null");
        }
        if (model.getSignature() != null) {
            model.getSignature().removeModel(model);
        }
        model.setSignature(this);
        models.put(model.getKey(), model);
        return model;
    }

    public void removeModel(Model model) {
        models.remove(model.getKey());
        model.setSignature(null);
    }

    /**
     * Map models to and from XML representation
     */
    @XmlTransient
    private static class ModelAdapter extends XmlAdapter<ModelsType, Map<String, Model>> {

        /** Map Java to XML type */
        @Override public ModelsType marshal(Map<String, Model> map) {
            return (map == null || map.isEmpty() ? null : new ModelsType(new HashSet<Model>(map.values())));
        }

        /** Map XML type to Java */
        @Override public Map<String, Model> unmarshal(ModelsType modelsType) {
            Map<String, Model> map = new HashMap<String, Model>();
            for (Model m : modelsType.getModels())  {
                map.put(m.getKey(), m);
            }
            return map;
        }

    }

    /**
     * Helper class for ModelAdapter
     */
    private final static class ModelsType {

        @XmlElement(name = "model")
        private final Set<Model> models;

        private ModelsType() { 
            models = null;
        }

        public ModelsType(Set<Model> models) {
            this.models = models;
        }

        public Set<Model> getModels() {
            return models;
        }

    }        

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Signature))
            return false;
        final Signature s = (Signature) o;
        return new EqualsBuilder()
                .append(accession, s.accession)
                .append(name, s.name)
                .append(type, s.type)
                .append(created, s.created)
                .append(updated, s.updated)
                .append(md5, s.md5)
                .append(getDescription(), s.getDescription())
                .append(getAbstract(), s.getAbstract())
                .append(signatureLibraryRelease, s.signatureLibraryRelease)
                .append(models, s.models)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(accession)
                .append(name)
                .append(type)
                .append(created)
                .append(updated)
                .append(md5)
                .append(getDescription())
                .append(getAbstract())
                .append(signatureLibraryRelease)
                .append(models)
                .toHashCode();
    }

    /**
     * TODO - this will not work, giving a null value for description or the abstract if the instance is retrieved from the database.
     * 
     * @return String representation of this object.
     */
    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
