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
     *
     * @param bottomProteinId bottom protein primary key, inclusive.
     * @param topProteinId    top protein primary key, inclusive.
     */
    @Override
    public void createStepInstances(Long bottomProteinId, Long topProteinId) {
        // No-op.
    }
}
