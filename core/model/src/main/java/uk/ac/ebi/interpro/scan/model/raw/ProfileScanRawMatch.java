package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * <a href="http://myhits.isb-sib.ch/cgi-bin/motif_scan">ProfileScan</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
//@Table(name="profile_scan_raw_match")
public abstract class ProfileScanRawMatch extends PfScanRawMatch {

    protected ProfileScanRawMatch() {
    }

    @Column(name = "raw_score")
    private double rawScore;

    @Column(name = "profile_level")
    private byte level;

    protected ProfileScanRawMatch(String sequenceIdentifier, String model,
                                  SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                                  int locationStart, int locationEnd,
                                  String cigarAlignment, double rawScore) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd, cigarAlignment);
        setRawScore(rawScore);
    }

    public double getRawScore() {
        return rawScore;
    }

    private void setRawScore(double rawScore) {
        this.rawScore = rawScore;
    }
}
