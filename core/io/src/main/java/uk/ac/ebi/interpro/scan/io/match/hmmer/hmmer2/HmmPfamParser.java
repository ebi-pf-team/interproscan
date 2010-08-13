package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamDomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2.parsemodel.Hmmer2HmmPfamSequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Parser for HMMER 2 hmmpfam binary output.
 * <p/>
 * TODO Does not parse alignment section yet. (Not needed for TIGRFAM or SMART)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 *        <p/>
 *        Example output (TIGRfam example):
 *        <p/>
 *        hmmpfam - search one or more sequences against HMM database
 *        HMMER 2.3.2 (Oct 2003)
 *        Copyright (C) 1992-2003 HHMI/Washington University School of Medicine
 *        Freely distributed under the GNU General Public License (GPL)
 *        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *        HMM file:                 /home/pjones/projects/i5_resources/support/data/tigrfam/TIGRFAMs_9.0_HMM.LIB.bin
 *        Sequence file:            temp/maple_20100806_132340769_7ny6/jobTIGRFAM/000000001001_000000002000.fasta
 *        - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *        <p/>
 *        Query sequence: 1011
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model    Description                                    Score    E-value  N
 *        -------- -----------                                    -----    ------- ---
 *        TIGR01163 rpe: ribulose-phosphate 3-epimerase            504.4   5.5e-149   1
 *        <p/>
 *        Parsed for domains:
 *        Model    Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        -------- ------- ----- -----    ----- -----      -----  -------
 *        TIGR01163   1/1       5   216 ..     1   216 []   504.4 5.5e-149
 *        //
 *        <p/>
 *        Query sequence: 1012
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model           Description                             Score    E-value  N
 *        --------        -----------                             -----    ------- ---
 *        TIGR03593       yidC_nterm: membrane protein insertas   513.0   1.4e-151   1
 *        TIGR03592       yidC_oxa1_cterm: membrane protein ins   398.7   3.5e-117   1
 *        <p/>
 *        Parsed for domains:
 *        Model           Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        --------        ------- ----- -----    ----- -----      -----  -------
 *        TIGR03593         1/1       3   352 ..     1   398 []   513.0 1.4e-151
 *        TIGR03592         1/1     353   533 ..     1   231 []   398.7 3.5e-117
 *        //
 *        <p/>
 *        Query sequence: 1013
 *        Accession:      [none]
 *        Description:    [none]
 *        <p/>
 *        Scores for sequence family classification (score includes all domains):
 *        Model    Description                                    Score    E-value  N
 *        -------- -----------                                    -----    ------- ---
 *        [no hits above thresholds]
 *        <p/>
 *        Parsed for domains:
 *        Model    Domain  seq-f seq-t    hmm-f hmm-t      score  E-value
 *        -------- ------- ----- -----    ----- -----      -----  -------
 *        [no hits above thresholds]
 *        //
 */
public class HmmPfamParser<T extends RawMatch> implements MatchParser {

    private static final Logger LOGGER = Logger.getLogger(HmmPfamParser.class.getName());

    private static final String END_OF_RECORD = "//";

    private static final String START_OF_SEQUENCE_MATCH_SECTION = "Scores for sequence family classification";

    private static final String START_OF_DOMAIN_MATCH_SECTION = "Parsed for domains:";

    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "[no hits above thresholds]";

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage {
        LOOKING_FOR_SEQUENCE_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCH_SECTION,
        LOOKING_FOR_DOMAIN_SECTION,
        IN_SEQUENCE_MATCH_SECTION, IN_DOMAIN_MATCH_SECTION, FINISHED_SEARCHING_RECORD

    }

    private SignatureLibrary signatureLibrary;

    private String signatureLibraryRelease;

    private Hmmer2ParserSupport<T> hmmer2ParserSupport;

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    @Required
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

    @Required
    public void setHmmer2ParserSupport(Hmmer2ParserSupport<T> hmmer2ParserSupport) {
        this.hmmer2ParserSupport = hmmer2ParserSupport;
    }

    @Override
    public Set<RawProtein<T>> parse(InputStream is) throws IOException {
        Map<String, RawProtein<T>> rawResults = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        ParsingStage stage = ParsingStage.LOOKING_FOR_SEQUENCE_ACCESSION;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            Hmmer2HmmPfamSearchRecord searchRecord = null;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith(END_OF_RECORD)) {
                    // Process a complete record - store all sequence / domain scores
                    // for the method.
                    if (searchRecord == null) {
                        throw new ParseException("Got to the end of a hmmpfam full output file section without finding any details of a method.", null, line, lineNumber);
                    }

                    hmmer2ParserSupport.addMatch(searchRecord, rawResults);
                    searchRecord = null; // Reset, so can check for last record after the loop.
                    stage = ParsingStage.LOOKING_FOR_SEQUENCE_ACCESSION;
                } else {
                    switch (stage) {
                        case LOOKING_FOR_SEQUENCE_ACCESSION:
                            if (line.startsWith(hmmer2ParserSupport.getHmmKey().getPrefix())) {
                                stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCH_SECTION;

                                Matcher sequenceIdentLinePatternMatcher = hmmer2ParserSupport.getSequenceIdentLinePattern().matcher(line);
                                if (sequenceIdentLinePatternMatcher.matches()) {
                                    searchRecord = new Hmmer2HmmPfamSearchRecord(hmmer2ParserSupport.getSequenceId(sequenceIdentLinePatternMatcher));
                                } else {
                                    throw new ParseException("Found a line starting with " + hmmer2ParserSupport.getHmmKey().getPrefix() + " but cannot parse it with the SEQUENCE_ACCESSION_LINE regex.", null, line, lineNumber);
                                }
                            }
                            break;

                        case LOOKING_FOR_SEQUENCE_MATCH_SECTION:
                            if (line.startsWith(START_OF_SEQUENCE_MATCH_SECTION)) {
                                stage = ParsingStage.IN_SEQUENCE_MATCH_SECTION;
                            }
                            break;

                        case IN_SEQUENCE_MATCH_SECTION:
                            // Sanity check.
                            if (searchRecord == null) {
                                throw new ParseException("The parse stage is 'looking for sequence matches' however there is no sequence in memory.", null, line, lineNumber);
                            }

                            if (line.contains(END_OF_GOOD_SEQUENCE_MATCHES) || line.trim().length() == 0) {
                                stage = (searchRecord.getSequenceMatches().size() == 0)
                                        ? ParsingStage.FINISHED_SEARCHING_RECORD
                                        : ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                            } else {

                                Matcher sequenceMatchLineMatcher = Hmmer2HmmPfamSequenceMatch.SEQUENCE_LINE_PATTERN.matcher(line);
                                if (sequenceMatchLineMatcher.matches()) {
                                    // Found a sequence match line above the threshold.
                                    Hmmer2HmmPfamSequenceMatch sequenceMatch = new Hmmer2HmmPfamSequenceMatch(sequenceMatchLineMatcher);
                                    searchRecord.addSequenceMatch(sequenceMatch);
                                }
                            }
                            break;

                        case LOOKING_FOR_DOMAIN_SECTION:
                            if (line.startsWith(START_OF_DOMAIN_MATCH_SECTION)) {
                                stage = ParsingStage.IN_DOMAIN_MATCH_SECTION;
                            }
                            break;

                        case IN_DOMAIN_MATCH_SECTION:

                            if (!(line.trim().length() == 0 ||
                                    line.startsWith("Model") ||
                                    line.startsWith("--------") ||
                                    line.contains(END_OF_GOOD_SEQUENCE_MATCHES))) {
                                // Should be a proper domain line.
                                Hmmer2HmmPfamDomainMatch domainMatch = new Hmmer2HmmPfamDomainMatch(line);
                                searchRecord.addDomainMatch(domainMatch);
                            }
                    }
                }
            }

            // Just in case the last record does not end with //
            if (searchRecord != null) {
                hmmer2ParserSupport.addMatch(searchRecord, rawResults);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new HashSet<RawProtein<T>>(rawResults.values());
    }
}
