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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
@XmlRootElement(name="signature-database-release")
@XmlType(name="SignatureDatabaseReleaseType", propOrder={"provider", "version", "signatures"})
@Entity
public class SignatureDatabaseRelease implements Serializable {

    @Id
    private Long id;

    @ManyToOne
    private SignatureProvider       provider;
    private String                  version;

    @OneToMany (mappedBy = "signatureDatabaseRelease")
    private Set<Signature> signatures;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SignatureDatabaseRelease() { }

    public SignatureDatabaseRelease(SignatureProvider provider, String version) {
        setProvider(provider);
        setVersion(version);
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

    /**
     * Returns lazy-initialised internal reference to signatures.
     * We lazy-initialise to avoid empty collection appearing as
     * &lt;signatures/&gt; in the XML
     *
     * @return Modifiable reference to signatures collection.
     */
    private Set<Signature> getModifiableSignatures()    {
        if (signatures == null) {
            signatures = new HashSet<Signature>();
        }
        return signatures;
    }    

    //TODO: Re-add element wrapper when solved problem of "@XmlElementWrapper is only allowed on a collection property"
    // Works in JAXB 2.0.3 but not 2.1.3 [http://forums.java.net/jive/thread.jspa?threadID=25940]
    //@XmlElementWrapper(name="signatures")
    @XmlElement(name="signature", required=true)
    public Set<Signature> getSignatures() {
        return (signatures == null ? null : Collections.unmodifiableSet(signatures));
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setSignatures(Set<Signature> signatures) {
        this.signatures = signatures;
    }

    public Signature addSignature(Signature signature) throws IllegalArgumentException {
        if (signature == null) {
            throw new IllegalArgumentException("'Signature' must not be null");
        }
        if (signature.getSignatureDatabaseRelease() != null) {
            signature.getSignatureDatabaseRelease().removeSignature(signature);
        }
        signature.setSignatureDatabaseRelease(this);
        getModifiableSignatures().add(signature);
        return signature;
    }

    public void removeSignature(Signature signature) {
        getModifiableSignatures().remove(signature);
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

    /*
    private static class SignatureMapAdapter extends XmlAdapter<Collection<Signature>, Map<String, Signature>> {
        // Map Java to XML type
        public Collection<Signature> marshal(Map<String, Signature> map) {
            return map.values();
        }
        // Map XML type to Java
        // TODO: Test unmarshal
        public Map<String, Signature> unmarshal(Collection<Signature> collection) {
            Map<String, Signature> map = new HashMap<String, Signature>();
            for (Signature s : collection)   {
                map.put(s.getKey(), s);
            }
            return map;
        }
    }
    */

}
