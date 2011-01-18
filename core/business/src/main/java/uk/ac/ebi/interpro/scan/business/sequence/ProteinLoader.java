package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This abstract class knows how to store protein sequences and cross references
 * <p/>
 * This must be a system-wide Singleton - achieved by ONLY injecting into the
 * SerialWorker JVM, from Spring.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 14:04:59
 */
public class ProteinLoader implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProteinLoader.class.getName());

    private PrecalculatedProteinLookup proteinLookup;

    private ProteinDAO proteinDAO;

    private int proteinInsertBatchSize;

    private Set<Protein> proteinsAwaitingPersistence;

    private Set<Protein> precalculatedProteins = new HashSet<Protein>();

    private Long bottomProteinId;

    private Long topProteinId;

    public void setProteinLookup(PrecalculatedProteinLookup proteinLookup) {
        this.proteinLookup = proteinLookup;
    }

    @Required
    public void setProteinInsertBatchSize(int proteinInsertBatchSize) {
        this.proteinInsertBatchSize = proteinInsertBatchSize;
        proteinsAwaitingPersistence = new HashSet<Protein>(proteinInsertBatchSize);
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }


    /**
     * This method stores sequences with (optionally) cross references.
     * The method attempts to store them in batches by calling the addProteinToBatch(Protein protein)
     * method.  This in turn calls persistBatch(), when the batch size has been reached.
     * <p/>
     * TODO - Needs to be refactored - currently does NOT store anything other than the Xref accession.
     * TODO - needs to be able to store the database name and the protein name too.
     *
     * @param sequence        being the protein sequence to store
     * @param crossReferences being a set of Cross references.
     */
    public void store(String sequence, String... crossReferences) {
        if (sequence != null && sequence.length() > 0) {
            Protein protein = new Protein(sequence);
            if (crossReferences != null) {
                for (String crossReference : crossReferences) {
                    ProteinXref xref = new ProteinXref(crossReference);
                    protein.addCrossReference(xref);
                }
            }
            Protein precalculatedProtein = (proteinLookup != null)
                    ? proteinLookup.getPrecalculated(protein)
                    : null;
            if (precalculatedProtein != null) {
                precalculatedProteins.add(precalculatedProtein);
            } else {
                addProteinToBatch(protein);
            }
        }
    }

    /**
     * Adds a protein to the batch of proteins to be persisted.  If the maximum
     * batch size is reached, store all these proteins (by calling persistBatch().)
     *
     * @param protein being the protein to be stored.
     */
    private void addProteinToBatch(Protein protein) {
        proteinsAwaitingPersistence.add(protein);
        if (proteinsAwaitingPersistence.size() == proteinInsertBatchSize) {
            persistBatch();
        }
    }

    /**
     * Persists all of the proteins in the list of proteinsAwaitingPersistence and empties
     * this Collection, ready to be used again.
     */
    private void persistBatch() {
        if (proteinsAwaitingPersistence.size() > 0) {
            final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(proteinsAwaitingPersistence);
            bottomProteinId = persistedProteins.updateBottomProteinId(bottomProteinId);
            topProteinId = persistedProteins.updateTopProteinId(topProteinId);
            proteinsAwaitingPersistence.clear();
        }
    }

    /**
     * Following persistence of proteins, calls the ProteinLoadListener with the bounds of the proteins
     * added, so analyses (StepInstance) can be created appropriately.
     *
     * @param proteinLoadListener which handles the creation of StepInstances for the new proteins added.
     */
    public void persist(ProteinLoadListener proteinLoadListener) {
        // Persist any remaining proteins (that last batch)
        persistBatch();

        // Grab hold of the lower and upper range of Protein IDs for ALL of the persisted proteins
        final Long bottomNewProteinId = bottomProteinId;
        final Long topNewProteinId = topProteinId;

        // Prepare the ProteinLoader for another set of proteins.
        resetBounds();


        // Now store the precalculated proteins (just updates - these should not be included in the
        // list of Proteins for the listener.)
        for (Protein precalculatedProtein : precalculatedProteins) {
            addProteinToBatch(precalculatedProtein);
        }

        persistBatch();

        final Long bottomPrecalcProteinId = bottomProteinId;
        final Long topPrecalcProteinId = topProteinId;


        proteinLoadListener.proteinsLoaded(bottomNewProteinId, topNewProteinId, bottomPrecalcProteinId, topPrecalcProteinId);

        // Prepare the ProteinLoader for another set of proteins.
        resetBounds();
    }

    /**
     * Helper method that sets the upper and lower bounds to null.
     */
    private void resetBounds() {
        bottomProteinId = null;
        topProteinId = null;
    }


}
