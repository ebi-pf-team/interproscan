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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Signature database release.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@Entity
@XmlRootElement(name="signature-database-release")
@XmlType(name="SignatureDatabaseReleaseType")
public class SignatureDatabaseRelease implements Serializable {

    @Id
    private Long id;

    @ManyToOne
    private SignatureProvider provider;
    
    private String version;

    @OneToMany (mappedBy = "signatureDatabaseRelease")
    @XmlElement(name="signature", required=true)
    private Set<Signature> signatures = new HashSet<Signature>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SignatureDatabaseRelease() { }

    public SignatureDatabaseRelease(SignatureProvider provider, String version) {
        setProvider(provider);
        setVersion(version);
    }

    public SignatureDatabaseRelease(SignatureProvider provider, String version, Set<Signature> signatures) {
        setProvider(provider);
        setVersion(version);
        setSignatures(signatures);
    }

    public Long getId() {
        return id;
    }

    @XmlAttribute(required=true)
    @XmlJavaTypeAdapter(SignatureProvider.SignatureProviderAdapter.class)
    public SignatureProvider getProvider() {
        return provider;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setProvider(SignatureProvider provider) {
        this.provider = provider;
    }

    // TODO: Could not add JAXB annotation here (had to add to field) - THIS CAUSES PROBLEMS:
    // TODO: Each signature will not have a reference to SignatureDatabaseRelease because setSignatures
    // TODO: is not used (JAXB accesses the field directly).
    // TODO: This needs fixing! (tried XmlAdapter to no avail -- see below)
    public Set<Signature> getSignatures() {
        return (signatures == null ? null : Collections.unmodifiableSet(signatures));
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setSignatures(Set<Signature> signatures) {
        for (Signature s : signatures)  {
            addSignature(s);
        }
    }

    public Signature addSignature(Signature signature) throws IllegalArgumentException {
        if (signature == null) {
            throw new IllegalArgumentException("'Signature' must not be null");
        }
        if (signature.getSignatureDatabaseRelease() != null) {
            signature.getSignatureDatabaseRelease().removeSignature(signature);
        }
        signature.setSignatureDatabaseRelease(this);
        signatures.add(signature);
        return signature;
    }

    public void removeSignature(Signature signature) {
        signatures.remove(signature);
        signature.setSignatureDatabaseRelease(null);
    }

    @XmlAttribute(required=true)
    public String getVersion() {
        return version;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setVersion(String version) {
        this.version = version;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SignatureDatabaseRelease))
            return false;
        final SignatureDatabaseRelease s = (SignatureDatabaseRelease) o;
        return new EqualsBuilder()
                .append(version, s.version)
                .append(provider, s.provider)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 39)
                .append(version)
                .append(provider)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
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
