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

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Database cross-reference.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@XmlType(name = "SignatureXrefType")
@Table(indexes = { @Index(columnList = "IDENTIFIER") })
public class SignatureXref extends Xref implements Serializable {

    @ManyToOne(optional = false)
    @JsonBackReference
    private Signature signature;

    /**
     * Zero arguments constructor for Hibernate.
     */
    protected SignatureXref() {
    }

    public SignatureXref(String identifier) {
        super(identifier);
    }

    public SignatureXref(String databaseName, String identifier, String name) {
        super(databaseName, identifier, name);
    }

    @XmlTransient
    public Signature getSignature() {
        return signature;
    }

    void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SignatureXref))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 47)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
