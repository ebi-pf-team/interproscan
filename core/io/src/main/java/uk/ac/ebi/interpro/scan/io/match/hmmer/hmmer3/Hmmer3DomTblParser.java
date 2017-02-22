package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.*;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for HMMER3 domtbl output, based upon the working parser used in Onion....
 */
public class Hmmer3DomTblParser {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Hmmer3DomTblParser.class.getName());

    private static final String END_OF_RECORD = "//";

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */

    public Map<String, DomTblDomainMatch> parse(InputStream is) throws IOException {

        Map<String, DomTblDomainMatch> domainTblLineMap = new HashMap ();
        BufferedReader reader = null;
        int rawDomainCount = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Look for a domain data line.
                Matcher domainDataLineMatcher = DomTblDomainMatch.DOMAIN_LINE_PATTERN.matcher(line);
                if (domainDataLineMatcher.matches()) {
                    DomTblDomainMatch domainMatch = new DomTblDomainMatch(domainDataLineMatcher);
                    String key = domainMatch.getDomTblDominLineKey();
                    domainTblLineMap.put(key, domainMatch);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        Utilities.verboseLog(10, " domtbl domain count : " + domainTblLineMap.values().size());
        LOGGER.debug(" domtbl domain count : " + domainTblLineMap.values().size());

        return domainTblLineMap;
    }
}
