package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to read in subfamilies.out file.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfSubfamilyFileParser implements Serializable {

    /*
     * Example file content (mapping of subfamily model accession to superfamily model accession):
     * PIRSF500165/tPIRSF016158
     * PIRSF500166/tPIRSF016158
     */

    private static final Logger LOGGER = Logger.getLogger(PirsfSubfamilyFileParser.class.getName());


    /**
     * @param resource Subfamlies.out file. Resource of the file, which needs to be parsed.
     * @return Mapping of subfamily model accessions to super family model accessions.
     * @throws IOException
     */
    public Map<String, String> parse(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists()) {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }
        final Map<String, String> result = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] chunks = line.split("/t");
                if (chunks != null && chunks.length == 2) {
                    result.put(chunks[0], chunks[1]);
                } else {
                    LOGGER.warn("Unexpected line in subfamilies.out file: " + line);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return result;
    }
}