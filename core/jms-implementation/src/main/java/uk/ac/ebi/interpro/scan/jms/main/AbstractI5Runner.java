package uk.ac.ebi.interpro.scan.jms.main;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class containing common code used by I5 when running in all it's various modes.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AbstractI5Runner {

    private static final Logger LOGGER = Logger.getLogger(AbstractI5Runner.class.getName());

    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
    private static final String HELP_MESSAGE_TITLE =
            "java -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods " +
                    "-Xms512M -Xmx2048M -jar interproscan-5.jar";
    private static final String HEADER =
            "\n\nPlease give us your feedback by sending an email to\n\ninterhelp@ebi.ac.uk\n\n";
    private static final String FOOTER = "Copyright (c) EMBL European Bioinformatics Institute, Hinxton, Cambridge, UK. (http://www.ebi.ac.uk) " +
            "The InterProScan software itself is " +
            "provided under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html). " +
            "Third party components (e.g. member database binaries and models) are subject to separate licensing - " +
            "please see the individual member database websites for details.\n\n";

    protected static final Mode DEFAULT_MODE = Mode.STANDALONE;

    protected static void printHelp(final Options commandLineOptionsForHelp) {
        HELP_FORMATTER.printHelp(HELP_MESSAGE_TITLE, HEADER, commandLineOptionsForHelp, FOOTER);
    }


    protected static void checkFileExistence(final String filePath, final String option) {
        checkDirectoryExistence(filePath, option);
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("For the (-" + option + ") option you specified a file which does not exist:");
            System.out.println(file);
            System.exit(2);
        }
    }


    protected static boolean createDirectory(final String filePath) {
        File dir = new File(filePath);
        try {
            boolean dirCreated = dir.mkdirs();
            return dirCreated;
        } catch (SecurityException e) {
            LOGGER.error("Directory creation test. Cannot create the specified directory !\n" +
                    "Specified directory path (absolute): " + dir.getAbsolutePath(), e);
            throw new IllegalStateException("The directory (-" + filePath + ")  you specified cannot be written to:", e);
        }

    }


    protected static boolean directoryExists(final String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
           return true;
        }
        return false;
    }

    protected static void checkDirectoryExistence(final String filePath, final String option) {
        String parent = new File(filePath).getParent();
        if (option.equals(I5Option.TEMP_DIRECTORY.getShortOpt())) {
            parent = filePath;
        }
        File dir = new File(parent);
        if (!dir.exists()) {
            System.out.println("For the (-" + option + ") option you specified a location which doesn't exist:");
            System.out.println(dir);
            System.exit(2);
        }
    }

    protected static void checkDirectoryExistenceAndFileWritePermission(final String filePath, final String option) {
        checkDirectoryExistence(filePath, option);
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.canWrite()) {
                System.out.println("Can write test.");
                System.out.println("For the (-" + option + ") option you specified a location which cannot be written to:");
                System.out.println(file);
                System.exit(2);
            }
        } else {
            //Do a file creation and deletion file test
            boolean fileCreated;
            boolean fileDeleted;
            try {
                fileCreated = file.createNewFile();
                fileDeleted = file.delete();
                if (!fileCreated || !fileDeleted) {
                    System.out.println("Create and delete test.");
                    System.out.println("For the (-" + option + ") option you specified a location which cannot be written to:");
                    System.out.println(file);
                    System.exit(2);
                }
            } catch (IOException e) {
                LOGGER.error("File creation test. Cannot create the specified output file!\n" +
                        "Specified output file path (absolute): " + file.getAbsolutePath(), e);
                throw new IllegalStateException("For the (-" + option + ") option you specified a location which cannot be written to:", e);
            }
        }
    }

}
