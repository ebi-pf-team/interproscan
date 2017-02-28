package uk.ac.ebi.interpro.scan.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * InterPro entry, for example IPR000152 [http://www.ebi.ac.uk/interpro/entry/IPR000152].
 *
 * @author Antony Quinn
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
@Entity
@BatchSize(size=4000)
@XmlRootElement(name = "entry")
@XmlType(name = "EntryType")
@Table(indexes = {
        @Index(name = "ENTRY_AC_IDX", columnList = "ACCESSION"),
        @Index(name = "ENTRY_NAME_IDX", columnList = "NAME"),
        @Index(name = "ENTRY_TYPE_IDX", columnList = "TYPE")

})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "created", "updated", "releases", "id", "abstract"}) // IBU-4703: "abstract" is never populated
public class Entry implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ENTRY_IDGEN")
    @TableGenerator(name = "ENTRY_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "entry", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String accession;

    @Column
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable(name = "ENTRY_DESCRIPTION_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> descriptionChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String descriptionFirstChunk;

    @Transient
    private String description;

    @Column
    private EntryType type;

    @Column(nullable = true)
    private Date created;

    @Column(nullable = true)
    private Date updated;

    @ElementCollection(fetch = FetchType.EAGER)     // Hibernate specific annotation.
    @JoinTable(name = "ENTRY_ABSTRACT_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(name = "ABSTRACT_CHUNK", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> abstractChunks = Collections.emptyList();

    @Column(nullable = true, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String abstractFirstChunk;

    @Transient
    private String abstractText;

    /**
     * Set cascading to detached, because we do not want to persist or update releases,
     * during the persistence of an entry.
     */
    @ManyToMany(
            targetEntity = Release.class,
            cascade = CascadeType.DETACH)
    @JoinTable(
            name = "ENTRY_RELEASE",
            joinColumns = @JoinColumn(name = "ENTRY_ID"),
            inverseJoinColumns = @JoinColumn(name = "RELEASE_ID"))
    // TODO: Validate annotation changes
    private Set<Release> releases;

    @ManyToMany(
            targetEntity = GoXref.class,
            cascade = CascadeType.ALL)
    @JoinTable(
            name = "ENTRY_GO_XREF",
            joinColumns = @JoinColumn(name = "ENTRY_ID"),
            inverseJoinColumns = @JoinColumn(name = "GO_XREF_ID"))
    @JsonManagedReference
    private Set<GoXref> goXRefs = new HashSet<GoXref>();

    @ManyToMany(
            targetEntity = PathwayXref.class,
            cascade = CascadeType.ALL)
    @JoinTable(
            name = "ENTRY_PATHWAY_XREF",
            joinColumns = @JoinColumn(name = "ENTRY_ID"),
            inverseJoinColumns = @JoinColumn(name = "PATHWAY_XREF_ID"))
    @JsonManagedReference
    private Set<PathwayXref> pathwayXRefs = new HashSet<PathwayXref>();

    @OneToMany(mappedBy = "entry", fetch = FetchType.EAGER)
    //@XmlElementWrapper(name = "signatures")
//    @XmlElement(name = "signature") // TODO: This should not be here (see TODO comments on getSignatures)
    @JsonBackReference
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
                 Set<Release> releases,
                 Set<Signature> signatures,
                 Set<GoXref> goXrefs,
                 Set<PathwayXref> pathwayXrefs) {
        setAccession(accession);
        setName(name);
        setDescription(description);
        setType(type);
        setAbstract(abstractText);
        setReleases(releases);
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
        private Set<Release> releases = new HashSet<Release>();
        private Set<Signature> signatures = new HashSet<Signature>();
        private Set<GoXref> goCrossReferences = new HashSet<GoXref>();
        private Set<PathwayXref> pathwayXRefs = new HashSet<PathwayXref>();

        public Builder(String accession) {
            this.accession = accession;
        }

        public Entry build() {
            Entry entry = new Entry(accession);
            entry.setName(name);
            entry.setDescription(description);
            entry.setType(type);
            entry.setAbstract(abstractText);
            entry.setCreated(created);
            entry.setUpdated(updated);
            if (!releases.isEmpty()) {
                entry.setReleases(releases);
            }
            if (!signatures.isEmpty()) {
                entry.setSignatures(signatures);
            }
            if (!goCrossReferences.isEmpty()) {
                entry.setGoXRefs(goCrossReferences);
            }
            if (!pathwayXRefs.isEmpty()) {
                entry.setPathwayXRefs(pathwayXRefs);
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

        public Builder release(Release release) {
            releases.add(release);
            return this;
        }

        public Builder releases(Set<Release> releases) {
            this.releases = releases;
            return this;
        }

        public Builder signature(Signature signature) {
            this.signatures.add(signature);
            return this;
        }

        public Builder signatures(Set<Signature> signatures) {
            this.signatures = signatures;
            return this;
        }

        public Builder goCrossReference(GoXref xref) {
            this.goCrossReferences.add(xref);
            return this;
        }

        public Builder goCrossReferences(Set<GoXref> xrefs) {
            this.goCrossReferences = xrefs;
            return this;
        }

        public Builder pathwayCrossReference(PathwayXref xref) {
            this.pathwayXRefs.add(xref);
            return this;
        }

        public Builder pathwayCrossReferences(Set<PathwayXref> xrefs) {
            this.pathwayXRefs = xrefs;
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
//    @XmlElement(name = "abstract")
    @XmlAttribute(name = "abstract")
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
    public Set<Release> getReleases() {
        return releases;
    }

    void setReleases(Set<Release> releases) {
        for (Release release : releases) {
            addRelease(release);
        }
    }

    /**
     * Adds and returns InterPro releases.
     *
     * @param release A new specified Interpro release which should be add to this entry.
     * @return The specified InterPro release.
     * @throws IllegalArgumentException if xref is null
     */
    public Release addRelease(Release release) throws IllegalArgumentException {
        if (release == null) {
            throw new IllegalArgumentException("Release should not be null");
        }
        if (releases == null) {
            releases = new HashSet<Release>();
        }
        releases.add(release);
        release.addEntry(this);
        return release;
    }

    public void removeRelease(Release release) {
        if (release == null) {
            throw new IllegalArgumentException("Release should not be null");
        }
        release.removeEntry(this);
        if (releases != null) {
            releases.remove(release);
        }
    }

    /**
     * Returns GO cross-references.
     *
     * @return GO cross-references
     */
    @XmlElement(name = "go-xref")
    public Set<GoXref> getGoXRefs() {
        return goXRefs;
    }

    public void setGoXRefs(Set<GoXref> xrefs) {
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
        if (goXRefs == null) {
            goXRefs = new HashSet<GoXref>();
        }
        goXRefs.add(xref);
        xref.addEntry(this);
        return xref;
    }

    public void removeGoXRef(GoXref xref) {
        if (goXRefs != null) {
            goXRefs.remove(xref);
        }
    }

    /**
     * Returns pathway cross-references.
     *
     * @return Pathway cross-references
     */
    @XmlElement(name = "pathway-xref")
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

    public void setPathwayXRefs(Collection<PathwayXref> xrefs) {
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
        if (pathwayXRefs == null) {
            pathwayXRefs = new HashSet<PathwayXref>();
        }
        pathwayXRefs.add(xref);
        xref.addEntry(this);
        return xref;
    }

    public void removePathwayXRef(PathwayXref xref) {
        if (pathwayXRefs != null) {
            pathwayXRefs.remove(xref);
        }
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
//        return Collections.unmodifiableSet(signatures);
        return signatures;
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
                .append(signatures, e.getSignatures())
//                .append(releases, e.getReleases())
//                .append(pathwayXRefs, e.getPathwayXRefs())
//                .append(goXRefs, e.getGoXRefs())
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
                .append(getSignatures())
//                .append(getReleases())
//                .append(getPathwayXRefs())
//                .append(getGoXRefs())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accession", accession)
                .append("name", name)
                .append("type", type)
                .append("created", created)
                .append("updated", updated)
                .append("description", getDescription())
                .append("abstract", getAbstract())
                .append("signatures", getSignatures())
//                .append("releases", getReleases())
//                .append("pathwayXRefs", getPathwayXRefs())
//                .append("goXRefs", getGoXRefs())
                .toString();
    }
}
