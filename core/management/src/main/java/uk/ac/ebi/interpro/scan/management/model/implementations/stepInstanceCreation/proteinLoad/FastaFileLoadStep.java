package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Loads a Fasta file into the database, creating new protein instance
 * where necessary and schedules new StepInstances for these new proteins.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class FastaFileLoadStep extends Step implements StepInstanceCreatingStep {

    public static final String FASTA_FILE_PATH_KEY = "FASTA_FILE_PATH";

    private static final Logger LOGGER = Logger.getLogger(FastaFileLoadStep.class.getName());

    private LoadFastaFile fastaFileLoader;

    /**
     * If this is set, this file name will override that passed in by the user as
     * a parameteter.  This is for use in the genomics sequence analysis pipeline,
     * where this Step loads a generated protein sequence file, rather than the
     * nucleic acid sequence file passed in by the user.
     */
    private String overridingFastaFileName;

    protected Jobs jobs;
    protected StepInstanceDAO stepInstanceDAO;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setFastaFileLoader(LoadFastaFile fastaFileLoader) {
        this.fastaFileLoader = fastaFileLoader;
    }

    public void setOverridingFastaFileName(String overridingFastaFileName) {
        this.overridingFastaFileName = overridingFastaFileName;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        Utilities.verboseLog(10, " FastaFileLoadStep - starting");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FastaFileLoadStep.fastaFileLoader : " + fastaFileLoader);
        }
        String providedPath;
        if (overridingFastaFileName == null || overridingFastaFileName.isEmpty()) {
            providedPath = stepInstance.getParameters().get(FASTA_FILE_PATH_KEY);
        } else {
            providedPath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, overridingFastaFileName);
        }
        String analysisJobNames = stepInstance.getParameters().get(ANALYSIS_JOB_NAMES_KEY);
        final String completionJobName = stepInstance.getParameters().get(COMPLETION_JOB_NAME_KEY);
        boolean useMatchLookupService = true;
        if (stepInstance.getParameters().containsKey(USE_MATCH_LOOKUP_SERVICE)) {
            useMatchLookupService = Boolean.parseBoolean(stepInstance.getParameters().get(USE_MATCH_LOOKUP_SERVICE));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fasta file path to be loaded: " + providedPath);
        }
        if (providedPath != null) {
            // Try resolving the fasta file as an absolute file path
            InputStream fastaFileInputStream = null;
            String fastaFileInputStatusMessage;
            try {
                Path path = Paths.get(providedPath); // E.g. "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/test_proteins.fasta"
                System.out.println(getTimeNow() + " Loading file " + providedPath);
                if (Files.exists(path)) {
                    fastaFileInputStatusMessage = " - fasta file exists";
                    if (Files.isReadable(path)) {
                        //
                        if (Files.size(path) == 0) {
                            //GetORF result file
                            // If getorf predicted nothing, there is nothing more to do.
                            // TODO Consider if there is a better way of dealing with this than throwing an Exception
                            // Should skip all subsequent steps, BUT should not "fail" - should elegantly report no proteins with zero exit status
                            // Need to output empty files of the types expected, so external pipeline is not broken.
                            if (path.getFileName().toString().contains("orfs")) {
                                System.out.println("\nThe ORF predication tool EMBOSS: getorf produced an empty result file (" + providedPath + ").");
                                System.out.println("Therefore there are no proteins for InterproScan to analyse");
                                System.out.println("Finishing...");
                                System.exit(0);
                            }
                            // TODO - Should this also behave elegantly and output empty files?
                            System.out.println("\nThe FASTA input file " + providedPath + " is empty.");
                            System.out.println("Therefore there are no sequences for InterproScan to analyse");
                            System.out.println("Finishing...");
                            System.exit(0);
                        }
                        try {
                            fastaFileInputStream = Files.newInputStream(path);
                        } catch (FileNotFoundException e) {
                            System.out.println("\nERROR: Could not find FASTA input file " + path.toAbsolutePath().toString());
                            System.out.println("Exiting...");
                            System.exit(2);
                        }
                    } else {
                        System.out.println("\nERROR: The FASTA input file " + providedPath + " is visible but cannot be read.  Please check the file permissions.");
                        System.out.println("Exiting...");
                        System.exit(2);
                    }
                } else {
                    // Absolute file path did not resolve, so try using the class loader.
                    fastaFileInputStatusMessage = " - fasta file does not exist and absolute file path did not resolve. ";
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The file " + providedPath + " does not exist.  Attempting to access this file using the ClassLoader.");
                    }
                    fastaFileInputStream = FastaFileLoadStep.class.getClassLoader().getResourceAsStream(providedPath);
                }
                //TODO unit test
                boolean stdinOn = false;
                if (providedPath.equals("-")) {
                    fastaFileInputStream = System.in;
                    stdinOn = true;
                }
                if ((!stdinOn) && fastaFileInputStream == null) {
                    System.out.println("Cannot find the fasta file located at " + providedPath + fastaFileInputStatusMessage);
                    System.out.println("Exiting...");
                    System.exit(2);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Number of jobs in i5: " + jobs.getJobList().size());
                    //All jobs
                    for (Job job4Debug : jobs.getAnalysisJobs().getJobList()){
                        LOGGER.debug("SignatureLibraryRelease: " +
                                job4Debug.getId() + ": " +
                                job4Debug.getLibraryRelease().getLibrary().getName() + ", " +
                                job4Debug.getLibraryRelease().getVersion() + ", " +
                                "active: " + job4Debug.isActive());

                        if(job4Debug.getLibraryRelease().getLibrary().getName().equalsIgnoreCase("gene3d")){
                            LOGGER.debug("Gene3d: " + job4Debug.getLibraryRelease().getVersion() + " - " +
                                    job4Debug.getSteps());
                        }
                        if(job4Debug.getLibraryRelease().getLibrary().getName().equalsIgnoreCase("panther")) {
                            LOGGER.debug("panther: " + job4Debug.getLibraryRelease().getVersion() + " - " +
                                    job4Debug.getSteps());
                        }
                    }
                }

                Map<String, SignatureLibraryRelease> analysisJobMap = new HashMap<>();
                Jobs analysisJobs;
                if (analysisJobNames == null) {
                    analysisJobs = jobs.getActiveAnalysisJobs();
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
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("analysisJobMap:" + analysisJobMap);
                }

                String analysesPrintOutStr = getTimeNow() + " Running the following analyses:\n";
                String analysesDisplayStr = getTimeNow() + " Running the following analyses:\n";
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

                System.out.println(analysesDisplayStr + "[" + analysesToDisplay.toString() +"]");


                Job completionJob = jobs.getJobById(completionJobName);

                StepCreationSequenceLoadListener sequenceLoadListener =
                        new StepCreationSequenceLoadListener(analysisJobs, completionJob, stepInstance.getParameters());
                sequenceLoadListener.setStepInstanceDAO(stepInstanceDAO);

                fastaFileLoader.loadSequences(fastaFileInputStream, sequenceLoadListener, analysisJobMap, useMatchLookupService);
                LOGGER.debug("Finished loading sequences and creating step instances.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fastaFileInputStream != null) {
                    try {
                        fastaFileInputStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to cleanly close the InputStream from FASTA file " + providedPath);
                    }
                }
            }
        }
        Utilities.verboseLog(10, " FastaFileLoadStep - done");
    }

    private String getTimeNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        return sdf.format(cal.getTime()); // Return the current date
    }
}
