package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * <a href="http://myhits.isb-sib.ch/cgi-bin/motif_scan">ProfileScan</a> raw match.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
@Entity
//@Table(name="profile_scan_raw_match")
public abstract class ProfileScanRawMatch extends RawMatch {

    @Column
    private double score; // location.score

    protected ProfileScanRawMatch() { }

    /* TODO - Perhaps don't need to pass in SignatureLIbrary?  Just set to SignatureLibrary.PROSITE_PROFILES ? */
    protected ProfileScanRawMatch(String sequenceIdentifier, String model,
                                  SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                                  int locationStart, int locationEnd,
                                  double score) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }
    
}
