package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

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
     * Write out raw protein details into a temporary file so the data can be made available for later steps in the job.
     * With PIRSF, only the best match (smallest e-value) is of interest for each protein.
     *
     * @param filePath              The file to write to
     * @param proteinIdBestMatchMap Protein and best match information.
     * @throws IOException Error if a problem is encountered whilst writing to the file system
     */
    public static void writeProteinBestMatchesToFile(String filePath,
                                                     Map<String, PIRSFHmmer2RawMatch> proteinIdBestMatchMap) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = createTmpFile(filePath);
            if (!file.exists()) {
                throw new IllegalStateException("Could not create file: " + filePath);
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (String proteinId : proteinIdBestMatchMap.keySet()) {
                PIRSFHmmer2RawMatch bestMatch = proteinIdBestMatchMap.get(proteinId);
                if (bestMatch != null) {
                    // Write all required information into a temporary text file such that a PIRSFHmmer2RawMatch can
                    // be instantiated and persisted later.

                    writer.write(proteinId);
                    writer.write('-');
                    writer.write(bestMatch.getModelId());
                    writer.write(',');
                    // Signature release (e.g. 2.74 or 2.78)
                    writer.write(bestMatch.getSignatureLibraryRelease());
                    writer.write(',');
                    // Signature library = PIRSF
                    writer.write(String.valueOf(bestMatch.getLocationStart()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getLocationEnd()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getEvalue()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getScore()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getHmmStart()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getHmmEnd()));
                    writer.write(',');
                    writer.write(bestMatch.getHmmBounds());
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getLocationEvalue()));
                    writer.write(',');
                    writer.write(String.valueOf(bestMatch.getLocationScore()));
                    writer.write('\n');
                }
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Create a new temporary file. If the file already exists it is replaced.
     *
     * @param pathToFile Path and filename
     * @return The file
     * @throws IOException If the file could not be created.
     */
    public static File createTmpFile(String pathToFile) throws IOException {
        File file = new File(pathToFile);
        if (file.exists()) {
            file.delete();
        }
        if (!file.createNewFile()) {
            LOGGER.warn("Couldn't create new File! Maybe the file " + file.getAbsolutePath() + " already exists!");
        }
        return file;
    }

    public static void writeSubFamiliesToFile(String filePath, Map<String, String> subFamToSuperFamMap) throws IOException {
        BufferedWriter writer = null;
        try {
            File file = createTmpFile(filePath);
            if (!file.exists()) {
                throw new IllegalStateException("Could not create file: " + filePath);
            }
            writer = new BufferedWriter(new FileWriter(file));
            for (String subFamilyId : subFamToSuperFamMap.keySet()) {
                writer.write(subFamilyId);
                writer.write("/t");
                writer.write(subFamToSuperFamMap.get(subFamilyId));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}