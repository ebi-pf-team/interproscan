package uk.ac.ebi.interpro.scan.model.raw;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/sprot/hamap/">HAMAP</a> raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="hamap_raw_match")
public class HamapRawMatch extends ProfileScanRawMatch {

    protected HamapRawMatch() { }

    public HamapRawMatch(String sequenceIdentifier, String model,
                         String signatureLibraryName, String signatureLibraryRelease,
                         int locationStart, int locationEnd,
                         double score, String generator) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease, locationStart, locationEnd, score, generator);
    }
}