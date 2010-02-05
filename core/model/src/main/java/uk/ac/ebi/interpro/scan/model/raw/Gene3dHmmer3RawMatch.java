package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * <a href="http://gene3d.biochem.ucl.ac.uk/Gene3D/">Gene3D</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="gene3d_hmmer3_raw_match")
public class Gene3dHmmer3RawMatch extends Hmmer3RawMatch {

    // Sequence alignment in CIGAR format
    private String cigarAlignment;

    protected Gene3dHmmer3RawMatch() { }

    public Gene3dHmmer3RawMatch(String sequenceIdentifier, String model,
                                String signatureLibraryRelease,
                                int locationStart, int locationEnd,
                                double evalue, double score,
                                int hmmStart, int hmmEnd, String hmmBounds,
                                double locationScore,
                                int envelopeStart, int envelopeEnd,
                                double expectedAccuracy, double fullSequenceBias,
                                double domainCeValue, double domainIeValue, double domainBias,
                                String cigarAlignment) {
        super(sequenceIdentifier, model, SignatureLibrary.GENE3D, signatureLibraryRelease, locationStart, locationEnd,
              evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy,
              fullSequenceBias, domainCeValue, domainIeValue, domainBias);
        this.cigarAlignment = cigarAlignment;
    }

    public String getCigarAlignment() {
        return cigarAlignment;
    }

    private void setCigarAlignment(String cigarAlignment) {
        this.cigarAlignment = cigarAlignment;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Gene3dHmmer3RawMatch))
            return false;
        final Gene3dHmmer3RawMatch m = (Gene3dHmmer3RawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(cigarAlignment, m.cigarAlignment)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(53, 61)
                .appendSuper(super.hashCode())
                .append(cigarAlignment)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}