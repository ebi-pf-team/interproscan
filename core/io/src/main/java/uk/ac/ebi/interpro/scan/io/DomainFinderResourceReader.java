package uk.ac.ebi.interpro.scan.io;

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