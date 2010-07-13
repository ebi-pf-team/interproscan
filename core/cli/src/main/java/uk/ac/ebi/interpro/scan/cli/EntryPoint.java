package uk.ac.ebi.interpro.scan.cli;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Entry point for command line interface.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class EntryPoint {

    private static final Logger LOGGER = Logger.getLogger(EntryPoint.class.getName());

    public static void main(String[] args) {

        // Parse command line arguments and options
        CommandLineValues values = new CommandLineValues();
        CmdLineParser parser = new CmdLineParser(values);
        parser.setUsageWidth(80);
        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: java EntryPoint [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }

        ApplicationContext ctx;
        if (values.getSpringContext() == null) {
            ctx = new ClassPathXmlApplicationContext(new String[]{"beans.xml"});
        } else {
            ctx = new FileSystemXmlApplicationContext(values.getSpringContext());
        }

        // Process
        try {
            Resource fastaFile = new FileSystemResource(values.getFastaFile());
            Resource hmmFile = new FileSystemResource(values.getHmmFile());
            if (values.isOnionMode()) {
                String resultsFile = values.getResultsFile();
                Gene3dOnionRunner runner = (Gene3dOnionRunner) ctx.getBean("gene3dOnionRunner");
                if (resultsFile == null) {
                    runner.execute(fastaFile, hmmFile, new FileSystemResource(values.getResultsDir()));
                } else {
                    runner.execute(resultsFile, fastaFile, hmmFile, new FileSystemResource(values.getResultsDir()));
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Done");
                }
            } else {
                Gene3dRunner runner = (Gene3dRunner) ctx.getBean("gene3dRunner");
                runner.execute(fastaFile, hmmFile);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static class CommandLineValues {

        private static String ONION_MODE = "onion";

        @Argument(required = true, index = 0, metaVar = "<fasta-file>", usage = "FASTA file")
        private String fastaFile;

        @Argument(required = true, index = 1, metaVar = "<hmm-file>", usage = "HMM file")
        private String hmmFile;

        @Option(name = "-m", aliases = {"--mode"}, usage = "Mode (one of: i5, onion)")
        private String mode = ONION_MODE;

        @Option(name = "-r", aliases = {"--resultsDir"}, usage = "Directory to hold result files")
        private String resultsDir = "/tmp";

        @Option(name = "-f", aliases = {"--resultsFile"}, usage = "Results file name")
        private String resultsFile = null;

        @Option(name = "-c", aliases = {"--springContext"}, usage = "Spring application context")
        private String springContext = null;

        public String getFastaFile() {
            return fastaFile;
        }

        public String getHmmFile() {
            return hmmFile;
        }

        public boolean isOnionMode() {
            return mode.equals(ONION_MODE);
        }

        public String getResultsDir() {
            return resultsDir;
        }

        public String getSpringContext() {
            return springContext;
        }

        public String getResultsFile() {
            return resultsFile;
        }
    }

}
