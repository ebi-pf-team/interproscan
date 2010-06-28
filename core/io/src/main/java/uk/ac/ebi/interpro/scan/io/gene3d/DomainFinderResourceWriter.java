package uk.ac.ebi.interpro.scan.io.gene3d;

import uk.ac.ebi.interpro.scan.io.AbstractResourceWriter;

import java.util.*;

/**
 * Writes DomainFinder3 output.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class DomainFinderResourceWriter extends AbstractResourceWriter<DomainFinderRecord> {

    @Override protected String createLine(DomainFinderRecord record) {
        return DomainFinderRecord.toLine(record);
    }

    /**
     * Returns records sorted by sequence e-value, then model ID, and then by sequence start if model hits sequence more than once.
     *
     * @param  records Collection to sort
     * @return Records sorted by sequence e-value, then model ID, and then by sequence start if model hits sequence more than once.
     */
    @Override protected Collection<DomainFinderRecord> sort(Collection<DomainFinderRecord> records) {
        List<DomainFinderRecord> list = new ArrayList<DomainFinderRecord>(records);
        Collections.sort(list, new Comparator<DomainFinderRecord>() {
            public int compare(DomainFinderRecord record1, DomainFinderRecord record2) {
                // Sort by sequence identifier.
                int c = record1.getSequenceId().compareTo(record2.getSequenceId());
                // Sort by sequence e-value
                if (c == 0) {
                    c = record1.getSequenceEvalue().compareTo(record2.getSequenceEvalue());
                }
                // If e-values are the same, sort by model ID (alphabetically: a to z)
                if (c == 0) {
                    c = record1.getModelId().compareTo(record2.getModelId());
                }
                // If model IDs are the same, sort by sequence start
                if (c == 0) {
                    return record1.getSequenceStart().compareTo(record2.getSequenceStart());
                }
                else    {
                    return c;
                }
            }
        }
        );
        return list;
    }

}