package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;

/**
 * Data access object implementation for Gene3D.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class Gene3dHmmer3RawMatchDAOImpl extends RawMatchDAOImpl<Gene3dHmmer3RawMatch> {

    public Gene3dHmmer3RawMatchDAOImpl() {
        super(Gene3dHmmer3RawMatch.class);
    }

}