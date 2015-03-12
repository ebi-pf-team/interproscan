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
import java.util.regex.Pattern;

/**
 * Parser for TMHMM 2.0c (Prediction of transmembrane helices in proteins) raw result output.
 * <br/>If you run TMHMM 2.0c binary file (decodeanhmm) with the following parameters:
 * -N 1
 * -PrintNumbers
 * -background '0.081 0.015 0.054 ...'
 * <p/>
 * you get the following result output for 2 specified protein sequences:
 * >1
 * %pred N0: O 1 199, M 200 222, i 223 233, M 234 256, o 257 270
 * ?0 OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO
 * <p/>
 * ?0 OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO OOOOOOOOOO
 * <p/>
 * >2
 * %pred N0: o 1 19, M 20 42, i 43 61, M 62 84, o 85 200
 * ?0 oooooooooo oooooooooM MMMMMMMMMM MMMMMMMMMM MMiiiiiiii iiiiiiiiii
 * <p/>
 * ?0 iMMMMMMMMM MMMMMMMMMM MMMMoooooo oooooooooo oooooooooo oooooooooo
 * <p/>
 * The main method of this parser returns a set of protein matches, which represent trans membrane regions.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class TMHMMRawResultParser {

    private static final Pattern PREDICTION_LINE_PATTERN = Pattern.compile("%pred N[B]?[(]?[0][)]?:[ ,a-zA-Z0-9]*");

    private static final Logger LOGGER = Logger.getLogger(TMHMMRawResultParser.class.getName());

    private final SignatureLibraryRelease signatureLibraryRelease;

    public TMHMMRawResultParser(SignatureLibraryRelease signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public Set<TMHMMProtein> parse(InputStream is) throws IOException {
        final Map<String, Set<TMHMMMatch>> proteinIdToMatchMap = new HashMap<String, Set<TMHMMMatch>>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 1 && line.charAt(0) == '>') {
                    //protein sequence identifier
                    String protSeqIdentifier = line.substring(1);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("New result for a given protein sequence with ID " + protSeqIdentifier + " found.");
                        LOGGER.debug("Processing transmembrane regions...");
                    }
                    parseTransmembraneRegions(proteinIdToMatchMap, protSeqIdentifier, reader.readLine());
                }
            }
        } catch (IOException io) {
            LOGGER.warn("Could not parse input stream!", io);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return createProteinsWithMatches(proteinIdToMatchMap);
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

    /**
     * Parses trans membrane protein regions out of the following line:
     * <p/>
     * %pred N0: o 1 19, M 20 42, i 43 61, M 62 84, o 85 200
     *
     * @param proteinIdToMatchMap
     * @param protSeqIdentifier
     * @param line
     */
    private void parseTransmembraneRegions(final Map<String, Set<TMHMMMatch>> proteinIdToMatchMap, String protSeqIdentifier, String line) {
        if (line != null && line.length() > 0) {
            if (PREDICTION_LINE_PATTERN.matcher(line).matches()) {
                int colonIndex = line.indexOf(":");
                line = line.substring(colonIndex + 1).trim();
                String[] proteinRegions = line.split(", ");
                for (String proteinRegion1 : proteinRegions) {
                    String proteinRegion = proteinRegion1.trim();
                    if (proteinRegion != null) {
                        String[] list = proteinRegion.split(" ");
                        if (list.length != 3) {
                            LOGGER.warn("Couldn't parse transmembrane region out of: " + Arrays.toString(list) + ". The array should look like this [M, 200, 222] and should be of length 3.");
                            return;
                        } else {
                            String signature = list[0].trim();
                            if (signature.equals("M")) {
                                int startPos = Integer.parseInt(list[1].trim());
                                int endPos = Integer.parseInt(list[2].trim());
                                saveTmhmmMatch(proteinIdToMatchMap, protSeqIdentifier, startPos, endPos, TMHMMSignature.MEMBRANE);
                            }
                        }
                    }
                }
            } else {
                LOGGER.warn("Unexpected format within prediction line - " + line);
            }
        }
    }

    private void saveTmhmmMatch(Map<String, Set<TMHMMMatch>> proteins, String sequenceIdentifier, int start,
                                int end, TMHMMSignature prediction) {
        TMHMMMatch.TMHMMLocation location = buildTmhmmLocation(start, end, prediction);
        Set<TMHMMMatch> matches = proteins.get(sequenceIdentifier);
        if (matches == null) {
            matches = new LinkedHashSet<TMHMMMatch>();
        }
        matches.add(createNewTmhmmMatch(prediction, Collections.singleton(location)));
        proteins.put(sequenceIdentifier, matches);
    }

    private TMHMMMatch.TMHMMLocation buildTmhmmLocation(int start, int end, TMHMMSignature prediction) {
        return new TMHMMMatch.TMHMMLocation.Builder(start, end)
                .prediction(prediction.toString())
                .build();
    }

    private TMHMMMatch createNewTmhmmMatch(TMHMMSignature prediction, Set<TMHMMMatch.TMHMMLocation> locations) {
        Signature signature = new Signature.Builder(prediction.getAccession()).
                description(prediction.getShortDesc()).
                signatureLibraryRelease(signatureLibraryRelease).
                build();
        return new TMHMMMatch(signature, locations);
    }
}
