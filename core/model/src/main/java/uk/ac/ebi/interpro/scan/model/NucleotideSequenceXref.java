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

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Protein cross-reference.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@XmlType(name = "NucleotideSequenceXrefType")
public class NucleotideSequenceXref extends Xref implements Serializable {

    @ManyToOne(optional = false)
    private NucleotideSequence sequence;

    /**
     * Zero arguments constructor just for Hibernate.
     */
    protected NucleotideSequenceXref() {
    }

    public NucleotideSequenceXref(String identifier) {
        this(null, identifier);
    }

    public NucleotideSequenceXref(String databaseName, String identifier) {
        this(databaseName, identifier, null);
    }

    public NucleotideSequenceXref(String databaseName, String identifier, String name) {
        super(databaseName, identifier, name);
    }

    @XmlTransient
    public NucleotideSequence getNucleotideSequence() {
        return sequence;
    }

    void setNucleotideSequence(NucleotideSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
