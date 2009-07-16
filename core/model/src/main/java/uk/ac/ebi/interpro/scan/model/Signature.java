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
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

/**
 * Signature, for example SSF53098 [http://supfam.mrc-lmb.cam.ac.uk/SUPERFAMILY/cgi-bin/models_list.cgi?sf=53098]
 *
 * @author  Antony Quinn
 * @version $Id: Signature.java,v 1.18 2009/07/14 17:11:12 aquinn Exp $
 * @since   1.0
 */

@Entity
@XmlRootElement(name="signature")
@XmlType(name="SignatureType")
public class Signature implements Serializable {

    // TODO: IMPACT XML: Handle Pfam Clans, FingerPrints Hierachiesm SMART thresholds ...etc [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]

    // TODO: Add xrefs (inc. GO terms) and InterPro entry [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]
    // TODO: See http://www.ebi.ac.uk/seqdb/confluence/x/DYAg#ND3.3StandardXMLformatforallcommondatatypes-SMART

    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String accession;
    private String name;
    private String description;
    private String type;
    private String abstractText;

    @ManyToOne
    private SignatureDatabaseRelease signatureDatabaseRelease;

    // TODO: Decide whether to use Map or Set (see ChEBI team)
    // TODO: Use ConcurrentHashMap if need concurrent modification of signatures
    // TODO: Use Hashtable if want to disallow duplicate values
    @OneToMany (mappedBy = "signature")
    @MapKey (name= "accession")
    private Map<String, Model> models = new HashMap<String, Model>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Signature() {}

    public Signature(String accession) {
        setAccession(accession);
    }

    public Signature(String accession,
                     String name,
                     String type,                     
                     String description,
                     String abstractText,
                     SignatureDatabaseRelease signatureDatabaseRelease,
                     Set<Model> models) {
        setAccession(accession);
        setName(name);
        setDescription(description);
        setType(type);
        setAbstract(abstractText);
        setSignatureDatabaseRelease(signatureDatabaseRelease);
        for (Model m : models)  {
            addModel(m);
        }
    }

    /**
     * Builder pattern (see Josh Bloch "Effective Java" 2nd edition)
     */
    @XmlTransient
    public static class Builder {

        private final String accession;
        private String name;
        private String description;
        private String type;
        private String abstractText;
        private SignatureDatabaseRelease signatureDatabaseRelease;

        public Builder(String accession) {
            this.accession = accession;
        }

        public Signature build() {
            Signature signature = new Signature(accession);
            signature.setName(name);
            signature.setDescription(description);
            signature.setType(type);
            signature.setAbstract(abstractText);
            signature.setSignatureDatabaseRelease(signatureDatabaseRelease);
            return signature;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder abstractText(String text) {
            this.abstractText = text;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder signatureDatabaseRelease(SignatureDatabaseRelease signatureDatabaseRelease) {
            this.signatureDatabaseRelease = signatureDatabaseRelease;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    /**
     * Returns signature accession, for example PF00001.
     *
     * @return Signature accession
     */
    @XmlAttribute(name="ac", required=true)
    public String getAccession() {
        return accession;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    private void setAccession(String accession) {
        this.accession = accession;
    }

    /**
     * Returns signature identifier, for example 7tm_1.
     *
     * @return Signature identifier
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /**
     * Returns signature description, for example "7 transmembrane receptor (rhodopsin family)".
     *
     * @return Signature description
     */
    @XmlAttribute(name="desc")
    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns type of signature according to member database, for example:
     * - Gene3D signatures are all Domain
     * - ProDom and ProSite signatures are all Family
     * - Pfam signatures are Family, Domain, Motif or Repeat
     *
     * @return Type of signature according to member database
     */
    @XmlAttribute
    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    /**
     * Returns signature abstract.
     *
     * @return Signature abstract
     */
    @XmlElement(name="abstract")
    public String getAbstract() {
        return abstractText;
    }

    private void setAbstract(String text) {
        this.abstractText = text;
    }

    @XmlTransient
    public SignatureDatabaseRelease getSignatureDatabaseRelease()    {
        return signatureDatabaseRelease;
    }

    void setSignatureDatabaseRelease(SignatureDatabaseRelease signatureDatabaseRelease)  {
        this.signatureDatabaseRelease = signatureDatabaseRelease;
    }

    @XmlJavaTypeAdapter(ModelAdapter.class)
    public Map<String, Model> getModels() {
        return (models == null ? null : Collections.unmodifiableMap(models));
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setModels(Map<String, Model> models) {
        // Ensure Signature reference is set ("this.models = models" won't do)
        for (Model m : models.values())  {
            addModel(m);
        }
    }

    /**
     * Returns key to use in, for example, HashMap.
     *
     * @return Key to use in, for example, HashMap.
     */
    @XmlTransient
    public String getKey() {
        // TODO: Use name or accession as signature key?
        return getAccession();
    }

    public Model addModel(Model model) throws IllegalArgumentException {
        if (model == null) {
            throw new IllegalArgumentException("'Model' must not be null");
        }
        if (model.getSignature() != null) {
            model.getSignature().removeModel(model);
        }
        model.setSignature(this);
        models.put(model.getKey(), model);
        return model;
    }

    public void removeModel(Model model) {
        models.remove(model.getKey());
        model.setSignature(null);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Signature))
            return false;
        final Signature s = (Signature) o;
        return new EqualsBuilder()
                .append(accession, s.accession)
                .append(name, s.name)
                .append(type, s.type)
                .append(description, s.description)
                .append(abstractText, s.abstractText)
                .append(signatureDatabaseRelease, s.signatureDatabaseRelease)
                .append(models, s.models)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(accession)
                .append(name)
                .append(type)
                .append(description)
                .append(abstractText)
                .append(signatureDatabaseRelease)
                .append(models)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Map models to and from XML representation
     */
    @XmlTransient
    private static class ModelAdapter extends XmlAdapter<ModelsType, Map<String, Model>> {

        /** Map Java to XML type */
        @Override public ModelsType marshal(Map<String, Model> map) {
            return (map == null || map.isEmpty() ? null : new ModelsType(new HashSet<Model>(map.values())));
        }

        /** Map XML type to Java */
        @Override public Map<String, Model> unmarshal(ModelsType modelsType) {
            Map<String, Model> map = new HashMap<String, Model>();
            for (Model m : modelsType.getModels())  {
                map.put(m.getKey(), m);
            }
            return map;
        }

    }

    /**
     * Helper class for ModelAdapter
     */
    private final static class ModelsType {

        @XmlElement(name = "model")
        private Set<Model> models;

        public ModelsType() { }

        public ModelsType(Set<Model> models) {
            this.models = models;
        }

        public Set<Model> getModels() {
            return models;
        }

    }

}
