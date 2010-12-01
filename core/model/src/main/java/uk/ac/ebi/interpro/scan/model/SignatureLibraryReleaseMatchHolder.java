package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates protein matches with a signature library release.
 * Note: Not stored in database, just returned by DAO as a convenience class.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@XmlRootElement(name = "signature-library-release-matches")
@XmlType(name = "SignatureLibraryReleaseMatchHolderType", propOrder = {"signatureLibraryRelease", "proteins"})
public final class SignatureLibraryReleaseMatchHolder implements Serializable {

    private SignatureLibraryRelease signatureLibraryRelease;
    private final Set<Protein> proteins = new HashSet<Protein>();

    private SignatureLibraryReleaseMatchHolder() {
        this.signatureLibraryRelease = null;
    }

    public SignatureLibraryReleaseMatchHolder(SignatureLibraryRelease signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @XmlElement(name="signature-library-release")
    public SignatureLibraryRelease getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    void setSignatureLibraryRelease(SignatureLibraryRelease release) {
        this.signatureLibraryRelease = release;
    }

    public void addProtein(Protein protein) {
        proteins.add(protein);
    }   

    @XmlElement(name = "protein")
    public Set<Protein> getProteins() {
        return proteins;
    }

    void setProteins(Set<Protein> proteins) {
        if (proteins == null) {
            throw new IllegalArgumentException("'Proteins' must not be null");
        }
        for (Protein protein : proteins) {
            addProtein(protein);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SignatureLibraryReleaseMatchHolder))
            return false;
        final SignatureLibraryReleaseMatchHolder s = (SignatureLibraryReleaseMatchHolder) o;
        return new EqualsBuilder()
                .append(signatureLibraryRelease, s.signatureLibraryRelease)
                .append(proteins, s.proteins)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(71, 53)
                .append(signatureLibraryRelease)
                .append(proteins)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
