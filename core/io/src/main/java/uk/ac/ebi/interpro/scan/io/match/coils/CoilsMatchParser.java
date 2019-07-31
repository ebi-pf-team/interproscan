package uk.ac.ebi.interpro.scan.io.match.coils;


import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.model.raw.CoilsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the Coils output format:
 * <p/>
 * >UNIPARC:UPI00000000FC status=active
 * 165 186
 * //
 * >UNIPARC:UPI0000000107 status=active
 * //
 * >UNIPARC:UPI0000000108 status=active
 * 73 94
 * 123 158
 * //
 * >UNIPARC:UPI0000000109 status=active
 * //
 *
 * @author Gift Nuka
 * @version $Id: CoilsMatchParser.java,v 1.1 2009/11/25 14:01:17 pjones Exp $
 * @since 1.0-SNAPSHOT
 */
public class CoilsMatchParser implements MatchParser<CoilsRawMatch> {

    private static final String END_OF_RECORD_MARKER = "//";

    private static final char PROTEIN_ID_LINE_START = '>';

    private  SignatureLibrary signatureLibrary;
    private  String signatureLibraryRelease;

    /**
     * Matches the line with the start and stop coordinates of the coiled region.
     * Group 1: Start
     * Group 2: Stop.
     */
    private static final Pattern START_STOP_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d+).*$");

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }


    public Set<RawProtein<CoilsRawMatch>> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein<CoilsRawMatch>> matchData = new HashMap<>();

        Set<CoilsRawMatch> rawMatches = parseFileInput(is);

        for (CoilsRawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (matchData.containsKey(sequenceId)) {
                RawProtein<CoilsRawMatch> rawProtein = matchData.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<CoilsRawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                matchData.put(sequenceId, rawProtein);
            }
        }

        return new HashSet<>(matchData.values());
    }

    public Set<CoilsRawMatch> parseFileInput(InputStream is) throws IOException, ParseException {
        BufferedReader reader = null;
        Set<CoilsRawMatch> matches = new HashSet<>();
        String coilsSignatureAccession = "Coil";
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String currentProteinAccession = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!END_OF_RECORD_MARKER.equals(line)) {
                    if (line.length() > 1 && line.charAt(0) == PROTEIN_ID_LINE_START) {
                        currentProteinAccession = line.substring(1).trim();
                    } else if (currentProteinAccession != null) {
                        Matcher matcher = START_STOP_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            final int start = Integer.parseInt(matcher.group(1));
                            final int end = Integer.parseInt(matcher.group(2));
                            matches.add(new CoilsRawMatch(
                                    currentProteinAccession,
                                    coilsSignatureAccession,
                                    SignatureLibrary.COILS,
                                    signatureLibraryRelease,
                                    start,
                                    end
                            ));
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return matches;
    }

}
