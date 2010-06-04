package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.interpro.scan.jms.activemq.DistributedWorkerController;
import uk.ac.ebi.interpro.scan.jms.master.Master;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

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

    private static final String HELP_MESSAGE_TITLE = "java -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx2048M -jar interproscan-5.jar";

// [-Dconfig=/path/to/config.properties]

    private static final int MEGA = 1024 * 1024;

   
    private enum I5Option {
        MODE("mode", "m", false, "MANDATORY Mode in which InterProScan is being run.  Must be one of: " + Mode.getCommaSepModeList(), "MODE-NAME",false),
        FASTA("fasta", "i", false, "Optional path to fasta file that should be loaded on Master startup.", "FASTA-FILE-PATH",false),
        OUTPUT_FORMAT("format", "F", false, "Optional output format. One of: tsv (tab separated values)", "OUTPUT-FORMAT",false),
        OUT_FILE("out-file", "o", false, "Optional output file path/name.", "OUTPUT-FILE-PATH",false),
        ANALYSES("analyses", "appl", false, "Optional comma separated list of analyses.  If this option is not set, ALL analyses will be run. ", "ANALYSES",true),
        PRIORITY("priority", "p", false, "Minimum message priority that the worker will accept. (0 low -> 9 high)", "JMS-PRIORITY",false);

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
        MASTER("master", "spring/master/master-context.xml"),
        I5STANDALONE("i5standalone", "spring/master/i5-single-jvm-context.xml"),
        WORKER("worker", "spring/worker/parallel-worker-context.xml"),
        MONITOR("monitor", "spring/monitor/monitor-context.xml"),
        INSTALLER("installer", "spring/installer/installer-context.xml"),
        AMQSTANDALONE("amqstandalone", "spring/jms/activemq/activemq-standalone-master-context.xml"),
        AMQMASTER("amqmaster", "spring/jms/activemq/activemq-distributed-master-context.xml"),
        AMQWORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-context.xml"),
        AMQCLEANRUN("cleanrun", "spring/jms/activemq/activemq-cleanrun-master-context.xml");

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

    private static final Mode DEFAULT_MODE= Mode.AMQSTANDALONE;


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

            final Mode mode = modeArgument!=null?Mode.valueOf(modeArgument.toUpperCase()):DEFAULT_MODE;

            //String config = System.getProperty("config");
            LOGGER.info("Welcome to InterProScan v5");

            LOGGER.info("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
            LOGGER.info("Running as: " + mode);
//
//            if (config == null) {
//                LOGGER.info("No custom config used. Use java -Dconfig=config/my.properties");
//            } else {
//                LOGGER.info("Custom config: " + config);
//            }

            AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{mode.getContextXML()});

            ctx.registerShutdownHook();

            if (args.length==0) {
                printHelp();
                System.out.println("Available analyses in this installation:");
                Jobs jobs=(Jobs)ctx.getBean("jobs");

                for (Job job : jobs.getAnalysisJobs().getJobList()) {
                    System.out.printf("    %20s : %s\n",job.getId().replace("job", ""),job.getDescription());
                }
                System.exit(1);
            }

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
                    if (parsedCommandLine.hasOption(I5Option.OUTPUT_FORMAT.getLongOpt())) {
                        master.setOutputFormat(parsedCommandLine.getOptionValue(I5Option.OUTPUT_FORMAT.getLongOpt()));
                    }
                    if (parsedCommandLine.hasOption(I5Option.ANALYSES.getLongOpt())) {
                        master.setAnalyses(parsedCommandLine.getOptionValues(I5Option.ANALYSES.getLongOpt()));
                    }
                }

                if (runnable instanceof DistributedWorkerController) {
                    if (parsedCommandLine.hasOption(I5Option.PRIORITY.getLongOpt())) {
                        final DistributedWorkerController workerController = (DistributedWorkerController) runnable;
                        final int priority = Integer.parseInt(parsedCommandLine.getOptionValue(I5Option.PRIORITY.getLongOpt()));
                        if (priority < 0 || priority > 9) {
                            throw new IllegalStateException("The JMS priority value must be an integer between 0 and 9.  The value passed in is " + priority);
                        }
                        workerController.setMinimumJmsPriority(priority);
                    }
                }

                // TODO Currently silently ignores command line parameters that are not appropriate for the mode.

                runnable.run();
            }
            ctx.close();
        }
        catch (ParseException exp) {
            LOGGER.fatal("Exception thrown when parsing command line arguments.  Error message: " + exp.getMessage());
            printHelp();
            System.exit(1);
        }
        catch (IllegalArgumentException iae) {
            LOGGER.fatal("The mode '" + modeArgument + "' is not handled.  Should be one of: " + Mode.getCommaSepModeList());
            System.exit(1);
        }
    }

    private static void printHelp() {
        HELP_FORMATTER.printHelp(HELP_MESSAGE_TITLE, COMMAND_LINE_OPTIONS);
    }

}
