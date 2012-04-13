package uk.ac.ebi.interpro.scan.io.prodom.match;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a ProDom results file.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BlastProDomMatchParser extends AbstractLineMatchParser<ProDomRawMatch> {

    /*
     * Example file content:
     *
     * UPI00004BBFB1      1    198 //  pd_PD400414;sp_U689_HUMAN_Q6UX39;       1    206 // S=426    E=1e-41  //  (3)  PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED 	 Length = 206
     * //
     * UPI00004BBFB2      1    212 //  pd_PD400414;sp_U689_HUMAN_Q6UX39;       1    206 // S=501    E=2e-50  //  (3)  PRECURSOR SIGNAL UNQ689/PRO1329 HOMOLOG DIRECT SEQUENCING EO-017 SECRETED 	 Length = 206
     * //
     * UPI00004BBFB6      1     96 //  pd_PD021296;sp_Q5RC17_PONPY_Q5RC17;      24    159 // S=401    E=2e-39  //  (6)  J IMMUNOGLOBULIN CHAIN GLYCOPROTEIN SEQUENCING DIRECT IGJ_PREDICTED ACID PYRROLIDONE CARBOXYLIC 	 Length = 136
     * //
     *
     */

    private static final Logger LOGGER = Logger.getLogger(BlastProDomMatchParser.class.getName());

    /*
     * 1. Protein Accession (as in Fasta File) (String)
     * 2. Sequence match start coordinate (Integer)
     * 3. Sequence match end coordinate (Integer)
     * 4. Prodom signature accession (String)
     * 5. Signature match start coordinate (Integer)
     * 6. Signature match stop coordinate (Integer)
     * 7. Score (Integer)
     * 8. E value (Floating point - could be exponent)
     * 9. Number of domains in this family (Integer)
     * 10. Protein description (String)
     * (11. Sequence match length - not required, can be derived)
     */

    private static final Pattern LINE_PATTERN = Pattern.compile("^(\\S+)\\s+\\d+\\s+\\d+\\s+//"); // E.g. line starts with "1 1 198 //"...

    private static final Pattern RECORD_END_PATTERN = Pattern.compile("^//$");

    public BlastProDomMatchParser(String signatureLibraryRelease) {
        super(SignatureLibrary.PRODOM, signatureLibraryRelease);
    }

    @Override
    protected ProDomRawMatch createMatch(String line) {
        line = line.trim();
        Matcher data = LINE_PATTERN.matcher(line);
        if (!data.find()) {
            // Not interested in this line so just ignore
            Matcher recordEndMatcher = RECORD_END_PATTERN.matcher(line); // A line of: "//" (end of record)
            if (!recordEndMatcher.find()) {
                /*
                 * Note that sometimes a result line with no ProDom entry is returned. Compare a correct entry:
                 * UPI0000695DD7    244    294 //  pd_PD000222;sp_SPT1_HUMAN_O43278;     250    300 // S=283    E=1e-24  //  (512)  INHIBITOR PROTEASE PRECURSOR SERINE SIGNAL SEQUENCING DIRECT MATRIX PATHWAY FACTOR        Length = 51
                 * with an incorrect one:
                 * UPI0000695DD5     12     87 //      27    102 // S=340    E=1e-31  //    Length = 76 //
                 * I don't know why this is, but it is repeatable for certain sequences. Hence ignore this result
                 * and don't fail parsing (just log the parsing error).
                 */
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("ProDom match result in unexpected format ignored: " + line);
                }
            }
            return null;
        }

        String token;

        String sequenceIdentifier = null;
        int sequenceStart = 0;
        int sequenceEnd = 0;
        String modelId = null;
        String spId = null;
        int modelStart = 0;
        int modelEnd = 0;
        int score = 0;
        double evalue = 0.0;
        int numDomainsInFamily = 0;
        StringBuilder descAndLength = new StringBuilder();
        String description;

        String[] values = line.split("\\s+");
        int i = 0;
        while (i < values.length) {

            token = values[i];

            try {

                switch (i) {
                    case 0:
                        sequenceIdentifier = token;
                        break;
                    case 1:
                        sequenceStart = Integer.parseInt(token);
                        break;
                    case 2:
                        sequenceEnd = Integer.parseInt(token);
                        break;
                    case 4:
                        // ProDom model and sp Id, for example: pd_PD000222;sp_SPT1_HUMAN_O43278;
                        if (token.startsWith("pd_") && token.contains(";")) {
                            modelId = token.substring(3, token.indexOf(';'));
                            if (!modelId.startsWith("PD")) {
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info("ProDom model: " + modelId + " ignored as it looks invalid. Line: " + line);
                                }
                                return null;
                            }
                        }
                        if (token.contains("sp_") && token.endsWith(";")) {
                            spId = token.substring(token.indexOf("sp_") + 3, token.length() - 1);
                        }
                        if (modelId == null || spId == null) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("ProDom model Id/SP Id: " + token + " ignored as it looks invalid. Line: " + line);
                            }
                        }
                        break;
                    case 5:
                        modelStart = Integer.parseInt(token);
                        break;
                    case 6:
                        modelEnd = Integer.parseInt(token);
                        break;
                    case 8:
                        if (token.startsWith("S=")) {
                            score = Integer.parseInt(token.substring(2));
                        } else {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("ProDom score: " + token + " ignored as it looks invalid. Line: " + line);
                            }
                            return null;
                        }
                        break;
                    case 9:
                        if (token.startsWith("E=")) {
                            evalue = Double.parseDouble(token.substring(2));
                        } else {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("ProDom evalue: " + token + " ignored as it looks invalid. Line: " + line);
                            }
                            return null;
                        }
                        break;
                    case 11:
                        if (token.startsWith("(") && token.endsWith(")")) {
                            numDomainsInFamily = Integer.parseInt(token.substring(1, token.indexOf(')')));
                        } else {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("ProDom number of domains in family: " + token + " ignored as it looks invalid. Line: " + line);
                            }
                            return null;
                        }
                        break;
                    default:
                        if (i > 11) {
                            // For example: "INHIBITOR PROTEASE PRECURSOR SERINE SIGNAL SEQUENCING DIRECT MATRIX PATHWAY FACTOR        Length = 51"
                            descAndLength.append(token).append(' ');
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Error parsing ProDom match output file line (ignoring): " + line + " - Exception " + e.getMessage());
                return null;
            }

            i++;
        }

        // Now just split out the length from the description
        // For example: "INHIBITOR PROTEASE PRECURSOR SERINE SIGNAL SEQUENCING DIRECT MATRIX PATHWAY FACTOR        Length = 51"

        description = descAndLength.toString().trim();
        String key = "Length = ";
        if (description.contains(key)) {
            int keyPos = description.indexOf(key);
            if (keyPos > 0) {
                // NOTE: Match length can be derived so no need to store it
                //matchLength = Integer.parseInt(description.substring(keyPos + key.length()));
                description = description.substring(0, description.indexOf(key) - 1);
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("ProDom description/length text: " + description + " ignored as it looks invalid. Line: " + line);
                }
                return null;
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("ProDom description/length text: " + description + " ignored as it looks invalid. Line: " + line);
            }
            return null;
        }

        return new ProDomRawMatch(sequenceIdentifier, modelId, this.getSignatureLibraryRelease(),
                sequenceStart, sequenceEnd, spId, modelStart, modelEnd, score, evalue, numDomainsInFamily, description);
    }

}
