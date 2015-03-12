package uk.ac.ebi.interpro.scan.business.filter;

import java.util.Set;

/**
 * For methods that have no post-processing, this filter
 * can be used as a simple solution to provide a
 * filter that all candidates pass.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AllPassRawMatchFilter implements RawMatchFilter {
    /**
     * Returns the same set of raw matches that are passed in.
     *
     * @param rawProteins Raw matches
     * @return Filtered matches - identical to the Raw matches passed in..
     */
    @Override
    public Set filter(Set rawProteins) {
        return rawProteins;
    }
}
