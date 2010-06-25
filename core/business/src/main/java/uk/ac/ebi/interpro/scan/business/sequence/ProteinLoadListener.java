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
     * Proteins have been loaded. These are divided into new proteins (where computations need to be performed) and
     * precalculated proteins (where computations do not need to be performed).
     *
     * These ranges should have no gap between them.
     *
     * @param bottomNewProteinId bottom protein primary key of new proteins, inclusive.
     * @param topNewProteinId top protein primary key of new proteins, inclusive.
     * @param bottomPrecalculatedProteinId bottom protein primary key of precalculated proteins, inclusive.
     * @param topPrecalculatedProteinId top protein primary key of precalculated proteins, inclusive.
     */
    void proteinsLoaded(Long bottomNewProteinId, Long topNewProteinId,Long bottomPrecalculatedProteinId, Long topPrecalculatedProteinId);
}
