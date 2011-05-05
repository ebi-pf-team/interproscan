package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * Simple utility class to create temporary files.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfFileUtil {
    private static final Logger LOGGER = Logger.getLogger(PirsfFileUtil.class.getName());

    public static File createTmpFile(String temporaryFileDirectory, String filePathName) throws IOException {
        StringBuilder pathToFile = new StringBuilder();
        if (temporaryFileDirectory != null) {
            pathToFile
                    .append(temporaryFileDirectory)
                    .append('/');
        }
        pathToFile.append(filePathName);
        File result = new File(pathToFile.toString());
        if (!result.createNewFile()) {
            LOGGER.warn("Couldn't create new File! Maybe the file " + result.getAbsolutePath() + " already exists!");
        }
        return result;
    }

    public static File createTmpFile(String filePathName) throws IOException {
        return createTmpFile(null, filePathName);
    }

    public static void writeFilteredRawMatchesToFile(String temporaryFileDirectory,
                                                     String fileName,
                                                     Set<String> passedProteinIds) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = PirsfFileUtil.createTmpFile(temporaryFileDirectory, fileName);
            if (!file.exists()) {
                return; // File already exists, so don't try to write it again.
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (String proteinId : passedProteinIds) {
                writer.write(proteinId);
                writer.write('\n');
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
