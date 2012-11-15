package uk.ac.ebi.interpro.scan.io.signalp.match;


import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse SignalP binary output file.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SignalPMatchParser implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SignalPMatchParser.class.getName());

    public static final String SIGNATURE_LIBRARY_RELEASE = "4.0";

    private final SignalPOrganismType type;

    public static final String FILE_START_LINE1 = "# SignalP-4.0 "; // Start of the first line of the file
    public static final String FILE_START_LINE2 = " predictions"; // End of the first line of the file
    public static final String RECORD_START_LINE = "# Measure  Position  Value  Cutoff  signal peptide?"; // First line of a result

    // Example match: "D     1-40    0.533    0.450  YES"
    private static final Pattern D_LINE = Pattern.compile("^D\\s+\\d+-\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+");

    // Example match: "Name=2   SP='Yes' Cleavage site between pos. 17 and 18: ASA-VP D=0.499 D-cutoff=0.450 Networks=SignalP-TM"
    private static final Pattern SP_LINE = Pattern.compile("^Name=.*\\s+SP=.*Networks=.*$");

    public SignalPMatchParser(String typeShortName) {
        this.type = SignalPOrganismType.getSignalPOrganismTypeByShortName(typeShortName);
    }

    public Set<RawProtein<SignalPRawMatch>> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein<SignalPRawMatch>> data = new HashMap<String, RawProtein<SignalPRawMatch>>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            int lineCount = 0;
            String line;

            String sequenceIdentifier;
            String model;
            Integer locationStart = null;
            Integer locationEnd = null;
            Double dScore = null;
            Double dCutoff = null;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (LOGGER.isDebugEnabled() && (lineCount % 10000 == 0)) {
                    LOGGER.debug("Parsing line: " + lineCount + " of the binary output file");
                }
                line = line.trim();
                if (line.startsWith(FILE_START_LINE1) && line.endsWith(FILE_START_LINE2)) {
                    // First line of the file, check the organism type
                    // E.g. "# SignalP-4.0 gram+ predictions"
                    int startIndex = FILE_START_LINE1.length();
                    int endIndex = line.indexOf(FILE_START_LINE2);
                    String typeString = line.substring(startIndex, endIndex);
                    if (type == null) {
                        LOGGER.error("SignalP organsim type not set in job XML, or not a valid value");
                        throw new IllegalStateException("SignalP organsim type not set in job XML");
                    } else if (!type.equals(SignalPOrganismType.getSignalPOrganismTypeByShortName(typeString))) {
                        LOGGER.error("SignalP organsim type in file:" + typeString + " does not match that supplied in job XML: " + type.getTypeShortName());
                        throw new IllegalStateException("SignalP organsim type in file:" + typeString + " does not match that supplied in job XML: " + type.getTypeShortName());
                    }
                    continue;
                } else if (line.equals(RECORD_START_LINE)) {
                    // Start of a new result record, reset the values
                    // E.g. "# Measure  Position  Value  Cutoff  signal peptide?"
                    sequenceIdentifier = null;
                    model = null;
                    locationStart = null;
                    locationEnd = null;
                    dScore = null;
                    dCutoff = null;
                    continue;
                }

                Matcher matcher1 = D_LINE.matcher(line);
                if (matcher1.find()) {
                    // Regex has ensured this matches the dScore line format
                    // E.g. "D     1-40    0.533    0.450  YES"
                    String[] lineArray = line.split("\\s+");
                    String[] startStop = lineArray[1].split("-");

                    locationStart = Integer.parseInt(startStop[0]);
                    locationEnd = Integer.parseInt(startStop[1]);
                    dScore = Double.parseDouble(lineArray[2]);
                    dCutoff = Double.parseDouble(lineArray[3]);
                    continue;
                }

                Matcher matcher2 = SP_LINE.matcher(line);
                if (matcher2.find()) {
                    // Regex has ensured this matches the SP line format
                    // E.g. "Name=2	SP='Yes' Cleavage site between pos. 17 and 18: ASA-VP D=0.499 D-cutoff=0.450 Networks=SignalP-TM"
                    String[] lineArray = line.split("\\s+");
                    if (lineArray[1].equalsIgnoreCase("SP='Yes'")) {
                        // Signal Peptide found, check previous line was OK
                        if (locationStart != null && locationEnd != null && dScore != null && dCutoff != null) {
                            sequenceIdentifier = lineArray[0]; // E.g. "Name=2"
                            if (sequenceIdentifier.startsWith("Name=")) {
                                sequenceIdentifier = sequenceIdentifier.substring(5); // E.g. "2"
                            } else {
                                LOGGER.warn("This line in the binary output file is in an unexpected format - ignoring: " + line);
                                continue;
                            }
                            model = lineArray[lineArray.length - 1]; // E.g. "Networks=SignalP-TM"
                            if (model.startsWith("Networks=")) {
                                model = model.substring(9); // E.g. "SignalP-TM"
                            } else {
                                LOGGER.warn("This line in the binary output file is in an unexpected format - ignoring: " + line);
                                continue;
                            }

                            SignatureLibrary signatureLibrary = SignalPOrganismType.getSignatureLibraryFromType(type);
                            if (signatureLibrary == null) {
                                throw new IllegalStateException("Invalid signature library for SignalP raw match");
                            }

                            SignalPRawMatch rawMatch = new SignalPRawMatch(sequenceIdentifier, model, signatureLibrary,
                                    SIGNATURE_LIBRARY_RELEASE, locationStart, locationEnd, this.type, dScore, dCutoff);

                            if (data.containsKey(sequenceIdentifier)) {
                                // Each protein will have one result - either Signal Peptide YES or NO!
                                // So the key should not already exist!
                                throw new IllegalStateException("Somehow protein " + sequenceIdentifier + " has already been processed!");
                            } else {
                                RawProtein<SignalPRawMatch> rawProtein = new RawProtein<SignalPRawMatch>(sequenceIdentifier);
                                rawProtein.addMatch(rawMatch);
                                data.put(sequenceIdentifier, rawProtein);
                            }

                        } else {
                            // Problem with the previous line, can't add this result
                            LOGGER.warn("Line " + lineCount + " not parsed as previous line was not parsed");
                        }
                    } else {
                        // No signal peptide found, don't need this result - carry on!
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("No signal peptide on line " + lineCount + ": " + line);
                        }
                    }
                }

            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new HashSet<RawProtein<SignalPRawMatch>>(data.values());
    }


}
