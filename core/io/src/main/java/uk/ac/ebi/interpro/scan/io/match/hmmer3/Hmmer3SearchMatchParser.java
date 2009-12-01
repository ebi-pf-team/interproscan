package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.HmmsearchOutputMethod;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.parsemodel.DomainMatch;

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
 *
Query:       7tm_2  [M=242]
Accession:   PF00002.17
Description: 7 transmembrane receptor (Secretin family)
Scores for complete sequences (score includes all domains):
   --- full sequence ---   --- best 1 domain ---    -#dom-
    E-value  score  bias    E-value  score  bias    exp  N  Sequence      Description
    ------- ------ -----    ------- ------ -----   ---- --  --------      -----------
      8e-42  152.1   7.7    1.1e-41  151.6   5.3    1.2  1  UPI00000015B6


Domain and alignment annotation for each sequence:
>> UPI00000015B6
   #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
 ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
   1 !  151.6   5.3   1.3e-48   1.1e-41       4     242 .]    2376    2605 ..    2373    2605 .. 0.94

  Alignments for each domain:
  == domain 1    score: 151.6 bits;  conditional E-value: 1.3e-48
                     ----S----------------------------------------------------------------------------------------- CS
          7tm_2    4 lkvittvGlslSlvaLlvaivilllfrklrctrntihlnLflslilrailvlvkiaalenkeeeseakCkvvavflhYfllanffWllvEglyl 97
                     lk++t+v+l++ l+aLl++++ l+l+r lr++++ i  nL ++l l+++++l++i++ +   +     C+v+a++lh+++l++f W l+E+l+l
  UPI00000015B6 2376 LKTLTYVALGVTLAALLLTFFFLTLLRILRSNQHGIRRNLTAALGLAQLVFLLGINQADLPFA-----CTVIAILLHFLYLCTFSWALLEALHL 2464
                     799*****************************************************9999997.....************************** PP

                     -------------------------------------.--------.----------------------------------------------- CS
          7tm_2   98 ylllvltffserkklkvylliGwgvPavvvvvwaivrkagyenekc.WlsnekkllwiikgpvlviilvNfvllinilrvlvqklrsketseke 190
                     y++l++++  ++  +++y+++GwgvPa ++ ++++++++gy+n ++ Wls  ++l+w+++gpv++++ + ++l i   r  ++  r+  ++ k
  UPI00000015B6 2465 YRALTEVRDVNTGPMRFYYMLGWGVPAFITGLAVGLDPEGYGNPDFcWLSIYDTLIWSFAGPVAFAVSMSVFLYILAARASCAAQRQGFEK-KG 2557
                     *************************************99****8877****99*****************************998844444.44 PP

                     ---------------------------------------------------- CS
          7tm_2  191 kkrkklvkstlvllpLLGityvlflfapeekvssvvflyleailnslqGffv 242
                       +   ++ ++++l+LL+ t++l+l+ ++++  + +f+yl+a+ n++qG f+
  UPI00000015B6 2558 PVS--GLQPSFAVLLLLSATWLLALLSVNSD--TLLFHYLFATCNCIQGPFI 2605
                     444..58999********************5..8***************886 PP



Internal pipeline statistics summary:
-------------------------------------
Query model(s):                            1  (242 nodes)
Target sequences:                          1  (2923 residues)
Passed MSV filter:                         1  (1); expected 0.0 (0.02)
Passed bias filter:                        1  (1); expected 0.0 (0.02)


 */
public class Hmmer3SearchMatchParser implements MatchParser {

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
     * and domain matches and converts it to RawProtein
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

    public Set<RawProtein> parse(InputStream is) throws IOException, ParseException {

        Map<String, RawProtein> rawResults = new HashMap<String, RawProtein>();
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
                            if (hmmer3ParserSupport.parseAlignments())  {
                                System.out.println("Parsing alignments...");
                            }
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
        return new HashSet<RawProtein> (rawResults.values());
    }
}
