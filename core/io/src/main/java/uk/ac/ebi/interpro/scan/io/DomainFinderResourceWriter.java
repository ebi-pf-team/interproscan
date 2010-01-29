package uk.ac.ebi.interpro.scan.io;

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