package uk.ac.ebi.interpro.scan.io.prints;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for pval files that contain PRINTS models. (Models in PRINTS parlance!)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class KdatParser implements Serializable {

    // /ebi/production/interpro/data/members/prints/40.0/prints40_1.kdat

    private static final String LINE_SIG_ACCESSION = "gx";
    private static final String LINE_SIG_NAME = "gc";
    private static final String LINE_SIG_ABSTRACT = "gd";

    private static final Logger LOGGER = Logger.getLogger(KdatParser.class);


    /**
     * @param resource
     * @return
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
        final Map<String, String> accessionToSignature = new HashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line, accession = null, name = null;
            StringBuffer printsAbstract = new StringBuffer();
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith(LINE_SIG_NAME)) {
                    addRecord(accessionToSignature, accession, name, printsAbstract, lineNumber);
                    printsAbstract.delete(0, printsAbstract.length());
                    accession = null;
                    name = extractLineContent(line);
                } else if (line.startsWith(LINE_SIG_ACCESSION)) {
                    accession = extractLineContent(line);
                } else if (line.startsWith(LINE_SIG_ABSTRACT)) {
                    final String abstractLine = extractLineContent(line);
                    if (abstractLine.length() > 0) {
                        printsAbstract.append(extractLineContent(line)).append(' ');
                    } else {
                        printsAbstract.append('\n');
                    }
                }
            }
            // Don't forget the last one!
            addRecord(accessionToSignature, accession, name, printsAbstract, lineNumber);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return accessionToSignature;
    }

    private String extractLineContent(String line) {
        return line.substring(3).trim();
    }

    private void addRecord(Map<String, String> accessionToSignature, String accession, String name, StringBuffer printAbstract, int lineNumber) {
        if (name != null) {
            if (accession == null) {  // Got a name but not an accession?
                throw new ParseException("The kdat file, line number " + lineNumber + " contains an entry with no accession (gx line).");
            }
            accessionToSignature.put(accession, printAbstract.toString());
        }
    }
}
