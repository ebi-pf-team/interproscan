package uk.ac.ebi.interpro.scan.io.gene3d;

import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomTblDomainMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Parser for CathResolveHitsoutput ....
 */
public class CathResolveHitsOutputParser {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(CathResolveHitsOutputParser.class.getName());

    /**
     *
     * @param is
     * @return
     * @throws IOException
     */

    public Map<String, CathResolverRecord> parse(InputStream is) throws IOException {

        Map<String, CathResolverRecord> cathResolverRecordMap = new HashMap ();
        BufferedReader reader = null;
        int rawDomainCount = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith("#")){
                    continue;
                }
                // Look for a domain data line.
                CathResolverRecord cathRecord = CathResolverRecord.valueOf(line);
                if (cathRecord != null) {
                    String key = cathRecord.getRecordKey();
                    cathResolverRecordMap.put(key, cathRecord);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        Utilities.verboseLog(10, "CathResolverRecord count : " + cathResolverRecordMap.values().size());
        LOGGER.debug(" domtbl domain count : " + cathResolverRecordMap.values().size());

        return cathResolverRecordMap;
    }
}
