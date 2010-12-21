package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://pfam.sanger.ac.uk/">Pfam</a> raw match.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
@Entity
@Table(name="pfam_hmmer3_raw_match")
public class PfamHmmer3RawMatch extends Hmmer3RawMatch implements Comparable<PfamHmmer3RawMatch> {

    protected PfamHmmer3RawMatch() { }    

    public PfamHmmer3RawMatch(String sequenceIdentifier, String model,
                              SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                              int locationStart, int locationEnd,
                              double evalue, double score,
                              int hmmStart, int hmmEnd, String hmmBounds,
                              double locationScore,
                              int envelopeStart, int envelopeEnd,
                              double expectedAccuracy, double fullSequenceBias,
                              double domainCeValue, double domainIeValue, double domainBias) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd,
              evalue, score, hmmStart, hmmEnd, hmmBounds, locationScore, envelopeStart, envelopeEnd, expectedAccuracy, fullSequenceBias, domainCeValue, domainIeValue, domainBias);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than , equal to, or greater than that object.
     *
     * The required ordering for Pfam A post processing is:
     *
     * domain I evalue ASC, Score DESC
     *
     * @param that being the PfamHmmer3RawMatch to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(PfamHmmer3RawMatch that) {
        if (this == that) return 0;
        if (this.getDomainIeValue() < that.getDomainIeValue()) return -1;     // First, sort by ievalue ASC
        if (this.getDomainIeValue() > that.getDomainIeValue()) return 1;
        if (this.getScore() > that.getScore()) return -1;                     // then by score ASC
        if (this.getScore() < that.getScore()) return 1;
        if (this.hashCode() > that.hashCode()) return -1;                     // then by hashcode to be consistent with equals.
        if (this.hashCode() < that.hashCode()) return 1;
        return 0;
    }
}
