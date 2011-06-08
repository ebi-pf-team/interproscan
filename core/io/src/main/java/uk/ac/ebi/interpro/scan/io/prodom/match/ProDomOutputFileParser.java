package uk.ac.ebi.interpro.scan.io.prodom.match;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parse the output from the ProDom perl script.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProDomOutputFileParser {

    private static final Logger LOGGER = Logger.getLogger(ProDomOutputFileParser.class.getName());

    private static final Pattern PATTERN = Pattern.compile("^\\d+\\s++PIRSF\\d{6,}+$");


    public static Map<Long, String> parse(String pathToFile) throws IOException {
        File blastMatchesFile = new File(pathToFile);
        if (blastMatchesFile == null) {
            throw new NullPointerException("Blast matches file resource is null");
        }
        if (!blastMatchesFile.exists()) {
            throw new IllegalStateException(blastMatchesFile.getName() + " does not exist");
        }
        if (!blastMatchesFile.canRead()) {
            throw new IllegalStateException(blastMatchesFile.getName() + " is not readable");
        }
        final Map<Long, String> data = new HashMap<Long, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(blastMatchesFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                Matcher modelStart = PATTERN.matcher(line);
//                if (modelStart.find()) {
//                    // New accession
//                    String[] text = line.split("\\s++");
//                    if (text != null && text.length != 2) {
//                        LOGGER.warn("Unexpected line in blast matches file: " + line);
//                    } else {
//                        Long proteinId = Long.parseLong(text[0]);
//                        String modelId = text[1];
//                        data.put(proteinId, modelId);
//                    }
//                } else {
//                    LOGGER.warn("Unexpected line in blast matches file: " + line);
//                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return data;
    }

}
