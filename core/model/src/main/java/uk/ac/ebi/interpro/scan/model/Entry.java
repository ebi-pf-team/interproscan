package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * InterPro entry, for example IPR000152 [http://www.ebi.ac.uk/interpro/IEntry?ac=IPR000152].
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@XmlRootElement(name = "entry")
@XmlType(name = "EntryType")
public class Entry implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ENTRY_IDGEN")
    @TableGenerator(name = "ENTRY_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "entry", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    @Index(name = "entry_ac_idx")
    private String accession;

    @Column
    @Index(name = "entry_name_idx")
    private String name;

    @CollectionOfElements(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable(name = "entry_description_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String descriptionFirstChunk;

    @Transient
    private String description;

    @Column
    @Index(name = "entry_type_idx")
    private EntryType type;

    @Column(nullable = true)
    private Date created;

    @Column(nullable = true)
    private Date updated;

    @CollectionOfElements(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable(name = "entry_abstract_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(name = "abstract_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> abstractChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String abstractFirstChunk;

    @Transient
    private String abstractText;

    @ManyToOne // TODO This needs to be ManyToMany so that an Entry can be re-used across releases.
    private Release release;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "entry")
    //@XmlElementWrapper(name = "xrefs")
    @XmlElement(name = "go-xref") // TODO: This should not be here (see TODO comments on getGoCrossReferences)
    private Set<GoXref> goXRefs = new HashSet<GoXref>();

    @ManyToMany(
            targetEntity = PathwayXref.class,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "ENTRY_PATHWAY",
            joinColumns = @JoinColumn(name = "ENTRY_ID"),
            inverseJoinColumns = @JoinColumn(name = "PATHWAY_ID"))
//    @XmlElement(name = "pathway-xref") // TODO: This should not be here
    private Collection<PathwayXref> pathwayXRefs;

    @Transient
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //@XmlElementWrapper(name = "signatures")
    @XmlElement(name = "signature") // TODO: This should not be here (see TODO comments on getSignatures)
    private Set<Signature> signatures = new HashSet<Signature>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Entry() {
    }

    public Entry(String accession) {
        setAccession(accession);
    }

    public Entry(String accession, String name, EntryType type) {
        setAccession(accession);
        setName(name);
        setType(type);
    }

    public Entry(String accession,
                 String name,
                 EntryType type,
                 String description,
                 String abstractText,
                 Release release,
                 Set<Signature> signatures,
                 Set<GoXref> goXrefs,
                 Collection<PathwayXref> pathwayXrefs) {
        setAccession(accession);
        setName(name);
        setDescription(description);
        setType(type);
        setAbstract(abstractText);
        setRelease(release);
        setSignatures(signatures);
        setGoXRefs(goXrefs);
        setPathwayXRefs(pathwayXrefs);
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
        private EntryType type;
        private Date created;
        private Date updated;
        private String abstractText;
        private Release release;
        private Set<Signature> signatures = new HashSet<Signature>();
        private Set<GoXref> goCrossReferences = new HashSet<GoXref>();
        private Collection<PathwayXref> pathwayXRefs = new HashSet<PathwayXref>();

        public Builder(String accession) {
            this.accession = accession;
        }

        public Entry build() {
            Entry entry = new Entry(accession);
            entry.setName(name);
            entry.setDescription(description);
            entry.setType(type);
            entry.setAbstract(abstractText);
            entry.setRelease(release);
            entry.setCreated(created);
            entry.setUpdated(updated);
            if (signatures != null) {
                entry.setSignatures(signatures);
            }
            if (!goCrossReferences.isEmpty()) {
                for (GoXref x : goCrossReferences) {
                    entry.addGoXRef(x);
                }
            }
            if (!pathwayXRefs.isEmpty()) {
                for (GoXref x : goCrossReferences) {
                    entry.addGoXRef(x);
                }
            }
            return entry;
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

        public Builder type(EntryType type) {
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

        public Builder Release(Release release) {
            this.release = release;
            return this;
        }

        public Builder signature(Signature signature) {
            this.signatures.add(signature);
            return this;
        }

        public Builder goCrossReference(GoXref xref) {
            this.goCrossReferences.add(xref);
            return this;
        }

    }

    public Long getId() {
        return id;
    }

    /**
     * Returns accession number, for example IPR000152.
     *
     * @return Accession number
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
     * Returns short name, for example EGF-type_Asp/Asn_hydroxyl_site.
     *
     * @return Short name
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /**
     * Returns description, for example "EGF-type aspartate/asparagine hydroxylation site".
     *
     * @return Description
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
     * Returns entry type, for example PTM
     *
     * @return Entry type
     */
    @XmlAttribute
    public EntryType getType() {
        return type;
    }

    private void setType(EntryType type) {
        this.type = type;
    }

    /**
     * Returns abstract.
     *
     * @return Abstract
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

    @XmlTransient
    public Release getRelease() {
        return release;
    }

    void setRelease(Release release) {
        this.release = release;
    }

    /**
     * Returns GO cross-references.
     *
     * @return GO cross-references
     */
    public Set<GoXref> getGoXRefs() {
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
        return Collections.unmodifiableSet(goXRefs);
    }

    private void setGoXRefs(Set<GoXref> xrefs) {
        for (GoXref xref : xrefs) {
            addGoXRef(xref);
        }
    }

    /**
     * Adds and returns GO cross-reference
     *
     * @param xref GO cross-reference to add
     * @return GO cross-reference
     * @throws IllegalArgumentException if xref is null
     */
    public GoXref addGoXRef(GoXref xref) throws IllegalArgumentException {
        if (xref == null) {
            throw new IllegalArgumentException("'xref' must not be null");
        }
        goXRefs.add(xref);
        xref.setEntry(this);
        return xref;
    }

    public void removeGoXRef(GoXref xref) {
        goXRefs.remove(xref);
    }

    /**
     * Returns pathway cross-references.
     *
     * @return Pathway cross-references
     */
    public Collection<PathwayXref> getPathwayXRefs() {
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
//        return Collections.unmodifiableSet(pathwayXRefs);
        return pathwayXRefs;
    }

    private void setPathwayXRefs(Collection<PathwayXref> xrefs) {
        for (PathwayXref xref : xrefs) {
            addPathwayXRef(xref);
        }
    }

    /**
     * Adds and returns pathway cross-reference
     *
     * @param xref Pathway cross-reference to add
     * @return Pathway cross-reference
     * @throws IllegalArgumentException if xref is null
     */
    public PathwayXref addPathwayXRef(PathwayXref xref) throws IllegalArgumentException {
        if (xref == null) {
            throw new IllegalArgumentException("'xref' must not be null");
        }
        pathwayXRefs.add(xref);
        xref.addEntry(this);
        return xref;
    }

    public void removePathwayXRef(PathwayXref xref) {
        pathwayXRefs.remove(xref);
    }


    /**
     * Returns signatures.
     *
     * @return signatures
     */
    public Set<Signature> getSignatures() {
        // TODO: Had to move @XmlElement annotation to field otherwise received message below - this is
        // TODO: bad because setSignatures() will not be used by JAXB (access field directly):
        /*
         java.lang.UnsupportedOperationException
            at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1037)
            at com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister.startPacking(Lister.java:296)
                ...
            at javax.xml.bind.helpers.AbstractUnmarshallerImpl.unmarshal(AbstractUnmarshallerImpl.java:105)
                ...
            at uk.ac.ebi.interpro.scan.model.AbstractTest.unmarshal(AbstractTest.java:150)
         */
        //@XmlElement(name="signature")
        return Collections.unmodifiableSet(signatures);
    }

    private void setSignatures(Set<Signature> signatures) {
        for (Signature signature : signatures) {
            addSignature(signature);
        }
    }

    /**
     * Adds and returns signature
     *
     * @param signature Signature to add
     * @return Signature
     * @throws IllegalArgumentException if signature is null
     */
    public Signature addSignature(Signature signature) throws IllegalArgumentException {
        if (signature == null) {
            throw new IllegalArgumentException("'signature' must not be null");
        }
        signatures.add(signature);
        signature.setEntry(this);
        return signature;
    }

    public void removeSignature(Signature signature) {
        signatures.remove(signature);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Entry))
            return false;
        final Entry e = (Entry) o;
        return new EqualsBuilder()
                .append(accession, e.accession)
                .append(name, e.name)
                .append(type, e.type)
                .append(created, e.created)
                .append(updated, e.updated)
                .append(getDescription(), e.getDescription())
                .append(getAbstract(), e.getAbstract())
                .append(signatures, e.signatures)
                .append(goXRefs, e.goXRefs)
                .append(pathwayXRefs, e.pathwayXRefs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(87, 23)
                .append(accession)
                .append(name)
                .append(type)
                .append(created)
                .append(updated)
                .append(getDescription())
                .append(getAbstract())
                        // TODO: Figure out why adding signatures to hashCode() causes Entry.equals() to fail
                .append(signatures)
                .append(goXRefs)
                .append(pathwayXRefs)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


}
