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

    /**
     * Write a list of protein Ids to a temporary file.
     * @param filePath The file path and name.
     * @param passedProteinIds List of protein Ids to write to the file.
     * @throws IOException If a problem was encountered whilst writing to the file.
     */
    public static void writeFilteredRawMatchesToFile(String filePath,
                                                     Set<String> passedProteinIds) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = createTmpFile(filePath);
            if (!file.exists()) {
                throw new IllegalStateException("Could not create file: " + filePath);
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

    /**
     * Create a new temporary file. If the file already exists a warning is logged but this is not considered a failure.
     *
     * @param pathToFile Path and filename
     * @return The file
     * @throws IOException If the file could not be created.
     */
    public static File createTmpFile(String pathToFile) throws IOException {
        File result = new File(pathToFile);
        if (!result.createNewFile()) {
            LOGGER.warn("Couldn't create new File! Maybe the file " + result.getAbsolutePath() + " already exists!");
        }
        return result;
    }

}
