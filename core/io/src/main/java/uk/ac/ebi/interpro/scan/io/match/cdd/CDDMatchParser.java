package uk.ac.ebi.interpro.scan.io.match.cdd;


import org.apache.log4j.Logger;
import org.apache.log4j.net.SyslogAppender;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the CDD output format:
 *
 * //
 *
 * @author Gift Nuka
 * @version $Id: CDDMatchParser.java,v 1.1 2015/12/16 14:01:17 nuka Exp $
 * @since 5.16
 */
public class CDDMatchParser implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(CDDMatchParser.class.getName());

    private static final char PROTEIN_ID_LINE_START = '>';

    private static final String DATA_BLOCK_START_MARKER = "DATA";
    private static final String DATA_BLOCK_END_MARKER = "ENDDATA";
    private static final String SESSION_BLOCK_START_MARKER = "SESSION";
    private static final String SESSION_BLOCK_END_MARKER = "ENDSESSION";
    private static final String QUERY_BLOCK_START_MARKER = "QUERY";
    private static final String QUERY_BLOCK_END_MARKER = "ENDQUERY";

    private static final String DOMAINS_BLOCK_START_MARKER = "DOMAINS";
    private static final String DOMAINS_BLOCK_END_MARKER = "ENDDOMAINS";
    private static final String SITES_BLOCK_START_MARKER = "SITES";
    private static final String SITES_BLOCK_END_MARKER = "ENDSITES";

    private static final String MOTIFS_BLOCK_START_MARKER = "MOTIFS";
    private static final String MOTIFS_BLOCK_END_MARKER = "ENDMOTIFS";

    private static final String END_OF_RECORD_MARKER = "//";

    /**
     #QUERY	<query-id>	<seq-type>	<seq-length>	<definition-line>
     #DOMAINS
     #<session-ordinal>	<query-id[readingframe]>	<hit-type>	<PSSM-ID>	<from>	<to>	<E-Value>	<bitscore>	<accession>	<short-name>	<incomplete>	<superfamily PSSM-ID>
     QUERY	Query_1	Peptide	590	sp|Q96N58|ZN578_HUMAN Zinc finger protein 578 OS=Homo sapiens GN=ZNF578 PE=2 SV=2
     DOMAINS
     1	Query_1	Specific	143639	24	60	3.46102e-15	69.5006	cd07765	KRAB_A-box	-	271597
     ENDDOMAINS

     */

    private static final Pattern QUERY_LINE_PATTERN
            = Pattern.compile("^QUERY\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(.*)$");
    private static final Pattern DOMAIN_LINE_PATTERN
            = Pattern.compile("^(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)");
    /**
     * Matches the line with the start and stop coordinates of the cdd region.
     * Group 1: Start
     * Group 2: Stop.
     */
    private static final Pattern START_STOP_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d+).*$");


    public Set<RawProtein<CDDRawMatch>> parse(InputStream is, String fileName) throws IOException, ParseException {

        Map<String, RawProtein<CDDRawMatch>> matchData = new HashMap<String, RawProtein<CDDRawMatch>>();

        Set<CDDRawMatch> rawMatches = parseFileInput(is);
        CDDRawMatch rawMatche;
        for (CDDRawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (matchData.containsKey(sequenceId)) {
                RawProtein<CDDRawMatch> rawProtein = matchData.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<CDDRawMatch> rawProtein = new RawProtein<CDDRawMatch>(sequenceId);
                rawProtein.addMatch(rawMatch);
                matchData.put(sequenceId, rawProtein);
            }
        }

        return new HashSet<RawProtein<CDDRawMatch>>(matchData.values());
    }

    public Set<CDDRawMatch> parseFileInput(InputStream is) throws IOException, ParseException {
        BufferedReader reader = null;
        Set<CDDRawMatch> matches = new HashSet<CDDRawMatch>();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String currentProteinAccession = null;
            String line;
            String proteinIdentifier = null;
            int lineNumber = 0;
            String definitionLine = "";
            String sequenceIdentifier = "";
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                LOGGER.debug("line: " + line);
                //System.out.println("line: " + line);

                if (line.startsWith(DATA_BLOCK_END_MARKER)) {

                } else if (line.startsWith(SESSION_BLOCK_START_MARKER)) {
                    //get the session data line
                    //line = reader.readLine();
                    proteinIdentifier = line.split("\\s+")[1].trim();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Protein: " + proteinIdentifier);
                    }
                    System.out.println("SESSION ID: " + proteinIdentifier);
                } else if (line.startsWith(QUERY_BLOCK_START_MARKER)) {
                    //QUERY	Query_1	Peptide	590	sp|Q96N58|ZN578_HUMAN Zinc finger protein 578 OS=Homo sapiens GN=ZNF578 PE=2 SV=2
                    System.out.println("Query line: " + line);
                    Matcher matcher = QUERY_LINE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String queryId = matcher.group(1);
                        String sequenceType = matcher.group(2);
                        String sequenceLength = matcher.group(3);
                        definitionLine = matcher.group(4);
                        sequenceIdentifier = definitionLine.trim();
                        System.out.println("Query: " + queryId
                                + ": sequenceIdentifier : " + sequenceIdentifier  + " "
                                + sequenceType + " " + sequenceLength + " " + definitionLine);
                    }
                } else if (line.startsWith(DOMAINS_BLOCK_START_MARKER)) {
                    //DOMAINS
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(DOMAINS_BLOCK_END_MARKER)) {
                            break;
                        }
                        //1	Query_1	Specific	143639	24	60	3.46102e-15	69.5006	cd07765	KRAB_A-box	-	271597
                        LOGGER.debug("line: " + line);
                        System.out.println("Domain line: " + line);
                        Matcher matcher = DOMAIN_LINE_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            int sessionNumber = Integer.parseInt(matcher.group(1));
                            String queryId = matcher.group(2);
                            String queryType = matcher.group(3);
                            String pssmID = matcher.group(4);
                            int locationStart = Integer.parseInt(matcher.group(5));
                            int locationEnd = Integer.parseInt(matcher.group(6));
                            double eValue = Double.parseDouble(matcher.group(7));
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Parsed out evalue: " + eValue);
                            }
                            double score = Double.parseDouble(matcher.group(8));
                            String model = matcher.group(9); //accession
                            String shortName = matcher.group(10);
                            String incomplete = matcher.group(11);
                            String superfamilyPSSMId = matcher.group(12);

//                                                if (proteinIdentifier == null) {
//                                                    throw new ParseException("CDD output parsing: Trying to parse raw output but don't appear to have a protein ID.", fileName, line, lineNumber);
//                                                }

                            //TODO remove the hard coded version
                            matches.add(new CDDRawMatch(sequenceIdentifier, definitionLine, sessionNumber, queryType,
                                    pssmID, model, locationStart, locationEnd, eValue, score,
                                    shortName, incomplete, superfamilyPSSMId, "3.14"));
                            System.out.println("match  : " + (CDDRawMatch) getLastElement(matches));
                        }
                    }
                }
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        System.out.println("CDD matches : " + matches);
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
