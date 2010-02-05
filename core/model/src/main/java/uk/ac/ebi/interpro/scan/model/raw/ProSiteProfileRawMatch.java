package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.expasy.ch/prosite/">PROSITE</a> Profile raw match.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name="prosite_profile_raw_match")
public class ProSiteProfileRawMatch extends ProfileScanRawMatch {

    protected ProSiteProfileRawMatch() { }

    public ProSiteProfileRawMatch(String sequenceIdentifier, String model,
                                  String signatureLibraryRelease,
                                  int locationStart, int locationEnd, double score) {
        super(sequenceIdentifier, model, SignatureLibrary.PROSITE_PROFILES, signatureLibraryRelease,
              locationStart, locationEnd, score);
    }
}