package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read in the sf.tb file.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BlastMatchesFileParser implements Serializable {

    /*
     * Example file content (tab separated):
     * 2	PIRSF000729
     * 3    PIRSF000089
     */

    private static final Logger LOGGER = Logger.getLogger(BlastMatchesFileParser.class.getName());

    private static final Pattern PATTERN = Pattern.compile("^\\d+\\s++PIRSF\\d{6,}+$");


    public static Map<Integer, String> parse(String pathToFile) throws IOException {
         File blastMatchesFile = new File(pathToFile);
        if (blastMatchesFile == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!blastMatchesFile.exists()) {
            throw new IllegalStateException(blastMatchesFile.getName() + " does not exist");
        }
        if (!blastMatchesFile.canRead()) {
            throw new IllegalStateException(blastMatchesFile.getName() + " is not readable");
        }
        final Map<Integer, String> data = new HashMap<Integer, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(blastMatchesFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = PATTERN.matcher(line);
                if (modelStart.find()) {
                    // New accession
                    String[] text = line.split("\\s++");
                    if (text != null && text.length != 2) {
                        LOGGER.warn("Unexpected line in blast matches file: " + line);
                    }
                    else {
                        Integer proteinId = Integer.parseInt(text[0]);
                        String modelId = text[1];
                        data.put(proteinId, modelId);
                    }
                }
                else {
                    LOGGER.warn("Unexpected line in blast matches file: " + line);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return data;
    }


}
