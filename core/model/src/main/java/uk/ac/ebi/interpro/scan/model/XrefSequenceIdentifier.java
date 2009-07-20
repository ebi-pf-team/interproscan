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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Sequence cross-reference
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     Match
 */
@Entity
@XmlTransient
public class XrefSequenceIdentifier
        extends AbstractSequenceIdentifier
        implements SequenceIdentifier, MatchableEntity, Serializable {

    /**
     * Reference to the protein that this Xref is an annotation of.
     */
    @ManyToOne (optional = false)
    private Protein protein;

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected XrefSequenceIdentifier (){
        super(null);
    }

    /**
     * Constructor for an Xref that takes the xref String as an argument.
     *
     * @param identifier the Xref String.
     */
    public XrefSequenceIdentifier(String identifier){
        super(identifier);
    }    

    /**
     * Returns the Protein that this accession / ID cross reference annotates.
     *
     * @return the Protein that this accession / ID cross reference annotates.
     */
    public Protein getProtein() {
        return protein;
    }

    /**
     * Package private setter used by the Protein class to create a reference to the annotated protein.
     *
     * @param protein the Protein that this accession / ID cross reference annotates.
     */
    void setProtein(Protein protein) {
        this.protein = protein;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof XrefSequenceIdentifier))
            return false;
        final XrefSequenceIdentifier x = (XrefSequenceIdentifier) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(15, 51)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
