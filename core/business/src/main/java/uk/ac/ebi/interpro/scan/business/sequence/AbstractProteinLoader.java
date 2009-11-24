package uk.ac.ebi.interpro.scan.business.sequence;

import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Xref;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

/**
 * This abstract class knows how to store protein sequences and cross references
 * User: phil
 * Date: 14-Nov-2009
 * Time: 14:04:59
 */
public class AbstractProteinLoader implements Serializable {

    private ProteinDAO proteinDAO;

    private int transactionProteinCount;

    private List<Protein> proteinsAwaitingPersistence;



    @Required
    public void setTransactionProteinCount(int transactionProteinCount) {
        this.transactionProteinCount = transactionProteinCount;
        proteinsAwaitingPersistence = new ArrayList<Protein>(transactionProteinCount);
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
    public List<Protein> store(String sequence, String... crossReferences) {
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
            return persist();
        }
        else{
            return null;
        }
    }

    public List<Protein> persist(){
        List<Protein> persistedProteins = Collections.emptyList();
        if (proteinsAwaitingPersistence.size() > 0){
            persistedProteins = proteinDAO.insertOrUpdate(proteinsAwaitingPersistence);
            proteinsAwaitingPersistence.clear();
        }
        return persistedProteins;
    }
}
