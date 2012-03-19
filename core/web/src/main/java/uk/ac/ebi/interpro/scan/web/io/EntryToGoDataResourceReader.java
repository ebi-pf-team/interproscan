package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.GoTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Phil Jones
 *         Date: 19/03/12
 */
public class EntryToGoDataResourceReader {

    private static final Logger LOGGER = Logger.getLogger(EntryToGoDataResourceReader.class.getName());

    public Map<String, List<GoTerm>> read(final Resource entryToGoMappingFile) throws IOException {
        final Map<String, List<GoTerm>> entryToGoTerm = new HashMap<String, List<GoTerm>>();
        if (entryToGoMappingFile == null) {
            throw new IllegalArgumentException("The Entry to GO mapping file resource is null.");
        }
        if (!entryToGoMappingFile.exists()) {
            throw new IllegalArgumentException("The Entry to GO mapping file resource does not exist.");
        }
        if (!entryToGoMappingFile.isReadable()) {
            throw new IllegalArgumentException("The Entry to GO mapping file resource exists but is not readable.");
        }

        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(entryToGoMappingFile.getInputStream()));
            String line;
            while ((line = buf.readLine()) != null) {
                final String[] part = line.split("\\|");
                if (part.length == 4) {
                    final String entryAc = part[0];
                    List<GoTerm> terms = entryToGoTerm.get(entryAc);
                    if (terms == null) {
                        terms = new ArrayList<GoTerm>();
                        entryToGoTerm.put(entryAc, terms);
                    }
                    terms.add(new GoTerm(part[3], part[1], part[2]));
                } else {
                    LOGGER.error("Unable to parse line of entry to go mapping file: " + line);
                }
            }
        } finally {
            if (buf != null) {
                buf.close();
            }
        }
        return Collections.unmodifiableMap(entryToGoTerm);
    }
}
