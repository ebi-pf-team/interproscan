package uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3;

import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import uk.ac.ebi.interpro.scan.parser.ParseException;
import uk.ac.ebi.interpro.scan.parser.matchparser.Parser;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.parser.matchparser.hmmer3.parsemodel.DomainMatch;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.springframework.beans.factory.annotation.Required;

/**
 * Parser for HMMER3 output, based upon the working parser used in Onion.
 * This has been designed to work as fast as possible, parsing a file
 * most of which is ignored.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer3SearchParser implements Parser {

    private static final String END_OF_RECORD = "//";

    private static final String MODEL_ACCESSION_LINE = "Accession:";

    /**
     * Group 1: Model accession.
     */
    private static final Pattern MODEL_ACCESSION_LINE_PATTERN = Pattern.compile ("^Accession:\\s+([0-9A-Z]+)\\.?\\d*$" );

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */
    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "inclusion threshold";

    private static final String DOMAIN_SECTION_START = ">> ";

    private static final String START_OF_DOMAIN_ALIGNMENT_SECTION = "Alignments for each domain";

    // Group 1: Uniparc protein accession
    private static final Pattern DOMAIN_SECTION_START_PATTERN = Pattern.compile("^>>\\s+(\\S+).+$");

    /**
     * This interface has a single method that
     * takes the HmmsearchOutputMethod object, containing sequence
     * and domain matches and converts it to RawSequenceIdentifier
     * objects.  The converter MAY perform additional steps, such as
     * filtering the raw results by specific criteria, such as GA value
     * cutoff.
     */
    private Hmmer3ParserSupport hmmer3ParserSupport;

    @Required
    public void setParserSupport(Hmmer3ParserSupport hmmer3ParserSupport) {
        this.hmmer3ParserSupport = hmmer3ParserSupport;
    }

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage{
        LOOKING_FOR_METHOD_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAIN_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        PARSING_DOMAIN_ALIGNMENTS,
        FINISHED_SEARCHING_RECORD
    }

    public Set<RawSequenceIdentifier> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawSequenceIdentifier> rawResults = new HashMap<String, RawSequenceIdentifier>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            HmmsearchOutputMethod method = null;
            String domainSectionCurrentUPI = null;
            ParsingStage stage = ParsingStage.LOOKING_FOR_METHOD_ACCESSION;
            int lineNumber = 0;
            while (reader.ready()){
                lineNumber++;
                String line = reader.readLine();
                if (line.startsWith(END_OF_RECORD)){
                    // Process a complete record - store all sequence / domain scores
                    // for the method.
                    if (method == null){
                        throw new ParseException("Got to the end of a hmmscan full output file section without finding any details of a method.", null, line, lineNumber);
                    }
                    // Store the matches to the method.
                    hmmer3ParserSupport.addMatch (method, rawResults);
                    method = null;  // Will check if method is not null after finishing the file, and store it if so.
                    stage = ParsingStage.LOOKING_FOR_METHOD_ACCESSION;
                }
                else {   // Trying to be efficient - only look for EXPECTED lines in the entry.
                    switch (stage){
                        case LOOKING_FOR_METHOD_ACCESSION:
                            if (line.startsWith(MODEL_ACCESSION_LINE)){
                                stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                                Matcher queryLineMatcher = MODEL_ACCESSION_LINE_PATTERN.matcher(line);
                                if (queryLineMatcher.matches()){
                                    method = new HmmsearchOutputMethod(queryLineMatcher.group(1));
                                }
                                else {
                                    throw new ParseException("Found a line starting with " + MODEL_ACCESSION_LINE + " but cannot parse it with the MODEL_ACCESSION_LINE regex.",null, line, lineNumber);
                                }
                            }
                            break;

                        case LOOKING_FOR_SEQUENCE_MATCHES:
                            // Sanity check.
                            if (method == null){
                                throw new ParseException("The parse stage is 'looking for sequence matches' however there is no method in memory.", null, line, lineNumber);
                            }
                            // Got to the end of the sequence matches that pass?
                            // Due to hmmer3 beta version peculiarities we cannot currently use
                            // the inclusion threshold marking to find the end of the matches, se we take them all
                            // Original code left in comments in case we can ever put it back
                            if (/*line.contains(END_OF_GOOD_SEQUENCE_MATCHES) || */ line.trim().length() == 0){
                                // If there are no good sequence matches, completely stop searching this record.
                                stage = (method.getSequenceMatches().size() == 0)
                                        ? ParsingStage.FINISHED_SEARCHING_RECORD
                                        : ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                            }
                            else {
                                Matcher sequenceMatchLineMatcher = SequenceMatch.SEQUENCE_LINE_PATTERN.matcher(line);
                                if (sequenceMatchLineMatcher.matches()){
                                    // Found a sequence match line above the threshold.
                                    // Make a record of the UPI.
                                    String upi = sequenceMatchLineMatcher.group(SequenceMatch.UPI_GROUP);
                                    SequenceMatch sequenceMatch = new SequenceMatch(sequenceMatchLineMatcher);
                                    method.addSequenceMatch(sequenceMatch);
                                }
                            }
                            break;

                        case LOOKING_FOR_DOMAIN_SECTION:

                            if (line.startsWith(DOMAIN_SECTION_START)){
                                // Find out which model the domain matches are for and then parse them.
                                Matcher domainSectionHeaderMatcher = DOMAIN_SECTION_START_PATTERN.matcher(line);
                                if (domainSectionHeaderMatcher.matches()){
                                    domainSectionCurrentUPI  = domainSectionHeaderMatcher.group(1);
                                }
                                else {
                                    throw new ParseException("This line looks like a domain section header line, but I cannot parse out the methodAccession id.", null, line, lineNumber);
                                }
                                stage = ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE;
                            }

                            break;

                        case LOOKING_FOR_DOMAIN_DATA_LINE:

                            // Look for a domain data line.
                            Matcher domainDataLineMatcher = DomainMatch.DOMAIN_LINE_PATTERN.matcher(line);
                            if (line.contains(START_OF_DOMAIN_ALIGNMENT_SECTION)){
                                stage = (hmmer3ParserSupport.parseAlignments())
                                        ? ParsingStage.PARSING_DOMAIN_ALIGNMENTS
                                        : ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                            }
                            else if (domainDataLineMatcher.matches()){
                                DomainMatch domainMatch = new DomainMatch(domainDataLineMatcher);
                                method.addDomainMatch(domainSectionCurrentUPI, domainMatch);
                            }
                            break;

                        case PARSING_DOMAIN_ALIGNMENTS:
                            // TODO Code to parse alignments should go here...
                            // TODO Note - an additional field (for the alignment) needs to be added
                            // TODO to the parsing model.





                            // TODO End of alignment parsing code.
                            // .. followed by... (just switching directly to this at the moment)
                            stage = ParsingStage.LOOKING_FOR_DOMAIN_SECTION;

                            break;
                    }
                }
            }
            if (method != null){
                hmmer3ParserSupport.addMatch (method, rawResults);
            }

        } finally {
            if (reader != null){
                reader.close();
            }
        }
        return new HashSet<RawSequenceIdentifier> (rawResults.values());
    }
}
