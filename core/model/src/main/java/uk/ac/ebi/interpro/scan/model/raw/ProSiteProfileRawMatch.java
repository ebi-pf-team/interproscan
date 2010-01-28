package uk.ac.ebi.interpro.scan.model.raw;

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
                                  String signatureLibraryName, String signatureLibraryRelease,
                                  int locationStart, int locationEnd, double score) {
        super(sequenceIdentifier, model, signatureLibraryName, signatureLibraryRelease,
              locationStart, locationEnd, score);
    }
}