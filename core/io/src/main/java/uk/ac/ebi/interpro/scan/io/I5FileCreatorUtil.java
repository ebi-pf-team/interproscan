package uk.ac.ebi.interpro.scan.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Simple utility class to create temporary files.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class I5FileCreatorUtil {
    private static final Logger LOGGER = Logger.getLogger(I5FileCreatorUtil.class.getName());

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
}
