package uk.ac.ebi.interpro.scan.io.pirsf;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read in temporary filtered matches file.<br>
 * Example file content (protein IDs):
 * 2
 * 3
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FilteredMatchesFileParser implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(FilteredMatchesFileParser.class.getName());

    private static final Pattern PATTERN = Pattern.compile("^\\d+$");


    public static Set<Long> parse(String pathToFile) throws IOException {
        File filteredMatchesFile = new File(pathToFile);
        if (filteredMatchesFile == null) {
            throw new NullPointerException("Filtered matches file resource is null");
        }
        if (!filteredMatchesFile.exists()) {
            throw new IllegalStateException(filteredMatchesFile.getName() + " does not exist");
        }
        if (!filteredMatchesFile.canRead()) {
            throw new IllegalStateException(filteredMatchesFile.getName() + " is not readable");
        }
        final Set<Long> data = new HashSet<Long>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filteredMatchesFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = PATTERN.matcher(line);
                if (modelStart.find()) {
                    // New accession
                    String[] text = line.split("\\s++");
                    if (text != null && text.length != 1) {
                        LOGGER.warn("Unexpected line in filtered matches file: " + line);
                    } else {
                        Long proteinId = Long.parseLong(text[0]);
                        data.add(proteinId);
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
