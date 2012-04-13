package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.TMHMMSignature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for TMHMM 2.0c (Prediction of transmembrane helices in proteins) output. Parses a single line of the raw result.
 * <br/>Example lines of TMHMM raw result (tab separated entries)
 * # 1
 * #         i         O         o         M
 * M      0.20497   0.77531   0.01971   0.00000
 * A      0.20497   0.77531   0.01971   0.00000
 * A      0.20497   0.77531   0.01971   0.00000
 * E      0.20497   0.77531   0.01971   0.00000
 * <p/>
 * Citation Robert Petryszak (copied from Onion code):
 * Note that the following method is used for gathering scores:
 * At each AA position, find the highest probability of the four (i, O, o, M), and treat
 * the corresponding prediction as the overriding one for that AA position.
 * If this prediction is different from the one for the previous AA position, then calculate
 * the median (the middle score from the list of scores sorted in ascending order) for the
 * previus prediction and store it with its corresponding to-from positions.
 * For the new prediction, store its initial position and start accumulating
 * the scores while that prediction's score is the highest of the four (i, O, o, M).
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @deprecated This parser still works fine, but was replaced by {@link TMHMMRawResultParser} within TMHMMParseStep. The reason for that is, that the format
 *             of the raw result output changed completely after changing the set of program parameters.
 */
@Deprecated
public final class TMHMMPredictionTableParser {

    private static final Logger LOGGER = Logger.getLogger(TMHMMPredictionTableParser.class.getName());

    private final SignatureLibraryRelease signatureLibraryRelease;

    public TMHMMPredictionTableParser(SignatureLibraryRelease signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    /**
     * Reads in the analysis result file and parses out all information.
     * </br>
     * Example lines of TMHMM raw result (tab separated entries)
     * # 1
     * #         i         O         o         M
     * M      0.20497   0.77531   0.01971   0.00000
     * A      0.20497   0.77531   0.01971   0.00000
     * A      0.20497   0.77531   0.01971   0.00000
     * E      0.20497   0.77531   0.01971   0.00000
     *
     * @param is
     * @return
     * @throws IOException
     */
    public Set<TMHMMProtein> parse(InputStream is) throws IOException {
        //Map between protein IDs (sequenceId)  and TMHMM match locations
        final Map<String, Set<TMHMMMatch>> seqIdMatchMap = new HashMap<String, Set<TMHMMMatch>>();

        String prevSequenceId, sequenceId = null;
        int startPos = 0;
        int currentAAPos = -1;
        TMHMMSignature prevPrediction = null;
        TMHMMSignature prediction;
        List<Float> scores = new ArrayList<Float>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (checkLineForNewEntry(line)) {  // e.g. # UPI0000003CFF OR # 1
                    if (prevPrediction != null) {
                        // Store the last prediction for prevSequenceId
                        saveTmhmmMatch(seqIdMatchMap, sequenceId,
                                startPos, currentAAPos, prevPrediction, scores);
                    }
                    prevSequenceId = sequenceId;
                    sequenceId = line.substring(2);
                    if (sequenceId.equals("")) {
                        LOGGER.warn("Parsing error: non existent sequence identifier " + ((prevSequenceId != null) ? " after " + prevSequenceId : ""));
                    }
                    prevPrediction = null;
                    prediction = null;
                    currentAAPos = 0;
                    startPos = 0;

                    //Consume the headings line below
                    //#         i         O         o         M
                    reader.readLine();
                } else if (Character.isLetter(line.charAt(0))) {
                    currentAAPos++;
                    PredictionMaxScoreWrapper predictionWrapper = TMHMMPredictionLineParser.parsePredictionLine(line);
                    prediction = predictionWrapper.getPrediction();
                    if (prediction != prevPrediction) {
                        if (prevPrediction != null) {
                            // Store the previous prediction for sequenceId
                            saveTmhmmMatch(seqIdMatchMap, sequenceId,
                                    startPos, currentAAPos - 1, prevPrediction, scores);
                        }
                        prevPrediction = prediction;
                        startPos = currentAAPos;
                    }
                    scores.add(predictionWrapper.getMaxScore());
                    Collections.sort(scores);
                } // end if (Character.isLetter(in.charAt(0)))
            } // end while (in != null)
            // Now store the last prediction for the last sequenceId in the result file
            if (prevPrediction != null) {
                // Store the previous prediction for sequenceId
                saveTmhmmMatch(seqIdMatchMap, sequenceId, startPos, currentAAPos, prevPrediction, scores);
            }
        } catch (IOException io) {
            LOGGER.warn("Could not parse input stream!", io);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return createProteinsWithMatches(seqIdMatchMap);
    }

    private Set<TMHMMProtein> createProteinsWithMatches(Map<String, Set<TMHMMMatch>> seqIdMatchMap) {
        Set<TMHMMProtein> result = new HashSet<TMHMMProtein>();
        for (String id : seqIdMatchMap.keySet()) {
            //Create a new protein
            TMHMMProtein protein = new TMHMMProtein(id);
            //Add matches to protein
            Set<TMHMMMatch> matches = seqIdMatchMap.get(id);
            protein.addAllMatches(matches);
            //Add new protein to result map
            result.add(protein);
            //
            if (LOGGER.isDebugEnabled()) {
                for (TMHMMMatch match : matches) {
                    for (TMHMMMatch.TMHMMLocation location : match.getLocations()) {
                        LOGGER.debug(id + "    " + location.getPrediction() + " " + location.getStart() + " " + location.getEnd() + " " + location.getScore());
                    }
                }
            }
        }
        return result;
    }

    private void saveTmhmmMatch(Map<String, Set<TMHMMMatch>> proteins, String sequenceIdentifier, int start,
                                int end, TMHMMSignature prediction, List<Float> scores) {
        TMHMMMatch.TMHMMLocation location = buildTmhmmLocation(start, end, prediction, getCurrentScore(scores));
        Set<TMHMMMatch> matches = proteins.get(sequenceIdentifier);
        if (matches == null) {
            matches = new LinkedHashSet<TMHMMMatch>();
        }
        matches.add(createNewTmhmmMatch(prediction, Collections.singleton(location)));
        proteins.put(sequenceIdentifier, matches);
        scores.clear();
    }

    private TMHMMMatch.TMHMMLocation buildTmhmmLocation(int start, int end, TMHMMSignature prediction, float currentScore) {
        return new TMHMMMatch.TMHMMLocation.Builder(start, end)
                .prediction(prediction.toString())
                .score(currentScore)
                .build();
    }

    private TMHMMMatch createNewTmhmmMatch(TMHMMSignature prediction, Set<TMHMMMatch.TMHMMLocation> locations) {
        Signature signature = new Signature.Builder(prediction.getAccession()).
                description(prediction.getShortDesc()).
                signatureLibraryRelease(signatureLibraryRelease).
                build();
        return new TMHMMMatch(signature, locations);
    }


    protected boolean checkLineForNewEntry(String input) {
        Pattern p = Pattern.compile("# [a-zA-Z_0-9]+");
        if (p != null) {
            Matcher m = p.matcher(input);
            if (m != null) {
                return m.matches();
            }
        }
        return false;
    }

    private float getCurrentScore(List<Float> scores) {
        if (scores != null && scores.size() > 0) {
            // If the current prediction differs from the previous one, store the information
            // about the previous one first
            // First extract the median (i.e. the middle element from the sorted scores Vector)
            int len = scores.size();
            int midPos = len / 2;
            Float currentScore = scores.get(midPos);
            if (currentScore != null) {
                return currentScore.floatValue();
            }
        }
        return -1f;
    }
}
