package uk.ac.ebi.interpro.scan.io.pirsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class which contains static methods to parse BLASTP standard output.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfBlastResultParser {

    /**
     * Parses all PIRSF IDs out of the BLAST standard output (line per line) and counts the occurrence of each ID.
     * The result is stored in a hash map.
     */
    public static Map<String, Integer> parseBlastStandardOutput(InputStream is) {
        Map<String, Integer> pirsfIdHitNumberMap = new HashMap<String, Integer>();
        if (is != null) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            try {
                String readline = null;
                while ((readline = bf.readLine()) != null) {
                    String pirsfId = parseBlastResultLine(readline);
                    Integer numberOfHits = pirsfIdHitNumberMap.get(pirsfId);
                    if (numberOfHits != null) {
                        numberOfHits++;
                    } else {
                        numberOfHits = 1;
                    }
                    pirsfIdHitNumberMap.put(pirsfId, numberOfHits);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bf != null) {
                    try {
                        bf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return pirsfIdHitNumberMap;
    }

    /**
     * Parses the PIRSF ID out of the specified Blast result line.<br>
     * Method is tested.
     */
    public static String parseBlastResultLine(String readline) {
        if (readline == null || readline.length() == 0)
            return null;

        String[] columns = readline.split("\t");
        if (columns != null && columns.length == 12) {
            String matchId = columns[1];
            String[] devidedMatchId = matchId.split("-");
            if (devidedMatchId != null && devidedMatchId.length == 2) {
                return devidedMatchId[1];
            }
        }
        return null;
    }

}
