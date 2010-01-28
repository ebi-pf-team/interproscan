package uk.ac.ebi.interpro.scan.io;

import uk.ac.ebi.interpro.scan.io.AbstractResourceReader;
import uk.ac.ebi.interpro.scan.io.DomainFinderRecord;

/**
 * Writes DomainFinder3 output.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public final class DomainFinderResourceWriter extends AbstractResourceWriter<DomainFinderRecord> {

    @Override protected String createLine(DomainFinderRecord record) {
        return DomainFinderRecord.toLine(record);
    }
}