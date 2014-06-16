package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static final Logger LOGGER = Logger.getLogger(PirsfBlastResultParser.class.getName());

    /**
     * Parses all PIRSF IDs out of the BLAST standard output (line per line) and counts the occurrence of each ID.
     * The result is stored in a hash map.
     */
//    public static Map<String, Integer> parseBlastStandardOutput(InputStream is) {
//        Map<String, Integer> pirsfIdHitNumberMap = new HashMap<String, Integer>();
//        if (is != null) {
//            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
//            try {
//                String readline = null;
//                while ((readline = bf.readLine()) != null) {
//                    String pirsfId = parseBlastResultLine(readline);
//                    Integer numberOfHits = pirsfIdHitNumberMap.get(pirsfId);
//                    if (numberOfHits != null) {
//                        numberOfHits++;
//                    } else {
//                        numberOfHits = 1;
//                    }
//                    pirsfIdHitNumberMap.put(pirsfId, numberOfHits);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (is != null) {
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (bf != null) {
//                    try {
//                        bf.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return pirsfIdHitNumberMap;
//    }

    /**
     * Parses all PIRSF IDs out of the BLAST standard output (line by line) and counts the occurrence of each ID.
     * The result is stored in a hash map.
     * <p/>
     * Example blast results file:
     * <p/>
     * 3	A1FUJ0	41.40	314	180	3	3	315	20	330	3.5e-55	218.4
     * 3	Q0I9F4-SF000729	35.99	364	225	3	7	363	5	367	4.5e-55	218.0
     * 3	Q011Y4	43.01	286	157	4	6	288	10	292	4.5e-55	218.0
     * 3	Q3BSJ0-SF000729	38.29	363	208	6	7	356	1	360	5.9e-55	217.6
     * 4	Q2S6F1	46.64	461	241	2	6	462	55	514	4e-117	424.5
     * 4	Q3SIV0-SF000210	44.52	456	250	1	6	458	10	465	7e-109	397.1
     * 4	Q01YS3-SF000210	46.07	458	241	2	5	462	4	455	2e-106	389.0
     * 4	Q4IBF8-SF000210	43.79	475	248	5	6	462	5	478	6e-106	387.5
     * 4	O69236-SF000350	34.79	457	292	2	6	462	85	535	7.1e-75	284.3
     * 4	Q9F4C3-SF000350	34.57	457	293	3	6	462	85	535	6.0e-74	281.2
     * 4	Q3Y2B1-SF000210	36.60	459	268	6	3	457	2	441	1.7e-73	279.6
     * 4	Q2BTS5-SF000210	36.78	454	266	7	9	458	7	443	1.7e-73	279.6
     * <p/>
     * Required output after parsing (HashMap):
     * <p/>
     * 3-SF000729 -> 2
     * 4-SF000210 -> 5
     * <p/>
     * Note: We are only interested in the number of hits for the FIRST (lowest e-value therefore best) blast match for
     * each protein Id, so no need to record number of hits for SF000350 match.
     *
     * @param pathToFile path to the File.
     * @return A Map of ID to count.
     * @throws java.io.IOException in the event of a problem reading the file.
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(blastOutputFile));
            String readline;
            Long currentProteinId = null;
            String currentModelId = null;
            while ((readline = reader.readLine()) != null) {
                //String pirsfId = parseBlastResultLine(readline);
                long proteinId;
                String modelId;
                String[] columns = readline.split("\t");
                if (columns != null && columns.length == 12) {
                    proteinId = Long.parseLong(columns[0]);
                    if (currentProteinId == null) {
                        // First result line for the blast output file
                        currentProteinId = proteinId;
                    } else if (currentProteinId != proteinId) {
                        // First line of results for this protein Id
                        currentProteinId = proteinId;
                        currentModelId = null;
                    }
                    String matchId = columns[1];
                    String[] dividedMatchId = matchId.split("-");
                    if (dividedMatchId != null && dividedMatchId.length == 2) {
                        // Only interested in this value if it is in this format: Q3SIV0-SF000210
                        if (currentModelId == null) {
                            // The first modelId to appear in the blast output file has the lowest e-value so is best
                            // and the only match we care about!
                            currentModelId = dividedMatchId[1];
                            String key = currentProteinId.toString() + '-' + currentModelId;
                            pirsfIdHitNumberMap.put(key, 1);
                        } else {
                            modelId = dividedMatchId[1];
                            if (currentModelId.equals(modelId)) {
                                // Another match for the best modelId for this
                                String key = currentProteinId.toString() + '-' + currentModelId;
                                if (pirsfIdHitNumberMap.containsKey(key)) {
                                    Integer numOfHits = pirsfIdHitNumberMap.get(key);
                                    numOfHits++;
                                    pirsfIdHitNumberMap.put(key, numOfHits);
                                } else {
                                    // Should never happen! Sanity check.
                                    LOGGER.warn("Could not increment number of hits for this protein Id and best match model Id");
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Skip line - wrong number of columns in blast output file for line: " + readline);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return pirsfIdHitNumberMap;
    }


}
