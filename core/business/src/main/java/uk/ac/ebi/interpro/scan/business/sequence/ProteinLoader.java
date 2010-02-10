package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Xref;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.io.Serializable;

/**
 * This abstract class knows how to store protein sequences and cross references
 * TODO Change to composition from inheritance - no need to make this abstract.
 *
 * This must be a system-wide Singleton - achieved by ONLY injecting into the
 * SerialWorker JVM, from Spring.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 14:04:59
 */
public class ProteinLoader implements Serializable {

    private ProteinDAO proteinDAO;

    private int transactionProteinCount;

    private Set<Protein> proteinsAwaitingPersistence;

    private Long bottomProteinId;

    private Long topProteinId;
    
    /**
     * TODO - COULD make this into a List of listeners, if required.
     * Highly unlikely to be required however.
     *
     * This interface is implemented to allow StepInstances to be
     * created.  The implementation appears in the management module.
     */
    private ProteinLoadListener proteinLoadListener;

    @Required
    public void setProteinLoadListener(ProteinLoadListener proteinLoadListener) {
        this.proteinLoadListener = proteinLoadListener;
    }

    @Required
    public void setTransactionProteinCount(int transactionProteinCount) {
        this.transactionProteinCount = transactionProteinCount;
        proteinsAwaitingPersistence = new HashSet<Protein>(transactionProteinCount);
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }


    /**
     * This method stores sequences with (optionally) cross references.
     * @param sequence
     * @param crossReferences
     */
    public void store(String sequence, String... crossReferences) {
        if (sequence != null && sequence.length() > 0){
            Protein protein = new Protein(sequence);
            if (crossReferences != null){
                for (String crossReference : crossReferences){
                    Xref xref = new Xref(crossReference);
                    protein.addCrossReference(xref);
                }
            }
            proteinsAwaitingPersistence.add (protein);
        }
        if (proteinsAwaitingPersistence.size() == transactionProteinCount){
            persistBatch();
        }
    }

    private void persistBatch(){
        ProteinDAO.PersistedProteins persistedProteins = null;
        if (proteinsAwaitingPersistence.size() > 0){
            persistedProteins = proteinDAO.insertNewProteins(proteinsAwaitingPersistence);
            bottomProteinId = persistedProteins.updateBottomProteinId(bottomProteinId);
            topProteinId = persistedProteins.updateTopProteinId(topProteinId);
            proteinsAwaitingPersistence.clear();
        }
    }

    public void persist(){
        persistBatch();
        // Create StepInstances here...
        proteinLoadListener.createStepInstances(bottomProteinId, topProteinId);
        bottomProteinId = null;
        topProteinId = null;
    }




}
