package uk.ac.ebi.interpro.scan.business.sequence;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Xref;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.io.Serializable;

/**
 * This abstract class knows how to store protein sequences and cross references
 *
 * This must be a system-wide Singleton - achieved by ONLY injecting into the
 * SerialWorker JVM, from Spring.
 * User: phil
 * Date: 14-Nov-2009
 * Time: 14:04:59
 */
public class ProteinLoader implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(ProteinLoader.class);

    private PrecalculatedProteinLookup proteinLookup;

    private ProteinDAO proteinDAO;

    private int proteinInsertBatchSize;

    private Set<Protein> proteinsAwaitingPersistence;

    private Set<Protein> precalculatedProteins=new HashSet<Protein>();

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
            Protein precalculatedProtein=proteinLookup!=null?proteinLookup.getPrecalculated(protein):null;
            if (precalculatedProtein!=null) precalculatedProteins.add(precalculatedProtein);
            else addProteinToBatch(protein);
        }

    }

    private void addProteinToBatch(Protein protein) {
        proteinsAwaitingPersistence.add (protein);
        if (proteinsAwaitingPersistence.size() == proteinInsertBatchSize) persistBatch();
    }

    private void persistBatch(){
        if (proteinsAwaitingPersistence.size() > 0){
            final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(proteinsAwaitingPersistence);
            bottomProteinId = persistedProteins.updateBottomProteinId(bottomProteinId);
            topProteinId = persistedProteins.updateTopProteinId(topProteinId);
            proteinsAwaitingPersistence.clear();
        }
    }

    public void persist(ProteinLoadListener proteinLoadListener){
        persistBatch();

        Long bottomNewProteinId=bottomProteinId;
        Long topNewProteinId=topProteinId;

        resetBounds();

        for (Protein precalculatedProtein : precalculatedProteins) addProteinToBatch(precalculatedProtein);

        persistBatch();

        Long bottomPrecalcProteinId=bottomProteinId;
        Long topPrecalcProteinId=topProteinId;


        proteinLoadListener.proteinsLoaded(bottomNewProteinId,topNewProteinId,bottomPrecalcProteinId,topPrecalcProteinId);

        resetBounds();
    }

    private void resetBounds() {
        bottomProteinId = null;
        topProteinId = null;
    }



}
