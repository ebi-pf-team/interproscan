package uk.ac.ebi.interpro.scan.io.pirsf;

import java.io.*;
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
     * Parses all PIRSF IDs out of the BLAST standard output (line per line) and counts the occurrence of each ID.
     * The result is stored in a hash map.
     */
    public static Map<String, Integer> parseBlastOutputFile(String pathToFile) throws IOException {
        Map<String, Integer> pirsfIdHitNumberMap = new HashMap<String, Integer>();
        File blastOutputFile = new File(pathToFile);
        if (blastOutputFile == null) {
            throw new NullPointerException("Blast output file resource is null");
        }
        if (!blastOutputFile.exists()) {
            throw new IllegalStateException(blastOutputFile.getName() + " does not exist");
        }
        if (!blastOutputFile.canRead()) {
            throw new IllegalStateException(blastOutputFile.getName() + " is not readable");
        }
        final Map<Long, String> data = new HashMap<Long, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(blastOutputFile));
            String readline = null;
            while ((readline = reader.readLine()) != null) {
                String pirsfId = parseBlastResultLine(readline);
                Integer numberOfHits = pirsfIdHitNumberMap.get(pirsfId);
                if (numberOfHits != null) {
                    numberOfHits++;
                } else {
                    numberOfHits = 1;
                }
                pirsfIdHitNumberMap.put(pirsfId, numberOfHits);
            }
        } finally {
            if (reader != null) {
                reader.close();
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
