package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import java.util.*;

/**
 * <a href="http://hmmer.janelia.org/">HMMER 3</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
public abstract class Hmmer3RawSite extends RawSite {

    protected Hmmer3RawSite() {
    }

    public Hmmer3RawSite(String sequenceIdentifier, String modelId, String title, String residues,
                            SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(sequenceIdentifier, modelId, title, residues, signatureLibrary, signatureLibraryRelease);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Hmmer3RawSite))
            return false;
        final Hmmer3RawSite m = (Hmmer3RawSite) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 59)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
