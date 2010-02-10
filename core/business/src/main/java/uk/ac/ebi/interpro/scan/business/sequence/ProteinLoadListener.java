package uk.ac.ebi.interpro.scan.business.sequence;

/**
 * Listener interface allowing an implementation which is
 * responsible for creating StepInstances in response to
 * Proteins being added to the database.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public interface ProteinLoadListener {

    /**
     * Implementations of this method will create StepInstances for the
     * range of proteins passed in as parameters.  If either of these
     * values are null, then no action is taken.
     * @param bottomProteinId bottom protein primary key, inclusive.
     * @param topProteinId top protein primary key, inclusive.
     */
    void createStepInstances(Long bottomProteinId, Long topProteinId);
}
