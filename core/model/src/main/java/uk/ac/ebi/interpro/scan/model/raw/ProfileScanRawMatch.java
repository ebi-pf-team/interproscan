package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * <a href="http://myhits.isb-sib.ch/cgi-bin/motif_scan">ProfileScan</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
//@Table(name="profile_scan_raw_match")
public abstract class ProfileScanRawMatch extends RawMatch {

    @Column
    private double score; // location.score

    protected ProfileScanRawMatch() { }

    protected ProfileScanRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryName, String signatureLibraryRelease,
                                  int locationStart, int locationEnd,
                                  double score) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd);
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }
    
}
