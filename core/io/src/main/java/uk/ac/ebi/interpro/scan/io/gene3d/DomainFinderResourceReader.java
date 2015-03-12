package uk.ac.ebi.interpro.scan.io.gene3d;

import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;

/**
 * Parser for DomainFinder3 output.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class DomainFinderResourceReader extends AbstractResourceReader<DomainFinderRecord> {

    @Override protected DomainFinderRecord createRecord(String line) {
        return DomainFinderRecord.valueOf(line);
    }
    
}