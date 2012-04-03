package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.io.ExternallySetLocationTemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.activemq.DistributedWorkerController;
import uk.ac.ebi.interpro.scan.jms.master.Master;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.JobStatusWrapper;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

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

    private static final Options COMMAND_LINE_OPTIONS = new Options();

    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    private static final String HELP_MESSAGE_TITLE =
            "java -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods " +
                    "-Xms512M -Xmx2048M -jar interproscan-5.jar";
    private static final String HEADER =
            "\n\nPlease note that this is a BETA RELEASE and should NOT be used for production purposes - please continue to use " +
                    "InterProScan version 4, which can be obtained from\n\nftp://ftp.ebi.ac.uk/pub/databases/interpro/iprscan\n\n" +
                    "Please give us your feedback by sending an email to\n\ninterproscan-5-dev@googlegroups.com\n\n";
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
        MODE("mode", "m", false, "MANDATORY Mode in which InterProScan is being run.  Must be one of: " + Mode.getCommaSepModeList(), "MODE-NAME", false),
        FASTA("fasta", "i", false, "Optional path to fasta file that should be loaded on Master startup.", "FASTA-FILE-PATH", false),
        OUTPUT_FORMATS("format", "F", false, "Optional comma separated list of output formats. Supported formats are TSV, XML, GFF3 and HTML. Default for protein sequences is all formats, or for nucleotide sequence scan GFF3 and XML.", "OUTPUT-FORMATS", true),
        OUT_FILE("out-file", "o", false, "Optional output file path/name (the file extension for the output format will be added automatically).", "OUTPUT-FILE-PATH", false),
        ANALYSES("analyses", "appl", false, "Optional comma separated list of analyses.  If this option is not set, ALL analyses will be run. ", "ANALYSES", true),
        PRIORITY("priority", "p", false, "Minimum message priority that the worker will accept. (0 low -> 9 high)", "JMS-PRIORITY", false),
        IPRLOOKUP("iprlookup", "iprlookup", false, "Switch on look up of corresponding InterPro annotation", null, false),
        GOTERMS("goterms", "goterms", false, "Switch on look up of corresponding Gene Ontology annotation (IMPLIES -iprlookup option)", null, false),
        PATHWAY_LOOKUP("pathways", "pa", false, "Switch on look up of corresponding Pathway annotation (IMPLIES -iprlookup option)", null, false),
        MASTER_URI("masteruri", "masteruri", false, "The TCP URI of the Master.", "MASTER-URI", false),
        SEQUENCE_TYPE("seqtype", "t", false, "The type of the input sequences (dna/rna (n) or protein (p)).", "SEQUENCE-TYPE", false),
        MIN_SIZE("minsize", "ms", false, "Minimum nucleotide size of ORF to report. Will only be considered if n is specified as a sequence type. " +
                "Please be aware of the fact that if you specify a too short value it might be that the analysis takes a very long time!", "MINIMUM-SIZE", false),
        TEMP_DIRECTORY("tempdirname", "td", false, "Used to start up a worker with the correct temporary directory.", "TEMP-DIR-NAME", false);

        private String longOpt;

        private boolean multipleArgs;

        private String shortOpt;

        private boolean required;

        private String description;

        private String argumentName;

        private I5Option(
                String longOpt,
                String shortOpt,
                boolean required,
                String description,
                String argumentName,
                boolean multipleArgs
        ) {
            this.longOpt = longOpt;
            this.shortOpt = shortOpt;
            this.required = required;
            this.description = description;
            this.argumentName = argumentName;
            this.multipleArgs = multipleArgs;
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
    }

    private enum Mode {
        MASTER("master", "spring/jms/activemq/activemq-distributed-master-context.xml"),
        WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-context.xml"),
        HIGHMEM_WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-highmem-context.xml"),
        STANDALONE("standalone", "spring/jms/activemq/activemq-standalone-master-context.xml"),
        CL_MASTER("clDist", "spring/jms/activemq/command-line-distributed-master-context.xml"),
        CL_WORKER("distributedWorkerController", "spring/jms/activemq/cl-dist-worker-context.xml"),
        CL_HIGHMEM_WORKER("distributedWorkerController", "spring/jms/activemq/cl-dist-high-mem-worker-context.xml"),
        MONITOR("monitor", "spring/monitor/monitor-context.xml"),
        INSTALLER("installer", "spring/installer/installer-context.xml");
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

            builder = OptionBuilder.withValueSeparator();

            COMMAND_LINE_OPTIONS.addOption(
                    (i5Option.getShortOpt() == null)
                            ? builder.create()
                            : builder.create(i5Option.getShortOpt()));
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
//
//            if (config == null) {
//                LOGGER.info("No custom config used. Use java -Dconfig=config/my.properties");
//            } else {
//                LOGGER.info("Custom config: " + config);
//            }

            final AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{mode.getContextXML()});

            // The command-line distributed mode selects a random port number for communications.
            // This block selects the random port number and sets it on the broker.
            String tcpConnectionString = null;
            if (mode == Mode.CL_MASTER) {
                tcpConnectionString = configureTCPTransport(ctx);
            }

            Jobs jobs = (Jobs) ctx.getBean("jobs");
            //Get deactivated jobs
            final Map<Job, JobStatusWrapper> deactivatedJobs = jobs.getDeactivatedJobs();
            //Info about active and de-active jobs is shown in the manual instruction (help) as well
            if (args.length == 0) {
                printHelp();
                System.out.println("Available analyses in this installation:");    // LEAVE as System.out
                for (Job job : jobs.getAnalysisJobs().getJobList()) {
                    // Print out available jobs
                    System.out.printf("    %20s : %s\n", job.getId().replace("job", ""), job.getDescription());       // LEAVE as System.out
                }
                System.out.println("\nCurrently deactivated analyses in this installation:");
                for (Job deactivatedJob : deactivatedJobs.keySet()) {
                    JobStatusWrapper jobStatusWrapper = deactivatedJobs.get(deactivatedJob);
                    // Print out deactivated jobs
                    System.out.printf("    %30s : %s\n", deactivatedJob.getId().replace("job", ""), jobStatusWrapper.getWarning() +
                            " Please open properties file 'interproscan.propties.' and specify a valid path.");
                }
                System.exit(1);
            }

            //Check existence of user-specified analyses
            //Expecting 1 entry regardless how many analyses are specified
            String[] parsedAnalyses = parsedCommandLine.getOptionValues(I5Option.ANALYSES.getLongOpt());
            if (parsedAnalyses != null && parsedAnalyses.length == 1) {
                parsedAnalyses = StringUtils.commaDelimitedListToStringArray(parsedAnalyses[0]);
            }

            //Get the following information by iterating once over the list of analysis jobs
            //Which user-specified analyses do exist, therefore store job IDs in a first instance
            final Set<String> realJobIDs = new HashSet<String>();
            for (Job job : jobs.getAllJobs().getJobList()) {
                //Store job IDs for existence check
                realJobIDs.add(job.getId());
            }
            final Map<String, String> parsedAnalysesExistentAnalysesMap = getRealAnalysesNames(parsedAnalyses, realJobIDs);

            StringBuilder nonexistentAnalysis = new StringBuilder();
            if (parsedAnalyses != null && parsedAnalyses.length > 0) {
                boolean doExit = false;
                for (String parsedAnalysisName : parsedAnalyses) {
                    if (parsedAnalysesExistentAnalysesMap.containsKey(parsedAnalysisName)) {
                        //Check if they are deactivated
                        String realJobId = parsedAnalysesExistentAnalysesMap.get(parsedAnalysisName);
                        for (Job deactivatedJob : deactivatedJobs.keySet()) {
                            if (deactivatedJob.getId().equalsIgnoreCase(realJobId)) {
                                JobStatusWrapper jobStatusWrapper = deactivatedJobs.get(deactivatedJob);
                                System.out.println("\n\n" + jobStatusWrapper.getWarning() + "\n\n");
                                doExit = true;
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

            parsedAnalyses = StringUtils.toStringArray(parsedAnalysesExistentAnalysesMap.values());

            if (mode.getRunnableBean() != null) {
                Runnable runnable = (Runnable) ctx.getBean(mode.getRunnableBean());

                // Set command line parameters on Master.
                if (runnable instanceof Master) {
                    Master master = (Master) runnable;
                    if (parsedCommandLine.hasOption(I5Option.FASTA.getLongOpt())) {
                        master.setFastaFilePath(parsedCommandLine.getOptionValue(I5Option.FASTA.getLongOpt()));
                    }
                    if (parsedCommandLine.hasOption(I5Option.OUT_FILE.getLongOpt())) {
                        master.setOutputFile(parsedCommandLine.getOptionValue(I5Option.OUT_FILE.getLongOpt()));
                    }
                    if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMATS.getLongOpt())) {
                        master.setOutputFormats(parsedCommandLine.getOptionValues(I5Option.OUTPUT_FORMATS.getLongOpt()));
                    }
                    if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
                        master.setAnalyses(parsedAnalyses);
                    }
                    if (tcpConnectionString != null) {
                        master.setTcpUri(tcpConnectionString);
                    }
                    if (parsedCommandLine.hasOption(I5Option.SEQUENCE_TYPE.getLongOpt())) {
                        String sequenceType = parsedCommandLine.getOptionValue(I5Option.SEQUENCE_TYPE.getLongOpt());
                        if (sequenceType.equalsIgnoreCase("n")) {
                            String[] outputFormats = parsedCommandLine.getOptionValues(I5Option.OUTPUT_FORMATS.getLongOpt());
                            if (outputFormats != null) {
                                for (String outputFormat : outputFormats) {
                                    if (outputFormat.equalsIgnoreCase("tsv") || outputFormat.equalsIgnoreCase("html")) {
                                        System.out.println("\n\nTSV and HTML formats are not supported if you run I5 against nucleotide sequences. Supported formats are GFF3 (Default) and XML.");
                                        System.exit(1);
//                                throw new IllegalArgumentException("TSV format is not supported if you run I5 against nucleotide sequences. Supported format are GFF3 (Default) and XML.");
                                    }
                                }
                            }
                        }
                        master.setSequenceType(sequenceType);
                    }

                    if (parsedCommandLine.hasOption(I5Option.MIN_SIZE.getLongOpt())) {
                        master.setMinSize(parsedCommandLine.getOptionValue(I5Option.MIN_SIZE.getLongOpt()));
                    }

                    // GO terms and/or pathways will also imply IPR lookup
                    final boolean mapToGo = parsedCommandLine.hasOption(I5Option.GOTERMS.getLongOpt());
                    master.setMapToGOAnnotations(mapToGo);
                    final boolean mapToPathway = parsedCommandLine.hasOption(I5Option.PATHWAY_LOOKUP.getLongOpt());
                    master.setMapToPathway(mapToPathway);
                    master.setMapToInterProEntries(mapToGo || mapToPathway || parsedCommandLine.hasOption(I5Option.IPRLOOKUP.getLongOpt()));
                }

                if (runnable instanceof DistributedWorkerController) {
//                    if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt()) || parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
                    final DistributedWorkerController workerController = (DistributedWorkerController) runnable;
                    if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt())) {
                        final int priority = Integer.parseInt(parsedCommandLine.getOptionValue(I5Option.PRIORITY.getLongOpt()));
                        if (priority < 0 || priority > 9) {
                            throw new IllegalStateException("The JMS priority value must be an integer between 0 and 9.  The value passed in is " + priority);
                        }
                        workerController.setMinimumJmsPriority(priority);
                    }
                    if (parsedCommandLine.hasOption(I5Option.MASTER_URI.getLongOpt())) {
                        final String masterUri = parsedCommandLine.getOptionValue(I5Option.MASTER_URI.getLongOpt());
                        workerController.setMasterUri(masterUri);
                    }
                    if (parsedCommandLine.hasOption(I5Option.TEMP_DIRECTORY.getLongOpt())) {
                        final String temporaryDirectoryName = parsedCommandLine.getOptionValue(I5Option.TEMP_DIRECTORY.getLongOpt());
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
//                    }
                }

                // TODO Currently silently ignores command line parameters that are not appropriate for the mode.
                System.out.println("Running InterProScan v5 in " + mode + " mode...");
                runnable.run();
            }
//            ctx.close();
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

    /**
     * Determines real job names from parsed analyses names.
     *
     * @param parsedAnalyses
     * @param realJobIDs
     * @return Map of parsed analysis name to real analysis name.
     */
    private static Map<String, String> getRealAnalysesNames(String[] parsedAnalyses, Set<String> realJobIDs) {
        Map<String, String> result = new HashMap<String, String>();
        if (parsedAnalyses != null && parsedAnalyses.length > 0) {
            for (String analysisName : parsedAnalyses) {
                for (String realJobID : realJobIDs) {
                    if (realJobID.toLowerCase().contains(analysisName.toLowerCase())) {
                        result.put(analysisName, realJobID);
                        break;
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

            final String uriString = new StringBuilder("tcp://").append(hostname).append(':').append(port).toString();

            final TransportConnector tc = new TransportConnector();
            tc.setUri(new URI(uriString));
            broker.addConnector(tc);
            broker.start();
            System.out.println("uriString = " + uriString);
            return uriString;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure the TCPTransport on the Broker", e);
        }
    }

    private static void printHelp() {
        HELP_FORMATTER.printHelp(HELP_MESSAGE_TITLE, HEADER, COMMAND_LINE_OPTIONS, FOOTER);
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
