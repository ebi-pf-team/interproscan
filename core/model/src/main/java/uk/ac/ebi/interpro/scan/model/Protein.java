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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Protein.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */

@Entity
@XmlRootElement(name = "protein")
@XmlType(name = "ProteinType", propOrder = {"sequence", "crossReferences", "matches"})
public class Protein implements Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    // TODO: Consider public static inner Sequence class so can implement Formatter interface
    // TODO: Consider moving md5 attribute to Sequence element: <sequence md5="hd83">AJGDW</sequence>

    /**
     * TODO - remove this - every now and then new amino acids appear from UniProt
     */
    @Transient
    private static final Pattern AMINO_ACID_PATTERN = Pattern.compile("^[A-I|K-N|P-Z-*]+$");

    @Transient
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PROT_IDGEN")
    @TableGenerator(name = "PROT_IDGEN", table = "KEYGEN", pkColumnValue = "protein", initialValue = 0, allocationSize = 100)
    protected Long id;

    @CollectionOfElements(fetch = FetchType.EAGER)
    // Hibernate specific annotation.
    @JoinTable(name = "protein_sequence_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(name = "sequence_chunk", length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> sequenceChunks;

    @Column(nullable = false, updatable = false, length = Chunker.CHUNK_SIZE, name = "sequence_first_chunk")
    @XmlTransient
    private String sequenceFirstChunk;

    @Transient
    private String sequence;

    @Column(nullable = false, unique = true, updatable = false, length = 32)
    private String md5;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Match> matches = new HashSet<Match>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "protein")
    @XmlElement(name = "xref")
    // TODO: This should not be here (so TODO comments on getCrossReferences)
    private Set<Xref> crossReferences = new HashSet<Xref>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Protein() {
    }

    public Protein(String sequence) {
        setSequenceAndMd5(sequence);
    }

    public Protein(String sequence, Set<Match> matches) {
        setMatches(matches);
        setSequenceAndMd5(sequence);
    }

    public Protein(String sequence, Set<Match> matches, Set<Xref> crossReferences) {
        setMatches(matches);
        setSequenceAndMd5(sequence);
        setCrossReferences(crossReferences);
    }

    /**
     * Utility method to add a List of cross references
     *
     * @param crossReferences
     */
    public void addCrossReferences(String... crossReferences) {
        for (String xrefName : crossReferences) {
            addCrossReference(new Xref(xrefName));
        }
    }

    /**
     * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
     *
     * @author Antony Quinn
     */
    @XmlTransient
    public static class Builder {

        // Required parameters
        private final String sequence;

        // Optional parameters
        private Set<Match> matches = new HashSet<Match>();
        private Set<Xref> crossReferences = new HashSet<Xref>();

        public Builder(String sequence) {
            this.sequence = sequence;
        }

        public Protein build() {
            Protein protein = new Protein(sequence);
            if (!matches.isEmpty()) {
                for (Match m : matches) {
                    protein.addMatch(m);
                }
            }
            if (!crossReferences.isEmpty()) {
                for (Xref x : crossReferences) {
                    protein.addCrossReference(x);
                }
            }
            return protein;
        }

        public Builder crossReference(Xref xref) {
            this.crossReferences.add(xref);
            return this;
        }

        public Builder match(Match match) {
            this.matches.add(match);
            return this;
        }

    }


    private void setSequenceAndMd5(String sequence) {
        setSequence(sequence);
        setMd5(Md5Helper.calculateMd5(sequence));
    }

    /**
     * Returns the unique identifier (e.g. database primary key) for this Protein.
     *
     * @return the unique identifier (e.g. database primary key) for this Protein.
     */
    public Long getId() {
        return id;
    }

    @XmlAttribute
    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * Returns {@link Match}es
     *
     * @return {@link Match}es
     */
    @XmlElement(name = "matches", required = true)
    @XmlJavaTypeAdapter(Match.MatchAdapter.class)
    public Set<Match> getMatches() {
        return Collections.unmodifiableSet(matches);
    }

    private void setMatches(Set<Match> matches) {
        for (Match m : matches) {
            addMatch(m);
        }
    }

    /**
     * Adds and returns filtered match to sequence
     *
     * @param match Match to add
     * @return Match to sequence
     * @throws IllegalArgumentException if match is null
     */
    public <T extends Match> T addMatch(T match) throws IllegalArgumentException {
        if (match == null) {
            throw new IllegalArgumentException("'Match' must not be null");
        }
        if (match.getProtein() != null) {
            match.getProtein().removeMatch(match);
        }
        match.setProtein(this);
        matches.add(match);
        return match;
    }

    /**
     * Removes filtered match from sequence
     *
     * @param match Match to remove
     */
    public <T extends Match> void removeMatch(T match) {
        matches.remove(match);
        match.setProtein(null);
    }

    @XmlElement
    public String getSequence() {
        if (sequence == null) {
            sequence = CHUNKER.concatenate(sequenceFirstChunk, sequenceChunks);
        }
        return sequence;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)

    private void setSequence(String sequence) {
        // Check for nulls
        if (sequence == null) {
            throw new IllegalArgumentException("'sequence' is null");
        }
        // Remove white space and convert to upper case
        sequence = WHITESPACE_PATTERN.matcher(sequence).replaceAll("");
        sequence = sequence.toUpperCase();
        // Check amino acid
        if (!AMINO_ACID_PATTERN.matcher(sequence).matches()) {
            throw new IllegalArgumentException("'sequence' is not an amino acid sequence [" + sequence + "]");
        }
        this.sequence = sequence;
        List<String> chunks = CHUNKER.chunkIntoList(sequence);
        this.sequenceFirstChunk = CHUNKER.firstChunk(chunks);
        this.sequenceChunks = CHUNKER.latterChunks(chunks);
    }

    /**
     * Returns cross-references.
     *
     * @return cross-references
     */
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
    public Set<Xref> getCrossReferences() {
        return Collections.unmodifiableSet(crossReferences);
    }

    private void setCrossReferences(Set<Xref> crossReferences) {
        for (Xref xref : crossReferences) {
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
    public Xref addCrossReference(Xref xref) throws IllegalArgumentException {
        if (xref == null) {
            throw new IllegalArgumentException("'xref' must not be null");
        }
        crossReferences.add(xref);
        xref.setProtein(this);
        return xref;
    }

    /**
     * Removes match from sequence
     *
     * @param xref Cross-reference to remove
     */
    public void removeCrossReference(Xref xref) {
        crossReferences.remove(xref);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Protein))
            return false;
        final Protein p = (Protein) o;
        return new EqualsBuilder()
                .append(sequence, p.sequence)
                .append(md5.toLowerCase(), p.md5.toLowerCase())
                .append(matches, p.matches)
                .append(crossReferences, p.crossReferences)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 47)
                .append(sequence)
                .append(md5)
                .append(matches)
                .append(crossReferences)
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

        private static final Pattern MD5_PATTERN = Pattern.compile("^[A-Fa-f0-9]{32}$");

        private static final MessageDigest m;

        private static final int HEXADECIMAL_RADIX = 16;

        static {
            try {
                m = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e) {
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
