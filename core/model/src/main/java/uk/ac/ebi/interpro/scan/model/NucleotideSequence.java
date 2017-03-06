/*
 * Copyright 2011 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a nucleotide sequence (DNA or RNA).
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@XmlRootElement(name = "nucleotide-sequence")
@XmlType(name = "NucleotideType", propOrder = {"sequenceObject", "crossReferences", "openReadingFrames"})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class NucleotideSequence implements OutputListElement, Serializable {

    // TODO: Refactor code that can be shared with Protein class

    @Transient
    //includes all valid IUPAC characters
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^[atcgunryswkmbdhv\\.\\-]+$");
    @Transient
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);
    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "NUC_IDGEN")
    @TableGenerator(name = "NUC_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "nucleotide", initialValue = 0, allocationSize = 100)
    protected Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "NUCLEOTIDE_SEQUENCE_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> sequenceChunks;

    @Column(nullable = false, updatable = false, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String sequenceFirstChunk;

    @Transient
    private String sequence;

    @Column(nullable = false, unique = true, updatable = false, length = 32)
    private String md5;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "nucleotideSequence")
    @BatchSize(size=4000)
    private final Set<OpenReadingFrame> orfs = new HashSet<OpenReadingFrame>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sequence")
    @BatchSize(size=4000)
    private final Set<NucleotideSequenceXref> xrefs = new HashSet<NucleotideSequenceXref>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected NucleotideSequence() {
    }

    public NucleotideSequence(String sequence) {
        setSequenceAndMd5(sequence);
    }

    /**
     * Returns the unique identifier (e.g. database primary key) for this Protein.
     *
     * @return the unique identifier (e.g. database primary key) for this Protein.
     */
    public Long getId() {
        return id;
    }

    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
    }

    // ORFs...

    public void addOpenReadingFrame(OpenReadingFrame orf) {
        if (orf == null) {
            throw new IllegalStateException("the orf argument cannot be null.");
        }
        orfs.add(orf);
        orf.setNucleotideSequence(this);
    }

    public void removeOpenReadingFrame(OpenReadingFrame orf) {
        orfs.remove(orf);
    }

    @XmlElement(name = "orf", required = true)
    public Set<OpenReadingFrame> getOpenReadingFrames() {
        return orfs;
    }

    void setOpenReadingFrames(Set<OpenReadingFrame> orfs) {
        if (orfs == null) {
            throw new IllegalArgumentException("'orfs' must not be null");
        }
        for (OpenReadingFrame orf : orfs) {
            addOpenReadingFrame(orf);
        }
    }

    // xrefs...

    public void addCrossReference(NucleotideSequenceXref xref) {
        if (xref == null) {
            throw new IllegalArgumentException("The xref argument cannot be null.");
        }
        xrefs.add(xref);
        xref.setNucleotideSequence(this);
    }

    public void removeCrossReference(NucleotideSequenceXref xref) {
        xrefs.remove(xref);
    }

    @XmlElement(name = "xref", required = true)
    public Set<NucleotideSequenceXref> getCrossReferences() {
        return xrefs;
    }

    void setCrossReferences(Set<OpenReadingFrame> orfs) {
        if (orfs == null) {
            throw new IllegalArgumentException("'orfs' must not be null");
        }
        for (OpenReadingFrame orf : orfs) {
            addOpenReadingFrame(orf);
        }
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    private void setSequence(String sequence) {
        // Check for nulls
        if (sequence == null) {
            throw new IllegalArgumentException("'sequence' is null");
        }
        // Remove white space and convert to lower-case
        sequence = WHITESPACE_PATTERN.matcher(sequence).replaceAll("");
        sequence = sequence.toLowerCase();
        // Check
        if (!SEQUENCE_PATTERN.matcher(sequence).matches()) {
            throw new IllegalArgumentException("'sequence' is not a nucleotide sequence [" + sequence + "]");
        }
        this.sequence = sequence;
        List<String> chunks = CHUNKER.chunkIntoList(sequence);
        this.sequenceFirstChunk = CHUNKER.firstChunk(chunks);
        this.sequenceChunks = CHUNKER.latterChunks(chunks);
    }

    public String getSequence() {
        if (sequence == null) {
            sequence = CHUNKER.concatenate(sequenceFirstChunk, sequenceChunks);
        }
        return sequence;
    }

    @XmlElement(name = "sequence")
    private Sequence getSequenceObject() {
        return new Sequence(getSequence(), getMd5());
    }

    private void setSequenceObject(Sequence sequence) {
        setSequence(sequence.getSequence());
        setMd5(sequence.getMd5());
    }

    private void setSequenceAndMd5(String sequence) {
        setSequence(sequence);
        setMd5(Md5Helper.calculateMd5(sequence));
    }

    /**
     * This class is used only for the purposes of JAXB - it is created on the fly
     * by the getSequenceObject() method and is not persisted.  If an XML is unmarshalled,
     * the setSequenceObject method retrieves the sequence and MD5 from this object
     * and sets them on the Protein object directly.
     */
    @XmlType(name = "NucleotideSequenceType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private static final class Sequence {

        private String md5;
        private String sequence;

        private Sequence() {
            this.md5 = null;
            this.sequence = null;
        }

        public Sequence(String sequence, String md5) {
            this.md5 = md5;
            this.sequence = sequence;
        }

        @XmlAttribute
        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @XmlValue
        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NucleotideSequence))
            return false;
        final NucleotideSequence s = (NucleotideSequence) o;
        return new EqualsBuilder()
                .append(md5.toLowerCase(), s.md5.toLowerCase())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(39, 47)
                .append(md5)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * MD5 helper class.
     *
     * @author Phil Jones
     * @author Antony Quinn
     */
    @XmlTransient
    private static class Md5Helper {

        private static final MessageDigest m;

        private static final int HEXADECIMAL_RADIX = 16;

        static {
            try {
                m = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Cannot find MD5 algorithm", e);
            }
        }

        static String calculateMd5(String sequence) {
            String md5;
            // As using single instance of MessageDigest, make thread safe.
            // This should be much faster than creating a new MessageDigest object
            // each time this method is called.
            synchronized (m) {
                m.reset();
                m.update(sequence.getBytes(), 0, sequence.length());
                md5 = new BigInteger(1, m.digest()).toString(HEXADECIMAL_RADIX);
            }
            return (md5.toLowerCase(Locale.ENGLISH));
        }

    }

}
