package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.activemq.ActiveMQConnectionFactory;
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
import uk.ac.ebi.interpro.scan.jms.master.BlackBoxMaster;
import uk.ac.ebi.interpro.scan.jms.master.DistributedBlackBoxMaster;
import uk.ac.ebi.interpro.scan.jms.master.DistributedBlackBoxMasterCopy;
import uk.ac.ebi.interpro.scan.jms.master.Master;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.JobStatusWrapper;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

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

public class Run {

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

    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    private static final String HELP_MESSAGE_TITLE =
            "java -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods " +
                    "-Xms512M -Xmx2048M -jar interproscan-5.jar";
    private static final String HEADER =
            "\n\nPlease give us your feedback by sending an email to\n\ninterproscan-5-dev@googlegroups.com\n\n";
    private static final String FOOTER = "Copyright (c) EMBL European Bioinformatics Institute, Hinxton, Cambridge, UK. (http://www.ebi.ac.uk) " +
            "The InterProScan software itself is " +
            "provided under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html). " +
            "Third party components (e.g. member database binaries and models) are subject to separate licensing - " +
            "please see the individual member database websites for details.\n\n";

// [-Dconfig=/path/to/config.properties]

    private static final int MEGA = 1024 * 1024;

    /**
     * Additional options:
     * -iprlookup    Switch on look up of corresponding InterPro annotation
     * <p/>
     * -goterms      Switch on look up of corresponding Gene Ontology
     * annotation (requires -iprlookup option to be used too)
     * <p/>
     * -pathways    Switch on look up of corresponding Pathway annotation  (requires -iprlookup option to be used too)
     */
    private enum I5Option {
        MODE("mode", "m", false, "Optional, the mode in which InterProScan is being run, the default mode is " + Mode.STANDALONE.getRunnableBean() + ". Must be one of: " + Mode.getCommaSepModeList() + ".", "MODE-NAME", false, false),
        FASTA("fasta", "i", false, "Optional, path to fasta file that should be loaded on Master startup.", "FASTA-FILE-PATH", false, true),
        OUTPUT_FORMATS("formats", "f", false, "Optional, case-insensitive, comma separated list of output formats. Supported formats are TSV, XML, GFF3 and HTML. Default for protein sequences are TSV, XML and GFF3, or for nucleotide sequences GFF3 and XML.", "OUTPUT-FORMATS", true, true),
        BASE_OUT_FILENAME("output-file-base", "b", false, "Optional, base output filename.  Note that this option and the --outfile (-o) option are mutually exclusive.  The appropriate file extension for the output format(s) will be appended automatically. By default the input file path/name will be used.", "OUTPUT-FILE-BASE", false, true),
        OUTPUT_FILE("outfile", "o", false, "Optional explicit output file name.  Note that this option and the --output-file-base (-b) option are mutually exclusive. If this option is given, you MUST specify a single output format using the -f option.  The output file name will not be modified. Note that specifying an output file name using this option OVERWRITES ANY EXISTING FILE.", "EXPLICIT_OUTPUT_FILENAME", false, true),
        ANALYSES("applications", "appl", false, "Optional, comma separated list of analyses.  If this option is not set, ALL analyses will be run. ", "ANALYSES", true, true),
        PRIORITY("priority", "p", false, "Minimum message priority that the worker will accept (0 low -> 9 high).", "JMS-PRIORITY", false, false),
        IPRLOOKUP("iprlookup", "iprlookup", false, "Switch on look up of corresponding InterPro annotation.", null, false, true),
        GOTERMS("goterms", "goterms", false, "Optional, switch on look up of corresponding Gene Ontology annotation (IMPLIES -iprlookup option)", null, false, true),
        PATHWAY_LOOKUP("pathways", "pa", false, "Optional, switch on look up of corresponding Pathway annotation (IMPLIES -iprlookup option)", null, false, true),
        MASTER_URI("masteruri", "masteruri", false, "The TCP URI of the Master.", "MASTER-URI", false, false),
        SEQUENCE_TYPE("seqtype", "t", false, "Optional, the type of the input sequences (dna/rna (n) or protein (p)).  The default sequence type is protein.", "SEQUENCE-TYPE", false, true),
        MIN_SIZE("minsize", "ms", false, "Optional, minimum nucleotide size of ORF to report. Will only be considered if n is specified as a sequence type. " +
                "Please be aware of the fact that if you specify a too short value it might be that the analysis takes a very long time!", "MINIMUM-SIZE", false, true),
        TEMP_DIRECTORY_NAME("tempdirname", "td", false, "Optional, used to start up a worker with the correct temporary directory.", "TEMP-DIR-NAME", false, false),
        TEMP_DIRECTORY("tempdir", "T", false, "Optional, specify temporary file directory. The default location is /temp.", "TEMP-DIR", false, true),
        DISABLE_PRECALC("disable-precalc", "dp", false, "Optional.  Disables use of the precalculated match lookup service.  All match calculations will be run locally.", null, false, true),
        HIGH_MEM("highmem", "hm", false, "Optional, switch on the creation of a high memory worker. Please note normal and high mem workers share the same Spring configuration file.", null, false, false),
        TIER1("tier1", "tier1", false, "Optional, switch to indicate the high memory worker is a child of the master.", null, false, false);

        private String longOpt;

        private boolean multipleArgs;

        private String shortOpt;

        private boolean required;

        private String description;

        private String argumentName;

        private boolean includeInUsageMessage;

        private I5Option(
                String longOpt,
                String shortOpt,
                boolean required,
                String description,
                String argumentName,
                boolean multipleArgs,
                boolean includeInUsageMessage
        ) {
            this.longOpt = longOpt;
            this.shortOpt = shortOpt;
            this.required = required;
            this.description = description;
            this.argumentName = argumentName;
            this.multipleArgs = multipleArgs;
            this.includeInUsageMessage = includeInUsageMessage;
        }

        public String getLongOpt() {
            return longOpt;
        }

        public String getShortOpt() {
            return shortOpt;
        }

        public boolean isRequired() {
            return required;
        }

        public String getDescription() {
            return description;
        }

        public String getArgumentName() {
            return argumentName;
        }

        public boolean hasMultipleArgs() {
            return multipleArgs;
        }

        public boolean isIncludeInUsageMessage() {
            return includeInUsageMessage;
        }
    }

    private enum Mode {
        //Mode for InterPro production
        MASTER("master", "spring/jms/activemq/activemq-distributed-master-context.xml"),
        //?
        WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-context.xml"),
        DISTRIBUTED_WORKER("distributedWorkerController", "spring/jms/worker/distributed-worker-context.xml"),
        HIGHMEM_WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-highmem-context.xml"),
        //Default mode. Mode to run the I5 black box version
        STANDALONE("standalone", "spring/jms/activemq/activemq-standalone-master-context.xml"),
        //This mode allows spawning of distributed workers on demand using the new i5jms architecture
        DISTRIBUTED_MASTER("distributedMaster", "spring/jms/master/distributed-master-context.xml"),
        CL_MASTER("clDist", "spring/jms/activemq/command-line-distributed-master-context.xml"),
        CL_WORKER("distributedWorkerController", "spring/jms/activemq/cl-dist-worker-context.xml"),
        CL_HIGHMEM_WORKER("distributedWorkerController", "spring/jms/activemq/cl-dist-high-mem-worker-context.xml"),
        MONITOR("monitor", "spring/monitor/monitor-context.xml"),
        INSTALLER("installer", "spring/installer/installer-context.xml"),
        // Use this mode for creating the test database that lives in /jms-implementation/src/test/resources/
        EMPTY_INSTALLER("installer", "spring/installer/empty-installer-context.xml");
        //
        private String contextXML;

        private String runnableBean;

        private static String commaSepModeList;

        static {
            StringBuilder sb = new StringBuilder();
            for (Mode mode : Mode.values()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(mode.toString().toLowerCase());
            }
            commaSepModeList = sb.toString();
        }

        /**
         * Constructor for modes.
         *
         * @param runnableBean Optional bean that implements Runnable.
         * @param contextXml   being the Spring context.xml file to load.
         */
        private Mode(String runnableBean, String contextXml) {
            this.runnableBean = runnableBean;
            this.contextXML = contextXml;
        }

        public String getRunnableBean() {
            return runnableBean;
        }

        public String getContextXML() {
            return contextXML;
        }

        public static String getCommaSepModeList() {
            return commaSepModeList;
        }
    }

    private static final Mode DEFAULT_MODE = Mode.STANDALONE;


    static {
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

            if (i5Option.includeInUsageMessage) {
                COMMAND_LINE_OPTIONS_FOR_HELP.addOption(option);
            }
        }
    }


    public static void main(String[] args) {
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        String modeArgument = null;
        try {
            // parse the command line arguments
            CommandLine parsedCommandLine = parser.parse(COMMAND_LINE_OPTIONS, args);

            modeArgument = parsedCommandLine.getOptionValue(I5Option.MODE.getLongOpt());

            final Mode mode = (modeArgument != null)
                    ? Mode.valueOf(modeArgument.toUpperCase())
                    : DEFAULT_MODE;

            System.out.println("Welcome to InterProScan v5.");
            //String config = System.getProperty("config");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
                LOGGER.info("Running as: " + mode);
            }

            final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{mode.getContextXML()});

            // The command-line distributed mode selects a random port number for communications.
            // This block selects the random port number and sets it on the broker.


            String[] parsedAnalyses = null;
            if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
                parsedAnalyses = parsedCommandLine.getOptionValues(I5Option.ANALYSES.getLongOpt());
                parsedAnalyses = tidyOptionsArray(parsedAnalyses);
            }


            if (mode != Mode.INSTALLER && mode != Mode.EMPTY_INSTALLER) {
                Jobs jobs = (Jobs) ctx.getBean("jobs");
                //Get deactivated jobs
                final Map<Job, JobStatusWrapper> deactivatedJobs = jobs.getDeactivatedJobs();
                //Info about active and de-active jobs is shown in the manual instruction (help) as well
                if (args.length == 0) {
                    printHelp();
                    System.out.println("Available analyses:");    // LEAVE as System.out
                    for (Job job : jobs.getAnalysisJobs().getJobList()) {
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
                final Map<String, Set<Job>> parsedAnalysesRealAnalysesMap = getRealAnalysesNames(parsedAnalyses, jobs.getAllJobs().getJobList());

                //Existence and job status checks
                StringBuilder nonexistentAnalysis = new StringBuilder();
                if (parsedAnalyses != null && parsedAnalyses.length > 0) {
                    boolean doExit = false;
                    for (String parsedAnalysisName : parsedAnalyses) {
                        if (parsedAnalysesRealAnalysesMap.containsKey(parsedAnalysisName)) {
                            //Check if they are deactivated
                            Set<Job> realAnalyses = parsedAnalysesRealAnalysesMap.get(parsedAnalysisName);
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
                }

                parsedAnalyses = getAnalysesToRun(parsedAnalysesRealAnalysesMap);
                String analysesPrintOutStr = "Running the following analyses:\n";
                //Version check
                if (parsedAnalyses.length > 0) {
                    Set<Job> jobsToCheckMultipleVersionsSet = new HashSet<Job>();
                    for (Set<Job> jobsToCheck : parsedAnalysesRealAnalysesMap.values()) {
                        jobsToCheckMultipleVersionsSet.addAll(jobsToCheck);
                    }
                    List<Job> jobsToCheckMultipleVersionsList = new ArrayList<Job>(jobsToCheckMultipleVersionsSet);
                    checkAnalysisJobsVersions(jobsToCheckMultipleVersionsList);
                    System.out.println(analysesPrintOutStr + Arrays.asList(parsedAnalyses));
                } else {
                    checkAnalysisJobsVersions(jobs.getAnalysisJobs().getJobList());
                    System.out.println(analysesPrintOutStr + jobs.getAnalysisJobs().getJobIdList());
                }
            } // End installer mode check

            // Validate the output formats supplied
            String[] parsedOutputFormats = null;
            if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMATS.getLongOpt())) {
                parsedOutputFormats = parsedCommandLine.getOptionValues(I5Option.OUTPUT_FORMATS.getLongOpt());
                parsedOutputFormats = tidyOptionsArray(parsedOutputFormats);
                validateOutputFormatList(parsedOutputFormats);
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

                // Check output format is valid for this sequence type
                if (sequenceType.equalsIgnoreCase("n")) {
                    if (parsedOutputFormats != null) {
                        for (String outputFormat : parsedOutputFormats) {
                            if (outputFormat.equalsIgnoreCase("tsv") || outputFormat.equalsIgnoreCase("html")) {
                                System.out.println("\n\nTSV and HTML formats are not supported if you run I5 against nucleotide sequences. Supported formats are GFF3 and XML.");
                                System.exit(1);
                            }
                        }
                    }
                }
            }

            if (mode.getRunnableBean() != null) {
                final Runnable runnable = (Runnable) ctx.getBean(mode.getRunnableBean());

                checkIfMasterAndConfigure(runnable, parsedAnalyses, parsedCommandLine, parsedOutputFormats, ctx, mode, sequenceType);

                checkIfDistributedWorkerAndConfigure(runnable, parsedCommandLine, ctx, mode);

                System.out.println("Running InterProScan v5 in " + mode + " mode...");

                runnable.run();
            }
            System.exit(0);

        } catch (ParseException exp) {
            LOGGER.fatal("Exception thrown when parsing command line arguments.  Error message: " + exp.getMessage());
            printHelp();
            System.exit(1);
        } catch (IllegalArgumentException iae) {
            LOGGER.fatal("The mode '" + modeArgument + "' is not handled.  Should be one of: " + Mode.getCommaSepModeList());
            System.exit(1);
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
            //process tmp dir parameter capital T
            if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
                master.setTemporaryDirectory(parsedCommandLine.getOptionValue(I5Option.TEMP_DIRECTORY.getLongOpt()));
            }

            checkIfBlackBoxMasterAndConfigure(master, parsedCommandLine, parsedOutputFormats, ctx, mode, sequenceType);
        }
    }

    private static void checkIfBlackBoxMasterAndConfigure(
            final Master master,
            final CommandLine parsedCommandLine,
            final String[] parsedOutputFormats,
            final AbstractApplicationContext ctx,
            final Mode mode,
            final String sequenceType
    ) {
        if (master instanceof BlackBoxMaster) {
            boolean haveSetBaseOutputFileName = false;

            BlackBoxMaster bbMaster = (BlackBoxMaster) master;
            if (parsedCommandLine.hasOption(I5Option.FASTA.getLongOpt())) {
                bbMaster.setFastaFilePath(parsedCommandLine.getOptionValue(I5Option.FASTA.getLongOpt()));
            }
            if (parsedCommandLine.hasOption(I5Option.BASE_OUT_FILENAME.getLongOpt())) {
                bbMaster.setOutputBaseFilename(parsedCommandLine.getOptionValue(I5Option.BASE_OUT_FILENAME.getLongOpt()));
                haveSetBaseOutputFileName = true;
            }
            if (parsedCommandLine.hasOption(I5Option.OUTPUT_FILE.getLongOpt())) {
                if (parsedOutputFormats == null || parsedOutputFormats.length != 1 || "html".equalsIgnoreCase(parsedOutputFormats[0])) {
                    System.out.println("\n\nYou must indicate a single output format excluding HTML, using the -f option if you wish to set an explicit output file name.");
                    System.exit(2);
                }

                if (haveSetBaseOutputFileName) {
                    System.out.println("The --output-file-base (-b) and --outfile (-o) options are mutually exclusive.");
                    System.exit(3);
                }
                bbMaster.setExplicitOutputFilename(parsedCommandLine.getOptionValue(I5Option.OUTPUT_FILE.getLongOpt()));
            }
            if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMATS.getLongOpt())) {
                bbMaster.setOutputFormats(parsedOutputFormats);
            }
            String tcpConnectionString = null;
            if (mode == Mode.CL_MASTER || mode==Mode.DISTRIBUTED_MASTER) {
                tcpConnectionString = configureTCPTransport(ctx);
            }

            if (bbMaster instanceof DistributedBlackBoxMaster && tcpConnectionString != null) {
                ((DistributedBlackBoxMaster) bbMaster).setTcpUri(tcpConnectionString);
            }
            //TODO: The copy of the distributed master will retire someday (if distributed computing works fine)
            if (bbMaster instanceof DistributedBlackBoxMasterCopy && tcpConnectionString != null) {
                ((DistributedBlackBoxMasterCopy) bbMaster).setTcpUri(tcpConnectionString);
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

    private static void checkIfDistributedWorkerAndConfigure(final Runnable runnable,
                                                             final CommandLine parsedCommandLine,
                                                             final AbstractApplicationContext ctx,
                                                             final Mode mode) {
        if (runnable instanceof WorkerImpl) {
//                    if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt()) || parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
            final WorkerImpl worker = (WorkerImpl) runnable;
            if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt())) {
                final int priority = Integer.parseInt(parsedCommandLine.getOptionValue(I5Option.PRIORITY.getLongOpt()));
                if (priority < 0 || priority > 9) {
                    throw new IllegalStateException("The JMS priority value must be an integer between 0 and 9.  The value passed in is " + priority);
                }
                worker.setMinimumJmsPriority(priority);
            }

            //start the local activemq broker
            String tcpConnectionString=null;
            if (mode == Mode.DISTRIBUTED_WORKER) {
                tcpConnectionString = configureTCPTransport(ctx);
            }
            //set the tcpUri for the worker
            if ( tcpConnectionString != null) {
                worker.setTcpUri(tcpConnectionString);
            }
            //set high memory option
            worker.setHighMemory(parsedCommandLine.hasOption(I5Option.HIGH_MEM.getLongOpt()));

            //set master worker
            worker.setMasterWorker(parsedCommandLine.hasOption(I5Option.TIER1.getLongOpt()));

            //set the master uri
            //Please note: Make sure you set the master worker flag and the high memory flag before you set the master URI
            if (parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
                final String masterUri = parsedCommandLine.getOptionValue(I5Option.MASTER_URI.getLongOpt());
                ActiveMQConnectionFactory activeMQConnectionFactory = worker.setMasterUri(masterUri);
                //want to change the remoteFactory
            }

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
     * Validate and tidy the comma separated list of output formats specified by the user:
     * - Do the formats exist?
     * - Is the format valid for this sequence type?
     *
     * @return The tidied list of file extensions
     */
    private static void validateOutputFormatList(String[] outputFormats) {
        // TODO With org.apache.commons.cli v2 could use EnumValidator instead, but currently we use cli v1.2
        if (outputFormats != null && outputFormats.length > 0) {
            // The user manually specified at least one output format, now check it's OK
            for (String outputFormat : outputFormats) {
                if (!FileOutputFormat.isExtensionValid(outputFormat)) {
                    System.out.println("\n\n" + "The specified output file format " + outputFormat + " was not recognised." + "\n\n");
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
    private static void checkAnalysisJobsVersions(List<Job> jobsToCheckMultipleVersion) {
        final Map<SignatureLibrary, Set<Job>> libraryToJobsMap = clusterJobsBySignatureLibrary(jobsToCheckMultipleVersion);
        //
        for (SignatureLibrary library : libraryToJobsMap.keySet()) {
            if (libraryToJobsMap.get(library).size() > 1) {
                String previousVersion = null;
                String currentAnalysisVersion = null;
                for (Job jobToCheck : libraryToJobsMap.get(library)) {
                    currentAnalysisVersion = jobToCheck.getLibraryRelease().getVersion();
                    if (previousVersion == null) {
                        previousVersion = currentAnalysisVersion;
                    }
                }
                System.out.println("\n\n" + "Found different versions (e.g. " + previousVersion + " AND " + currentAnalysisVersion + ") of the same analysis - " + library +
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
     * Determines real jobs for the parsed analyses names, e.g. Pfam -> Pfam-26.0
     *
     * @param parsedAnalyses
     * @param realJobs
     * @return Map of parsed analysis name to real analysis name.
     */
    private static Map<String, Set<Job>> getRealAnalysesNames(String[] parsedAnalyses, List<Job> realJobs) {
        Map<String, Set<Job>> result = new HashMap<String, Set<Job>>();
        if (parsedAnalyses != null && parsedAnalyses.length > 0) {
            for (String analysisName : parsedAnalyses) {
                for (Job realJob : realJobs) {
                    if (realJob.getId().toLowerCase().contains(analysisName.toLowerCase())) {
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
            final String hostname = InetAddress.getLocalHost().getHostName();

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
            //Set a random broker name, otherwise you get RMI protocol exception when workers running on the same machine
            //broker.setBrokerName(Utilities.createUniqueJobName(8));

            //Setting transport connector
            final String uriString = new StringBuilder("tcp://").append(hostname).append(':').append(port).toString();
            final TransportConnector tc = new TransportConnector();
            tc.setUri(new URI(uriString));
            broker.addConnector(tc);
            //
            broker.start();
            System.out.println("uriString = " + uriString);
            return uriString;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure the TCPTransport on the Broker", e);
        }
    }


    private static void printHelp() {
        HELP_FORMATTER.printHelp(HELP_MESSAGE_TITLE, HEADER, COMMAND_LINE_OPTIONS_FOR_HELP, FOOTER);
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
}
