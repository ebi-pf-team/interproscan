package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/sprot/hamap/">HAMAP</a> raw match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name = "hamap_raw_match")
public class HamapRawMatch extends ProfileScanRawMatch {

    protected HamapRawMatch() {
    }

    public HamapRawMatch(String sequenceIdentifier, String model,
                         String signatureLibraryRelease,
                         int locationStart, int locationEnd, String cigarAlignment, double score) {
        super(sequenceIdentifier, model, SignatureLibrary.HAMAP, signatureLibraryRelease,
                locationStart, locationEnd, cigarAlignment, score);
    }
}
