package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <a href="http://www.pantherdb.org/">PANTHER</a> raw match.
 *
 * @author Antony Quinn
 * @version $Id$
 */
@Entity
@Table(name = "panther_raw_match")
public class PantherRawMatch extends RawMatch {

    protected PantherRawMatch() {
    }

    protected PantherRawMatch(String sequenceIdentifier, String model,
                              String signatureLibraryRelease,
                              int locationStart, int locationEnd) {
        super(sequenceIdentifier, model, SignatureLibrary.PANTHER, signatureLibraryRelease, locationStart, locationEnd);
    }

}