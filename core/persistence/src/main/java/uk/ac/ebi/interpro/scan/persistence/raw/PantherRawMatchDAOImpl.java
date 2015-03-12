package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;

/**
 * Data access object implementation for Panther.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class PantherRawMatchDAOImpl extends RawMatchDAOImpl<PantherRawMatch> {

    public PantherRawMatchDAOImpl() {
        super(PantherRawMatch.class);
    }

}