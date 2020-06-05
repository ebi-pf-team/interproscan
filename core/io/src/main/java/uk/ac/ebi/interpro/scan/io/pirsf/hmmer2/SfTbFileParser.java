package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
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
public class SfTbFileParser implements Serializable {

    /*
     * Example file content:
     * SF000077 1260
     * SF000729 410
     */

    private static final Logger LOGGER = LogManager.getLogger(SfTbFileParser.class.getName());

    private static final Pattern SF_TB_PATTERN = Pattern.compile("^SF\\d{6,}+\\s++\\d{1,}+$");


    public Map<String, Integer> parse(Resource sfTbFileResource) throws IOException {
        if (sfTbFileResource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!sfTbFileResource.exists()) {
            throw new IllegalStateException(sfTbFileResource.getFilename() + " does not exist");
        }
        if (!sfTbFileResource.isReadable()) {
            throw new IllegalStateException(sfTbFileResource.getFilename() + " is not readable");
        }
        final Map<String, Integer> data = new HashMap<String, Integer>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(sfTbFileResource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher modelStart = SF_TB_PATTERN.matcher(line);
                if (modelStart.find()) {
                    // New accession
                    String[] text = line.split("\\s++");
                    if (text != null) {
                        if (text.length != 2) {
                            LOGGER.warn("Unexpected line in sf.tb: " + line);
                        } else {
                            String sf = text[0];
                            Integer num = Integer.parseInt(text[1]);
                            data.put(sf, num);
                        }
                    }
                } else {
                    LOGGER.warn("Unexpected line in sf.tb: " + line);
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
