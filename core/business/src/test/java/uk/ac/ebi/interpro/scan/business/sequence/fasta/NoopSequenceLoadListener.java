package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;

import java.util.List;

/**
 * No-op implementation of the SequenceLoadListener - does nothing
 * when the proteins are loaded.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class NoopSequenceLoadListener implements SequenceLoadListener {

    /**
     * Implementations of this method will create StepInstances for the
     * range of proteins passed in as parameters.  If either of these
     * values are null, then no action is taken.
     */


    public void sequencesLoaded(Long bottomNewSequenceId, Long topNewSequenceId, Long bottomPrecalculatedSequenceId, Long topPrecalculatedSequenceId) {
        //no-op
    }

    @Override
    public void sequencesLoaded(final Long bottomNewSequenceId, final Long topNewSequenceId,
                                final Long bottomPrecalculatedSequenceId, final Long topPrecalculatedSequenceId, boolean useMatchLookupService, List<Long> idsWithoutLookupHit) {
        //no-op
    }
}
