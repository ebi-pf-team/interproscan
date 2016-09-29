package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.ModelMatchTMP;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for HMMER3 output, based upon the working parser used in Onion.
 * This has been designed to work as fast as possible, parsing a file
 * most of which is ignored.
 *
 * @author Phil Jones
 * @author David Binns
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0-SNAPSHOT
 * <p>
 *
 *     Query:       UPI00043D6473  [L=1179]
Scores for complete sequence (score includes all domains):
--- full sequence ---   --- best 1 domain ---    -#dom-
E-value  score  bias    E-value  score  bias    exp  N  Model      Description
------- ------ -----    ------- ------ -----   ---- --  --------   -----------
7.1e-163  538.7   3.5    1.3e-80  269.8   0.3    2.2  2  PF01434.15  Peptidase family M41
1.7e-78  261.3   0.1      4e-43  146.9   0.0    4.0  2  PF00004.26  ATPase family associated with various cellular ac
7.8e-11   42.0   0.9    0.00038   20.3   0.0    4.6  0  PF07728.11  AAA domain (dynein-related subfamily)
7.6e-09   36.0   4.4      0.005   17.1   0.2    4.1  0  PF13191.3   AAA ATPase domain
1.2e-08   35.2   0.0      0.016   15.4   0.0    3.3  0  PF06480.12  FtsH Extracellular
1.6e-07   31.1   3.2      0.027   14.1   0.0    4.3  0  PF01695.14  IstB-like ATP binding protein
5e-07   29.9   1.3       0.11   12.7   0.1    3.8  0  PF13401.3   AAA domain
2.5e-06   27.6   0.1       0.14   12.2   0.0    3.1  0  PF13671.3   AAA domain
3.9e-06   26.3   1.3       0.04   13.2   0.0    2.4  0  PF01078.18  Magnesium chelatase, subunit ChlI
6.5e-06   26.2   0.0      0.021   14.8   0.0    3.8  0  PF07724.11  AAA domain (Cdc48 subfamily)
5.3e-05   22.5   0.1      0.034   13.3   0.0    2.7  0  PF05496.9   Holliday junction DNA helicase ruvB N-terminus


Domain annotation for each model (and alignments):
>> PF01434.15  Peptidase family M41
#    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
1 !  269.8   0.3   9.1e-84   1.3e-80       2     211 .]     461     674 ..     460     674 .. 0.98
2 !  269.8   0.3   9.1e-84   1.3e-80       2     211 .]     955    1168 ..     954    1168 .. 0.98

Alignments for each domain:
== domain 1  score: 269.8 bits;  conditional E-value: 9.1e-84
HHHHHHHHHHHCHHHH-XXXXXHHHHHHHHHHHHHHHHHHCCSSSXSXBXXEESXXTTSXXXXXXXXXXTSSX.B...HHHHHHHHHHHHHHHHHH CS
PF01434.15   2 mkeleeavdrvlaGlekksrvisekekklvAyHEaGHalvglllkeadpveKvtiiPRgqalGltlslPeedkls...ltkeellarlavllGGra 94
++e+++a++r++aG+ekk++v+se++k+lvAyHEaGHalvg+l++e dpv+K++iiPRgqa+Glt++ P+e++l+   +++++l +++av+lGGr+
UPI00043D6473 461 KDEISDALERIIAGPEKKNAVVSEEKKRLVAYHEAGHALVGALMPEYDPVAKISIIPRGQAGGLTFFAPSEERLEsglYSRSYLENQMAVALGGRV 556
5799********************************************************************998888****************** PP

HHTTSXB..GHHHHHHHHHHHHHHHHTSXXXTTTXSXXXXXXXXXX.XXXXXXXSXXHHHHHHHHHHHHHHHHHHHHHHHHHHHHTHHHHHHHHHH CS
PF01434.15  95 aEelifg..evttGasnDlekatkiarkmvtefGmsdklGpvsleeeeeeevflgkelkkekelseetaeeideevkelveeayerakeileekre 188
aEe+ifg  +vttGasnD+++++++ar+mv++fG+s+k+G++++    + ++flg++++++k++s +ta+ +d+ev+elve+ay+ra++i++++ +
UPI00043D6473 557 AEEVIFGqeNVTTGASNDFMQVSRVARQMVERFGFSKKIGQIAIGGPGG-NPFLGQQMSSQKDYSMATADVVDAEVRELVEKAYSRATQIITTHID 651
 *******999************************************998.79******************************************** PP

HHHHSEEXHHHHHHHXXXXXXXX CS
PF01434.15 189 elealaeaLlekEtldaeeieel 211
l++la+ L+ekEt+d+ee+++l
UPI00043D6473 652 ILHKLAQLLMEKETVDGEEFMSL 674
 *******************9875 PP

== domain 2  score: 269.8 bits;  conditional E-value: 9.1e-84
HHHHHHHHHHHCHHHH-XXXXXHHHHHHHHHHHHHHHHHHCCSSSXSXBXXEESXXTTSXXXXXXXXXXTSSX.B...HHHHHHHHHHHHHHHH CS
PF01434.15    2 mkeleeavdrvlaGlekksrvisekekklvAyHEaGHalvglllkeadpveKvtiiPRgqalGltlslPeedkls...ltkeellarlavllGG 92
++e+++a++r++aG+ekk++v+se++k+lvAyHEaGHalvg+l++e dpv+K++iiPRgqa+Glt++ P+e++l+   +++++l +++av+lGG
UPI00043D6473  955 KDEISDALERIIAGPEKKNAVVSEEKKRLVAYHEAGHALVGALMPEYDPVAKISIIPRGQAGGLTFFAPSEERLEsglYSRSYLENQMAVALGG 1048
5799********************************************************************998888**************** PP

HHHHTTSXB..GHHHHHHHHHHHHHHHHTSXXXTTTXSXXXXXXXXXX.XXXXXXXSXXHHHHHHHHHHHHHHHHHHHHHHHHHHHHTHHHHHH CS
PF01434.15   93 raaEelifg..evttGasnDlekatkiarkmvtefGmsdklGpvsleeeeeeevflgkelkkekelseetaeeideevkelveeayerakeile 184
r+aEe+ifg  +vttGasnD+++++++ar+mv++fG+s+k+G++++    + ++flg++++++k++s +ta+ +d+ev+elve+ay+ra++i++
UPI00043D6473 1049 RVAEEVIFGqeNVTTGASNDFMQVSRVARQMVERFGFSKKIGQIAIGGPGG-NPFLGQQMSSQKDYSMATADVVDAEVRELVEKAYSRATQIIT 1141
 *********999************************************998.79**************************************** PP

HHHHHHHHSEEXHHHHHHHXXXXXXXX CS
PF01434.15  185 ekreelealaeaLlekEtldaeeieel 211
++ + l++la+ L+ekEt+d+ee+++l
UPI00043D6473 1142 THIDILHKLAQLLMEKETVDGEEFMSL 1168
 ***********************9875 PP

>> PF00004.26  ATPase family associated with various cellular activities (AAA)
#    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
1 !  146.9   0.0   2.7e-46     4e-43       2     130 ..     268     399 ..     267     400 .. 0.97
2 !   92.0   0.0   2.5e-29   3.8e-26      38     130 ..     798     893 ..     795     894 .. 0.95

Alignments for each domain:
== domain 1  score: 146.9 bits;  conditional E-value: 2.7e-46
EEESSTTSSHHHHHHHHHHHSHTHCHHHHHHHHHHHEEEEEEECSCGEEHSTCSEETSEEEEEESXXXXXXX..............XXXXXXXXXX CS
PF00004.26   2 llyGppGtGKTllakavakelgvefleisgsellskyvgesekkirelfkeakekapsilfiDEidalaksr...sgseseeeervvnqLlteldg 94
ll+GppGtGKTlla+ava e+gv+f++  +se+++ +vg +++++r+lf++ak+kap+i+fiDEida++++r    g+ ++e+e+++nqLlte+dg
UPI00043D6473 268 LLVGPPGTGKTLLARAVAGEAGVPFFSCAASEFVELFVGVGASRVRDLFEKAKAKAPCIVFIDEIDAVGRQRgagLGGGNDEREQTINQLLTEMDG 363
9***********************************************************************9998899***************** PP

XXXXXXXXXXXXXXXXXXXXXXXXX..XXXXXXXXXX CS
PF00004.26  95 vkkkeskvivigatnrpdkldpallr.gRfdrkieie 130
+  + s viv++atnrpd ld allr gRfdr++ ++
UPI00043D6473 364 FAGN-SGVIVLAATNRPDVLDAALLRpGRFDRQVTVD 399
 ***7.55*************************99886 PP

== domain 2  score: 92.0 bits;  conditional E-value: 2.5e-29
EEEEEEECSCGEEHSTCSEETSEEEEEESXXXXXXX..............XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX..XXXXXXXXX CS
PF00004.26  38 yvgesekkirelfkeakekapsilfiDEidalaksr...sgseseeeervvnqLlteldgvkkkeskvivigatnrpdkldpallr.gRfdrkiei 129
+vg +++++r+lf++ak+kap+i+fiDEida++++r    g+ ++e+e+++nqLlte+dg+  + s viv++atnrpd ld allr gRfdr++ +
UPI00043D6473 798 FVGVGASRVRDLFEKAKAKAPCIVFIDEIDAVGRQRgagLGGGNDEREQTINQLLTEMDGFAGN-SGVIVLAATNRPDVLDAALLRpGRFDRQVTV 892
699*********************************9998899********************7.55*************************9988 PP

X CS
PF00004.26 130 e 130
+
UPI00043D6473 893 D 893
6 PP

>> PF07728.11  AAA domain (dynein-related subfamily)
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF13191.3  AAA ATPase domain
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF06480.12  FtsH Extracellular
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF01695.14  IstB-like ATP binding protein
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF13401.3  AAA domain
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF13671.3  AAA domain
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF01078.18  Magnesium chelatase, subunit ChlI
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF07724.11  AAA domain (Cdc48 subfamily)
[No individual domains that satisfy reporting thresholds (although complete target did)]

>> PF05496.9  Holliday junction DNA helicase ruvB N-terminus
[No individual domains that satisfy reporting thresholds (although complete target did)]



Internal pipeline statistics summary:
-------------------------------------
Query sequence(s):                         1  (1179 residues searched)
Target model(s):                       16295  (2857995 nodes)
Passed MSV filter:                       395  (0.0242406); expected 325.9 (0.02)
Passed bias filter:                      346  (0.0212335); expected 325.9 (0.02)
Passed Vit filter:                        77  (0.00472538); expected 16.3 (0.001)
Passed Fwd filter:                        38  (0.002332); expected 0.2 (1e-05)
Initial search space (Z):              16295  [as set by --Z on cmdline]
Domain search space  (domZ):              11  [number of targets reported over threshold]

 *
 */
public class Hmmer3ScanMatchParser<T extends RawMatch> implements MatchParser {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(Hmmer3ScanMatchParser.class.getName());

    private static final String END_OF_RECORD = "//";

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */
//    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "inclusion threshold";

    private  static final String DOMAIN_SECTION_START_TEXT = "Domain annotation for each model (and alignments)";

    private static final String DOMAIN_SECTION_START = ">> ";

    private static final String DOMAIN_ALIGNMENT_SECTION_START = "  ==";

    private static final String START_OF_DOMAIN_ALIGNMENT_SECTION = "Alignments for each domain";

    private static final String END_OF_OUTPUT_FILE = "[ok]";

    //Output File to write Gene3D parser output in ssf format suitable for Domain Finder input
    //File ssfFile = new File("C:\\Manjula\\input_for_DF.txt");

    // Group 1: Uniparc protein accession
    private static final Pattern DOMAIN_SECTION_START_PATTERN = Pattern.compile("^>>\\s+(\\S+).*$");

    /**
     * This interface has a single method that
     * takes the HmmsearchOutputMethod object, containing sequence
     * and domain matches and converts it to RawProtein
     * objects.  The converter MAY perform additional steps, such as
     * filtering the raw results by specific criteria, such as GA value
     * cutoff.
     */
    private Hmmer3ParserSupport<T> hmmer3ParserSupport;

    private final SignatureLibrary signatureLibrary;
    private final String signatureLibraryRelease;

    private Hmmer3ScanMatchParser() {
        signatureLibrary = null;
        signatureLibraryRelease = null;
    }

    public Hmmer3ScanMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        this.signatureLibrary = signatureLibrary;
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setParserSupport(Hmmer3ParserSupport<T> hmmer3ParserSupport) {
        this.hmmer3ParserSupport = hmmer3ParserSupport;
    }

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage {
        LOOKING_FOR_SEQUENCE_IDENTIFIER,
        LOOKING_FOR_METHOD_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAIN_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        PARSING_DOMAIN_ALIGNMENTS,
        FINISHED_SEARCHING_RECORD
    }

    public Set<RawProtein<T>> parse(InputStream is) throws IOException {

        Map<String, RawProtein<T>> rawResults = new HashMap<String, RawProtein<T>>();
        BufferedReader reader = null;
        int rawDomainCount = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            Map<String, HmmSearchRecord> searchRecords = new HashMap();
            String currentSequenceIdentifier = null;
            String currentModelIdentifier = null;
            Map<String, SequenceMatch> sequenceMatchMap = new HashMap();
            Map<String, DomainMatch> domains = new HashMap<String, DomainMatch>();
            StringBuilder alignSeq = new StringBuilder();
            DomainMatch currentDomain = null;
            //generate ssf file for Domain Finder
            //DomainFinderInputWriter dfiw = new DomainFinderInputWriter();
            ParsingStage stage = ParsingStage.LOOKING_FOR_SEQUENCE_IDENTIFIER;
            //Matcher domainAlignSequenceMatcher = null;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
//                Utilities.verboseLog(line);
                // New code block to handle matches where there is a sequence match with
                // no corresponding domain matches.
                if (stage == ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE && line.startsWith(DOMAIN_SECTION_START)) {
                    stage = ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                }
                if (line.startsWith(END_OF_RECORD)) {
                    // Process a complete record - store all sequence / domain scores
                    // for the method.
                    //if there are no matches then just exit
                    //TODO fefactor this section of code
                    if (searchRecords.isEmpty()) {
                        String nextLine = null;
                        boolean domainParsingError = true;
                        if ((nextLine = reader.readLine()) != null) {
                            if (nextLine.trim().equals(END_OF_OUTPUT_FILE)) {
                                //likely there were no domain hits
                                Utilities.verboseLog("likely there were no domain hits");
                                Utilities.verboseLog("rawDomainCount: " + rawDomainCount);

                            }
                            domainParsingError = false;
                        }
                        if (domainParsingError) {
                            LOGGER.warn("Parsing error- line:" + line + " next line: " + nextLine);
                            throw new ParseException("Got to the end of a hmmscan/hmmsearch full output file section without finding any details of a method.", null, line, lineNumber);
                        }
                    }
                    currentSequenceIdentifier = null;
                    stage = ParsingStage.LOOKING_FOR_SEQUENCE_IDENTIFIER;

                } else {   // Trying to be efficient - only look for EXPECTED lines in the entry.
                    switch (stage) {
                        case LOOKING_FOR_SEQUENCE_IDENTIFIER:
                            Matcher sequenceIdLineMatcher = ModelMatchTMP.SEQUENCE_IDENTIFIER_lINE_PATTERN.matcher(line);
                            if (sequenceIdLineMatcher.matches()) {
                                stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                                currentSequenceIdentifier = sequenceIdLineMatcher.group(1);
                                Utilities.verboseLog("currentSequenceIdentifier:  " + currentSequenceIdentifier);
                                if (currentSequenceIdentifier == null){
                                    throw new ParseException("Found a line starting with " +ModelMatchTMP.SEQUENCE_IDENTIFIER_lINE_PATTERN.toString() + " but cannot parse it with the SEQUENCE_ID_LINE regex.", null, line, lineNumber);
                                }
                            }
                            break;

                        case LOOKING_FOR_SEQUENCE_MATCHES:
                            // Got to the end of the sequence matches that pass?
                            // Due to hmmer3 beta version peculiarities we cannot currently use
                            // the inclusion threshold marking to find the end of the matches, se we take them all
                            // Original code left in comments in case we can ever put it back
                            if (/*line.contains(END_OF_GOOD_SEQUENCE_MATCHES) || */ line.trim().length() == 0) {
                                // If there are no good sequence matches, completely stop searching this record.
                                stage = (sequenceMatchMap.get(currentSequenceIdentifier) == null)
                                        ? ParsingStage.FINISHED_SEARCHING_RECORD
                                        : ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
//                                currentSequenceIdentifier = null;
//                                currentDomain = null;

                            } else {
                                Matcher sequenceMatchLineMatcher = ModelMatchTMP.SEQUENCE_LINE_PATTERN.matcher(line);
                                if (sequenceMatchLineMatcher.matches()) {
                                    // Found a sequence match line above the threshold.
                                    // Make a record of the UPI.
//                                    String upi = sequenceMatchLineMatcher.group(SequenceMatch.SEQUENCE_ID_GROUP);
                                    ModelMatchTMP hmmscanSequenceMatch = new ModelMatchTMP(sequenceMatchLineMatcher);
                                    SequenceMatch sequenceMatch = new SequenceMatch(currentSequenceIdentifier, hmmscanSequenceMatch.getEValue(), hmmscanSequenceMatch.getScore(), hmmscanSequenceMatch.getBias());
                                    sequenceMatchMap.put(currentSequenceIdentifier, sequenceMatch);
                                    if (! searchRecords.containsKey(hmmscanSequenceMatch.getModelIdentifier())){
                                        HmmSearchRecord searchRecord = new HmmSearchRecord(hmmscanSequenceMatch.getModelIdentifier());
                                        searchRecords.put(hmmscanSequenceMatch.getModelIdentifier(), searchRecord);
                                    }
                                    Utilities.verboseLog("sequenceMatch: " + sequenceMatch);
                                    searchRecords.get(hmmscanSequenceMatch.getModelIdentifier()).addSequenceMatch(sequenceMatch);
                                    //searchRecord.addSequenceMatch(sequenceMatch);
                                }
                            }
                            break;

                        case LOOKING_FOR_METHOD_ACCESSION:
                            //there is no modell accession line as is in the hmmsearch output, so may not need this part
                            //if (hmmer3ParserSupport.getHmmKey()== Hmmer3ParserSupport.HmmKey.ACCESSION)
                            //if (line.startsWith(MODEL_ACCESSION_LINE)){
                                stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;

                            break;

                        case LOOKING_FOR_DOMAIN_SECTION:

                            // Example: >> PF01434
                            if (line.startsWith(DOMAIN_SECTION_START)) {

                                // Find out which model the domain matches are for and then parse them.
                                Matcher domainSectionHeaderMatcher = DOMAIN_SECTION_START_PATTERN.matcher(line);
                                if (domainSectionHeaderMatcher.matches()) {
                                    domains.clear();
                                    currentModelIdentifier = domainSectionHeaderMatcher.group(1);
                                } else {
                                    throw new ParseException("This line looks like a domain section header line, but it is not possible to parse out the sequence id.", null, line, lineNumber);
                                }
                                stage = ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE;
                            }

                            if (hmmer3ParserSupport.parseAlignments()) {
                                //to handle domain alignment
                                if (line.startsWith(DOMAIN_ALIGNMENT_SECTION_START)) {

                                    Matcher domainAlignmentMatcher = DomainMatch.DOMAIN_ALIGNMENT_LINE_PATTERN.matcher(line);
                                    if (domainAlignmentMatcher.matches()) {

                                        alignSeq.setLength(0);
                                        String domainNumber = domainAlignmentMatcher.group(1);
                                        currentDomain = domains.get(domainNumber); //get the current domain object.
                                    } else {
                                        throw new ParseException("Unable to parse domain alignment section line", null, line, lineNumber);
                                    }
                                }
                                // getting the actual alignment sequence string

                                if ((currentDomain != null) && (currentSequenceIdentifier != null)) {
                                    Matcher alignmentSequencePattern = DomainMatch.ALIGNMENT_SEQUENCE_PATTERN.matcher(line);
                                    if (alignmentSequencePattern.matches()) {
                                        if (alignmentSequencePattern.group(1).equals(currentSequenceIdentifier)) {
                                            alignSeq.append(alignmentSequencePattern.group(3));
                                            currentDomain.setAlignment(alignSeq.toString());
                                        }
                                    }
                                }
                            }

                            break;

                        /* Example:
                         #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
                       ---   ------ ----- --------- --------- ------- -------    ------- -------    ------- -------    ----
                         1 !   21.5   0.0   6.5e-09   0.00065      12      64 ..     564     615 ..     423     621 .. 0.70
                         2 !   21.0   0.0   8.8e-09   0.00088      12      64 ..     564     615 ..     553     645 .. 0.88
                        */
                        case LOOKING_FOR_DOMAIN_DATA_LINE:

                            // Look for a domain data line.
                            Matcher domainDataLineMatcher = DomainMatch.DOMAIN_LINE_PATTERN.matcher(line);
                            if (line.contains(START_OF_DOMAIN_ALIGNMENT_SECTION)) {
                                stage = ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                            } else if (domainDataLineMatcher.matches()) {
                                DomainMatch domainMatch = new DomainMatch(domainDataLineMatcher);
                                String domainLineId = domainDataLineMatcher.group(1);
                                searchRecords.get(currentModelIdentifier).addDomainMatch(currentSequenceIdentifier, domainMatch);

                            }
                            break;
                        case FINISHED_SEARCHING_RECORD:
                            Utilities.verboseLog("FINISHED_SEARCHING_RECORD");
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_IDENTIFIER;
                            break;

                    }
                }
            }
            if (! searchRecords.isEmpty()) {
                for (HmmSearchRecord hmmSearchRecord: searchRecords.values()) {
                    hmmer3ParserSupport.addMatch(hmmSearchRecord, rawResults);
                    rawDomainCount += getSequenceMatchCount(hmmSearchRecord);
                    Utilities.verboseLog("hmmSearchRecord: " + hmmSearchRecord.toString());
                }

            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        //TODO consider using the utilities methods
        Utilities.verboseLog(10, " RawResults.size : " + rawResults.size() + " domainCount: " + rawDomainCount);
//       LOGGER.debug(getTimeNow() + " RawResults.size : " + rawResults.size() + " domainCount: " +  rawDomainCount);
        return new HashSet<RawProtein<T>>(rawResults.values());
    }

    /**
     * display time now
     *
     * @return
     */
    public static String getTimeNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        String currentDate = sdf.format(cal.getTime());
        return currentDate;
    }

    public int getSequenceMatchCount(HmmSearchRecord searchRecord) {
        int count = 0;
        for (SequenceMatch sequenceMatch : searchRecord.getSequenceMatches().values()) {
            count += sequenceMatch.getDomainMatches().size();
        }
        return count;
    }
}
