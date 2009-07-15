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

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Collection of protein matches and signature database releases.
 *
 * @author  Antony Quinn
 * @version $Id: ProteinSignatureCollection.java,v 1.8 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 */
@XmlRootElement(name="protein-signature-collection")
@XmlType(name="ProteinSignatureCollectionType")
// TODO - for the moment, not persisting this object, which just defines the root element of the XML.
// TODO - this should be reproducible using a DAO instead.
//@Entity
public class ProteinSignatureCollection implements Serializable {


    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    private Long id;

    @ManyToMany
    private Set<Protein> proteins;

    /**
     * TODO - THIS SHOULD NOT BE @Transient.  Need a reference to the ProteinSignatureCollection in SignatureDatabaseRelease.
     */
    @Transient
    private Set<SignatureDatabaseRelease> signatureDatabaseReleases;

    @XmlElement(name="protein", required=true)
    @XmlElementWrapper(name ="proteins")
//    @XmlJavaTypeAdapter(ProteinsAdapter.class)
    public Set<Protein> getProteins() {
        return (proteins == null ? null : Collections.unmodifiableSet(proteins));
    }

    /**
     * Returns lazy-initialised internal reference to models.
     * We lazy-initialise to avoid empty collection appearing as
     * &lt;proteins/&gt; in the XML
     *
     * @return Modifiable reference to proteins collection.
     */
    private Set<Protein> getModifiableProteins()    {
        if (proteins == null) {
            // TODO: Use ConcurrentHashMap if need concurrent modification of signatures
            // TODO: Use Hashtable if want to disallow duplicate values
            proteins = new HashSet<Protein>();
        }
        return proteins;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setProteins(Set<Protein> proteins) {
        this.proteins = proteins;
    }

    public Protein addProtein(Protein protein) throws IllegalArgumentException {
        if (protein == null) {
            throw new IllegalArgumentException("'Protein' must not be null");
        }
        getModifiableProteins().add(protein);
        return protein;
    }

    public void removeProtein(Protein protein) {
        getModifiableProteins().remove(protein);
    }

    @XmlElementWrapper(name="signature-database-releases")
    @XmlElement(name="signature-database-release", required=true)
    public Set<SignatureDatabaseRelease> getSignatureDatabaseReleases() {
        return (signatureDatabaseReleases == null ? null : Collections.unmodifiableSet(signatureDatabaseReleases));
    }

    /**
     * Returns lazy-initialised internal reference to models.
     * We lazy-initialise to avoid empty collection appearing as
     * &lt;signature-database-releases/&gt; in the XML
     *
     * @return Modifiable reference to models collection.
     */
    private Set<SignatureDatabaseRelease> getModifiableSignatureDatabaseReleases()    {
        if (signatureDatabaseReleases == null) {
            signatureDatabaseReleases = new HashSet<SignatureDatabaseRelease>();
        }
        return signatureDatabaseReleases;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    private void setSignatureDatabaseReleases(Set<SignatureDatabaseRelease> signatureDatabaseReleases) {
        this.signatureDatabaseReleases = signatureDatabaseReleases;
    }

    public SignatureDatabaseRelease addSignatureDatabaseRelease(SignatureDatabaseRelease signatureDatabaseRelease) throws IllegalArgumentException {
        if (signatureDatabaseRelease == null) {
            throw new IllegalArgumentException("'SignatureDatabaseRelease' must not be null");
        }
        getModifiableSignatureDatabaseReleases().add(signatureDatabaseRelease);
        return signatureDatabaseRelease;
    }

    public void removeSignatureDatabaseRelease(SignatureDatabaseRelease signatureDatabaseRelease) {
        getModifiableSignatureDatabaseReleases().remove(signatureDatabaseRelease);
    }

//     /**
//     *  Ensure sub-classes of AbstractMatch are represented correctly in XML.
//     */
//    @XmlTransient
//    private static final class ProteinsAdapter extends XmlAdapter<ProteinsType, Map<String, Protein>> {
//
//        // Adapt original Java construct to a type (ProteinsType) which we can easily map to the XML output we want
//        @Override public ProteinsType marshal(Map<String, Protein> proteins) {
//            return new ProteinsType(proteins.values().toArray(new Protein[proteins.size()]));
//
//        }
//
//        // map XML type to Java
//        @Override public Map<String, Protein> unmarshal(ProteinsType proteinTypes) {
//            // TODO: Test unmarshal
//            Map<String, Protein> proteins = new HashMap<String, Protein>();
//            for (Protein p : proteinTypes.getProteins()) {
//                proteins.put(p.getKey(), p);
//            }
//            return proteins;
//        }
//
//    }

//    /**
//     * Helper class for ProteinsAdapter
//     */
//    private final static class ProteinsType {
//
//        private final Protein[] proteins;
//
//        private ProteinsType() {
//            this.proteins = null;
//        }
//
//        public ProteinsType(Protein[] proteins) {
//            this.proteins = proteins;
//        }
//
//        @XmlElement(name = "protein")
//        public Protein[] getProteins() {
//            return proteins;
//        }
//
//    }

    
}
