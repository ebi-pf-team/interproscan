package uk.ac.ebi.interpro.scan.io.match.coils;


import uk.ac.ebi.interpro.scan.io.ParseException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the Coils output format:
 * <p/>
 * >UNIPARC:UPI00000000FC status=active
 * 165 186
 * //
 * >UNIPARC:UPI0000000107 status=active
 * //
 * >UNIPARC:UPI0000000108 status=active
 * 73 94
 * 123 158
 * //
 * >UNIPARC:UPI0000000109 status=active
 * //
 *
 * @author Phil Jones
 * @version $Id: CoilsMatchParser.java,v 1.1 2009/11/25 14:01:17 pjones Exp $
 * @since 1.0-SNAPSHOT
 */
public class CoilsMatchParser implements Serializable {

    private static final String END_OF_RECORD_MARKER = "//";

    private static final char PROTEIN_ID_LINE_START = '>';

    /**
     * Matches the line with the start and stop coordinates of the coiled region.
     * Group 1: Start
     * Group 2: Stop.
     */
    private static final Pattern START_STOP_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d+).*$");


    public Set<ParseCoilsMatch> parse(InputStream is, String fileName) throws IOException, ParseException {
        BufferedReader reader = null;
        Set<ParseCoilsMatch> matches = new HashSet<ParseCoilsMatch>();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String currentProteinAccession = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!END_OF_RECORD_MARKER.equals(line)) {
                    if (line.length() > 1 && line.charAt(0) == PROTEIN_ID_LINE_START) {
                        currentProteinAccession = line.substring(1).trim();
                    } else if (currentProteinAccession != null) {
                        Matcher matcher = START_STOP_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            final int start = Integer.parseInt(matcher.group(1));
                            final int end = Integer.parseInt(matcher.group(2));
                            matches.add(new ParseCoilsMatch(
                                    currentProteinAccession,
                                    start,
                                    end
                            ));
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return matches;
    }
}
