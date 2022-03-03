package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

//port org.apache.commons.lang3.StringUtils;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.io.ExternallySetLocationTemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.converter.Converter;
import uk.ac.ebi.interpro.scan.jms.exception.InvalidInputException;
import uk.ac.ebi.interpro.scan.jms.master.*;
import uk.ac.ebi.interpro.scan.jms.monitoring.MasterControllerApplication;
import uk.ac.ebi.interpro.scan.persistence.EntryKVDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.JobStatusWrapper;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import uk.ac.ebi.interpro.scan.persistence.kvstore.LevelDBStore;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.security.AccessController;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main entry point for the the master and workers in a
 * Java Messaging configuration of InterProScan.
 * <p/>
 * Runs in mode 'master' by default.
 * <p/>
 * Usage:
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar master
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar worker
 * java -Dconfig=conf/myconfig.props -jar interproscan-5.jar monitor
 */

public class Run extends AbstractI5Runner {

    private static final Logger LOGGER = LogManager.getLogger(Run.class.getName());

    /**
     * This is the REAL set of options that the Run class will accept
     */
    private static final Options COMMAND_LINE_OPTIONS = new Options();

    /**
     * Same contents as COMMAND_LINE_OPTIONS, however if the I5Option enum value
     * has includeInUsageMessage == false, the option is excluded.
     * <p/>
     * This is to remove clutter from the help message that may confuse users.
     */
    private static final Options COMMAND_LINE_OPTIONS_FOR_HELP = new Options();

    private static final int MEGA = 1024 * 1024;

    String temporaryDirectory = null;

    private boolean deleteWorkingDirectoryOnCompletion = true;

    static {
        //Usual I5 options
        for (I5Option i5Option : I5Option.values()) {
            final Option.Builder builder;
            builder = (i5Option.getShortOpt() == null)
                    ? Option.builder()
                    : Option.builder(i5Option.getShortOpt());
            builder.longOpt(i5Option.getLongOpt());
            builder.desc(i5Option.getDescription());
            if (i5Option.isRequired()) {
                builder.required();
            }
            if (i5Option.getArgumentName() != null) {
                builder.argName(i5Option.getArgumentName());
                if (i5Option.hasMultipleArgs()) {
                    builder.hasArgs();
                } else {
                    builder.hasArg();
                }
            }
            builder.valueSeparator();

            final Option option = builder.build();

            COMMAND_LINE_OPTIONS.addOption(option);

        }
    }


    public static void main(String[] args) {
        // create the command line parser

        CommandLineParser parser = new DefaultParser();
        String modeArgument;
        Mode mode = null;

        String temporaryDirectory = null;
        boolean deleteWorkingDirectoryOnCompletion = true;

        try {
            //change Loglevel
            //TODO use the properties file
            changeLogLevel("DEBUG");

            // parse the command line arguments
            CommandLine parsedCommandLine = parser.parse(COMMAND_LINE_OPTIONS, args);

            modeArgument = parsedCommandLine.getOptionValue(I5Option.MODE.getLongOpt());

            try {
                mode = getMode(modeArgument);
            } catch (IllegalArgumentException iae) {
                LOGGER.fatal("The mode '" + modeArgument + "' is not handled.  Should be one of: " + Mode.getCommaSepModeList());
                System.exit(1);
            }

            for (Option option : COMMAND_LINE_OPTIONS.getOptions()) {
                final String shortOpt = option.getOpt();
                if (I5Option.showOptInHelpMessage(shortOpt, mode)) {
                    COMMAND_LINE_OPTIONS_FOR_HELP.addOption(option);
                }
            }


            ArrayList<String> analysesHelpInformation = new ArrayList<>();

            String i5Version = "5.55-88.0";
            String i5BuildType = "64-Bit";
            //32bitMessage:i5BuildType = "32-Bit";

            //print version and exit
            if (parsedCommandLine.hasOption(I5Option.VERSION.getLongOpt())) {
                printVersion(i5Version, i5BuildType);
                System.exit(0);
            }

            System.out.println(Utilities.getTimeNow() + " Welcome to InterProScan-" + i5Version);
            //32bitMessage:System.out.println(Utilities.getTimeNow() + " You are running the 32-bit version");

            String operatingSystem = System.getProperty("os.name");
            System.out.println(Utilities.getTimeNow() + " Running InterProScan v5 in " + mode + " mode... on " + operatingSystem);

            //String config = System.getProperty("config");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
                LOGGER.info("Running in " + mode + " mode");
            }

            //create the dot i5 dir/file
            //$USER_HOME/.interproscan-5/interproscan.properties
            if (System.getProperty("user.home") != null && !System.getProperty("user.home").isEmpty()) {
                String dotInterproscan5Dir = System.getProperty("user.home") + "/.interproscan-5";
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("dotInterproscan5Dir : " + dotInterproscan5Dir);
                }
                String userInterproscan5Properties = dotInterproscan5Dir + "/interproscan.properties";
                File userInterproscan5PropertiesFile = new File(userInterproscan5Properties);
                if (!checkPathExistence(dotInterproscan5Dir)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Create dotInterproscan5Dir : " + dotInterproscan5Dir);
                    }
                    createDirectory(dotInterproscan5Dir);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Directory $USER_HOME/.interproscan-5/interproscan.properties  - " + dotInterproscan5Dir + " exists");
                    }
                }
                //Create file if it doesnot exists
                if (!userInterproscan5PropertiesFile.exists()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(" Creating the  userInterproscan5Properties file : " + userInterproscan5Properties);
                    }
                    try {
                        userInterproscan5PropertiesFile.createNewFile();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to access  " + userInterproscan5Properties);
                        //check the permisions in the directory of user.home
                        try {
                            String actions = "read,write";
                            AccessController.checkPermission(new FilePermission(System.getProperty("user.home"), actions));
//                            System.out.println("You have read/write permition to use : " + System.getProperty("user.home"));
                        } catch (SecurityException se) {
                            LOGGER.warn("You don't have read/write permition to use : " + System.getProperty("user.home"));
                        }

                        LOGGER.warn(e);
                    }

                }

                //Deal with user supplied config file from the command line
                String systemInterproscanProperties = userInterproscan5Properties;
                if (System.getProperty("system.interproscan.properties") == null) {
                    LOGGER.debug("USer has not supplied any properties file");
                    System.setProperty("system.interproscan.properties", systemInterproscanProperties);
                }
            } else {
                //system and interproscan.properties are the same in case the user has not supplied any file
                if (System.getProperty("system.interproscan.properties") == null) {
                    LOGGER.debug("USer has not supplied any properties file");
                    System.setProperty("system.interproscan.properties", "interproscan.properties");
                }
            }

            final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{mode.getContextXML()});

            //deal with active mq
            //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","uk.ac.ebi.interpro.scan.management.model.StepExecution");
            //System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");

//            String contextFile = mode.getContextXML();
//            XmlWebApplicationContext context = new XmlWebApplicationContext();
//            context.setConfigLocation(contextFile);
//            context.setServletContext(request.getServletContext());
//            context.refresh();
//
//            final AbstractApplicationContext ctx = context;

            // The command-line distributed mode selects a random port number for communications.
            // This block selects the random port number and sets it on the broker.

            // Def. analysesToRun: List of analyses jobs which will be performed/submitted by I5
            String[] analysesToRun = null;
            String[] depreactedAnalysesToRun = null;
            String[] excludedAnalyses = null;

            if (!mode.equals(Mode.INSTALLER) && !mode.equals(Mode.EMPTY_INSTALLER) && !mode.equals(Mode.CONVERT) && !mode.equals(Mode.MONITOR)) {
                Jobs jobs = (Jobs) ctx.getBean("jobs");
                temporaryDirectory = jobs.getBaseDirectoryTemporaryFiles();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("temporaryDirectory: jobs.getBaseDirectoryTemporaryFiles() - " + temporaryDirectory.toString());
                }
                //Get deactivated jobs
                final Map<Job, JobStatusWrapper> deactivatedJobs = jobs.getDeactivatedJobs();
                //Info about active and de-active jobs is shown in the manual instruction (help) as well

                final Map<Job, JobStatusWrapper> deprecatedJobs = jobs.getDeprecatedJobs();

                if (isInvalid(mode, parsedCommandLine)) {
                    printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
                    analysesHelpInformation.add("Available analyses:\n");    // LEAVE as System.out
                    for (Job job : jobs.getActiveAnalysisJobs().getJobList()) {
                        // Print out available jobs
                        SignatureLibraryRelease slr = job.getLibraryRelease();
                        if (!job.isDeprecated()) {
                            analysesHelpInformation.add(String.format("    %25s (%s) : %s\n", slr.getLibrary().getName(), slr.getVersion(), job.getDescription())); // LEAVE as System.out
                        }
                    }
                    if (deactivatedJobs.size() > 0) {
                        analysesHelpInformation.add("\nDeactivated analyses:\n");
                    }
                    for (Job deactivatedJob : deactivatedJobs.keySet()) {
                        JobStatusWrapper jobStatusWrapper = deactivatedJobs.get(deactivatedJob);
                        // Print out deactivated jobs
                        SignatureLibraryRelease slr = deactivatedJob.getLibraryRelease();
                        analysesHelpInformation.add(String.format("    %25s (%s) : %s\n", slr.getLibrary().getName(), slr.getVersion(), jobStatusWrapper.getWarning()));
                    }
                    if (deprecatedJobs.size() > 0) {
                        analysesHelpInformation.add("\nDeprecated analyses:\n");
                    }
                    for (Job deprecatedJob : deprecatedJobs.keySet()) {
                        JobStatusWrapper jobStatusWrapper = deprecatedJobs.get(deprecatedJob);
                        // Print out deactivated jobs
                        SignatureLibraryRelease slr = deprecatedJob.getLibraryRelease();
                        analysesHelpInformation.add(String.format("    %25s (%s) : %s\n", slr.getLibrary().getName(), slr.getVersion(), deprecatedJob.getDescription())); // LEAVE as System.out

                    }
                    printStringList(analysesHelpInformation);
                    System.exit(1);
                }

                //print help and exit
                if (parsedCommandLine.hasOption(I5Option.HELP.getLongOpt())) {
                    printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
                    printStringList(analysesHelpInformation);
                    System.exit(0);
                }

                try {
                    //System.out.println("Deal with depreactedAnalysesToRun and excludedAnalyses");
                    depreactedAnalysesToRun = getDeprecatedApplications(parsedCommandLine, jobs);
                    //System.out.println("depreacted Analyses To Run :" + Arrays.asList(depreactedAnalysesToRun).toString());

                    excludedAnalyses = getExcludedApplications(parsedCommandLine, jobs);

                    //System.out.println("excludedAnalyses Analyses  :" + Arrays.asList(excludedAnalyses).toString());

                    analysesToRun = getApplications(parsedCommandLine, jobs);
                    if (LOGGER.isDebugEnabled()) {
                        StringBuilder analysisItems = new StringBuilder();
                        for (String analysisItem : analysesToRun) {
                            analysisItems.append(analysisItem).append(" ");
                        }
                        LOGGER.debug("analysesToRun :- " + analysisItems.toString());
                    }
                } catch (InvalidInputException e) {
                    System.out.println("Invalid input specified for -appl/--applications parameter:\n" + e.getMessage());
                    System.exit(1);
                }

            }
            //Print help for the convert mode
            else if (mode.equals(Mode.CONVERT)) {
                if (isInvalid(mode, parsedCommandLine)) {
                    exitI5(Mode.CONVERT, 1);
                }
            }

            // Validate the output formats supplied
            String[] parsedOutputFormats = null;
            if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMATS.getLongOpt())) {
                parsedOutputFormats = parsedCommandLine.getOptionValues(I5Option.OUTPUT_FORMATS.getLongOpt());
                parsedOutputFormats = tidyOptionsArray(parsedOutputFormats);
                validateOutputFormatList(parsedOutputFormats, mode);
            }

            // Validate the sequence type

            String sequenceType = "p";
            if (!mode.equals(Mode.INSTALLER) && !mode.equals(Mode.EMPTY_INSTALLER) && !mode.equals(Mode.CONVERT) && !mode.equals(Mode.MONITOR)) {
                Set<String> sequenceTypes = (HashSet<String>) ctx.getBean("sequenceTypes");
                if (parsedCommandLine.hasOption(I5Option.SEQUENCE_TYPE.getLongOpt())) {
                    sequenceType = parsedCommandLine.getOptionValue(I5Option.SEQUENCE_TYPE.getLongOpt());

                    // Check the sequence type is "n" or "p"
                    //Set<String> sequenceTypes = (HashSet<String>) ctx.getBean("sequenceTypes");
                    if (sequenceTypes != null && !sequenceTypes.contains(sequenceType)) {
                        System.out.print("\n\nThe specified sequence type " + sequenceType + " was not recognised, expected: ");
                        StringBuilder expectedSeqTypes = new StringBuilder();
                        for (String seqType : sequenceTypes) {
                            if (expectedSeqTypes.length() > 0) {
                                expectedSeqTypes.append(",");
                            }
                            expectedSeqTypes.append(seqType);
                        }
                        System.out.println(expectedSeqTypes + "\n\n");
                        System.exit(1);
                    }
                }
                if (sequenceTypes != null && sequenceTypes.contains(sequenceType)) {
                    if (sequenceType.equalsIgnoreCase("n")) {
                        //TODO
                        //System.out.println(Utilities.getTimeNow() + " Input sequence type - Nucleotide sequences ");
                    }
                    //System.out.println("expectedSeqTypes " + sequenceTypes + "\n");
                } else {
                    System.out.println("expectedS sequence types: " + sequenceTypes + "\n");
                }

            }
            Utilities.setSequenceType(sequenceType);

            if (mode.getRunnableBean() != null) {
                final Runnable runnable = (Runnable) ctx.getBean(mode.getRunnableBean());

                //Set up converter mode
                if (runnable instanceof Converter) {
                    runConvertMode(runnable, parsedCommandLine, parsedOutputFormats);
                } else if (runnable instanceof MasterControllerApplication) {
                    runMasterControllerApplicationMode(runnable, parsedCommandLine, ctx, mode);
                } else {

                    checkIfMasterAndConfigure(runnable, analysesToRun, parsedCommandLine, parsedOutputFormats, ctx, mode, sequenceType);

                    checkIfDistributedWorkerAndConfigure(runnable, parsedCommandLine, ctx, mode);
                }
                //checkIfDistributedWorkerAndConfigure(runnable, parsedCommandLine, ctx, mode);


                String workingTemporaryDirectory = "";

                //get temp directory for cleanup even in convert mode we need temp dir
                if (!(mode.equals(Mode.INSTALLER) || mode.equals(Mode.WORKER) || mode.equals(Mode.DISTRIBUTED_WORKER)
                        || mode.equals(Mode.CONVERT) || mode.equals(Mode.HIGHMEM_WORKER))) {
                    //|| mode.equals(Mode.CONVERT)
                    final AbstractMaster master = (AbstractMaster) runnable;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Utilities.getTimeNow() + " 1. Checking working Temporary Directory -master.getTemporaryDirectory() : " + master.getTemporaryDirectory());
                    }
                    master.setupTemporaryDirectory();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Utilities.getTimeNow() + " 1. temporaryDirectory is  " + temporaryDirectory);
                    }
                    try {
                        temporaryDirectory = master.getWorkingTemporaryDirectoryPath();
                    } catch (IllegalStateException e) {
                        final String tempDir = master.getTemporaryDirectory();
                        System.out.println("Could not write to temporary directory: " + tempDir.substring(0, tempDir.lastIndexOf(File.separator)));
                        System.exit(1);
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Utilities.getTimeNow() + " 1. BaseDirectoryTemporary is  " + master.getJobs().getBaseDirectoryTemporaryFiles());
                    }
                    workingTemporaryDirectory = master.getWorkingTemporaryDirectoryPath();
                    deleteWorkingDirectoryOnCompletion = master.isDeleteWorkingDirectoryOnCompletion();

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Utilities.getTimeNow() + " 1. workingTemporaryDirectory is  " + workingTemporaryDirectory);
                    }


                }

                LevelDBStore kvStoreEntry = null;
                LevelDBStore kvStoreProteins = null;
                LevelDBStore kvStoreProteinsNotInLookup = null;
                LevelDBStore kvStoreProteinsOther = null;
                LevelDBStore kvStoreMatches = null;
                LevelDBStore kvStoreNucleotides = null;

                boolean entryDBInitialSetup = false;
                if (mode.equals(Mode.INSTALLER) ) {
                    // configure the entriedDB for the Loading of models
                    entryDBInitialSetup = true;
                    kvStoreEntry = (LevelDBStore) ctx.getBean("kvStoreEntry");
                    configureKVStoreEntry(kvStoreEntry, workingTemporaryDirectory, entryDBInitialSetup);

                    EntryKVDAO entryKVDAO = (EntryKVDAO) ctx.getBean("entryKVDAO");
                    entryKVDAO.checkKVDBStores();
                }

                if (!workingTemporaryDirectory.isEmpty()) {
                    // configure the KVStores

                    // configure the entriedDB
                    kvStoreEntry = (LevelDBStore) ctx.getBean("kvStoreEntry");

                    kvStoreProteins = (LevelDBStore) ctx.getBean("kvStoreProteins");
                    //System.out.println(Utilities.getTimeNow() + " kvStoreProteins name : " + kvStoreProteins.getDbName());
                    kvStoreProteinsNotInLookup = (LevelDBStore) ctx.getBean("kvStoreProteinsNotInLookup");
                    //System.out.println(Utilities.getTimeNow() + " kvStoreProteinsNotInLookup name : " + kvStoreProteinsNotInLookup.getDbName());

                    kvStoreProteinsOther = (LevelDBStore) ctx.getBean("kvStoreProteinsOther");
                    //System.out.println(Utilities.getTimeNow() + " kvStoreProteinsOther  name : " + kvStoreProteinsOther.getDbName());
                    kvStoreMatches = (LevelDBStore) ctx.getBean("kvStoreMatches");
                    //System.out.println(Utilities.getTimeNow() + " kvStoreMatches  name : " + kvStoreMatches.getDbName());
                    //configureKVStores(kvStoreProteins, kvStoreProteinsNotInLookup, kvStoreProteinsOther,  kvStoreMatches, workingTemporaryDirectory );

                    kvStoreNucleotides = (LevelDBStore) ctx.getBean("kvStoreNucleotides");
                    //System.out.println(Utilities.getTimeNow() + " kvStoreNucleotides  name : " + kvStoreNucleotides.getDbName());

                    //System.out.println(Utilities.getTimeNow() + " workingTemporaryDirectory  : " + workingTemporaryDirectory);
                    configureKVStores(kvStoreProteins, kvStoreProteinsNotInLookup, kvStoreProteinsOther, kvStoreMatches, kvStoreNucleotides, workingTemporaryDirectory);
                    configureKVStoreEntry(kvStoreEntry, workingTemporaryDirectory, entryDBInitialSetup);
                    //System.out.println(Utilities.getTimeNow() + " kvStoreProteinsNotInLookup name - take 2 : " + kvStoreProteinsNotInLookup.toString());

                    EntryKVDAO entryKVDAO = (EntryKVDAO) ctx.getBean("entryKVDAO");
                    entryKVDAO.checkKVDBStores();

                    ProteinDAO proteinDAO = (ProteinDAO) ctx.getBean("proteinDAO");
                    proteinDAO.checkKVDBStores();

                } else {
                    LOGGER.warn("Working Temporary Directory is not set");
                }

                if (!(mode.equals(Mode.INSTALLER) || mode.equals(Mode.CONVERT))) {
                    //deal with panther  stepPantherHMM3RunPantherScore
                    //this maynot be necessary anymore
                    //TODO maybe remove
                    //final PantherNewBinaryStep stepPantherRunBinary = (PantherNewBinaryStep) ctx.getBean("stepPantherRunBinary");
//                    final PantherScoreStep stepPantherRunBinary = (PantherScoreStep) ctx.getBean("stepPantherHMM3RunPantherScore");
                    //stepPantherRunBinary.setUserDir(parsedCommandLine.getOptionValue(I5Option.USER_DIR.getLongOpt()).trim());
                }


                runnable.run();

                if (!workingTemporaryDirectory.isEmpty()) {
                    closeKVStores(kvStoreEntry, kvStoreProteins, kvStoreProteinsNotInLookup, kvStoreProteinsOther, kvStoreMatches, kvStoreNucleotides, workingTemporaryDirectory);

                }
                if (mode.equals(Mode.INSTALLER) ) {
                    kvStoreEntry.close();
                }

            }

            //System.exit(0);

        } catch (UnrecognizedOptionException exp) {
            // bit of a hack - but it at least prints something informative for completely unknown options
            // if we've caught the exception above COMMAND_LINE_OPTIONS_FOR_HELP should be empty anyway
            if (COMMAND_LINE_OPTIONS_FOR_HELP.getOptions().size() == 0) {
                for (Option option : (Collection<Option>) COMMAND_LINE_OPTIONS.getOptions()) {
                    COMMAND_LINE_OPTIONS_FOR_HELP.addOption(option);
                }
            }
            printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
            System.out.println("Unrecognised option: " + exp.getOption());
            System.exit(1);
        } catch (ParseException exp) {
            LOGGER.fatal("Exception thrown when parsing command line arguments.  Error message: " + exp.getMessage());
            printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            //clean up the temp files
            //System.out.println(Utilities.getTimeNow() + " Please clean up the TEMP files in ... " + temporaryDirectory);
            if (temporaryDirectory != null) {
                //deleteWorkingDirectory(true, temporaryDirectory);
//                System.out.println(Utilities.getTimeNow() + " Please clean up the TEMP files in ... " + temporaryDirectory);
                cleanUpWorkingDirectory(deleteWorkingDirectoryOnCompletion, temporaryDirectory);

            }
        }
        System.exit(0);
    }

    /**
     * Get InterProScan mode from the command line argument supplied
     *
     * @param modeArgument
     * @return
     * @throws IllegalArgumentException
     */
    public static Mode getMode(String modeArgument) throws IllegalArgumentException {
        Mode mode = (modeArgument != null)
                ? Mode.valueOf(modeArgument.toUpperCase())
                : DEFAULT_MODE;
        return mode;
    }

    private static void runMasterControllerApplicationMode(Runnable runnable, CommandLine parsedCommandLine, AbstractApplicationContext ctx, Mode mode) {
        final MasterControllerApplication masterControllerApplication = (MasterControllerApplication) runnable;

        //set the master uri
        if (parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
            LOGGER.debug("commandline has option Master_ URI ");
            final String masterUri = parsedCommandLine.getOptionValue(I5Option.MASTER_URI.getLongOpt());
            masterControllerApplication.setBrokerURL(masterUri);
        }
    }

    /**
     * @param mode   One of InterProScan's mode.
     * @param status Exit status for system.exit call.
     */
    private static void exitI5(final Mode mode, final int status) {
        if (mode.equals(Mode.CONVERT)) {
//            buildConvertModeOptions();
            printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
            System.exit(status);
        }
    }

    private static void runConvertMode(final Runnable runnable,
                                       final CommandLine parsedCommandLine,
                                       final String[] parsedOutputFormats) {
        if (runnable instanceof SimpleBlackBoxMaster) {
            SimpleBlackBoxMaster simpleMaster = (SimpleBlackBoxMaster) runnable;
            LOGGER.debug("Setting up the simple black box master...");
            setupSimpleBlackBoxMaster(simpleMaster, parsedCommandLine, parsedOutputFormats, "i5_convert_mode_output.out");
        }
    }

    private static void checkIfMasterAndConfigure(final Runnable runnable,
                                                  final String[] parsedAnalyses,
                                                  final CommandLine parsedCommandLine,
                                                  final String[] parsedOutputFormats,
                                                  final AbstractApplicationContext ctx,
                                                  final Mode mode,
                                                  final String sequenceType) {
        if (runnable instanceof Master) {
            final Master master = (Master) runnable;
            if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
                master.setAnalyses(parsedAnalyses);
            }
            //process tmp dir (-T) option
            if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
                String temporaryDirectory = getAbsoluteFilePath(parsedCommandLine.getOptionValue(I5Option.TEMP_DIRECTORY.getLongOpt()), parsedCommandLine);
                if (!checkPathExistence(temporaryDirectory)) {
                    createDirectory(temporaryDirectory);
                }
                master.setTemporaryDirectory(temporaryDirectory);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("temporaryDirectory: master.getTemporaryDirectory() - " + master.getTemporaryDirectory());
            }
            checkIfProductionMasterAndConfigure(master, ctx);
            checkIfBlackBoxMasterAndConfigure(master, parsedCommandLine, parsedOutputFormats, ctx, mode, sequenceType);
        }
    }


    private static void checkIfProductionMasterAndConfigure(
            final Master master,
            final AbstractApplicationContext ctx) {

        if (master instanceof ProductionMaster) {
            LOGGER.info("Configuring tcpUri for ProductionMaster");
            String tcpConnectionString = configureTCPTransport(ctx);
            ((ProductionMaster) master).setTcpUri(tcpConnectionString);
        }

    }


    private static void checkIfBlackBoxMasterAndConfigure(
            final Master master,
            final CommandLine parsedCommandLine,
            final String[] parsedOutputFormats,
            final AbstractApplicationContext ctx,
            final Mode mode,
            final String sequenceType) {

        if (master instanceof SimpleBlackBoxMaster) {
            SimpleBlackBoxMaster simpleMaster = (SimpleBlackBoxMaster) master;
            LOGGER.debug("Setting up the simple black box master...");
            setupSimpleBlackBoxMaster(simpleMaster, parsedCommandLine, parsedOutputFormats, "i5_output.out");
        }
        if (master instanceof BlackBoxMaster) {
            BlackBoxMaster bbMaster = (BlackBoxMaster) master;
            LOGGER.debug("Setting up the black box master...");
            String tcpConnectionString = null;
            if (mode == Mode.DISTRIBUTED_MASTER || mode == Mode.CLUSTER) {
                tcpConnectionString = configureTCPTransport(ctx);
            }

            if (bbMaster instanceof DistributedBlackBoxMasterOLD && tcpConnectionString != null) {
                ((DistributedBlackBoxMasterOLD) bbMaster).setTcpUri(tcpConnectionString);
                if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
                    final String projectId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                    ((ClusterUser) bbMaster).setProjectId(projectId);
                    ((ClusterUser) bbMaster).setSubmissionWorkerRunnerProjectId(projectId);
                }
            }
            //TODO: The copy of the distributed master will retire someday (if distributed computing works fine)
            if (bbMaster instanceof DistributedBlackBoxMaster && tcpConnectionString != null) {
                ((DistributedBlackBoxMaster) bbMaster).setTcpUri(tcpConnectionString);
                //if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
//                    final String projectId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                // }
                if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
                    LOGGER.debug("We have a Project/Cluster Run ID.");
                    final String projectId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                    System.out.println("The Project/Cluster Run ID for this run is: " + projectId);
                    ((ClusterUser) bbMaster).setProjectId(projectId);
                    ((DistributedBlackBoxMaster) bbMaster).setSubmissionWorkerRunnerProjectId(projectId);
                    final String userDir = parsedCommandLine.getOptionValue(I5Option.USER_DIR.getLongOpt());
                    ((DistributedBlackBoxMaster) bbMaster).setUserDir(userDir);
                    ((DistributedBlackBoxMaster) bbMaster).setSubmissionWorkerRunnerUserDir(userDir);
                    //setup the logdir
                    final File dir = new File(((DistributedBlackBoxMaster) bbMaster).getLogDir(), projectId.replaceAll("\\s+", ""));
                    if (!dir.exists() && !dir.mkdirs()) {
                        try {
                            throw new IOException("Unable to create " + dir.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String logDir = ((DistributedBlackBoxMaster) bbMaster).getLogDir() + File.separator + projectId.replaceAll("\\s+", "");
                    ((DistributedBlackBoxMaster) bbMaster).setLogDir(logDir);
                    ((DistributedBlackBoxMaster) bbMaster).setSubmissionWorkerLogDir(logDir);

                } else {
                    LOGGER.fatal("InterProScan 5 in CLUSTER mode needs a Cluster Run ID to continue, please specify the -clusterrunid (-crid) option.");
                    System.exit(1);
                }
            }

            if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
                final String runId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                if (bbMaster instanceof StandaloneBlackBoxMaster) {
                    ((StandaloneBlackBoxMaster) master).setRunId(runId);
//                    final ResourceMonitor resourceMonitor = (ResourceMonitor) ctx.getBean("resourceMonitor");
//                    resourceMonitor.setRunId(runId);
                }
                if (bbMaster instanceof SingleSeqOptimisedBlackBoxMaster) {
                    ((SingleSeqOptimisedBlackBoxMaster) master).setRunId(runId);
                }
            }
            //deal with cpu cores specified by user
            if (parsedCommandLine.hasOption(I5Option.CPU.getLongOpt())) {
                int numberOfCPUCores = Integer.parseInt(parsedCommandLine.getOptionValue(I5Option.CPU.getLongOpt()));
                if (numberOfCPUCores == 0) {
                    LOGGER.warn("--cpu 0 is not allowed, updated to --cpu 1");
                    numberOfCPUCores = 2;
                }
                if (bbMaster instanceof StandaloneBlackBoxMaster) {
                    //deal with cpu cores
                    ((StandaloneBlackBoxMaster) master).setMaxConcurrentInVmWorkerCount(numberOfCPUCores);
                }
                if (bbMaster instanceof DistributedBlackBoxMaster) {
                    //deal with cpu cores
                    ((DistributedBlackBoxMaster) master).setMaxConcurrentInVmWorkerCount(numberOfCPUCores);
                }
            }


            if (parsedCommandLine.hasOption(I5Option.SEQUENCE_TYPE.getLongOpt())) {
                bbMaster.setSequenceType(sequenceType);
                Utilities.setSequenceType(sequenceType);
            }

            if (parsedCommandLine.hasOption(I5Option.MIN_SIZE.getLongOpt())) {
                bbMaster.setMinSize(parsedCommandLine.getOptionValue(I5Option.MIN_SIZE.getLongOpt()));
            }

            //
            if (parsedCommandLine.hasOption(I5Option.DISABLE_PRECALC.getLongOpt())) {
                bbMaster.disablePrecalc();
            }

            //consider more verbose output
            if (parsedCommandLine.hasOption(I5Option.VERBOSE.getLongOpt())) {
                //String verboseOption = parsedCommandLine.getOptionValue(I5Option.VERBOSE.getLongOpt());
                //System.out.println(" verbose parameter value: " + verboseOption);
                bbMaster.setVerboseLog(true);
                bbMaster.setVerboseLogLevel(10);
            }
            if (parsedCommandLine.hasOption(I5Option.VERBOSE_LEVEL.getLongOpt())) {
                String verboseOption = parsedCommandLine.getOptionValue(I5Option.VERBOSE_LEVEL.getLongOpt());
                Map<String, Integer> logLevels = new HashMap<>();
                logLevels.put("OFF", 0);
                logLevels.put("INFO", 10);
                logLevels.put("DEBUG", 20);
                logLevels.put("TRACE", 30);
                logLevels.put("ALL", 90);
                int logLevel = 10;
                Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                //if(StringUtils.isNumeric(verboseOption)) {
                if  (pattern.matcher(verboseOption).matches()){
                    int verboseOptionAsInteger = Integer.parseInt(verboseOption);
                    if (verboseOptionAsInteger >= 0){
                        logLevel = verboseOptionAsInteger;
                    }
                }else{
                    logLevel = logLevels.get(verboseOption); //verboseOption.strip()
                }
                bbMaster.setVerboseLog(true);
                bbMaster.setVerboseLogLevel(logLevel);
            }

            // Exclude sites from output?
            final boolean includeTsvSites = parsedCommandLine.hasOption(I5Option.ENABLE_TSV_RESIDUE_ANNOT.getLongOpt());
            bbMaster.setIncludeTsvSites(includeTsvSites);

            // Exclude sites from output?
            final boolean excludeSites = parsedCommandLine.hasOption(I5Option.DISABLE_RESIDUE_ANNOT.getLongOpt());
            bbMaster.setExcludeSites(excludeSites);


            // GO terms and/or pathways will also imply IPR lookup
            final boolean mapToGo = parsedCommandLine.hasOption(I5Option.GOTERMS.getLongOpt());
            bbMaster.setMapToGOAnnotations(mapToGo);
            final boolean mapToPathway = parsedCommandLine.hasOption(I5Option.PATHWAY_LOOKUP.getLongOpt());
            bbMaster.setMapToPathway(mapToPathway);
            //scwitch iprlookup up on by default
//            final boolean mapToIPR = parsedCommandLine.hasOption(I5Option.IPRLOOKUP.getLongOpt());
            final boolean mapToIPR = true;
            bbMaster.setMapToInterProEntries(mapToGo || mapToPathway || mapToIPR);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("temporaryDirectory: bbmaster.getTemporaryDirectory() -- " + bbMaster.getTemporaryDirectory());
            }

            // Include version file with TSV output?
            final boolean inclTSVVersion = parsedCommandLine.hasOption(I5Option.TSV_VERSION_OUTPUT.getLongOpt());
            bbMaster.setInclTSVVersion(inclTSVVersion);



        }
    }

    /**
     * Used to setup STANDALONE. SINGLESEQ and CONVERT mode.
     */
    private static void setupSimpleBlackBoxMaster(final SimpleBlackBoxMaster master,
                                                  final CommandLine parsedCommandLine,
                                                  final String[] parsedOutputFormats,
                                                  String defaultOutputFileName) {

        // Get the value for the (-i) option (could be FASTA file, or in CONVERT mode, an XML file)
        if (parsedCommandLine.hasOption(I5Option.INPUT.getLongOpt())) {
            String fastaFilePath = parsedCommandLine.getOptionValue(I5Option.INPUT.getLongOpt());
            if (!fastaFilePath.equals("-")) {
                fastaFilePath = getAbsoluteFilePath(parsedCommandLine.getOptionValue(I5Option.INPUT.getLongOpt()), parsedCommandLine);
                // Check input exists
                checkPathExistence(fastaFilePath, false, false, I5Option.INPUT, true);
            }
            master.setFastaFilePath(fastaFilePath);
            defaultOutputFileName = new File(fastaFilePath).getName();
        }

        final boolean haveSetUserDirName = parsedCommandLine.hasOption(I5Option.USER_DIR.getLongOpt());
        final boolean haveSetBaseOutputFileName = parsedCommandLine.hasOption(I5Option.BASE_OUT_FILENAME.getLongOpt());
        final boolean haveSetOutputFileName = parsedCommandLine.hasOption(I5Option.OUTPUT_FILE.getLongOpt());
        final boolean haveSetOutputDirName = parsedCommandLine.hasOption(I5Option.OUTPUT_DIRECTORY.getLongOpt());

        // Check the (-u) default output directory exists for now, but we are not necessarily going to write to this location
        if (haveSetUserDirName) {
            String defaultOutputBaseFileName = getAbsoluteFilePath(defaultOutputFileName, parsedCommandLine);
            // E.g. defaultOutputBaseFileName = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/test_proteins.fasta"
            checkPathExistence(defaultOutputBaseFileName, true, false, I5Option.USER_DIR);
        }

        // Get the value for the (-b) option if specified
        if (haveSetBaseOutputFileName) {
            if (haveSetOutputFileName || haveSetOutputDirName) {
                System.out.println("The options --output-file-base (-b), --outfile (-o) and --output-dir (-d) are mutually exclusive.");
                System.exit(3);
            }
            String outputBaseFileName = parsedCommandLine.getOptionValue(I5Option.BASE_OUT_FILENAME.getLongOpt());
            outputBaseFileName = getAbsoluteFilePath(outputBaseFileName, parsedCommandLine);
            // E.g. for "-b OUT" outputBaseFileName = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT"

            if (outputBaseFileName.endsWith(File.separator)) {
                // If a base directory is supplied then check it's writable and add on the default base file output name
                // E.g. "-b OUT/"
                checkPathExistence(outputBaseFileName, false, true, I5Option.BASE_OUT_FILENAME);
                outputBaseFileName += defaultOutputFileName;
            } else {
                // If a base filename is supplied (with optional path elements) then check the base directory is writable
                // E.g. "-b OUT"
                checkPathExistence(outputBaseFileName, true, true, I5Option.BASE_OUT_FILENAME);
            }
            master.setOutputBaseFilename(outputBaseFileName);
        }
        //Get the value for the (-o) option if specified
        else if (haveSetOutputFileName) {
            if (parsedOutputFormats == null || parsedOutputFormats.length != 1 || "html".equalsIgnoreCase(parsedOutputFormats[0]) || "svg".equalsIgnoreCase(parsedOutputFormats[0])) {
                System.out.println("\n\nYou must indicate a single output format excluding HTML and SVG using the -f option if you wish to set an explicit output file name.");
                System.exit(2);
            }

            if (haveSetBaseOutputFileName || haveSetOutputDirName) {
                System.out.println("The options --output-file-base (-b), --outfile (-o) and --output-dir (-d) are mutually exclusive.");
                System.exit(3);
            }

            String outputFilename = parsedCommandLine.getOptionValue(I5Option.OUTPUT_FILE.getLongOpt());
            String explicitOutputFilename = outputFilename;
            if (!outputFilename.trim().equals("-")) {
                explicitOutputFilename = getAbsoluteFilePath(outputFilename, parsedCommandLine);
                // E.g. for "-o OUT.tsv" explicitOutputFilename = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/OUT.tsv"
                checkPathExistence(explicitOutputFilename, true, true, I5Option.OUTPUT_FILE);
            }
            master.setExplicitOutputFilename(explicitOutputFilename);
        }
        // Get the value for the (-d) option if specified
        else if (haveSetOutputDirName) {
            if (haveSetBaseOutputFileName || haveSetOutputFileName) {
                System.out.println("The options --output-file-base (-b), --outfile (-o) and --output-dir (-d) are mutually exclusive.");
                System.exit(3);
            }
            String outputDirValue = parsedCommandLine.getOptionValue(I5Option.OUTPUT_DIRECTORY.getLongOpt());
            outputDirValue = getAbsoluteFilePath(outputDirValue, parsedCommandLine);
            // E.g. for "-d outputs" outputDirValue = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/outputs"
            checkPathExistence(outputDirValue, false, true, I5Option.OUTPUT_DIRECTORY);
            if (!outputDirValue.endsWith(File.separator)) {
                outputDirValue += File.separatorChar;
            }
            final String outputBaseFileName = outputDirValue + defaultOutputFileName;
            master.setOutputBaseFilename(outputBaseFileName);
        }
        // If the (-b) or (-d) or (-o) options AREN'T specified, but the (-u) option is set (it should always be set)
        // then the default file output path will be (USER_DIR + defaultFileOutputName)
        else if (haveSetUserDirName) {
            String outputBaseFileName = getAbsoluteFilePath(defaultOutputFileName, parsedCommandLine);
            // E.g. default outputBaseFileName "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/test_proteins.fasta"
            // Write to the default output location
            checkPathExistence(outputBaseFileName, true, true, I5Option.USER_DIR);
            master.setOutputBaseFilename(outputBaseFileName);
        }

        //Get the values for the output formats
        if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMATS.getLongOpt())) {
            master.setOutputFormats(parsedOutputFormats);
        }

        // Set temporary directory
        String filePath;
        if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
            filePath = parsedCommandLine.getOptionValue(I5Option.TEMP_DIRECTORY.getLongOpt());
        } else {
            filePath = master.getTemporaryDirectory();
            if (filePath == null) {
                filePath = "temp/"; // Default
            }
        }
        final String temporaryDirectory = getAbsoluteFilePath(filePath, parsedCommandLine);
        // E.g. for "-T temp" temporaryDirectory = "~/Projects/github-i5/interproscan/core/jms-implementation/target/interproscan-5-dist/temp"
        if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
            checkPathExistence(temporaryDirectory, false, true, I5Option.TEMP_DIRECTORY);
        }
        master.setTemporaryDirectory(temporaryDirectory);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("temporaryDirectory: simple master.getTemporaryDirectory() - " + master.getTemporaryDirectory());
        }
    }

    /**
     * Transforms relative file paths to absolutes using the value of the USER_DIR option (which is mandatory).
     *
     * @param filePath
     * @param parsedCommandLine
     * @return
     */
    private static String getAbsoluteFilePath(final String filePath, final CommandLine parsedCommandLine) {
        if (new File(filePath).isAbsolute()) {
            return filePath;
        } else {
            if (!parsedCommandLine.hasOption(I5Option.USER_DIR.getLongOpt())) {
                throw new IllegalStateException("User directory option (-u) has to be present as it is mandatory, but it isn't.");
            } else {
                return parsedCommandLine.getOptionValue(I5Option.USER_DIR.getLongOpt()) +
                        File.separator + filePath;
            }
        }
    }

    private static void checkIfDistributedWorkerAndConfigure(final Runnable runnable,
                                                             final CommandLine parsedCommandLine,
                                                             final AbstractApplicationContext ctx,
                                                             final Mode mode) {
        if (runnable instanceof WorkerImpl) {
//                    if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt()) || parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
            final WorkerImpl worker = (WorkerImpl) runnable;
            LOGGER.debug("--- runnable is WorkerImpl --- ");
            if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt())) {
                final int priority = Integer.parseInt(parsedCommandLine.getOptionValue(I5Option.PRIORITY.getLongOpt()));
                if (priority < 0 || priority > 9) {
                    throw new IllegalStateException("The JMS priority value must be an integer between 0 and 9.  The value passed in is " + priority);
                }
                worker.setMinimumJmsPriority(priority);
            }

            //start the local activemq broker
            String tcpConnectionString = null;
            if (mode == Mode.DISTRIBUTED_WORKER) {
                tcpConnectionString = configureTCPTransport(ctx);
            }
            //set the tcpUri for the worker
            if (tcpConnectionString != null) {
                worker.setTcpUri(tcpConnectionString);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Remote worker has tcpConnectionString: " + tcpConnectionString);
                }
            }
            //set high memory option
            worker.setHighMemory(parsedCommandLine.hasOption(I5Option.HIGH_MEM.getLongOpt()));

            //set master worker
            boolean highmemDebug = true;
            if (parsedCommandLine.hasOption(I5Option.TIER1.getLongOpt())) {
                LOGGER.debug("Worker has worker tier # set");
                worker.setMasterWorker(parsedCommandLine.hasOption(I5Option.TIER1.getLongOpt()));
                LOGGER.debug("Run: get worker tier ");
                String tier = parsedCommandLine.getOptionValue(I5Option.TIER1.getLongOpt());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Run: set worker tier " + tier);
                }
                int tierInt = Integer.parseInt(tier);
                worker.setTier(tierInt);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Worker tier:  " + tier);
                }
                highmemDebug = true;
            }

            //set the master uri
            //Please note: Make sure you set the master worker flag and the high memory flag before you set the master URI
            if (parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
                LOGGER.debug("commandline has option Master_URI ");
                final String masterUri = parsedCommandLine.getOptionValue(I5Option.MASTER_URI.getLongOpt());
                worker.setMasterUri(masterUri);
                //want to change the remoteFactory
            }

            //set the jms template for the messagesender
//            final WorkerMessageSenderImpl workerMessageSender = (WorkerMessageSenderImpl) ctx.getBean("workerMessageSender");
//            workerMessageSender.setRemoteJmsTemplate(worker.getRemoteJmsTemplate());
//            LOGGER.debug("parsedCommandLine 1: " + parsedCommandLine.toString());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("I5Option.TEMP_DIRECTORY_NAME: " + I5Option.TEMP_DIRECTORY_NAME.getLongOpt());
            }
//            System.out.println("temp dir name: ");
            if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY_NAME.getLongOpt())) {
                final String temporaryDirectoryName = parsedCommandLine.getOptionValue(I5Option.TEMP_DIRECTORY_NAME.getLongOpt());
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Have a temporary directory name passed in: " + temporaryDirectoryName);
                // Attempt to modify the TemporaryDirectoryManager by retrieving it by name from the context.
                final TemporaryDirectoryManager tdm = (TemporaryDirectoryManager) ctx.getBean("tempDirectoryManager");
                // Check it is the right kind of directory manager, if it is, set the directory.
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Retrieved the TemporaryDirectoryManager - is it an ExternallySetLocationTemporaryDirectoryManager?");
                if (tdm != null && tdm instanceof ExternallySetLocationTemporaryDirectoryManager) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("YES!  Calling setPassedInDirectoryName on the DirectoryManager.");
                    ((ExternallySetLocationTemporaryDirectoryManager) tdm).setPassedInDirectoryName(temporaryDirectoryName);
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("NO!  So can't set the temporary directory manager.  Details of Directory Manager:" + tdm.toString());
                }
            }
            //set the project name for this i5 run
            if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
                String projectId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                worker.setProjectId(projectId);

                //setup the logdir
                final File dir = new File(worker.getLogDir(), projectId.replaceAll("\\s+", ""));
                if (!dir.exists() && !dir.mkdirs()) {
                    try {
                        throw new IOException("Unable to create " + dir.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String logDir = worker.getLogDir() + File.separator + projectId.replaceAll("\\s+", "");
                worker.setLogDir(logDir);
                worker.setSubmissionWorkerLogDir(logDir);

            }
            if (parsedCommandLine.hasOption(I5Option.MASTER_MAXLIFE.getLongOpt())) {
                LOGGER.debug("commandline has option MASTER_MAXLIFE ");
                final String masterMaxlife = parsedCommandLine.getOptionValue(I5Option.MASTER_MAXLIFE.getLongOpt());
                String[] masterTime = masterMaxlife.split(":");
                String masterClockTimeStr = masterTime[0];
                String masterLifeRemainingStr = masterTime[1];
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("master time passed: " + masterMaxlife);
                    LOGGER.debug("master time passed: " + masterClockTimeStr + " - " + masterLifeRemainingStr);
                }

                long masterClockTime = Long.valueOf(masterClockTimeStr.trim());
                long masterLifeRemaining = Long.valueOf(masterLifeRemainingStr.trim());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.info("masterclock time: " + masterClockTime + " master life remaining: " + masterLifeRemaining);
                }
                worker.setCurrentMasterClockTime(masterClockTime);
                worker.setCurrentMasterlifeSpanRemaining(masterLifeRemaining);
                //want to change the remoteFactory
            }

            if (parsedCommandLine.hasOption(I5Option.SEQUENCE_COUNT.getLongOpt())) {
                final String sequenceCountStr = parsedCommandLine.getOptionValue(I5Option.SEQUENCE_COUNT.getLongOpt());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.info("setSequenceCount : " + sequenceCountStr);
                }
                int sequenceCount = Integer.parseInt(sequenceCountStr);
                worker.setSequenceCount(sequenceCount);
                Utilities.sequenceCount = sequenceCount;
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("parsedCommandLine: " + parsedCommandLine.toString());
            }

        }
    }

    /**
     * Tidy an array of options for a command line option that takes multiple values.
     * For example { "Pfam,", "Gene3d,SMART", ",", ",test" } becomes { "Pfam", "Gene3d", "SMART", "test" }.
     * The validity of the options are not checked here.
     *
     * @param options Un-tidy array of options
     * @return Array of options after tidying.
     */
    private static String[] tidyOptionsArray(String[] options) {
        if (options == null || options.length < 1) {
            return options;
        }

        Set<String> parsedOptions = new HashSet<String>();

        // Examples of un-tidy options arrays:
        // 1. Commons.cli stores "-appl Pfam -appl Gene3d" as an array with 2 items { "Pfam", "Gene3d" }
        // 2. Commons.cli stores "-appl Pfam,Gene3d" as array with 1 item { "Pfam,Gene3d" }
        // 3. The I5 code below also allows something like "-appl Pfam, Gene3d,SMART, , ,test" which comes through as an
        //    array with 4 items { "Pfam,", "Gene3d,SMART", ",", ",test" } and needs to be tidied.
        for (String optionsArrayItem : options) {
            String[] optionsArrayItems = optionsArrayItem.split("[,\\s+]+");
            for (String option : optionsArrayItems) {
                if (option != null && !option.equals("")) {
                    parsedOptions.add(option);
                }
            }
        }

        return parsedOptions.toArray(new String[parsedOptions.size()]);
    }

    /**
     * Validate and tidy up the comma separated list of output formats specified by the user:
     * - Do the formats exist?
     *
     * @return The tidied list of file extensions
     */
    private static void validateOutputFormatList(String[] outputFormats, Mode mode) {
        // TODO With org.apache.commons.cli v2 could use EnumValidator instead, but currently we use cli v1.2
        if (outputFormats != null && outputFormats.length > 0) {
            // The user manually specified at least one output format, now check it's OK
            for (String outputFormat : outputFormats) {
                if (!FileOutputFormat.isExtensionValid(outputFormat)) {
                    System.out.println("\n\n" + "The specified output file format " + outputFormat + " was not recognised." + "\n\n");
                    System.exit(1);
                } else if (!mode.equals(Mode.CONVERT) && outputFormat.equalsIgnoreCase("raw")) {
                    // RAW output (InterProScan 4 TSV output) is only allowed in CONVERT mode
                    System.out.println("\n\n" + "The specified output file format " + outputFormat + " is only supported in " + Mode.CONVERT.name() + " mode." + "\n\n");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Clusters jobs of the same library(analysis).
     *
     * @param jobs
     * @return
     */
    private static Map<SignatureLibrary, Set<Job>> clusterJobsBySignatureLibrary(List<Job> jobs) {
        Map<SignatureLibrary, Set<Job>> result = new HashMap<SignatureLibrary, Set<Job>>();
        for (Job job : jobs) {
            SignatureLibraryRelease release = job.getLibraryRelease();
            if (release != null) {
                SignatureLibrary library = release.getLibrary();
                Set<Job> jobsForLibrary = result.get(library);
                if (jobsForLibrary == null) {
                    jobsForLibrary = new HashSet<Job>();
                }
                jobsForLibrary.add(job);
                result.put(library, jobsForLibrary);
            }
        }
        return result;
    }

    /**
     * Assembles all analyses jobs for that run.
     *
     * @param parsedAnalysesRealAnalysesMap
     * @return
     */
    protected static String[] getActiveAnalysesToRun(final Map<String, Set<Job>> parsedAnalysesRealAnalysesMap) {
        Set<String> result = new HashSet<String>();
        for (String key : parsedAnalysesRealAnalysesMap.keySet()) {
            Set<Job> realJobs = parsedAnalysesRealAnalysesMap.get(key);
            for (Job realJob : realJobs) {
                if (realJob.isActive()) {
                    result.add(realJob.getId());
                }
            }
        }
        return StringUtils.toStringArray(result);
    }

    /**
     * Determines real jobs for the parsed analyses names, e.g. Pfam -> Pfam-26.0
     *
     * @param parsedAnalyses
     * @param realJobs
     * @return Map of parsed analysis name to real analysis name.
     */
    protected static Map<String, Set<Job>> getRealAnalysesNames(String[] parsedAnalyses, List<Job> realJobs) {
        Map<String, Set<Job>> result = new HashMap<String, Set<Job>>();
        if (parsedAnalyses != null && parsedAnalyses.length > 0) {
            for (Job realJob : realJobs) {
                for (String analysisName : parsedAnalyses) {
                    if ((realJob.isActive() && realJob.getId().toLowerCase().contains(analysisName.toLowerCase())) || realJob.getId().toLowerCase().endsWith(analysisName.toLowerCase())) {
                        Set<Job> mappedJobs = result.get(analysisName);
                        if (mappedJobs == null) {
                            mappedJobs = new HashSet<Job>();
                        }
                        mappedJobs.add(realJob);
                        result.put(analysisName, mappedJobs);
                    }
                }
            }
        }
        return result;
    }

    public static String[] getApplications(CommandLine parsedCommandLine, Jobs allJobs) throws InvalidInputException {

        // To build a list of each analysis and the version specified (valid inputs only)
        List<String> analysesToRun = new ArrayList<String>();

        //Hack to allow old names
        Map<String, String> deprecatedNames = new HashMap<String, String>();
        deprecatedNames.put("PFAMA", SignatureLibrary.PFAM.getName());
        deprecatedNames.put("SIGNALP-EUK", SignatureLibrary.SIGNALP_EUK.getName());
        deprecatedNames.put("SIGNALP-GRAM_POSITIVE", SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName());
        deprecatedNames.put("SIGNALP-GRAM_NEGATIVE", SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName());


        // List of analyses parsed from command line, exactly as the user entered them
        String[] parsedAnalyses = null;
        if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
            parsedAnalyses = parsedCommandLine.getOptionValues(I5Option.ANALYSES.getLongOpt());
            parsedAnalyses = tidyOptionsArray(parsedAnalyses);
        }
        if (parsedAnalyses != null && parsedAnalyses.length > 0) {

            // To build a set of error messages relating to the inputs (invalid inputs only)
            Set<String> inputErrorMessages = new HashSet<String>();

            // Check the input matches the expected regex and build a user entered member database -> version number map
            Map<String, String> userAnalysesMap = new HashMap<String, String>();
            final Pattern applNameRegex = Pattern.compile("^[a-zA-Z0-9_-]+"); // E.g. "PIRSF", "Gene3d", "SignalP-GRAM_NEGATIVE"
            final Pattern applVersionRegex = Pattern.compile("\\d[0-9a-zA-Z._]*$"); // E.g. "3.01", "2.0c", "3", "2017_10"

            for (int i = 0; i < parsedAnalyses.length; i++) {
                final String parsedAnalysis = parsedAnalyses[i]; // E.g. "PIRSF", "PIRSF-3.01"
                String applName;
                String applVersion = null; // Could remain NULL if no specific version number specified by the user
                if (parsedAnalysis.endsWith("-")) {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                    continue;
                }
                int lastHyphen = parsedAnalysis.lastIndexOf('-');
                if (lastHyphen == -1 || !Character.isDigit(parsedAnalysis.charAt(lastHyphen + 1))) {
                    // No specific version number specified by the user
                    applName = parsedAnalysis;
                } else {
                    applName = parsedAnalysis.substring(0, lastHyphen);
                    applVersion = parsedAnalysis.substring(lastHyphen + 1);
                }
                final Matcher m1 = applNameRegex.matcher(applName);

                if (m1.matches() && (applVersion == null || applVersionRegex.matcher(applVersion).matches())) {
                    if (applName.equalsIgnoreCase("SignalP")) {
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_EUK.getName(), applVersion);
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName(), applVersion);
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName(), applVersion);
                    } else {
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, applName, applVersion);
                    }
                } else {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }

            //User specified jobs

            // Now check the user entered analysis versions actually exists
            for (Map.Entry<String, String> mapEntry : userAnalysesMap.entrySet()) {
                String userApplName = mapEntry.getKey();
                String userApplVersion = mapEntry.getValue();
                boolean found = false;
                //deal with deprecated application names
                String possibleUserApplName = deprecatedNames.get(userApplName.toUpperCase());
                if (possibleUserApplName != null) {
                    userApplName = possibleUserApplName;
                }
                for (Job job : allJobs.getAnalysisJobs().getJobList()) { // Loop through (not deactivated) analysis jobs
                    SignatureLibraryRelease slr = job.getLibraryRelease();
                    String applName = slr.getLibrary().getName();
                    String applVersion = slr.getVersion();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SignatureLibraryRelease: " + applName + ", " + applVersion);
                    }
                    if (applName.equalsIgnoreCase(userApplName)) {
                        // This analysis name exists, what about the version?
                        if (userApplVersion == null) {
                            // User didn't specify a version, just use the latest (active) version for this analysis
                            // Exactly one version of each member database analysis should be active at a time (TODO write unit test for that)
                            if (job.isActive()) {
                                analysesToRun.add(job.getId());
                                found = true;
                                break; // Found it!
                            }
                        } else if (applVersion.equalsIgnoreCase(userApplVersion)) {
                            analysesToRun.add(job.getId());
                            found = true;
                            break; // Found it!
                        }

                    }
                }
                if (!found) {
                    // Didn't find the user specified analysis version
                    inputErrorMessages.add("Analysis " + userApplName + ((userApplVersion == null) ? "" : "-" + userApplVersion) + " does not exist or is deactivated.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }
        }

        return StringUtils.toStringArray(analysesToRun);
    }

    public static String[] getDeprecatedApplications(CommandLine parsedCommandLine, Jobs allJobs) throws InvalidInputException {
        List<String> deprecatedAnalysesToRun = new ArrayList<String>();
        String[] inc_analyses = null;
        if (parsedCommandLine.hasOption(I5Option.INC_ANALYSES.getLongOpt())) {
            inc_analyses = parsedCommandLine.getOptionValues(I5Option.INC_ANALYSES.getLongOpt());
            inc_analyses = tidyOptionsArray(inc_analyses);
        }
        if (inc_analyses != null && inc_analyses.length > 0) {

            // To build a set of error messages relating to the inputs (invalid inputs only)
            Set<String> inputErrorMessages = new HashSet<String>();

            // Check the input matches the expected regex and build a user entered member database -> version number map
            Map<String, String> userAnalysesMap = new HashMap<String, String>();
            final Pattern applNameRegex = Pattern.compile("^[a-zA-Z0-9_-]+"); // E.g. "PIRSF", "Gene3d", "SignalP-GRAM_NEGATIVE"
            final Pattern applVersionRegex = Pattern.compile("\\d[0-9a-zA-Z._]*$"); // E.g. "3.01", "2.0c", "3", "2017_10"

            for (int i = 0; i < inc_analyses.length; i++) {
                final String parsedAnalysis = inc_analyses[i]; // E.g. "PIRSF", "PIRSF-3.01"
                String applName;
                String applVersion = null; // Could remain NULL if no specific version number specified by the user
                if (parsedAnalysis.endsWith("-")) {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                    continue;
                }
                int lastHyphen = parsedAnalysis.lastIndexOf('-');
                if (lastHyphen == -1 || !Character.isDigit(parsedAnalysis.charAt(lastHyphen + 1))) {
                    // No specific version number specified by the user
                    applName = parsedAnalysis;
                } else {
                    applName = parsedAnalysis.substring(0, lastHyphen);
                    applVersion = parsedAnalysis.substring(lastHyphen + 1);
                }
                final Matcher m1 = applNameRegex.matcher(applName);

                if (m1.matches() && (applVersion == null || applVersionRegex.matcher(applVersion).matches())) {
                    if (applName.equalsIgnoreCase("SignalP")) {
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_EUK.getName(), applVersion);
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName(), applVersion);
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName(), applVersion);
                    } else {
                        addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, applName, applVersion);
                    }
                } else {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }

            //User specified jobs

            // Now check the user entered analysis versions actually exists
            for (Map.Entry<String, String> mapEntry : userAnalysesMap.entrySet()) {
                String userApplName = mapEntry.getKey();
                String userApplVersion = mapEntry.getValue();
                boolean found = false;

                for (Job job : allJobs.getAnalysisJobs().getJobList()) { // Loop through (not deactivated) analysis jobs
                    SignatureLibraryRelease slr = job.getLibraryRelease();
                    String applName = slr.getLibrary().getName();
                    String applVersion = slr.getVersion();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SignatureLibraryRelease: " + applName + ", " + applVersion);
                    }
                    if (applName.equalsIgnoreCase(userApplName)) {
                        // This analysis name exists, what about the version?
                        if (userApplVersion == null) {
                            // User didn't specify a version, just use the latest (active) version for this analysis
                            // Exactly one version of each member database analysis should be active at a time (TODO write unit test for that)
                            if (job.isActive()) {
                                deprecatedAnalysesToRun.add(job.getId());
                                job.setDeprecated(false);
                                found = true;
                                break; // Found it!
                            }
                        } else if (applVersion.equalsIgnoreCase(userApplVersion)) {
                            deprecatedAnalysesToRun.add(job.getId());
                            job.setDeprecated(false);
                            found = true;
                            break; // Found it!
                        }

                    }
                }
                if (!found) {
                    // Didn't find the user specified analysis version
                    inputErrorMessages.add("Analysis " + userApplName + ((userApplVersion == null) ? "" : "-" + userApplVersion) + " does not exist or is deactivated.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }
        }
        //System.out.println("deprecatedAnalysesToRun: " + deprecatedAnalysesToRun.toString());
        return StringUtils.toStringArray(deprecatedAnalysesToRun);


    }


    public static String[] getExcludedApplications(CommandLine parsedCommandLine, Jobs allJobs) throws InvalidInputException {
        List<String> excludedAnalyses = new ArrayList<String>();
        String[] exclude_analyses = null;
        if (parsedCommandLine.hasOption(I5Option.EXC_ANALYSES.getLongOpt())) {
            exclude_analyses = parsedCommandLine.getOptionValues(I5Option.EXC_ANALYSES.getLongOpt());
            exclude_analyses = tidyOptionsArray(exclude_analyses);
        }

        if (exclude_analyses != null && exclude_analyses.length > 0) {

            // To build a set of error messages relating to the inputs (invalid inputs only)
            Set<String> inputErrorMessages = new HashSet<String>();

            // Check the input matches the expected regex and build a user entered member database -> version number map
            Map<String, String> userAnalysesMap = new HashMap<String, String>();
            final Pattern applNameRegex = Pattern.compile("^[a-zA-Z0-9_-]+"); // E.g. "PIRSF", "Gene3d", "SignalP-GRAM_NEGATIVE"
            final Pattern applVersionRegex = Pattern.compile("\\d[0-9a-zA-Z._]*$"); // E.g. "3.01", "2.0c", "3", "2017_10"

            for (int i = 0; i < exclude_analyses.length; i++) {
                final String parsedAnalysis = exclude_analyses[i]; // E.g. "PIRSF", "PIRSF-3.01"
                String applName;
                String applVersion = null; // Could remain NULL if no specific version number specified by the user
                if (parsedAnalysis.endsWith("-")) {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                    continue;
                }
                int lastHyphen = parsedAnalysis.lastIndexOf('-');
                if (lastHyphen == -1 || !Character.isDigit(parsedAnalysis.charAt(lastHyphen + 1))) {
                    // No specific version number specified by the user
                    applName = parsedAnalysis;
                } else {
                    applName = parsedAnalysis.substring(0, lastHyphen);
                    applVersion = parsedAnalysis.substring(lastHyphen + 1);
                }
                final Matcher m1 = applNameRegex.matcher(applName);

                if (m1.matches() && (applVersion == null || applVersionRegex.matcher(applVersion).matches())) {
                    addApplVersionToUserMap(userAnalysesMap, inputErrorMessages, applName, applVersion);
                } else {
                    inputErrorMessages.add(parsedAnalysis + " not a valid input.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }

            //User specified jobs

            // Now check the user entered analysis versions actually exists
            for (Map.Entry<String, String> mapEntry : userAnalysesMap.entrySet()) {
                String userApplName = mapEntry.getKey();
                String userApplVersion = mapEntry.getValue();
                boolean found = false;

                for (Job job : allJobs.getAnalysisJobs().getJobList()) { // Loop through (not deactivated) analysis jobs
                    SignatureLibraryRelease slr = job.getLibraryRelease();
                    String applName = slr.getLibrary().getName();
                    String applVersion = slr.getVersion();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SignatureLibraryRelease: " + applName + ", " + applVersion);
                    }
                    if (applName.equalsIgnoreCase(userApplName)) {
                        // This analysis name exists, what about the version?
                        if (userApplVersion == null) {
                            // User didn't specify a version, just use the latest (active) version for this analysis
                            // Exactly one version of each member database analysis should be active at a time (TODO write unit test for that)
                            if (job.isActive()) {
                                excludedAnalyses.add(job.getId());
                                job.setDeprecated(true);
                                found = true;
                                break; // Found it!
                            }
                        } else if (applVersion.equalsIgnoreCase(userApplVersion)) {
                            excludedAnalyses.add(job.getId());
                            job.setDeprecated(true);
                            found = true;
                            break; // Found it!
                        }

                    }
                }
                if (!found) {
                    // Didn't find the user specified analysis version
                    inputErrorMessages.add("Analysis " + userApplName + ((userApplVersion == null) ? "" : "-" + userApplVersion) + " does not exist or is deactivated.");
                }
            }
            if (inputErrorMessages.size() > 0) {
                throw new InvalidInputException(inputErrorMessages);
            }
        }
        //System.out.println("excludedAnalyses : " + excludedAnalyses.toString());
        return StringUtils.toStringArray(excludedAnalyses);


    }


    private static void addApplVersionToUserMap(Map<String, String> userAnalysesMap, Set<String> inputErrorMessages, String applName, String applVersion) {
        if (userAnalysesMap.containsKey(applName)) {
            // Multiple versions/entries of the same application are not allowed in the same InterProScan run
            inputErrorMessages.add(applName + " was specified more than once in the same InterProScan run.");
        } else {
            userAnalysesMap.put(applName, applVersion);
        }
    }

    public List<String> checkAnalysesToRun(Map<String, String> userAnalysesMap, Map<String, String> deprecatedNames, Jobs allJobs, Set<String> inputErrorMessages) {
        List<String> analysesToRun = new ArrayList<String>();

        // Now check the user entered analysis versions actually exists
        for (Map.Entry<String, String> mapEntry : userAnalysesMap.entrySet()) {
            String userApplName = mapEntry.getKey();
            String userApplVersion = mapEntry.getValue();
            boolean found = false;
            //deal with deprecated application names
            String possibleUserApplName = deprecatedNames.get(userApplName.toUpperCase());
            if (possibleUserApplName != null) {
                userApplName = possibleUserApplName;
            }
            for (Job job : allJobs.getAnalysisJobs().getJobList()) { // Loop through (not deactivated) analysis jobs
                SignatureLibraryRelease slr = job.getLibraryRelease();
                String applName = slr.getLibrary().getName();
                String applVersion = slr.getVersion();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("SignatureLibraryRelease: " + applName + ", " + applVersion);
                }
                if (applName.equalsIgnoreCase(userApplName)) {
                    // This analysis name exists, what about the version?
                    if (userApplVersion == null) {
                        // User didn't specify a version, just use the latest (active) version for this analysis
                        // Exactly one version of each member database analysis should be active at a time (TODO write unit test for that)
                        if (job.isActive()) {
                            analysesToRun.add(job.getId());
                            found = true;
                            break; // Found it!
                        }
                    } else if (applVersion.equalsIgnoreCase(userApplVersion)) {
                        analysesToRun.add(job.getId());
                        found = true;
                        break; // Found it!
                    }

                }
            }
            if (!found) {
                // Didn't find the user specified analysis version
                inputErrorMessages.add("Analysis " + userApplName + ((userApplVersion == null) ? "" : "-" + userApplVersion) + " does not exist or is deactivated.");
            }
        }

        return analysesToRun;
    }

    private static final String PORT_EXCLUSION_LIST_BEAN_ID = "portExclusionList";

    /**
     * this method selects a random port number to run the TCP broker transport on.
     * <p/>
     * It tests the port is available and then attempts to use it (There is a very small chance
     * that another process will start using this port between checking and using, in which case this method
     * will barfe out and InterProScan will exit non-zero.  This is very unlikely however,
     * as the two InterProScan instances would need to be running on the same host and have
     * randomly picked the same port number at the same moment!)
     * <p/>
     * But never say never...
     *
     * @param ctx the Spring application context.
     * @return the URI as a String for this transport, e.g. tcp://myservername:1901
     */
    private static String configureTCPTransport(final AbstractApplicationContext ctx) {
        LOGGER.info(" configure TCP Transport, start Broker Service ");
        List<Integer> portExclusionList = null;

        if (ctx.containsBean(PORT_EXCLUSION_LIST_BEAN_ID)) {
            final String exclusionString = (String) ctx.getBean(PORT_EXCLUSION_LIST_BEAN_ID);
            if (exclusionString != null && !exclusionString.isEmpty()) {
                final String[] exclusionStringArray = exclusionString.split(",");
                portExclusionList = new ArrayList<Integer>(exclusionStringArray.length);
                for (String portString : exclusionStringArray) {
                    final String trimmedPortString = portString.trim();
                    if (!trimmedPortString.isEmpty()) {
                        try {
                            portExclusionList.add(new Integer(trimmedPortString));
                        } catch (NumberFormatException nfe) {
                            throw new IllegalStateException("Please check that the property 'tcp.port.exclusion.list' is a comma separated list of integer values (port numbers) or is empty. (White space is fine.)", nfe);
                        }
                    }
                }
            }
        }

        // Configure the Broker with a random TCP port number.
        //final BrokerService broker1 = (BrokerService) ctx.getBean("localhostJMSBroker");
        final BrokerService broker = (BrokerService) ctx.getBean("jmsBroker");
        String brokerTmpDataDirectory = broker.getTmpDataDirectory().getAbsolutePath();
        String brokerDataDirectory = broker.getBrokerDataDirectory().getAbsolutePath();
        String dataDirectoryfile = broker.getDataDirectoryFile().getAbsolutePath();

        System.out.println("brokerTmpDataDirectory: " + brokerTmpDataDirectory);
        System.out.println("brokerDataDirectory: " + brokerDataDirectory);
        System.out.println("dataDirectoryfile: " + dataDirectoryfile);

        try {
            // Get hostname
            //get canonical hostname as otherwise hostname may not be exactly how other machines see this host
            final String hostname = InetAddress.getLocalHost().getCanonicalHostName();
            if (Utilities.verboseLogLevel >= 110){
                Utilities.verboseLog(110, "process hostname: " + hostname);
            }

            // Select a random port above 1024, excluding LSF ports and check availability.
            boolean portAssigned = false;
            int port = 0;
            final Random rand = new Random();
            while (!portAssigned) {
                port = rand.nextInt(64510) + 1025; // > 1024 and < 65535
                if (portExclusionList != null && portExclusionList.contains(port)) {
                    continue;
                }
                // Test the port is available on this machine.
                portAssigned = available(port);
            }
            //if this is not a production master, set a random broker name, otherwise you get RMI protocol exception when workers running on the same machine
            //broker.setBrokerName(Utilities.createUniqueJobName(8));

            //Setting transport connector
            final String uriString = new StringBuilder("tcp://").append(hostname).append(':').append(port).toString();
            final TransportConnector tc = new TransportConnector();

            tc.setUri(new URI(uriString));
            broker.addConnector(tc);


            //
            broker.start();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("BrokerService  running at uriString = " + uriString);
            }
            return uriString;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure the TCPTransport on the Broker", e);
        }
    }

    /**
     * Checks to see if a specific port is available.
     * Checks for use both TCP and UDP use of ports.
     *
     * @param port number to check for availability
     * @return true if the port number is available.
     */
    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            // The port is currently not used for anything and is available for TCP / UDP
            return true;
        } catch (IOException e) {
            // Don't need to do anything here - if an Exception is thrown attempting to connect to
            // a port then this method returns false, indicating that the
            // specified port is not available. (Which is the desired behaviour).
            return false;
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }
    }


    public static void configureKVStores(LevelDBStore kvStoreProteins, LevelDBStore kvStoreProteinsNotInLookup, LevelDBStore kvStoreProteinsOther,
                                         LevelDBStore kvStoreMatches, LevelDBStore kvStoreNucleotides, String tempDir) {
        String kvstoreDir = "kvstore";
        String kvstoreBase = tempDir + File.separator + kvstoreDir;
        String kvStoreProteinsDBPath = kvstoreBase + File.separator + kvStoreProteins.getDbName();
        Utilities.verboseLog(110, " kvStoreProteinsDBPath: " + kvStoreProteinsDBPath);
        kvStoreProteins.setLevelDBStore(kvStoreProteinsDBPath);

        String kvStoreProteinsNotInLookupDBPath = kvstoreBase + File.separator + kvStoreProteinsNotInLookup.getDbName();
        Utilities.verboseLog(110, " kvStoreProteinsNotInLookupDBPath: " + kvStoreProteinsNotInLookupDBPath);
        kvStoreProteinsNotInLookup.setLevelDBStore(kvStoreProteinsNotInLookupDBPath);

        //String kvStoreProteinsOtherDBPath = kvstoreBase + File.separator +  kvStoreProteinsOther.getDbName();
        //Utilities.verboseLog(110, "kvStoreProteinsOtherDBPath: " + kvStoreProteinsOtherDBPath);
        //kvStoreProteinsOther.setLevelDBStore(kvStoreProteinsOtherDBPath);

        String kvStoreMatchesDBPath = kvstoreBase + File.separator + kvStoreMatches.getDbName();
        Utilities.verboseLog(110, "kvStoreMatchesDBPath: " + kvStoreMatchesDBPath);
        kvStoreMatches.setLevelDBStore(kvStoreMatchesDBPath);

        String kvStoreNucleotidesDBPath = kvstoreBase + File.separator + kvStoreNucleotides.getDbName();
        Utilities.verboseLog(110, "kvStoreNucleotidesDBPath: " + kvStoreNucleotidesDBPath);
        kvStoreNucleotides.setLevelDBStore(kvStoreNucleotidesDBPath);

    }
    public static void configureKVStoreEntry(LevelDBStore kvStoreEntry, String tempDir, boolean entryDBInitialSetup) {
        if (entryDBInitialSetup){
            //System.out.println(Utilities.getTimeNow() + " getDbPath: " + kvStoreEntry.getDbPath());
            //System.out.println(Utilities.getTimeNow() + " getDbName: " + kvStoreEntry.getDbName());
            String kvstoreBase = kvStoreEntry.getDbPath();
            String kvStoreEntryDBPath = kvstoreBase + File.separator + kvStoreEntry.getDbName();
            try {
                FileUtils.deleteDirectory(new File(kvStoreEntryDBPath));
            } catch (IOException e) {
                LOGGER.error("Unable  to delete/initialise directory " + kvStoreEntryDBPath);
                e.printStackTrace();
            }
            kvStoreEntry.setLevelDBStore(kvStoreEntryDBPath);
            //System.out.println(Utilities.getTimeNow() + " kvStoreEntryDBPath: " + kvStoreEntryDBPath +
            //        " kvStoreEntry.getDbPath(): " + kvStoreEntry.getDbPath());
        } else {
            //System.out.println(Utilities.getTimeNow() + " getDbPath: " + kvStoreEntry.getDbPath());
            //System.out.println(Utilities.getTimeNow() + " getDbName: " + kvStoreEntry.getDbName());
            String kvstoreInstalledBase = kvStoreEntry.getDbPath();
            String kvStoreInstalledEntryDBPath = kvstoreInstalledBase + File.separator + kvStoreEntry.getDbName();

            //System.out.println(Utilities.getTimeNow() + " kvStoreInstalledEntryDBPath: " + kvStoreInstalledEntryDBPath);
            String kvstoreWorkDir = "kvstore";
            String kvstoreWorkBase = tempDir + File.separator + kvstoreWorkDir;
            String kvStoreWorkEntryDBPath = kvstoreWorkBase + File.separator + kvStoreEntry.getDbName();

            File sourceDirectory = new File(kvStoreInstalledEntryDBPath);
            File destinationDirectory = new File(kvStoreWorkEntryDBPath);
            try {
                FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
            } catch (IOException e) {
                LOGGER.error("Unable  to continue, cp installed to workdir " + kvStoreInstalledEntryDBPath +  " to  "  +  kvStoreWorkEntryDBPath);
                e.printStackTrace();
            }

            kvStoreEntry.setLevelDBStore(kvStoreWorkEntryDBPath);
//            System.out.println(Utilities.getTimeNow() + " kvStoreWorkEntryDBPath: " + kvStoreWorkEntryDBPath +
//                    " kvStoreEntry.getDbPath(): " + kvStoreEntry.getDbPath());
        }

    }

    public static void closeKVStores(LevelDBStore kvStoreEntry, LevelDBStore kvStoreProteins, LevelDBStore kvStoreProteinsNotInLookup, LevelDBStore kvStoreProteinsOther,
                                     LevelDBStore kvStoreMatches, LevelDBStore kvStoreNucleotides, String tempDir) {
        kvStoreEntry.close();
        kvStoreProteins.close();
        kvStoreProteinsNotInLookup.close();
        //kvStoreProteinsOther.close();
        kvStoreMatches.close();
        kvStoreNucleotides.close();
    }


    private static boolean isInvalid(final Mode mode, final CommandLine commandline) {
        Option[] options = commandline.getOptions();

        if (options.length == 0) {
            return true;
        } else if (options.length == 1) {
            if (options[0].getLongOpt() == I5Option.USER_DIR.getLongOpt()) {
                return true;
            }
        } else if (!commandline.hasOption(I5Option.INPUT.getLongOpt())) {
            if (mode.equals(Mode.SINGLESEQ) || mode.equals(Mode.STANDALONE) || mode.equals(Mode.DISTRIBUTED_MASTER) || mode.equals(Mode.CLUSTER) || mode.equals(Mode.CONVERT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ideally we should have one config file for the application
     *
     * @param logLevel
     */
    public static void changeLogLevel(String logLevel) {
        //LogManager.getRootLogger().setLevel(Level.WARN);
        /*
        LoggerContext loggerCtx = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerCtx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.WARN);
        loggerCtx.updateLoggers();

        */

        // org.apache.logging.log4j.core.config.Configurator;


        Configurator.setLevel("uk.ac.ebi.interpro.scan", Level.WARN);
        Configurator.setLevel("org.apache.activemq", Level.WARN);
        Configurator.setLevel("org.hibernate.boot", Level.ERROR);  // check if its possoble to configure to not diplsay the WARNING
//        Configurator.setLevel("uk.ac.ebi.interpro.scan", Level.DEBUG);

        // You can also set the root logger:
        Configurator.setRootLevel(Level.WARN);

        //TODO check again, works for now??
        org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
        //org.apache.logging.log4j.LogManager.getRootLogger().se.setLevel(org.apache.logging.log4j.Level.WARN);

//        org.apache.log4j.LogManager.getRootLogger().setLevel("uk.ac.ebi.interpro.scan", org.apache.log4j.Level.WARN);

        return;
        /*
        Logger root = LogManager.getLogger("uk.ac.ebi.interpro.scan");
        //setting the logging level according to input
        if ("FATAL".equalsIgnoreCase(logLevel)) {
            root.setLevel(Level.FATAL);
        }else if ("ERROR".equalsIgnoreCase(logLevel)) {
            root.setLevel(Level.ERROR);
        }else if ("WARN".equalsIgnoreCase(logLevel)) {
            root.setLevel(Level.WARN);
        }else if ("DEBUG".equalsIgnoreCase(logLevel)) {
            root.setLevel(Level.DEBUG);
        }
        */
    }

    /**
     * delete the temporaryWorkingFileDirectory
     *
     * @param dirPath
     * @throws IOException
     */
    private static void deleteWorkingTemporaryDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        try {
            //FileUtils.deleteDirectory(dir);
            FileUtils.forceDelete(dir);
        } catch (IOException e) {
            LOGGER.warn("At Run completion, unable to delete temporary directory " + dir.getAbsolutePath());
        }
    }

    /**
     * if deleteWorkingDirectoryOnCompletion then clean up the temporaryFileDirectory
     *
     * @param deleteWorkingDirectoryOnCompletion
     * @param temporaryFileDirectory
     */
    private static void cleanUpWorkingDirectory(boolean deleteWorkingDirectoryOnCompletion, String temporaryFileDirectory) {
        if (deleteWorkingDirectoryOnCompletion) {
            try {
                if (new File(temporaryFileDirectory).exists()) {
                    LOGGER.debug("Cleaning up temporaryDirectoryName : " + temporaryFileDirectory);
                    Utilities.verboseLog(110, "TemporaryDirectoryName : " + temporaryFileDirectory + " exists, so delet");
                    deleteWorkingTemporaryDirectory(temporaryFileDirectory);
                }
            } catch (IOException e) {
                LOGGER.warn("At Run completion, unable to delete temporary working directory " + temporaryFileDirectory);
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("deleteWorkingDirectoryOnCompletion : " + deleteWorkingDirectoryOnCompletion);
        }
    }


    @Override
    public void finalize() throws Throwable {
        try {
            if (temporaryDirectory != null) {
                //do some cleanup
                cleanUpWorkingDirectory(deleteWorkingDirectoryOnCompletion, temporaryDirectory);
            }
        } finally {
            super.finalize();
        }
    }
}
