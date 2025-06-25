package uk.ac.ebi.interpro.scan.io.match.mobidb;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.MobiDBRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the IDRPred output format:
 * PROTEIN_A	16	47	-
 * PROTEIN_A	115	142	-
 * PROTEIN_A	117	130	Polyampholyte
 * PROTEIN_B	609	705	-
 * PROTEIN_B	622	632	Proline-rich
 * PROTEIN_B	655	675	Polar
 * PROTEIN_B	696	705	Polyampholyte
 * PROTEIN_B	792	820	-
 * PROTEIN_B	808	820	Polar
 * PROTEIN_B	874	893	-
 * PROTEIN_B	948	1566	-
 * PROTEIN_B	963	974	Polyampholyte
 *
 * @author Gift Nuka, Matthias Blum
 */
public class MobiDBMatchParser implements MatchParser<MobiDBRawMatch> {
    private  SignatureLibrary signatureLibrary;
    private  String signatureLibraryRelease;

    private static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)(.*)$");

    public MobiDBMatchParser() {
        super();
    }

    public MobiDBMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        this.signatureLibrary = signatureLibrary;
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

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

    public Set<RawProtein<MobiDBRawMatch>> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein<MobiDBRawMatch>> matchData = new HashMap<>();

        Set<MobiDBRawMatch> rawMatches = parseFileInput(is);

        for (MobiDBRawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (matchData.containsKey(sequenceId)) {
                RawProtein<MobiDBRawMatch> rawProtein = matchData.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<MobiDBRawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                matchData.put(sequenceId, rawProtein);
            }
        }

        return new HashSet<>(matchData.values());
    }

    public Set<MobiDBRawMatch> parseFileInput(InputStream is) throws IOException, ParseException {
        Set<MobiDBRawMatch> matches = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Matcher matcher = DOMAIN_LINE_PATTERN.matcher(line.trim());

                if (matcher.matches()) {
                    String sequenceIdentifier = matcher.group(1);
                    int locationStart = Integer.parseInt(matcher.group(2));
                    int locationEnd = Integer.parseInt(matcher.group(3));
                    String feature = matcher.group(4).trim();
                    if (feature.equals("-")) {
                        feature = null;
                    }

                    matches.add(new MobiDBRawMatch(sequenceIdentifier, "mobidb-lite",
                            SignatureLibrary.MOBIDB_LITE, signatureLibraryRelease,
                            locationStart, locationEnd, feature));
                }
            }
        }

        return matches;
    }
}
