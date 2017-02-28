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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

/**
 * Signature, for example SSF53098 [http://supfam.org/SUPERFAMILY/cgi-bin/models_list.cgi?sf=53098]
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
@XmlRootElement(name = "signature")
@XmlType(name = "SignatureType")
@Table(indexes = {
        @Index(name = "SIGNATURE_AC_IDX", columnList = "ACCESSION"),
        @Index(name = "SIGNATURE_NAME_IDX", columnList = "SIG_NAME"),
        @Index(name = "SIGNATURE_TYPE_IDX", columnList = "TYPE"),
        @Index(name = "SIGNATURE_MD5_IDX", columnList = "MD5")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "updated", "created", "id", "crossReferences", "abstract", "comment", "md5", "deprecatedAccessions"}) // IBU-4703: "abstract", "comment" and "md5" are never populated
public class Signature implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    // TODO: IMPACT XML: Handle Pfam Clans, FingerPrints Hierachiesm SMART thresholds ...etc [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]

    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SIG_LIB_IDGEN")
    @TableGenerator(name = "SIG_LIB_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "signature", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String accession;

    @Column(length = 4000, name = "sig_name")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "SIGNATURE_DESCRIPTION_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    @BatchSize(size=4000)
    private List<String> descriptionChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String descriptionFirstChunk;

    @Transient
    private String description;

    /**
     * Member database specific category for the Signature
     */
    @Column
    private String type;

    @Column(nullable = true)
    private Date created;

    @Column(nullable = true)
    private Date updated;

    private String md5;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "SIGNATURE_ABSTRACT_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(name = "ABSTRACT_CHUNK", length = Chunker.CHUNK_SIZE, nullable = true)
    @BatchSize(size=4000)
    private List<String> abstractChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String abstractFirstChunk;

    @Transient
    private String abstractText;

    //TODO: Switch back to eager loading after schema update (loading entries to database)
    @ManyToOne(fetch = FetchType.LAZY)
    // TODO: This needs to be ManyToMany so that a Signature can be re-used across releases.
    @BatchSize(size=4000)
    @JsonManagedReference
    private SignatureLibraryRelease signatureLibraryRelease;

    // TODO: Decide whether to use Map or Set (see ChEBI team)
    // TODO: Use ConcurrentHashMap if need concurrent modification of signatures
    // TODO: Use Hashtable if want to disallow duplicate values
    @OneToMany(mappedBy = "signature", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
//    @OneToMany(mappedBy = "signature", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "accession")
    @BatchSize(size=4000)
    @JsonManagedReference
    private Map<String, Model> models = new HashMap<String, Model>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "signature")
    //@XmlElementWrapper(name = "xrefs")
    @XmlElement(name = "xref") // TODO: This should not be here (see TODO comments on getCrossReferences)
    @BatchSize(size=4000)
    @JsonManagedReference
    private Set<SignatureXref> crossReferences = new HashSet<SignatureXref>();

    @ElementCollection
//    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "signature_deprecated_acs")
    @Column(nullable = true)
    @BatchSize(size=4000)
    private Set<String> deprecatedAccessions = new HashSet<String>();

    @Column(nullable = true, name = "signature_comment")  // comment is an SQL reserved word.
    private String comment;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
//    @ManyToOne(optional = true, cascade = CascadeType.MERGE)
    @JsonManagedReference
    private Entry entry;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Signature() {
    }

    public Signature(String accession) {
        setAccession(accession);
    }

    public Signature(String accession, String name) {
        setAccession(accession);
        setName(name);
    }

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
     * @author Antony Quinn
     */
    @XmlTransient
    public static class Builder {

        private final String accession;
        private String name;
        private String description;
        private String type;
        private Entry entry;
        private Date created;
        private Date updated;
        private String md5;
        private String abstractText;
        private String comment;
        private SignatureLibraryRelease signatureLibraryRelease;
        private Set<Model> models = new HashSet<Model>();
        private Set<SignatureXref> crossReferences = new HashSet<SignatureXref>();
        private Set<String> deprecatedAccessions = new HashSet<String>();


        public Builder(String accession) {
            this.accession = accession;
        }

        public Signature build() {
            Signature signature = new Signature(accession);
            signature.setName(name);
            signature.setDescription(description);
            signature.setEntry(entry);
            signature.setType(type);
            signature.setAbstract(abstractText);
            signature.setSignatureLibraryRelease(signatureLibraryRelease);
            signature.setCreated(created);
            signature.setUpdated(updated);
            signature.setMd5(md5);
            signature.setComment(comment);
            if (models != null) {
                signature.setModels(models);
            }
            if (!crossReferences.isEmpty()) {
                for (SignatureXref x : crossReferences) {
                    signature.addCrossReference(x);
                }
            }
            if (deprecatedAccessions != null) {
                signature.setDeprecatedAccessions(deprecatedAccessions);
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

        public Builder entry(Entry entry) {
            this.entry = entry;
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

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder signatureLibraryRelease(SignatureLibraryRelease signatureLibraryRelease) {
            this.signatureLibraryRelease = signatureLibraryRelease;
            return this;
        }

        public Builder model(Model model) {
            this.models.add(model);
            return this;
        }

        public Builder crossReference(SignatureXref xref) {
            this.crossReferences.add(xref);
            return this;
        }

        public Builder deprecatedAccession(String ac) {
            this.deprecatedAccessions.add(ac);
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
    @XmlAttribute(name = "ac", required = true)
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
    @XmlElement(name = "abstract")
    public String getAbstract() {
        if (abstractText == null) {
            abstractText = CHUNKER.concatenate(abstractFirstChunk, abstractChunks);
        }
        return abstractText;
    }

    private void setAbstract(String text) {
        this.abstractText = text;
        List<String> chunks = CHUNKER.chunkIntoList(abstractText);
        abstractFirstChunk = CHUNKER.firstChunk(chunks);
        abstractChunks = CHUNKER.latterChunks(chunks);
    }

    @XmlAttribute
    public Date getCreated() {
        return created;
    }

    private void setCreated(Date created) {
        this.created = created;
    }

    @XmlAttribute
    public Date getUpdated() {
        return updated;
    }

    private void setUpdated(Date updated) {
        this.updated = updated;
    }

    @XmlAttribute
    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
    }

    @XmlElement
    public String getComment() {
        return comment;
    }

    private void setComment(String comment) {
        this.comment = comment;
    }

    @XmlTransient
    public SignatureLibraryRelease getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    void setSignatureLibraryRelease(SignatureLibraryRelease signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @XmlElement(name = "signature-library-release")
    private SignatureLibraryRelease getSignatureLibraryReleaseShallow() {
        if (this.signatureLibraryRelease == null) {
            return null;
        }
        return new SignatureLibraryRelease(this.signatureLibraryRelease.getLibrary(), this.signatureLibraryRelease.getVersion());
    }

    // Corresponding setter for "shallow" version for XML marshalling and unmarshalling
    private void setSignatureLibraryReleaseShallow(SignatureLibraryRelease signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    //    @XmlElementWrapper(name = "history")
    @XmlElement(name = "deprecated-ac")
    public Set<String> getDeprecatedAccessions() {
        return deprecatedAccessions;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setDeprecatedAccessions(Set<String> deprecatedAccessions) {
        this.deprecatedAccessions = deprecatedAccessions;
    }

    /**
     * Adds and returns deprecated accession
     *
     * @param ac Accession
     * @return Accession
     * @throws IllegalArgumentException if ac is null
     */
    public String addDeprecatedAccession(String ac) throws IllegalArgumentException {
        if (ac == null) {
            throw new IllegalArgumentException("'accession' must not be null");
        }
        deprecatedAccessions.add(ac);
        return ac;
    }

    public void removeDeprecatedAccession(String ac) {
        deprecatedAccessions.remove(ac);
    }

    @XmlJavaTypeAdapter(ModelAdapter.class)
    public Map<String, Model> getModels() {
//        return (models == null ? null : Collections.unmodifiableMap(models));
        return models;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setModels(Map<String, Model> models) {
        setModels(models.values());
    }

    private void setModels(Collection<Model> models) {
        // Ensure Signature reference is set ("this.models = models" won't do)
        for (Model m : models) {
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

        /**
         * Map Java to XML type
         */
        @Override
        public ModelsType marshal(Map<String, Model> map) {
            return (map == null || map.isEmpty() ? null : new ModelsType(new HashSet<Model>(map.values())));
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Map<String, Model> unmarshal(ModelsType modelsType) {
            Map<String, Model> map = new HashMap<String, Model>();
            for (Model m : modelsType.getModels()) {
                map.put(m.getKey(), m);
            }
            return map;
        }

    }

    /**
     * Helper class for ModelAdapter
     */
    @XmlType(name = "modelsType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
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

    /**
     * Returns cross-references.
     *
     * @return cross-references
     */
    public Set<SignatureXref> getCrossReferences() {
        // TODO: Had to move @XmlElement annotation to field otherwise received message below - this is
        // TODO: bad because setCrossReferences() will not be used by JAXB (access field directly):
        /*
         java.lang.UnsupportedOperationException
            at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1037)
            at com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister.startPacking(Lister.java:296)
                ...
            at javax.xml.bind.helpers.AbstractUnmarshallerImpl.unmarshal(AbstractUnmarshallerImpl.java:105)
                ...
            at uk.ac.ebi.interpro.scan.model.AbstractTest.unmarshal(AbstractTest.java:150)
         */
        //@XmlElement(name="xref")
        // TODO: Example: Expected: Xref[protein=uk.ac.ebi.interpro.scan.model.Protein@1f49969]
        // TODO: Example: Actual:   Xref[protein=<null>]
        // TODO: Actually found that setCrossReferences() not called even if return modifiable set -- is this a bug in
        // TODO: JAXB or do we have to use an XmlAdapter?
//        return Collections.unmodifiableSet(crossReferences);
        return crossReferences;
    }

    private void setCrossReferences(Set<SignatureXref> crossReferences) {
        for (SignatureXref xref : crossReferences) {
            addCrossReference(xref);
        }
    }

    /**
     * Adds and returns cross-reference
     *
     * @param xref Cross-reference to add
     * @return Cross-reference
     * @throws IllegalArgumentException if xref is null
     */
    public SignatureXref addCrossReference(SignatureXref xref) throws IllegalArgumentException {
        if (xref == null) {
            throw new IllegalArgumentException("'xref' must not be null");
        }
        crossReferences.add(xref);
        xref.setSignature(this);
        return xref;
    }

    public void removeCrossReference(SignatureXref xref) {
        crossReferences.remove(xref);
    }

    @XmlElement
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    private String getSafeMd5(String md5) {
        return (md5 == null ? "" : md5.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
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
                .append(getSafeMd5(md5), getSafeMd5(s.md5))
                .append(comment, s.comment)
                .append(getSignatureLibraryRelease(),s.getSignatureLibraryRelease())
//                .append(getCrossReferences(), s.getCrossReferences())
//                .append(getDescription(), s.getDescription())
//                .append(getAbstract(), s.getAbstract())
//                .append(models, s.models)
//                .append(deprecatedAccessions, s.deprecatedAccessions)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(accession)
                .append(name)
                .append(type)
                .append(created)
                .append(updated)
                .append(getSafeMd5(md5))
                .append(comment)
                .append(getSignatureLibraryRelease())
//                .append(getCrossReferences())
//                .append(getDescription())
//                .append(getAbstract())
// TODO: Figure out why adding models to hashCode() causes Signature.equals() to fail
//                .append(models)
//                .append(deprecatedAccessions)
                .toHashCode();
    }

    /**
     * TODO - this will not work, giving a null value for description or the abstract if the instance is retrieved from the database.
     *
     * @return String representation of this object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accession", accession)
                .append("name", name)
                .append("type", type)
                .append("created", created)
                .append("updated", updated)
                .append("md5", getSafeMd5(md5))
                .append("comment", comment)
                .append("signatureLibraryRelease",getSignatureLibraryRelease())
//                .append("XRefs", getCrossReferences())
//                .append("description", getDescription())
//                .append("abstract", getAbstract())
//                .append("models", getModels())
//                .append("deprecatedAccessions", deprecatedAccessions)
                .toString();
    }

}
