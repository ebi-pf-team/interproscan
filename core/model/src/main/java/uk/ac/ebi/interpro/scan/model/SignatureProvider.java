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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;

/**
 * Signature provider.
 *
 * @author  Antony Quinn
 * @version $Id: SignatureProvider.java,v 1.8 2009/07/14 17:11:12 aquinn Exp $
 * @since   1.0
 */
@XmlType(name="SignatureProviderType")
@Entity
public class SignatureProvider implements Serializable {

    // select upper(dbshort)||'("'||dbcode||'", "'||dbshort||'", "'||dbname||'"),'
    // from interpro.cv_database order by dbshort;

    /**
     * Unique ID for persistence.
     */
    @Id
    private Long id;

    private String name;
    private String description;
    

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected SignatureProvider() { }

    public SignatureProvider(String name) {
        setName(name);
    }

    public SignatureProvider(String name, String description) {
        setName(name);
        setDescription(description);
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SignatureProvider))
            return false;
        final SignatureProvider s = (SignatureProvider) o;
        return new EqualsBuilder()
                .append(name, s.name)
                .append(description, s.description)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 43)
                .append(name)
                .append(description)
                .toHashCode();
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }    

    /**
     * Map SignatureProvider to and from XML representation
     */
    @XmlTransient
    static final class SignatureProviderAdapter extends XmlAdapter<String, SignatureProvider> {
        // Map Java to XML type
        @Override public String marshal(SignatureProvider provider) {
            return provider.getName();
        }
        // Map XML type to Java
        @Override public SignatureProvider unmarshal(String name) {
            // TODO: Test unmarshal
            return new SignatureProvider(name);
        }
    }    
    
}
