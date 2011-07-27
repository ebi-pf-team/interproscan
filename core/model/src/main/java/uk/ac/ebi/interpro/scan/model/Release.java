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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * InterPro release.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"version"}))
@XmlRootElement(name = "interpro-release")
@XmlType(name = "ReleaseType")
public class Release implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RELEASE_IDGEN")
    @TableGenerator(name = "RELEASE_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "release", initialValue = 0, allocationSize = 1)
    private Long id;

    @Column(length = 255, nullable = false)
    private String version;

    /**
     * Set fetch type to eager, which means no lazy loading..
     */
    @ManyToMany(mappedBy = "releases",
            targetEntity = Entry.class, fetch = FetchType.EAGER)
    @XmlElement(name = "entry")
    private Set<Entry> entries = new HashSet<Entry>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Release() {
    }

    public Release(String version) {
        setVersion(version);
    }

    public Release(String version, Set<Entry> entries) {
        setVersion(version);
        setEntries(entries);
    }

    public Long getId() {
        return id;
    }

    // TODO: Could not add JAXB annotation here (had to add to field) - THIS CAUSES PROBLEMS:
    // TODO: Each entry will not have a reference to Release because setEntries
    // TODO: is not used (JAXB accesses the field directly).
    // TODO: This needs fixing! (tried XmlAdapter to no avail -- see below)
    public Set<Entry> getEntries() {
        return (entries == null ? new HashSet<Entry>() : entries);
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setEntries(Set<Entry> entries) {
        for (Entry e : entries) {
            addEntry(e);
        }
    }

    protected void addEntry(Entry entry) {
        if (this.entries == null) {
            this.entries = new HashSet<Entry>();
        }
        entries.add(entry);
    }

    public void removeEntry(Entry entry) {
        if (entries != null) {
            entries.remove(entry);
        }
    }

    @XmlAttribute(required = true)
    public String getVersion() {
        return version;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Release))
            return false;
        final Release s = (Release) o;
        return new EqualsBuilder()
                .append(version, s.version)
                .append(getEntries(), s.getEntries())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 39)
                .append(version)
                .append(getEntries())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("version", version)
                .append("entries", getEntries())
                .toString();
    }

//    Could not get following to work    
//    /**
//     * Map Signatures to and from XML representation
//     */
//    @XmlTransient
//    private static final class SignatureAdapter extends XmlAdapter<SignaturesType, Set<Signature>> {
//        /** Map Java to XML type */
//        @Override public SignaturesType marshal(Set<Signature> signatures) {
//            return (signatures == null || signatures.isEmpty() ? null : new SignaturesType(signatures));
//        }
//        /** Map XML type to Java */
//        @Override public Set<Signature> unmarshal(SignaturesType signaturesType) {
//            return signaturesType.getSignatures();
//        }
//    }
//
//    /**
//     * Helper class for SignatureAdapter
//     */
//    private final static class SignaturesType {
//
//        @XmlElement(name = "signature")
//        private final Set<Signature> signatures;
//
//        private SignaturesType() {
//            signatures = null;
//        }
//
//        public SignaturesType(Set<Signature> signatures) {
//            this.signatures = signatures;
//        }
//
//        public Set<Signature> getSignatures() {
//            return signatures;
//        }
//
//    }    

}
