package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.io.ExternallySetLocationTemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.converter.Converter;
import uk.ac.ebi.interpro.scan.jms.master.*;
import uk.ac.ebi.interpro.scan.jms.monitoring.MasterControllerApplication;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.JobStatusWrapper;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.*;

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

    private static final Logger LOGGER = Logger.getLogger(Run.class.getName());

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

    static {
        //Usual I5 options
        for (I5Option i5Option : I5Option.values()) {
            OptionBuilder builder = OptionBuilder.withLongOpt(i5Option.getLongOpt())
                    .withDescription(i5Option.getDescription());
            if (i5Option.isRequired()) {
                builder = builder.isRequired();
            }
            if (i5Option.getArgumentName() != null) {
                builder = builder.withArgName(i5Option.getArgumentName());
                if (i5Option.hasMultipleArgs()) {
                    builder = builder.hasArgs();
                } else {
                    builder = builder.hasArg();
                }
            }

            builder = builder.withValueSeparator();

            final Option option = (i5Option.getShortOpt() == null)
                    ? builder.create()
                    : builder.create(i5Option.getShortOpt());

            COMMAND_LINE_OPTIONS.addOption(option);

        }
    }


    public static void main(String[] args) {
        // create the command line parser

        CommandLineParser parser = new PosixParser();
        String modeArgument = null;
        Mode mode = null;
        try {
            // parse the command line arguments
            CommandLine parsedCommandLine = parser.parse(COMMAND_LINE_OPTIONS, args);

            modeArgument = parsedCommandLine.getOptionValue(I5Option.MODE.getLongOpt());

            try {
                mode = (modeArgument != null)
                        ? Mode.valueOf(modeArgument.toUpperCase())
                        : DEFAULT_MODE;
            } catch (IllegalArgumentException iae) {
                LOGGER.fatal("The mode '" + modeArgument + "' is not handled.  Should be one of: " + Mode.getCommaSepModeList());
                System.exit(1);
            }

            for (Option option : (Collection<Option>) COMMAND_LINE_OPTIONS.getOptions()) {
                final String shortOpt = option.getOpt();
                if (I5Option.showOptInHelpMessage(shortOpt, mode)) {
                    COMMAND_LINE_OPTIONS_FOR_HELP.addOption(option);
                }
            }


            System.out.println(Utilities.getTimeNow() + " Welcome to InterProScan-5.8-49.0");
            //String config = System.getProperty("config");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
                LOGGER.info("Running in " + mode + " mode");
            }

            final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{mode.getContextXML()});

            // The command-line distributed mode selects a random port number for communications.
            // This block selects the random port number and sets it on the broker.

            // Def. parsedAnalyses: List of analyses jobs parsed from command line
            String[] parsedAnalyses = null;
            // Def. analysesToRun: List of analyses jobs which will be performed/submitted by I5
            String[] analysesToRun = null;
            if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
                parsedAnalyses = parsedCommandLine.getOptionValues(I5Option.ANALYSES.getLongOpt());
                parsedAnalyses = tidyOptionsArray(parsedAnalyses);
            }


            if (!mode.equals(Mode.INSTALLER) && !mode.equals(Mode.EMPTY_INSTALLER) && !mode.equals(Mode.CONVERT) && !mode.equals(Mode.MONITOR)) {
                Jobs jobs = (Jobs) ctx.getBean("jobs");
                //Get deactivated jobs
                final Map<Job, JobStatusWrapper> deactivatedJobs = jobs.getDeactivatedJobs();
                //Info about active and de-active jobs is shown in the manual instruction (help) as well
                if (isInvalid(mode, parsedCommandLine)) {
                    printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
                    System.out.println("Available analyses:");    // LEAVE as System.out
                    for (Job job : jobs.getActiveAnalysisJobs().getJobList()) {
                        // Print out available jobs
                        System.out.printf("    %25s : %s\n", job.getId().replace("job", ""), job.getDescription());       // LEAVE as System.out
                    }
                    if (deactivatedJobs.size() > 0) {
                        System.out.println("\nDeactivated analyses:");
                    }
                    for (Job deactivatedJob : deactivatedJobs.keySet()) {
                        JobStatusWrapper jobStatusWrapper = deactivatedJobs.get(deactivatedJob);
                        // Print out deactivated jobs
                        System.out.printf("    %25s : %s\n", deactivatedJob.getId().replace("job", ""), jobStatusWrapper.getWarning());
                    }
                    System.exit(1);
                }

                //Before running analyses we need to do some checks
                //The algorithm works as following:
                //1. If analyses are specified via appl parameter:
                //1. a) Existence check - Check if specified analysis name does exist -> print warning if NOT
                //1. b) Job status check (deactivation check) - Check if one of the specified analyses is deactivated or not -> print warning if so
                //2. If analyses are specified via appl parameter or not
                //2. a) Version check - Check if multiple versions of the same analysis occur
                final Map<String, Set<Job>> parsedAnalysesToRealAnalysesMap = getRealAnalysesNames(parsedAnalyses, jobs.getAllJobs().getJobList());

                StringBuilder nonexistentAnalysis = new StringBuilder();
                if (parsedAnalyses != null && parsedAnalyses.length > 0) {
                    //Check job existence and job status (activated or deactivated/not configured properly)
                    boolean doExit = false;
                    for (String parsedAnalysisName : parsedAnalyses) {
                        if (parsedAnalysesToRealAnalysesMap.containsKey(parsedAnalysisName)) {
                            //Check if they are deactivated
                            Set<Job> realAnalyses = parsedAnalysesToRealAnalysesMap.get(parsedAnalysisName);
                            for (Job deactivatedJob : deactivatedJobs.keySet()) {
                                for (Job realAnalysis : realAnalyses) {
                                    if (deactivatedJob.getId().equalsIgnoreCase(realAnalysis.getId())) {
                                        JobStatusWrapper jobStatusWrapper = deactivatedJobs.get(deactivatedJob);
                                        System.out.println("\n\n" + jobStatusWrapper.getWarning() + "\n\n");
                                        doExit = true;
                                    }
                                }
                            }
                        } else {
                            if (nonexistentAnalysis.length() > 0) {
                                nonexistentAnalysis.append(",");
                            }
                            nonexistentAnalysis.append(parsedAnalysisName);
                        }
                    }
                    if (nonexistentAnalysis.length() > 0) {
                        System.out.println("\n\nYou have requested the following analyses / applications that are not available in this distribution of InterProScan: " + nonexistentAnalysis.toString() + ".  Please run interproscan.sh with no arguments for a list of available analyses.\n\n");
                        doExit = true;
                    }
                    if (doExit) {
                        System.exit(1);
                    }

                    //Do multiple version check (e.g. if -appl pirsf-2.84,pirsf-3.01 I5 will exit)
                    Set<Job> jobsToCheckMultipleVersionsSet = new HashSet<Job>();
                    for (Set<Job> jobsToCheck : parsedAnalysesToRealAnalysesMap.values()) {
                        jobsToCheckMultipleVersionsSet.addAll(jobsToCheck);
                    }
                    List<Job> jobsToCheckMultipleVersionsList = new ArrayList<Job>(jobsToCheckMultipleVersionsSet);
                    checkAnalysisJobsVersions(jobsToCheckMultipleVersionsList);
                }
                analysesToRun = getAnalysesToRun(parsedAnalysesToRealAnalysesMap);
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
            if (parsedCommandLine.hasOption(I5Option.SEQUENCE_TYPE.getLongOpt())) {
                sequenceType = parsedCommandLine.getOptionValue(I5Option.SEQUENCE_TYPE.getLongOpt());

                // Check the sequence type is "n" or "p"
                Set<String> sequenceTypes = (HashSet<String>) ctx.getBean("sequenceTypes");
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

                System.out.println(Utilities.getTimeNow() + " Running InterProScan v5 in " + mode + " mode...");

                runnable.run();
            }
            System.exit(0);

        } catch (ParseException exp) {
            LOGGER.fatal("Exception thrown when parsing command line arguments.  Error message: " + exp.getMessage());
            printHelp(COMMAND_LINE_OPTIONS_FOR_HELP);
            System.exit(1);
        }
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
                checkDirectoryExistenceAndFileWritePermission(temporaryDirectory, I5Option.TEMP_DIRECTORY.getShortOpt());
                master.setTemporaryDirectory(temporaryDirectory);
            }
            checkIfProductionMasterAndConfigure(master, parsedCommandLine, ctx);
            checkIfBlackBoxMasterAndConfigure(master, parsedCommandLine, parsedOutputFormats, ctx, mode, sequenceType);
        }
    }


    private static void checkIfProductionMasterAndConfigure(
            final Master master,
            final CommandLine parsedCommandLine,
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
            LOGGER.debug("Setting up the distributed black box master...");
            String tcpConnectionString = null;
            if (mode == Mode.CL_MASTER || mode == Mode.DISTRIBUTED_MASTER || mode == Mode.CLUSTER) {
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
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    String logDir = ((DistributedBlackBoxMaster) bbMaster).getLogDir() + "/" + projectId.replaceAll("\\s+", "");
                    ((DistributedBlackBoxMaster) bbMaster).setLogDir(logDir);
                    ((DistributedBlackBoxMaster) bbMaster).setSubmissionWorkerLogDir(logDir);

                } else {
                    LOGGER.fatal("InterProScan 5 in CLUSTER mode needs a Cluster Run ID to continue, please specify the -clusterrunid (-crid) option.");
                    System.exit(1);
                }
            }

            if (bbMaster instanceof SingleSeqOptimisedBlackBoxMaster) {
                if (parsedCommandLine.hasOption(I5Option.CLUSTER_RUN_ID.getLongOpt())) {
                    final String runId = parsedCommandLine.getOptionValue(I5Option.CLUSTER_RUN_ID.getLongOpt());
                    ((SingleSeqOptimisedBlackBoxMaster) master).setRunId(runId);
//                    final ResourceMonitor resourceMonitor = (ResourceMonitor) ctx.getBean("resourceMonitor");
//                    resourceMonitor.setRunId(runId);
                }
            }

            if (parsedCommandLine.hasOption(I5Option.SEQUENCE_TYPE.getLongOpt())) {
                bbMaster.setSequenceType(sequenceType);
            }

            if (parsedCommandLine.hasOption(I5Option.MIN_SIZE.getLongOpt())) {
                bbMaster.setMinSize(parsedCommandLine.getOptionValue(I5Option.MIN_SIZE.getLongOpt()));
            }

            if (parsedCommandLine.hasOption(I5Option.DISABLE_PRECALC.getLongOpt())) {
                bbMaster.disablePrecalc();
            }

            // GO terms and/or pathways will also imply IPR lookup
            final boolean mapToGo = parsedCommandLine.hasOption(I5Option.GOTERMS.getLongOpt());
            bbMaster.setMapToGOAnnotations(mapToGo);
            final boolean mapToPathway = parsedCommandLine.hasOption(I5Option.PATHWAY_LOOKUP.getLongOpt());
            bbMaster.setMapToPathway(mapToPathway);
            bbMaster.setMapToInterProEntries(mapToGo || mapToPathway || parsedCommandLine.hasOption(I5Option.IPRLOOKUP.getLongOpt()));
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
                checkFileExistence(fastaFilePath, I5Option.INPUT.getShortOpt());
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
            String outputBaseFileName = getAbsoluteFilePath(defaultOutputFileName, parsedCommandLine);
            checkDirectoryExistence(outputBaseFileName, I5Option.USER_DIR.getShortOpt());
        }

        // Get the value for the (-b) option if specified
        if (haveSetBaseOutputFileName) {
            if (haveSetOutputFileName || haveSetOutputDirName) {
                System.out.println("The options --output-file-base (-b), --outfile (-o) and --output-dir (-d) are mutually exclusive.");
                System.exit(3);
            }
            String outputBaseFileName = parsedCommandLine.getOptionValue(I5Option.BASE_OUT_FILENAME.getLongOpt());
            // If outputBaseFileName is a directory (simply check the ending) then set the defaultFileOutputName
            if (outputBaseFileName.endsWith("/")) {
                outputBaseFileName += defaultOutputFileName;
            }
            outputBaseFileName = getAbsoluteFilePath(outputBaseFileName, parsedCommandLine);
            checkDirectoryExistenceAndFileWritePermission(outputBaseFileName, I5Option.BASE_OUT_FILENAME.getShortOpt());
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
                checkDirectoryExistenceAndFileWritePermission(explicitOutputFilename, I5Option.OUTPUT_FILE.getShortOpt());
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
            if (!outputDirValue.endsWith("/")) {
                outputDirValue += "/";
            }
            outputDirValue += defaultOutputFileName;
            String outputBaseFileName = getAbsoluteFilePath(outputDirValue, parsedCommandLine);
            checkDirectoryExistenceAndFileWritePermission(outputBaseFileName, I5Option.OUTPUT_DIRECTORY.getShortOpt());
            master.setOutputBaseFilename(outputBaseFileName);
        }
        // If the (-b) or (-d) or (-o) options AREN'T specified, but the (-u) option is set (it should always be set)
        // then the default file output path will be (USER_DIR + defaultFileOutputName)
        else if (haveSetUserDirName) {
            String outputBaseFileName = getAbsoluteFilePath(defaultOutputFileName, parsedCommandLine);
            // Write to the default output location
            checkDirectoryExistenceAndFileWritePermission(outputBaseFileName, I5Option.USER_DIR.getShortOpt());
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
        if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
            checkDirectoryExistenceAndFileWritePermission(temporaryDirectory, I5Option.TEMP_DIRECTORY.getShortOpt());
        }
        master.setTemporaryDirectory(temporaryDirectory);
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
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                String logDir = worker.getLogDir() + "/" + projectId.replaceAll("\\s+", "");
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
     * Checks if different versions of the same analyses occur.
     *
     * @param jobsToCheckMultipleVersion
     */
    protected static void checkAnalysisJobsVersions(List<Job> jobsToCheckMultipleVersion) {
        final Map<SignatureLibrary, Set<Job>> libraryToJobsMap = clusterJobsBySignatureLibrary(jobsToCheckMultipleVersion);
        //Iterate over all signature libraries Pfam, Gene3D, PIRSF etc.
        for (SignatureLibrary library : libraryToJobsMap.keySet()) {
            //Get all jobs for a certain signature library e.g. job to run PIRSF v2.84 and another job to run PIRSF v3.01
            final Set<Job> libraryJobs = libraryToJobsMap.get(library);
            if (libraryJobs.size() > 1) {
                String versions = "";
                for (Job jobToCheck : libraryJobs) {
                    versions = versions + jobToCheck.getLibraryRelease().getVersion() + ",";
                }
                System.out.println("\n\n" + "Found different versions (" + versions + ") of the same analysis - " + library +
                        " which is not allowed in this version of InterProScan 5." + "\n\n");
                System.exit(1);
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
     * @return Array of analyses jobs.
     */
    private static String[] getAnalysesToRun(final Map<String, Set<Job>> parsedAnalysesRealAnalysesMap) {
        Set<String> result = new HashSet<String>();
        for (Set<Job> realJobs : parsedAnalysesRealAnalysesMap.values()) {
            for (Job realJob : realJobs) {
                result.add(realJob.getId());
            }
        }
        return StringUtils.toStringArray(result);
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
        final BrokerService broker = (BrokerService) ctx.getBean("jmsBroker");
        try {
            // Get hostname
            //get canonical hostname as otherwise hostname may not be exactly how other machines see this host
            final String hostname = InetAddress.getLocalHost().getCanonicalHostName();

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

}
