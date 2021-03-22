package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//import org.eclipse.jetty.util.ConcurrentHashSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.business.sequence.BerkeleyPrecalculatedProteinLookup;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupStoreToI5ModelDAO;
import uk.ac.ebi.interpro.scan.precalc.client.MatchHttpClient;
import uk.ac.ebi.interpro.scan.util.Utilities;

//import javax.rmi.CORBA.Util;

import java.util.*;

/**
 * Lookup match service step
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class MatchLookupStep extends Step implements StepInstanceCreatingStep {

    private static final Logger LOGGER = LogManager.getLogger(MatchLookupStep.class.getName());


    protected Jobs jobs;
    protected StepInstanceDAO stepInstanceDAO;

    private ProteinDAO proteinDAO;

    private MatchHttpClient preCalcMatchClient;

    private LookupStoreToI5ModelDAO lookupStoreToI5ModelDAO;

    private Map<String, SignatureLibraryRelease> analysisJobMap;

    private String interproscanVersion;

    private int proteinInsertBatchSize = 200;

    private int proteinInsertBatchSizeNoLookup;

    private int proteinPrecalcLookupBatchSize = 100;

    final private Set<Protein> proteinsAwaitingPrecalcLookup = ConcurrentHashMap.newKeySet(); //new ConcurrentHashSet<>();

    final private Set<Protein> proteinsAwaitingPersistence = ConcurrentHashMap.newKeySet(); // new ConcurrentHashSet<>();

    private Long bottomProteinId;

    private Long topProteinId;

    private boolean isGetOrfOutput;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public void setPreCalcMatchClient(MatchHttpClient preCalcMatchClient) {
        this.preCalcMatchClient = preCalcMatchClient;
    }

    public void setLookupStoreToI5ModelDAO(LookupStoreToI5ModelDAO lookupStoreToI5ModelDAO) {
        this.lookupStoreToI5ModelDAO = lookupStoreToI5ModelDAO;
    }

    public void setProteinInsertBatchSize(int proteinInsertBatchSize) {
        this.proteinInsertBatchSize = proteinInsertBatchSize;
    }

    public void setProteinInsertBatchSizeNoLookup(int proteinInsertBatchSizeNoLookup) {
        this.proteinInsertBatchSizeNoLookup = proteinInsertBatchSizeNoLookup;
    }

    public void setProteinPrecalcLookupBatchSize(int proteinPrecalcLookupBatchSize) {
        this.proteinPrecalcLookupBatchSize = proteinPrecalcLookupBatchSize;
    }

    public void setInterproscanVersion(String interproscanVersion) {
        this.interproscanVersion = interproscanVersion;
    }


    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        long bottomProtein = stepInstance.getBottomProtein();
        long topProtein =        stepInstance.getTopProtein();
        String proteinRange = "[" + bottomProtein + "-" + topProtein + "]";
        Map <String, Long> proteinRanges = new HashMap<>();
        proteinRanges.put("bottom", bottomProtein);
        proteinRanges.put("top", topProtein);

        Utilities.verboseLog(110, " Match Lookup Step  [" + bottomProtein + "-" + topProtein + "] - starting ");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Match Lookup Step (" + bottomProtein + "-" + topProtein + ")");
        }

        // get the analysisJob Map, this looks like the thrid time we are doing this so refactor and have a function call instead??
        Map<String, SignatureLibraryRelease> analysisJobMap = new HashMap<>();
        String analysisJobNames = stepInstance.getParameters().get(ANALYSIS_JOB_NAMES_KEY);
        Jobs analysisJobs;
        if (analysisJobNames == null) {
            analysisJobs = jobs.getActiveAnalysisJobs();
            analysisJobs = jobs.getActiveNonDeprecatedAnalysisJobs();
            List<String> analysisJobIdList = analysisJobs.getJobIdList();
            StringBuilder analysisJobNamesBuilder = new StringBuilder();
            for (String jobName : analysisJobIdList) {
                if (analysisJobNamesBuilder.length() > 0) {
                    analysisJobNamesBuilder.append(',');
                }
                analysisJobNamesBuilder.append(jobName);
            }
            analysisJobNames = analysisJobNamesBuilder.toString();
        } else {
            analysisJobs = jobs.subset(StringUtils.commaDelimitedListToStringArray(analysisJobNames));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("analysisJobs: " + analysisJobs);
            LOGGER.debug("analysisJobNames: " + analysisJobNames);
        }
        for (Job analysisJob : analysisJobs.getJobList()){
            SignatureLibraryRelease signatureLibraryRelease = analysisJob.getLibraryRelease();
            if(signatureLibraryRelease != null) {
                //TODO - should the name always be in upppercase
                analysisJobMap.put(signatureLibraryRelease.getLibrary().getName().toUpperCase(), signatureLibraryRelease);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Name: " + signatureLibraryRelease.getLibrary().getName() + " version: " + signatureLibraryRelease.getVersion() + " name: " + signatureLibraryRelease.getLibrary().getName());
                }
                Utilities.verboseLog(120, "Name: " + signatureLibraryRelease.getLibrary().getName() + " version: " + signatureLibraryRelease.getVersion());
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("analysisJobMap:" + analysisJobMap);
        }

        String analysesPrintOutStr = Utilities.getTimeNow() + " Running the following analyses:\n";
        String analysesDisplayStr = Utilities.getTimeNow() + " Running the following analyses:\n";
        //System.out.println(analysesPrintOutStr + Arrays.asList(analysisJobNames));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(analysesPrintOutStr + Arrays.asList(analysisJobNames));
        }
        StringBuilder analysesToRun = new StringBuilder();

//                StringBuilder analysesToDisplay = new StringBuilder();
        StringJoiner analysesToDisplay = new StringJoiner(",");

        //sort the keys
        List<String> analysisJobMapKeySet = new ArrayList(analysisJobMap.keySet());
        Collections.sort(analysisJobMapKeySet);

        for (String key: analysisJobMapKeySet){
            analysesToRun.append(analysisJobMap.get(key).getLibrary().getName() + "-" + analysisJobMap.get(key));
            analysesToDisplay.add(String.join("-", analysisJobMap.get(key).getLibrary().getName(),
                    analysisJobMap.get(key).getVersion()));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(analysesPrintOutStr + Collections.singletonList(analysisJobNames));
            LOGGER.debug(analysesDisplayStr + analysesToDisplay.toString());
        }

        //System.out.println(analysesDisplayStr + "[" + analysesToDisplay.toString() +"]");

        boolean useMatchLookupService = true;
        if (stepInstance.getParameters().containsKey(USE_MATCH_LOOKUP_SERVICE)) {
            useMatchLookupService = Boolean.parseBoolean(stepInstance.getParameters().get(USE_MATCH_LOOKUP_SERVICE));
        }
        final Set<Protein> localPrecalculatedProteinsTest = ConcurrentHashMap.newKeySet(); //new ConcurrentHashSet<>();
        if(useMatchLookupService){
            final List<Protein> proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            //            final PrecalculatedProteinLookup precalculatedProteinLookup ;


            final BerkeleyPrecalculatedProteinLookup precalculatedProteinLookup =
                    new BerkeleyPrecalculatedProteinLookup(proteins,
                            proteinRanges, proteinDAO, lookupStoreToI5ModelDAO, preCalcMatchClient);
            precalculatedProteinLookup.setAnalysisJobMap(analysisJobMap);
            precalculatedProteinLookup.setInterproscanVersion(interproscanVersion);
            //TODO the following to be treated as comment
            //precalculatedProteinLookup.setProteinsWithoutLookupHit(proteinsWithoutLookupHit);

            Thread lookupThread = new Thread(precalculatedProteinLookup);

            Utilities.verboseLog(110, "Starting Thread... precalculatedProteinLookup - " + precalculatedProteinLookup.hashCode());

            try {
                lookupThread.start();
                lookupThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*
        if(useMatchLookupService){
            try {
                final List<Protein> proteins = proteinDAO.getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());

                //proteinsAwaitingPrecalcLookup = new ConcurrentHashSet<>();
                //proteinsAwaitingPersistence = new ConcurrentHashSet<>();
                int count = 0;
                int batchCount = 0;
                Utilities.verboseLog(1100, "1. Protein batch "  + proteinRange + "  size: " + proteins.size());
                for (Protein protein : proteins) {
                    count++;
                    if (proteinsAwaitingPrecalcLookup == null){
                        Utilities.verboseLog(1100, "proteinsAwaitingPrecalcLookup is null -- " + proteinsAwaitingPrecalcLookup);
                    }
                    proteinsAwaitingPrecalcLookup.add(protein);
                    if (proteinsAwaitingPrecalcLookup.size() > proteinPrecalcLookupBatchSize) {
                        batchCount++;
                        Utilities.verboseLog(proteinRange + " lookup up protein batch no. " + batchCount);
                        lookupProteins(stepInstance, analysisJobMap);
                        precalculatedProteins.addAll(proteinsAwaitingPrecalcLookup);
                        localPrecalculatedProteinsTest.addAll(proteinsAwaitingPrecalcLookup);
                        proteinsAwaitingPrecalcLookup.clear();
                    }
                    protein.getMd5();
                    protein.getId();
                    //lookup the md5s
                }
                Thread.sleep(10 * 1000);
                Utilities.verboseLog(1100, "1. Precalculated Proteins "  + proteinRange + "  size: " + precalculatedProteins.size());
                Utilities.verboseLog(1100, "1. Local Precalculated Proteins "  + proteinRange + "  size: " + localPrecalculatedProteinsTest.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" Match Lookup Step - done" + "  + proteinRange + " );
        }
        System.out.println("The hashcode for this Instance [" + bottomProtein + "-" + topProtein + "]:" + hashCode());

        Utilities.verboseLog(110, "2. Precalculated Proteins "  + proteinRange + "  size: " + precalculatedProteins.size());
        Utilities.verboseLog(110, "2. Local Precalculated Proteins Test "  + proteinRange + "  size: " + localPrecalculatedProteinsTest.size());
         */
        try {
            List<Protein> proteinsNotInLookup = proteinDAO.getProteinsNotInLookup();
            int proteinsNotInLookupCount = proteinsNotInLookup.size();
            Utilities.verboseLog(110, "1. ProteinsNotInLookupCount :  "  + proteinsNotInLookupCount);
        } catch (Exception e) {
            e.printStackTrace();
        }



        Utilities.verboseLog(110, " Match Lookup Step  "  + proteinRange + "  - done");
    }


    /**
     *
     * @param analysisJobMap
     */
    /*
    private void lookupProteins(StepInstance stepInstance, Map<String, SignatureLibraryRelease> analysisJobMap) {
        if (proteinsAwaitingPrecalcLookup.size() > 0) {
            final boolean usingLookupService = precalculatedProteinLookup != null;
            if (! usingLookupService){
                proteinInsertBatchSize = proteinInsertBatchSizeNoLookup;
            }


            final Set<Protein> localPrecalculatedProteins = (usingLookupService)
                    ? precalculatedProteinLookup.getPrecalculated(proteinsAwaitingPrecalcLookup, analysisJobMap)
                    : null;


            //Set<Protein> localPrecalculatedProteins = proteinsAwaitingPrecalcLookup;

//            if(proteinLookup.isAnalysisVersionConsistent(analysisJobMap)){
//
//            }

            // Put precalculated proteins into a Map of MD5 to Protein;
            if (localPrecalculatedProteins != null) {
                Utilities.verboseLog(1100, "We have precalculated proteins: " +  localPrecalculatedProteins.size());
                final Map<String, Protein> md5ToPrecalcProtein = new HashMap<>(localPrecalculatedProteins.size());
                for (Protein precalc : localPrecalculatedProteins) {
                    md5ToPrecalcProtein.put(precalc.getMd5(), precalc);
                }

                for (Protein protein : proteinsAwaitingPrecalcLookup) {
                    if (md5ToPrecalcProtein.keySet().contains(protein.getMd5())) {
                        precalculatedProteins.add(md5ToPrecalcProtein.get(protein.getMd5()));
                    } else {
                        addProteinToBatch(protein);
                    }
                }
            } else {
                //there are no matches or we are not using the lookup match service
                Utilities.verboseLog(1100, "There are NO matches for these proteins: " +  proteinsAwaitingPrecalcLookup.size());
                for (Protein protein : proteinsAwaitingPrecalcLookup) {
                    addProteinToBatch(protein);
                }
            }
            // All dealt with, so clear.
            proteinsAwaitingPrecalcLookup.clear();
        }
    }

*/

    /**
     * Adds a protein to the batch of proteins to be persisted.  If the maximum
     * batch size is reached, store all these proteins (by calling persistBatch().)
     *
     * @param protein being the protein to be stored.
     */
    private void addProteinToBatch(Protein protein) {
        proteinsAwaitingPersistence.add(protein);

        if (proteinsAwaitingPersistence.size() == proteinInsertBatchSize) {
            Utilities.verboseLog(1100, "ProteinInsertBatchSize " + proteinInsertBatchSize);

            persistBatch();
        }
    }

    /**
     * Persists all of the proteins in the list of proteinsAwaitingPersistence and empties
     * this Collection, ready to be used again.
     */
    private void persistBatch() {
        LOGGER.debug("ProteinLoader.persistBatch() method has been called.");
        if (proteinsAwaitingPersistence.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + proteinsAwaitingPersistence.size() + " proteins");
            }
            Utilities.verboseLog(110, "Persisting " + proteinsAwaitingPersistence.size() + " proteins");
            for (Protein newProtein: proteinsAwaitingPersistence) {

                if (bottomProteinId == null || bottomProteinId > newProtein.getId()) {
                    bottomProteinId = newProtein.getId();
                }
                if (topProteinId == null || topProteinId < newProtein.getId()) {
                    topProteinId = newProtein.getId();
                }
            }
            /*
            final ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(proteinsAwaitingPersistence);
            bottomProteinId = persistedProteins.updateBottomProteinId(bottomProteinId);
            topProteinId = persistedProteins.updateTopProteinId(topProteinId);
            Utilities.verboseLog(1100, "Completed Persisting topProteinId: " + topProteinId + " bottomProteinId: " + bottomProteinId);
            if (isGetOrfOutput) {
                Utilities.verboseLog(1100, "Persisting  getOrfOutput topProteinId: " + topProteinId + " bottomProteinId: " + bottomProteinId);

                createAndPersistNewORFs(persistedProteins);
                Utilities.verboseLog(1100, "Completed Persisting  getOrfOutput ");
            }
            */

            proteinsAwaitingPersistence.clear();

        }
    }

    private void createAndPersistNewORFs(final ProteinDAO.PersistedProteins persistedProteins) {
        //Holder for new ORFs which should be persisted
        Set<OpenReadingFrame> orfsAwaitingPersistence = new HashSet<>();

        Set<Protein> newProteins = persistedProteins.getNewProteins();
    }
}
