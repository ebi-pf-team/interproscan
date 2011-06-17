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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.*;

/**
 * Represents an open reading frame.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@XmlRootElement(name = "orf")
@XmlType(name = "OrfType")
public class OpenReadingFrame implements Serializable {

    private int start;
    private int end;
    private NucleotideSequenceStrand strand;
    private final Set<Protein> proteins = new HashSet<Protein>();

    /** protected no-arg constructor required by JPA - DO NOT USE DIRECTLY */
    protected OpenReadingFrame() {
    }

    public OpenReadingFrame(int start, int end, NucleotideSequenceStrand strand) {
        this.start  = start;
        this.end    = end;
        this.strand = strand;
    }

    @XmlAttribute(required = true)
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @XmlAttribute(required = true)
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @XmlAttribute(required = true)
    public NucleotideSequenceStrand getStrand() {
        return strand;
    }

    public void setStrand(NucleotideSequenceStrand strand) {
        this.strand = strand;
    }

    public void addProtein(Protein protein) {
        proteins.add(protein);
    }

    @XmlElement(name = "protein")
    public Set<Protein> getProteins() {
        return proteins;
    }

    void setProteins(Set<Protein> proteins) {
        if (proteins == null) {
            throw new IllegalArgumentException("'Proteins' must not be null");
        }
        for (Protein protein : proteins) {
            addProtein(protein);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OpenReadingFrame))
            return false;
        final OpenReadingFrame f = (OpenReadingFrame) o;
        return new EqualsBuilder()
                .append(start, f.start)
                .append(end, f.end)
                .append(strand, f.strand)
                .append(proteins, f.proteins)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(71, 113)
                .append(start)
                .append(end)
                .append(strand)
                .append(proteins)
                .toHashCode();
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
