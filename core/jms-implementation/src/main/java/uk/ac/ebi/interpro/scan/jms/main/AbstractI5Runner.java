package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Abstract class containing common code used by I5 when running in all it's various modes.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AbstractI5Runner {

    private static final Logger LOGGER = LogManager.getLogger(AbstractI5Runner.class.getName());

    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
    static {
        HELP_FORMATTER.setWidth(120);
    }
    private static final String HELP_MESSAGE_TITLE = "InterProScan: genome-scale protein function classification.";
    private static final String HEADER = "\n\nDOCUMENTATION\n\tSee https://interproscan-docs.readthedocs.io for full documentation.\n";
    private static final String FOOTER = "Copyright \u00a9 EMBL-EBI, Wellcome Genome Campus, Hinxton, Cambridgeshire, UK (https://www.ebi.ac.uk).\n" +
            "The InterProScan software itself is provided under the Apache License, Version 2.0 (https://www.apache.org/licenses/LICENSE-2.0.html). " +
            "Third party components (e.g. member database binaries and models) are subject to separate licensing - " +
            "please see the individual member database websites for details.\n\n";

    protected static final Mode DEFAULT_MODE = Mode.STANDALONE;

    protected static void printHelp(final Options commandLineOptionsForHelp) {
        HELP_FORMATTER.printHelp(HELP_MESSAGE_TITLE, HEADER, commandLineOptionsForHelp, FOOTER);
    }

    protected static void printVersion(String version, String buildType) {
        System.out.println("InterProScan version " + version);
        System.out.println("InterProScan " + buildType + " build " + " (requires Java 11)");
    }

    protected static void printStringList(ArrayList<String> infoList) {
        for (String helpInfo: infoList){
            System.out.print(helpInfo);
        }
    }

    /**
     * Create any directory/directories required in the supplied path
     * @param path The path
     * @return True if all succeeded
     */
    protected static boolean createDirectory(final String path) {
        File dir = new File(path);
        try {
            boolean dirCreated = dir.mkdirs();
            return dirCreated;
        } catch (SecurityException e) {
            LOGGER.error("Directory creation . Cannot create the specified directory !\n" +
                    "Specified directory path (absolute): " + dir.getAbsolutePath(), e);
            throw new IllegalStateException("The directory (-" + path + ")  you specified cannot be written to:", e);
        }
    }


    /**
     * Check if a specified path exists and is readable.
     * @param path The full file or directory path under review (e.g. "/tmp/test_proteins.fasta")
     * @param checkParent Do we just check the parent path? (e.g. "/tmp")
     * @param checkWriteable Should we also check that the path or parent path can be written to?
     * @param option The user input {@link I5Option} this path relates to (or null if not applicable)
     * @return True if the checks succeed, otherwise false (although the system will exit if a {@link I5Option} check fails)
     */
    protected static boolean checkPathExistence(final String path, final boolean checkParent, final boolean checkWriteable, final I5Option option) {
        return checkPathExistence(path, checkParent, checkWriteable, option, false);
    }

    /**
     * Check if a specified path exists and is readable.
     * @param path The full file or directory path under review (e.g. "/tmp/test_proteins.fasta")
     * @param checkParent Do we just check the parent path? (e.g. "/tmp")
     * @param checkWriteable Should we also check that the path or parent path can be written to?
     * @param option The user input {@link I5Option} this path relates to (or null if not applicable)
     * @param checkIsFile Check if the path is a file or symblic link to a file (e.g. not a directory)?
     * @return True if the checks succeed, otherwise false (although the system will exit if a {@link I5Option} check fails)
     */
    protected static boolean checkPathExistence(final String path, final boolean checkParent, final boolean checkWriteable, final I5Option option, boolean checkIsFile) {
        String pathToCheck = path;
        if (checkParent) {
            pathToCheck = path.substring(0, path.lastIndexOf(File.separator));
        }
        Path p = FileSystems.getDefault().getPath(pathToCheck);
        boolean exists = Files.isReadable(p);
        if (option != null && !exists) {
            System.out.println("For the (-" + option.getShortOpt() + ") option you specified a location which doesn't exist or is not readable:");
            System.out.println(path);
            System.exit(2);
        }
        if (exists) {
            if (checkIsFile && !Files.isRegularFile(p)) {
                System.out.println("For the (-" + option.getShortOpt() + ") option you specified a location which is not a file:");
                System.out.println(path);
                System.exit(2);
            }
            if (checkWriteable) {
                boolean writable = Files.isWritable(p);
                if (option != null && !writable) {
                    System.out.println("For the (-" + option.getShortOpt() + ") option you specified a location which is not writable:");
                    System.out.println(path);
                    System.exit(2);
                }
                return writable;
            }
        }
        return exists;
    }


    /**
     * Checks if a specified path exists.
     * @param path The path to check (e.g. file path, directory path etc)
     * @return True if the path exists, otherwise false
     */
    protected static boolean checkPathExistence(final String path) {
        return checkPathExistence(path, false, false, null);
    }

}
