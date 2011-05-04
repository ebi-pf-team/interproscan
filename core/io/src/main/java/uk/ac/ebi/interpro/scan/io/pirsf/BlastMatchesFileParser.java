package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.I5FileUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read in temporary BLAST matches file.<br>
 * Example file content (tab separated):
 * 2	PIRSF000729
 * 3    PIRSF000089
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BlastMatchesFileParser implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(BlastMatchesFileParser.class.getName());

    private static final Pattern PATTERN = Pattern.compile("^\\d+\\s++PIRSF\\d{6,}+$");


    public static Map<Long, String> parse(String pathToFile) throws IOException {
        File blastMatchesFile = I5FileUtil.createTmpFile(pathToFile);
        if (blastMatchesFile == null) {
            throw new NullPointerException("Resource is null");
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
                Matcher modelStart = PATTERN.matcher(line);
                if (modelStart.find()) {
                    // New accession
                    String[] text = line.split("\\s++");
                    if (text != null && text.length != 2) {
                        LOGGER.warn("Unexpected line in blast matches file: " + line);
                    } else {
                        Long proteinId = Long.parseLong(text[0]);
                        String modelId = text[1];
                        data.put(proteinId, modelId);
                    }
                } else {
                    LOGGER.warn("Unexpected line in blast matches file: " + line);
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