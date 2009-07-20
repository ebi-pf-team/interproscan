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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Protein.
 *
 * @author  Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlRootElement(name="protein")
@XmlType(name="ProteinType", propOrder={"sequence", "filteredMatches"})
public class Protein
        extends AbstractMatchableEntity 
        implements PersistentEntity, MatchableEntity, Serializable {

    // TODO: Consider public static inner Sequence class so can implement Formatter interface

    private static final Pattern AMINO_ACID_PATTERN = Pattern.compile("^[A-Z]+$");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    
    // TODO Consider whether this needs to be based upon CHUNKING as used in PRIDE, so a long protein sequence is stored
    // TODO in indexed VARCHAR columns, rather than using CLOBs that give very poor performance.
    @Column(name="protein_sequence", length = 100000, unique = true, updatable = false)     // Length based upon current longest protein in UniParc: 37777 residues.
    private String sequence;

    @Column(nullable = false, unique = true, updatable = false, length = 32)
    private String md5;

    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "protein")
    private Set<XrefSequenceIdentifier> crossReferences = new HashSet<XrefSequenceIdentifier>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Protein() {}

    public Protein(String sequence) {
        setSequenceAndMd5(sequence);
    }

    public Protein(String sequence, Set<FilteredMatch> filteredMatches) {
        super(filteredMatches);
        setSequenceAndMd5(sequence);
    }        

    public Protein(String sequence, Set<RawMatch> rawMatches, Set<FilteredMatch> filteredMatches) {
        super(rawMatches, filteredMatches);
        setSequenceAndMd5(sequence);
    }

    private void setSequenceAndMd5(String sequence)    {
        setSequence(sequence);
        setMd5(calculateMd5(sequence));
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

    @XmlElement
    public String getSequence() {
        return sequence;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    private void setSequence(String sequence) {
        // Check for nulls
        if (sequence == null)   {
            throw new IllegalArgumentException("'sequence' is null");
        }
        // Remove white space and convert to upper case
        sequence = WHITESPACE_PATTERN.matcher(sequence).replaceAll("");
        sequence = sequence.toUpperCase();
        // Check amino acid
        if (!AMINO_ACID_PATTERN.matcher(sequence).matches())   {
            throw new IllegalArgumentException("'sequence' is not an amino acid sequence [" + sequence + "]");
        }
        this.sequence = sequence;
    }

    /**
     * Returns filtered matches
     *
     * TODO - Check why this is here - does it need to be?  Probably does to allow JPA annotations to know what Entity to map to.
     * 
     * @return Filtered matches.
     * @see    FilteredMatch
     */
    @XmlElement(name="matches", required=true)
    @XmlJavaTypeAdapter(AbstractFilteredMatch.FilteredMatchAdapter.class)
    @Override public Set<FilteredMatch> getFilteredMatches() {
        return super.getFilteredMatches();
    }

    /**
     * Returns key to use in, for example, HashMap.
     *
     * @return Key to use in, for example, HashMap.
     */
    @XmlTransient
    public String getKey() {
        return getMd5();
    }

    /**
     * Returns cross-references.
     *
     * @return cross-references
     */
    public Set<XrefSequenceIdentifier> getCrossReferences() {
        return Collections.unmodifiableSet(crossReferences);
    }

    /**
     * Adds and returns cross-reference
     *
     * @param xref Cross-reference to add
     * @return Cross-reference
     * @throws IllegalArgumentException if xref is null
     */
    public XrefSequenceIdentifier addCrossReference(XrefSequenceIdentifier xref) throws IllegalArgumentException {
        if (xref == null) {
            throw new IllegalArgumentException("'xref' must not be null");
        }
        crossReferences.add(xref);
        xref.setProtein (this);
        return xref;
    }

    /**
     * Removes match from sequence
     *
     * @param xref Cross-reference to remove
     */
    public void removeCrossReference(XrefSequenceIdentifier xref) {
        crossReferences.remove(xref);
    }

    private static String calculateMd5(String sequence)   {
        try {
            // TODO - Check this - the JavaDoc suggests that this method call creates a new instance of
            // TODO - the digest each time it is called.  Is this thread safe?  If so, make singleton or static.
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(sequence.getBytes(), 0, sequence.length());
            final int RADIX = 16; // TODO: Why is radix 16?
            String md5 = new BigInteger(1, m.digest()).toString(RADIX);
            return (md5.toLowerCase(Locale.ENGLISH));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 implementation not available", e);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Protein))
            return false;
        final Protein p = (Protein) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(sequence, p.sequence)
                .append(md5, p.md5)
                .append(crossReferences, p.crossReferences)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 47)
                .appendSuper(super.hashCode())
                .append(sequence)
                .append(md5)
                .append(crossReferences)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
