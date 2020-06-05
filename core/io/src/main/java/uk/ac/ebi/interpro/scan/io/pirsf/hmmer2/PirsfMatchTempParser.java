package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse PIRSF raw matches that have been written into a temporary file.
 * Part of the mechanism that allows data to be passed between steps in a job.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class PirsfMatchTempParser {
    private static final Logger LOGGER = LogManager.getLogger(PirsfMatchTempParser.class.getName());

    private static final Pattern PATTERN = Pattern.compile("^\\d+-PIRSF\\d{6,}+,");

    /*
     * Example file format:
     *
     * 1-PIRSF001500,1,364,1.4E-213,718.5,1,391,[],1.4E-213,718.5
     * 2-PIRSF002000,5,554,1.1E-211,18.3,1,431,[],1.1E-23,711.2
     */

    /**
     * Parse the temporary file.
     * If a line cannot be parsed (not the expected format) the line is ignored (but is logged).
     *
     * @param pathToFile The file to parse
     * @return The set of raw protein objects described within the file
     * @throws IOException in the event of a problem reading the file.
     */
    public static Set<RawProtein<PIRSFHmmer2RawMatch>> parse(String pathToFile) throws IOException {
        File file = new File(pathToFile);
        if (file == null) {
            throw new NullPointerException("PIRSF temporary file resource is null");
        }
        if (!file.exists()) {
            throw new IllegalStateException(file.getName() + " does not exist");
        }
        if (!file.canRead()) {
            throw new IllegalStateException(file.getName() + " is not readable");
        }
        final Set<RawProtein<PIRSFHmmer2RawMatch>> data = new HashSet<RawProtein<PIRSFHmmer2RawMatch>>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = PATTERN.matcher(line);
                if (modelStart.find()) {
                    // New accession
                    String[] text = line.split(",");
                    if (text != null && text.length != 11) {
                        LOGGER.warn("Unexpected line format in blast matches file: " + line);
                    } else {
                        String[] proteinIdModelId = text[0].split("-");
                        if (proteinIdModelId != null && proteinIdModelId.length != 2) {
                            LOGGER.warn("Unexpected line format in blast matches file: " + line);
                        } else {
                            String proteinId = proteinIdModelId[0];
                            String modelId = proteinIdModelId[1];
                            try {
                                PIRSFHmmer2RawMatch bestMatch = new PIRSFHmmer2RawMatch(proteinId,
                                        modelId,
                                        SignatureLibrary.PIRSF, // Signature library
                                        text[1], // Signature release
                                        Integer.parseInt(text[2]), // Location start
                                        Integer.parseInt(text[3]), // Location end
                                        Double.parseDouble(text[4]), // E-value
                                        Double.parseDouble(text[5]), // Score
                                        Integer.parseInt(text[6]), // HMM start
                                        Integer.parseInt(text[7]), // HMM end
                                        text[8], // HMM bounds
                                        Double.parseDouble(text[9]), // Location e-value
                                        Double.parseDouble(text[10]) // Location score
                                );

                                RawProtein<PIRSFHmmer2RawMatch> protein = new RawProtein<PIRSFHmmer2RawMatch>(proteinId);
                                protein.addMatch(bestMatch);
                                data.add(protein);
                            } catch (NumberFormatException e) {
                                LOGGER.error("Error parsing PIRSF temporary match file line (ignoring): " + line + " - Exception " + e.getMessage());
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Unexpected line format in blast matches file: " + line);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return data;
    }

}
