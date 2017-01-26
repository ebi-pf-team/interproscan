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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

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
 * Signature library release.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 */
@Entity
@BatchSize(size=200)
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"library", "version"}))
@XmlRootElement(name = "signature-library-release")
@XmlType(name = "SignatureLibraryReleaseType")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "id"})
public class SignatureLibraryRelease implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SIG_LIB_IDGEN")
    @TableGenerator(name = "SIG_LIB_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "signature_library_release", initialValue = 0, allocationSize = 1)
    private Long id;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Column(nullable = false)
    private SignatureLibrary library;

    @Column(length = 255, nullable = false)
    private String version;

    // TODO This needs to be ManyToMany so that a Signature can be re-used across releases.
    // TODO - This was an IMPACT requirement, but may well cause more problems than it solves.
    // TODO - so don't worry too much about getting this in place.
    @OneToMany(mappedBy = "signatureLibraryRelease", cascade = CascadeType.ALL)
    @XmlElement(name = "signature")
    @JsonBackReference
    protected Set<Signature> signatures = new HashSet<Signature>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SignatureLibraryRelease() {
    }

    public SignatureLibraryRelease(SignatureLibrary library, String version) {
        setLibrary(library);
        setVersion(version);
    }

    public SignatureLibraryRelease(SignatureLibrary library, String version, Set<Signature> signatures) {
        setLibrary(library);
        setVersion(version);
        setSignatures(signatures);
    }

    public Long getId() {
        return id;
    }

    @XmlAttribute(required = true)
//    @XmlJavaTypeAdapter(SignatureLibrary.SignatureLibraryAdapter.class)
    public SignatureLibrary getLibrary() {
        return library;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setLibrary(SignatureLibrary library) {
        this.library = library;
    }

    // TODO: Could not add JAXB annotation here (had to add to field) - THIS CAUSES PROBLEMS:
    // TODO: Each signature will not have a reference to SignatureLibraryRelease because setSignatures
    // TODO: is not used (JAXB accesses the field directly).
    // TODO: This needs fixing! (tried XmlAdapter to no avail -- see below)
    public Set<Signature> getSignatures() {
//        return (signatures == null ? null : Collections.unmodifiableSet(signatures));
        return signatures;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setSignatures(Set<Signature> signatures) {
        for (Signature s : signatures) {
            addSignature(s);
        }
    }

    public Signature addSignature(Signature signature) throws IllegalArgumentException {
        if (signature == null) {
            throw new IllegalArgumentException("'Signature' must not be null");
        }
        if (signature.getSignatureLibraryRelease() != null) {
            signature.getSignatureLibraryRelease().removeSignature(signature);
        }
        signature.setSignatureLibraryRelease(this);
        signatures.add(signature);
        return signature;
    }

    public void removeSignature(Signature signature) {
        signatures.remove(signature);
        signature.setSignatureLibraryRelease(null);
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
        if (!(o instanceof SignatureLibraryRelease))
            return false;
        final SignatureLibraryRelease s = (SignatureLibraryRelease) o;
        return new EqualsBuilder()
                .append(library, s.library)
                .append(version, s.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 39)
                .append(library)
                .append(version)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("library", library)
                .append("version", version)
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

    /**
     * TEST METHOD - this is here to prevent circular XML marshalling, if
     * marshalling from Proteins outwards.
     */
    public void clearReferences() {
        this.signatures = Collections.emptySet();
    }
}
