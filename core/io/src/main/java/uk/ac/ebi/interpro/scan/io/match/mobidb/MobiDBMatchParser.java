package uk.ac.ebi.interpro.scan.io.match.mobidb;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;

import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.MobiDBRawMatch;

import uk.ac.ebi.interpro.scan.model.raw.PfScanRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the MobiDB output format:
 * <p>
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
 * @version $Id: MobiDBMatchParser.java,v 5.20 2016/10/21 14:01:17 nuka Exp $
 * @since 1.0-SNAPSHOT
 */
public class MobiDBMatchParser implements MatchParser<MobiDBRawMatch> {

    private static final Logger LOGGER = Logger.getLogger(MobiDBMatchParser.class.getName());

    private static final String END_OF_RECORD_MARKER = "//";

    private static final char PROTEIN_ID_LINE_START = '>';

    private  SignatureLibrary signatureLibrary;
    private  String signatureLibraryRelease;

    private static final Pattern QUERY_LINE_PATTERN
            = Pattern.compile("^QUERY\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(.*)$");
    private static final Pattern DOMAIN_LINE_PATTERN
            = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)");
            //s+(.*)$");

    /**
     * Matches the line with the start and stop coordinates of the disordered region.
     * Group 1: Start
     * Group 2: Stop.
     */
    private static final Pattern START_STOP_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d+).*$");


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
            String proteinIdentifier;
            int lineNumber = 0;
            String definitionLine = "";
            String sequenceIdentifier = "";
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                LOGGER.debug("Line: " + line);
                //id 80	99
//                Utilities.verboseLog("Domain line: " + line);
                Matcher matcher = DOMAIN_LINE_PATTERN.matcher(line.trim());
                if (matcher.matches()) {
                    sequenceIdentifier = matcher.group(1);
                    int locationStart = Integer.parseInt(matcher.group(2));
                    int locationEnd = Integer.parseInt(matcher.group(3));

                    //TODO hardcoded accession should be removed
                    matches.add(new MobiDBRawMatch(sequenceIdentifier, "mobidb-lite",
                            SignatureLibrary.MOBIDB_LITE, signatureLibraryRelease,
                            locationStart, locationEnd));
//                    Utilities.verboseLog(10, "Match  : " + getLastElement(matches));
                }
            }
        }
        Utilities.verboseLog("MobiDB matches size : " + matches.size());
        return matches;
    }

    //get  the last item in the set
    public Object getLastElement(final Collection c) {
        final Iterator itr = c.iterator();
        Object lastElement = itr.next();
        while (itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }


}
