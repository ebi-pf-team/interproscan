package uk.ac.ebi.interpro.scan.business.sequence.fasta;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Gift Nuka
 * @date 9.01.2019
 */
public class FinaliseInitialSetupTasks {

    private static final Logger LOGGER = Logger.getLogger(FinaliseInitialSetupTasks.class.getName());

    private SequenceLoader<Protein> sequenceLoader;

    private ProteinDAO proteinDAO;


    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setSequenceLoader(SequenceLoader sequenceLoader) {
        this.sequenceLoader = sequenceLoader;
    }

     @Transactional
    public void execute(SequenceLoadListener sequenceLoaderListener, Map<String, SignatureLibraryRelease> analysisJobMap, boolean useMatchLookupService) {
        sequenceLoader.setDisplayLookupMessage(false);
        sequenceLoader.setUseMatchLookupService(useMatchLookupService);
        LOGGER.debug("Entered FinaliseInitialSetupTasks execute method");
        Utilities.verboseLog(1100, "Entered FinaliseInitialSetupTasks execute method");

//        for (Protein protein : parsedMolecules) {
//            LOGGER.debug("protein to persist: " + protein.getMd5() + " : " + protein.toString());
//            proteinDAO.insert(protein.getMd5(), protein);
//        }
//        Utilities.verboseLog(1100, "Stored parsed sequences into KVDB: " + levelDBStoreName);
        //Load in the h2DB as well
        //final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(parsedMolecules);
//        Long bottomProteinId = persistedProteins.updateBottomProteinId(null);
//        Long topProteinId = persistedProteins.updateTopProteinId(null);

        Long bottomProteinId = 1l;
        Long topProteinId = proteinDAO.getMaximumPrimaryKey();

        Long bottomPrecalculatedSequenceId = null;
        Long topPrecalculatedSequenceId = null;
        Utilities.verboseLog(1100, "FinaliseInitialSetupTasks: topProteinId: " + topProteinId + " bottomProteinId: " + bottomProteinId);

        //sequenceLoader.storeAll(parsedMolecules, analysisJobMap);
        //Utilities.verboseLog(1100, "Store parsed sequences (processed lookup): " + parsedMolecules.size());

        Map<Long, Protein> proteinIdsWithoutLookupHit = new HashMap<>();

        if(useMatchLookupService) {
            Utilities.verboseLog(1100, "FinaliseInitialSetupTasks .. useMatchLookupService: " + useMatchLookupService);
            Set<Protein> proteinsWithoutLookupHit = proteinDAO.getProteinsWithoutLookupHit();
            for (Protein protein: proteinsWithoutLookupHit){
                proteinIdsWithoutLookupHit.put(protein.getId(), protein);
            }
            proteinDAO.setProteinIdsWithoutLookupHit(proteinIdsWithoutLookupHit);
        }else{
            Utilities.verboseLog(1100, "FinaliseInitialSetupTasks dont use lookup .. useMatchLookupService: " + useMatchLookupService);
        }

        //TODO this is for testing, we need to revisit this approach later
        // we still need some kind of control when we have proteins in the lookup service ??

        List<Protein> proteinsNotInLookup = null;
        try {
            proteinsNotInLookup = proteinDAO.getProteinsNotInLookup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int proteinsNotInLookupCount = proteinsNotInLookup.size();

        ArrayList<Long> idsWithoutLookupHit = null;
        //TODO resolve this var proteinsWithoutLookupHit
        if (proteinsNotInLookupCount > 0){
            idsWithoutLookupHit = new ArrayList<>();
            for (Protein protein: proteinsNotInLookup) {
                idsWithoutLookupHit.add(protein.getId());
            }
            Collections.sort(idsWithoutLookupHit);
            Utilities.verboseLog(1100, "FinaliseInitialSetupTasks ...  proteinsNotInLookupCount : " + proteinsNotInLookupCount);
        }else{
            Utilities.verboseLog(1100, "FinaliseInitialSetupTasks ...  proteinsNotInLookup is NULL : " );
        }


        sequenceLoaderListener.sequencesLoaded(bottomProteinId, topProteinId, bottomPrecalculatedSequenceId, topPrecalculatedSequenceId, useMatchLookupService, idsWithoutLookupHit);
        //sequenceLoader.persist(sequenceLoaderListener, analysisJobMap);
        Utilities.verboseLog(1100, "FinaliseInitialSetupTasks ...  completed");
        Utilities.verboseLog(1100, "FinaliseInitialSetupTasks ...  completed");

    }


    private boolean findReasonableSignalPSequenceCounts(int sliceSize, long topProteinId){
        int count = 0;
        long bottomProteinId = 1;

        int defaultSliceSize = 8000;

        for (long bottom = bottomProteinId; bottom <= topProteinId; bottom += sliceSize) {
            final long top = Math.min(topProteinId, bottom + sliceSize - 1);

            for (long proteinId = bottom; proteinId <= top; proteinId++) {
                Protein proteinNotInLookup = proteinDAO.getProteinNotInLookup(Long.toString(proteinId));
                if (proteinNotInLookup != null) {
                    count++;
                }
            }
        }

        return true;

    }


}
//TODO edit
