package uk.ac.ebi.interpro.scan.io.tmhmm;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;

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
 */
public final class TMHMMPredicationTableParser {

    private static final Logger LOGGER = Logger.getLogger(TMHMMPredicationTableParser.class.getName());

    /**
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
    public Map<String, Protein> parse(InputStream is) throws IOException {
        //Map between protein IDs (sequenceId)  and TMHMM matches
        final Map<String, Set<TMHMMMatch.TMHMMLocation>> seqIdLocationMap = new HashMap<String, Set<TMHMMMatch.TMHMMLocation>>();

        //First read in the analysis result file
        String prevSequenceId, sequenceId = null;
        int startPos = 0;
        int currentAAPos = -1;
        Prediction prevPred = null;
        Prediction pred = null;
        List<Float> scores = new ArrayList<Float>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (checkLineForNewEntry(line)) {  // e.g. # UPI0000003CFF OR # 1
                    if (prevPred != null) {
                        // Store the last prediction for prevSequenceId
                        saveTmhmmLocation(seqIdLocationMap, sequenceId,
                                startPos, currentAAPos, prevPred, scores);
                    }
                    prevSequenceId = sequenceId;
                    sequenceId = line.substring(2);
                    prevPred = null;
                    pred = null;
                    currentAAPos = 0;
                    startPos = 0;

                    //Consume the headings line below
                    //#         i         O         o         M
                    reader.readLine();
                    if (sequenceId == null || sequenceId.equals("")) {
                        LOGGER.warn("Parsing error: non existent sequence identifier " + ((prevSequenceId != null) ? "after " + prevSequenceId : ""));
                    }
                } else if (Character.isLetter(line.charAt(0))) {
                    PredictionMaxScoreWrapper predictionMaxScoreWrapper = null;
                    currentAAPos++;
                    String[] splitLine = line.split("\\s+");
                    int len = splitLine.length;
                    if (len == 5) {
                        //Get value for column i
                        float scoreInside = Float.parseFloat(splitLine[1].trim());
                        //Get value for column O
                        float scoreO = Float.parseFloat(splitLine[2].trim());
                        //Get value for column o
                        float scoreOutside = Float.parseFloat(splitLine[3].trim());
                        //Get value for column M
                        float scoreMembrane = Float.parseFloat(splitLine[4].trim());
                        //Get max score value and prediction
                        predictionMaxScoreWrapper = getPredictionValue(scoreInside, scoreO, scoreOutside, scoreMembrane);
                    }
                    if (pred != prevPred) {
                        if (prevPred != null) {
                            // Store the previous prediction for sequenceId
                            saveTmhmmLocation(seqIdLocationMap, sequenceId,
                                    startPos, currentAAPos - 1, prevPred, scores);
                        }
                        prevPred = pred;
                        scores.add(predictionMaxScoreWrapper.getMaxScore());
                        startPos = currentAAPos;

                    } else {
                        // prediction hasn't changed, in which case add maxScore into the sorted scores
                        int j = 0;
                        int size = scores.size();
                        while (j < size && scores.get(j) < predictionMaxScoreWrapper.getMaxScore()) {
                            j++;
                        }
                        scores.add(j, predictionMaxScoreWrapper.getMaxScore());
                    }
                } // end if (Character.isLetter(in.charAt(0)))
            } // end while (in != null)

            // Now store the last prediction for the last sequenceId in the result file
            if (prevPred != null) {
                // Store the previous prediction for sequenceId
                saveTmhmmLocation(seqIdLocationMap, sequenceId, startPos, currentAAPos - 1, prevPred, scores);
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not parse input stream!");
        }
        return createProteinsAndAttachMatches(seqIdLocationMap);
    }

    /**
     * Determines largest float value of all specified parameter values.
     *
     * @return Prediction value.
     */
    private PredictionMaxScoreWrapper getPredictionValue(float scoreInside, float scoreO, float scoreOutside, float scoreMembrane) {
        Prediction result = Prediction.INSIDE_CELL;
        float maxScore = scoreInside;
        if (scoreO > maxScore) {
            result = Prediction.OTHER;
            maxScore = scoreO;
        }
        if (scoreOutside > maxScore) {
            result = Prediction.OUTSIDE_CELL;
            maxScore = scoreOutside;
        }
        if (scoreMembrane > maxScore) {
            result = Prediction.MEMBRANE;
            maxScore = scoreMembrane;
        }
        return new PredictionMaxScoreWrapper(result, maxScore);
    }

    private Map<String, Protein> createProteinsAndAttachMatches(Map<String, Set<TMHMMMatch.TMHMMLocation>> seqIdLocationMap) {
        for (String id : seqIdLocationMap.keySet()) {
            Set<TMHMMMatch.TMHMMLocation> locations = seqIdLocationMap.get(id);
            for (TMHMMMatch.TMHMMLocation location : locations) {
                System.out.println(id + "    " + location.getPrediction() + " " + location.getStart() + " " + location.getEnd() + " " + location.getScore());
            }
        }
        return null;
    }

    private void saveTmhmmLocation(Map<String, Set<TMHMMMatch.TMHMMLocation>> proteins, String sequenceIdentifier, int start,
                                   int end, Prediction prediction, List<Float> scores) {
        TMHMMMatch.TMHMMLocation location = buildTmhmmLocation(start, end, prediction, getCurrentScore(scores));
        Set<TMHMMMatch.TMHMMLocation> locations = proteins.get(sequenceIdentifier);
        if (locations == null) {
            locations = new LinkedHashSet<TMHMMMatch.TMHMMLocation>();
        }
        locations.add(location);
        proteins.put(sequenceIdentifier, locations);
        scores.clear();
    }

    private TMHMMMatch.TMHMMLocation buildTmhmmLocation(int start, int end, Prediction prediction, float currentScore) {
        return new TMHMMMatch.TMHMMLocation.Builder(start, end)
                .prediction(prediction.toString())
                .score(currentScore)
                .build();
    }

    private TMHMMMatch createNewTmhmmMatch(TMHMMMatch.TMHMMLocation location) {
        Signature signature = new Signature.Builder("tmhmm").description("transmembrane_region").build();
        //
        Set<TMHMMMatch.TMHMMLocation> locations = new LinkedHashSet<TMHMMMatch.TMHMMLocation>();
        locations.add(location);
        //
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

    /**
     * Defines a transmembrane prediction as used by TMHMM.
     */
    enum Prediction {
        //TODO:Find out what upper case O means in this context
        INSIDE_CELL("i"), OUTSIDE_CELL("o"), MEMBRANE("M"), OTHER("O");

        private String location;

        private Prediction(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location;
        }
    }

    /**
     * Simple wrapper class. Wraps predictions and max scores.
     */
    class PredictionMaxScoreWrapper {
        private Prediction prediction;
        private float maxScore;

        protected PredictionMaxScoreWrapper() {
        }

        public PredictionMaxScoreWrapper(Prediction prediction, float maxScore) {
            this.prediction = prediction;
            this.maxScore = maxScore;
        }

        public Prediction getPrediction() {
            return prediction;
        }

        public float getMaxScore() {
            return maxScore;
        }
    }
}