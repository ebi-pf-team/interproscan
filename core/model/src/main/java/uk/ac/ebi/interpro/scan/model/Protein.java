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

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Protein.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
// TODO: Class should really be called "Peptide"
@Entity
@XmlRootElement(name = "protein")
@XmlType(name = "ProteinType", propOrder = {"sequenceObject", "crossReferences", "superMatches", "matches"})
@JsonIgnoreProperties({"id", "superMatches", "orfs", "openReadingFrames"})
public class Protein implements OutputListElement, Serializable {

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    /**
     * NOTE: Changed to any letter of the alphabet, as some pretty odd codes are allowed in addition
     * to the 20 standard amino acids:
     * Selenocysteine	U
     * Pyrrolysine	O
     * In addition to the specific amino acid codes, placeholders are used in cases where chemical or crystallographic analysis of a peptide or protein cannot conclusively determine the identity of a residue.
     * Asparagine or aspartic acid		B
     * Glutamine or glutamic acid		Z
     * Leucine or Isoleucine		J
     * Unspecified or unknown amino acid		X
     * <p/>
     * All or any of these may appear in UniParc.
     */
    @Transient
    public static final Pattern AMINO_ACID_PATTERN = Pattern.compile("^[A-Z-*]+$");

    /* This pattern is used to detect asterix (in combination with the AMINO_ACID_PATTERN pattern). */
    @Transient
    public static final Pattern AMINO_ACID_WITHOUT_ASTERIX_PATTERN = Pattern.compile("^[A-Z-]+$");

    @Transient
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PROT_IDGEN")
    @TableGenerator(name = "PROT_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "protein", initialValue = 0, allocationSize = 100)
    protected Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "PROTEIN_SEQUENCE_CHUNK")
    @OrderColumn(name = "CHUNK_INDEX")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    @BatchSize(size=4000)
    private List<String> sequenceChunks;

    @Column(nullable = false, updatable = false, length = Chunker.CHUNK_SIZE)
    @XmlTransient
    private String sequenceFirstChunk;

    @Transient
    private String sequence;

    @Column(nullable = false, unique = true, updatable = false, length = 32)
    private String md5;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "protein")
    @BatchSize(size=4000)
    @JsonManagedReference
    private Set<Match> matches = new HashSet<Match>();

    @Transient
    //@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "protein")
    private final Set<SuperMatch> superMatches = new HashSet<SuperMatch>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "protein")
    @XmlElement(name = "xref")
    @BatchSize(size=4000)
    // TODO: This should not be here (so TODO comments on getCrossReferences)
    @JsonManagedReference
    private Set<ProteinXref> crossReferences = new HashSet<ProteinXref>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "protein")
    @BatchSize(size=4000)
    private final Set<OpenReadingFrame> orfs = new HashSet<OpenReadingFrame>();

    @Transient
    @XmlTransient
    private int sequenceLength = 0;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Protein() {
    }

    public Protein(String sequence) {
        setSequenceAndMd5(sequence);
    }

    //TODO: Why does this constructor not call the previous one?
    public Protein(String sequence, Set<Match> matches) {
        setMatches(matches);
        setSequenceAndMd5(sequence);
    }

    public Protein(String sequence, Set<Match> matches, Set<ProteinXref> crossReferences) {
        setMatches(matches);
        setSequenceAndMd5(sequence);
        setCrossReferences(crossReferences);
    }

    /**
     * Utility method to add a List of cross references
     *
     * @param accessions
     */
    public void addCrossReferences(String... accessions) {
        for (String accession : accessions) {
            addCrossReference(new ProteinXref(accession));
        }
    }

    /**
     * Get the length of the sequence.
     * Lazy load for efficiency - may be called repeatedly.
     *
     * @return The length
     */
    public int getSequenceLength() {
        if (sequenceLength == 0) {
            final String seq = getSequence();
            if (seq == null) {
                throw new IllegalStateException("Protein sequence was NULL");
            }
            sequenceLength = seq.length();
        }
        return sequenceLength;
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
        private Set<ProteinXref> crossReferences = new HashSet<ProteinXref>();

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
                for (ProteinXref x : crossReferences) {
                    protein.addCrossReference(x);
                }
            }
            return protein;
        }

        public Builder crossReference(ProteinXref ProteinXref) {
            this.crossReferences.add(ProteinXref);
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
//        return Collections.unmodifiableSet(matches);
        return matches;
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

    //@XmlElementWrapper(name = "super-matches")
    @XmlElement(name = "super-match")
    public Set<SuperMatch> getSuperMatches() {
        return superMatches;
    }

    private void setSuperMatches(Set<SuperMatch> matches) {
        for (SuperMatch m : matches) {
            addSuperMatch(m);
        }
    }

    public SuperMatch addSuperMatch(SuperMatch match) throws IllegalArgumentException {
        if (match == null) {
            throw new IllegalArgumentException("'match' must not be null");
        }
        if (match.getProtein() != null) {
            match.getProtein().removeSuperMatch(match);
        }
        match.setProtein(this);
        superMatches.add(match);
        return match;
    }

    public void removeSuperMatch(SuperMatch match) {
        superMatches.remove(match);
        match.setProtein(null);
    }

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
        if (!AMINO_ACID_WITHOUT_ASTERIX_PATTERN.matcher(sequence).matches()) {
            throw new IllegalArgumentException("You have submitted a protein sequence which contains an asterix (*). This may be from an ORF prediction program. " +
                    "'*' is not a valid IUPAC amino acid character and amino acid sequences which go through our pipeline should not contain it. Please strip out " +
                    "all asterix characters from your sequence and resubmit your search.");
        }
        this.sequence = sequence;
        List<String> chunks = CHUNKER.chunkIntoList(sequence);
        this.sequenceFirstChunk = CHUNKER.firstChunk(chunks);
        this.sequenceChunks = CHUNKER.latterChunks(chunks);
    }

    @XmlElement(name = "sequence")
    private Sequence getSequenceObject() {
        return new Sequence(getSequence(), getMd5());
    }

    private void setSequenceObject(Sequence sequence) {
        setSequence(sequence.getSequence());
        setMd5(sequence.getMd5());
    }

    /**
     * This class is used only for the purposes of JAXB - it is created on the fly
     * by the getSequenceObject() method and is not persisted.  If an XML is unmarshalled,
     * the setSequenceObject method retrieves the sequence and MD5 from this object
     * and sets them on the Protein object directly.
     */
    @XmlType(name = "SequenceType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
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
    //@XmlElement(name="ProteinXref")
    // TODO: Example: Expected: ProteinXref[protein=uk.ac.ebi.interpro.scan.model.Protein@1f49969]
    // TODO: Example: Actual:   ProteinXref[protein=<null>]
    // TODO: Actually found that setCrossReferences() not called even if return modifiable set -- is this a bug in
    // TODO: JAXB or do we have to use an XmlAdapter?
    public Set<ProteinXref> getCrossReferences() {
//        return Collections.unmodifiableSet(crossReferences);
        return crossReferences;
    }

    private void setCrossReferences(Set<ProteinXref> crossReferences) {
        for (ProteinXref ProteinXref : crossReferences) {
            addCrossReference(ProteinXref);
        }
    }

    /**
     * Adds and returns cross-reference
     *
     * @param ProteinXref Cross-reference to add
     * @return Cross-reference
     * @throws IllegalArgumentException if ProteinXref is null
     */
    public ProteinXref addCrossReference(ProteinXref ProteinXref) throws IllegalArgumentException {
        if (ProteinXref == null) {
            throw new IllegalArgumentException("'ProteinXref' must not be null");
        }
        crossReferences.add(ProteinXref);
        ProteinXref.setProtein(this);
        return ProteinXref;
    }

    /**
     * Removes match from sequence
     *
     * @param ProteinXref Cross-reference to remove
     */
    public void removeCrossReference(ProteinXref ProteinXref) {
        crossReferences.remove(ProteinXref);
    }

    public void addOpenReadingFrame(OpenReadingFrame orf) {
        if (orf == null) {
            throw new IllegalStateException("the orf argument cannot be null.");
        }
        orfs.add(orf);
        orf.setProtein(this);
    }

    public void removeOpenReadingFrame(OpenReadingFrame orf) {
        orfs.remove(orf);
    }

    @XmlTransient
    public Set<OpenReadingFrame> getOpenReadingFrames() {
        return orfs;
    }

    public void setOpenReadingFrames(Set<OpenReadingFrame> orfs) {
        for (OpenReadingFrame orf : orfs) {
            addOpenReadingFrame(orf);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Protein))
            return false;
        final Protein p = (Protein) o;
        return new EqualsBuilder()
                .append(md5.toLowerCase(), p.md5.toLowerCase())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 47)
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

        private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

        static {
            try {
                m = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Cannot find MD5 algorithm", e);
            }
        }

       private static String calculateMd5(String sequence) {
            String md5;
            // As using single instance of MessageDigest, make thread safe.
            // This should be much faster than creating a new MessageDigest object
            // each time this method is called.
            synchronized (m) {
                m.reset();
                m.update(sequence.getBytes(), 0, sequence.length());
                byte[] data = m.digest();
                md5 = toHex(data);
            }
            return (md5.toLowerCase(Locale.ENGLISH));
        }

        private static String toHex(byte[] data) {
            char[] chars = new char[data.length * 2];
            for (int i = 0; i < data.length; i++) {
                chars[i * 2] = HEX_DIGITS[(data[i] >> 4) & 0xf];
                chars[i * 2 + 1] = HEX_DIGITS[data[i] & 0xf];
            }
            return new String(chars);
        }

    }

}
