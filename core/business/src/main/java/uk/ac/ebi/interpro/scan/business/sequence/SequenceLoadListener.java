package uk.ac.ebi.interpro.scan.business.sequence;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener interface allowing an implementation which is
 * responsible for creating StepInstances in response to
 * sequences being added to the database.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public interface SequenceLoadListener {


    /**
     * sequences have been loaded. These are divided into new sequences (where computations need to be performed) and
     * precalculated sequences (where computations do not need to be performed).
     * <p/>
     * These ranges should have no gap between them.
     *
     * @param bottomNewSequenceId           bottom sequence primary key of new sequences, inclusive.
     * @param topNewSequenceId              top sequence primary key of new sequences, inclusive.
     * @param bottomPrecalculatedSequenceId bottom sequence primary key of precalculated sequences, inclusive.
     * @param topPrecalculatedSequenceId    top sequence primary key of precalculated sequences, inclusive.
     */
    @Transactional
    void sequencesLoaded(Long bottomNewSequenceId, Long topNewSequenceId, Long bottomPrecalculatedSequenceId, Long topPrecalculatedSequenceId, boolean useMatchLookupService, List<Long> idsWithoutLookupHit);
}
