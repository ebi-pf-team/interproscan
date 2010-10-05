package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;

/**
 * No-op implementation of the ProteinLoadListener - does nothing
 * when the proteins are loaded.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class NoopProteinLoadListener implements ProteinLoadListener {

    /**
     * Implementations of this method will create StepInstances for the
     * range of proteins passed in as parameters.  If either of these
     * values are null, then no action is taken.
     */

    @Override
    public void proteinsLoaded(Long bottomNewProteinId, Long topNewProteinId, Long bottomPrecalculatedProteinId, Long topPrecalculatedProteinId) {
        //no-op
    }
}
