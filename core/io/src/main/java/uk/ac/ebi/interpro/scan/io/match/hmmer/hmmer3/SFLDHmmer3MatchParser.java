package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.io.match.MatchAndSiteParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceDomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for SFLD HMMER3 output
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 5.20-SNAPSHOT
 * <p>
 * #Sequence:  e-value:  score:  bias:
 * #Domains:
 * #model_accession  hmmstart hmmend  [hhmbounds] domain_score envelope_start envelope_end ali_start ali_end domain_ce_value domain_ie_value  expected_accurracy domain_bias
 * #Sites:
 * #residue position_start position_end [description]
 * <p>
 * Sequence: UPI00027F6947 e-value: 2.1E-56 score: 189.1 bias: 5.2
 * Domains:
 * SFLDS00014 9 64 .. 22.6 171 234 176 232 0.88 3.7E-9 4.4E-5 0.0
 * SFLDS00024 1 133 [] 188.1 481 613 481 613 0.99 7.5E-60 4.5E-56 0.0
 * Sites:
 * SFLDS00014 C38,C63
 * SFLDS00024 C24, C56, K44
 * Sequence: UPI00027F6942 e-value: 5.9E-30 score: 189.1 bias: 5.2
 * Domains:
 * SFLDS00454 1 133 [] 188.1 481 613 481 613 0.99 7.5E-60 4.5E-56 0.0
 * Sites:
 * SFLDS00454 C112
 */
public class SFLDHmmer3MatchParser<T extends RawMatch> implements MatchAndSiteParser {

    private static final Logger LOGGER = Logger.getLogger(SFLDHmmer3MatchParser.class.getName());

    private static final String END_OF_RECORD = "//";

    /**
     * DON'T GET RID OF THIS!  If HMMER3 is working properly, this is used to
     * correctly parse the file.  At the moment, beta 3 contains a bug, so the inclusion
     * threshold line is useless.  The code below has a line commented out which can
     * easily be put back to use the inclusion threshold.
     */
//    private static final String END_OF_GOOD_SEQUENCE_MATCHES = "inclusion threshold";

    private static final String SEQUENCE_SECTION_START = "Sequence:";

    private static final String DOMAIN_SECTION_START = "Domains:";

    private static final String SITE_SECTION_START = "Sites:";

    //     Group 1: Uniparc protein accession
//    private static final Pattern SEQUENCE_SECTION_START_PATTERN = Pattern.compile("^Sequence:\\s+(\\S+)\\s+e-value:\\s+(\\S+)\\s+score:\\s+(\\S+)\\s+bias:\\s+(\\S+).*$");
    private static final Pattern SEQUENCE_SECTION_START_PATTERN = Pattern.compile("^Sequence:\\s+(\\S+).*$");

//    private static final Pattern DOMAIN_SECTION_START_PATTERN = Pattern.compile("^Domains:\\s+(\\S+).*$");
//    private static final Pattern SITE_SECTION_START_PATTERN = Pattern.compile("^Sites:\\s+(\\S+).*$");

    private static final Pattern SITES_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)$");
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

    private SFLDHmmer3MatchParser() {
        signatureLibrary = null;
        signatureLibraryRelease = null;
    }

    public SFLDHmmer3MatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
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
        LOOKING_FOR_METHOD_ACCESSION,
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAIN_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        LOOKING_FOR_SITE_SECTION,
        LOOKING_FOR_SITE_DATA_LINE,
        PARSING_DOMAIN_ALIGNMENTS,
        FINISHED_SEARCHING_RECORD
    }

    public MatchSiteData parseMatchesAndSites(InputStream is) throws IOException {

        Map<String, RawProtein<SFLDHmmer3RawMatch>> rawProteinMap = new HashMap<>();
        MatchData matchData = parseFileInput(is);
        Set<SFLDHmmer3RawMatch> rawMatches = matchData.getMatches();

        for (SFLDHmmer3RawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (rawProteinMap.containsKey(sequenceId)) {
                RawProtein<SFLDHmmer3RawMatch> rawProtein = rawProteinMap.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<SFLDHmmer3RawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                rawProteinMap.put(sequenceId, rawProtein);
            }
        }

        Map<String, RawProteinSite<SFLDHmmer3RawSite>> rawProteinSiteMap = new HashMap<>();
        Set<SFLDHmmer3RawSite> rawSites = matchData.getSites();
        for (SFLDHmmer3RawSite rawSite : rawSites) {
            String sequenceId = rawSite.getSequenceIdentifier();
            if (rawProteinSiteMap.containsKey(sequenceId)) {
                RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = rawProteinSiteMap.get(sequenceId);
                rawProteinSite.addSite(rawSite);
            } else {
                RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = new RawProteinSite<>(sequenceId);
                rawProteinSite.addSite(rawSite);
                rawProteinSiteMap.put(sequenceId, rawProteinSite);
            }
        }
        Utilities.verboseLog("Parsed sites count: " + rawProteinSiteMap.values().size());
        return new MatchSiteData<>(new HashSet<>(rawProteinMap.values()), new HashSet<>(rawProteinSiteMap.values()));
    }

    public MatchData parseFileInput(InputStream is) throws IOException {
        Map<String, RawProtein<T>> rawResults = new HashMap<>();
        Set<SFLDHmmer3RawMatch> rawMatches = new HashSet<>();
        Set<SFLDHmmer3RawSite> rawSites = new HashSet<>();

        int rawDomainCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            HmmSearchRecord searchRecord;
            String currentSequenceIdentifier = null;

            Map<String, DomainMatch> domains = new HashMap<>();

            SequenceMatch sequenceMatch;

            ParsingStage stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Example: Sequence: UPI0000054B90
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("line: " + line + "  stage: " + stage.toString());
                }
                switch (stage) {
                    case LOOKING_FOR_SEQUENCE_MATCHES:
                        if (line.startsWith(SEQUENCE_SECTION_START)) {
                            Matcher sequenceSectionHeaderMatcher = SEQUENCE_SECTION_START_PATTERN.matcher(line);
                            if (sequenceSectionHeaderMatcher.matches()) {
                                domains.clear();
                                currentSequenceIdentifier = sequenceSectionHeaderMatcher.group(1);
                                LOGGER.debug("currentSequenceIdentifier = " + currentSequenceIdentifier);
                            } else {
                                throw new ParseException("This line looks like a domain section header line, but it is not possible to parse out the sequence id.", null, line, lineNumber);
                            }
                            stage = ParsingStage.LOOKING_FOR_DOMAIN_SECTION;
                        }
                        if (line.startsWith(END_OF_RECORD)) {
                            //the end of the record might be at the start of the output
                            continue;
                        }
                        break;
                    case LOOKING_FOR_DOMAIN_SECTION:
                        // Example: Domains:
                        if (line.startsWith(DOMAIN_SECTION_START)) {
                            domains.clear();
                            stage = ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE;
                        } else {
                            throw new ParseException("This line looks like a domain header line, but it is not possible to parse the header.", null, line, lineNumber);
                        }
                        break;
                    case LOOKING_FOR_DOMAIN_DATA_LINE:
                        Matcher domainDataLineMatcher = SequenceDomainMatch.DOMAIN_LINE_PATTERN.matcher(line);
                        if (line.startsWith(SITE_SECTION_START)) {
                            stage = ParsingStage.LOOKING_FOR_SITE_DATA_LINE;
                        } else if (domainDataLineMatcher.matches()) {
                            SequenceDomainMatch sequenceDomainMatch = new SequenceDomainMatch(domainDataLineMatcher);
                            if (checkDomainCoordinates(sequenceDomainMatch)){
                                //we now have a match and can create a raw match
                                DomainMatch domainMatch = new DomainMatch(sequenceDomainMatch);
                                //add the domain match to the search record
                                searchRecord = new HmmSearchRecord(sequenceDomainMatch.getModelAccession());
                                sequenceMatch = new SequenceMatch(currentSequenceIdentifier, sequenceDomainMatch.getSequenceEvalue(), sequenceDomainMatch.getSequenceScore(), sequenceDomainMatch.getSequenceBias());
                                searchRecord.addSequenceMatch(sequenceMatch);
                                searchRecord.addDomainMatch(currentSequenceIdentifier, new DomainMatch(sequenceDomainMatch));
                                domains.put(sequenceDomainMatch.getModelAccession(), domainMatch);
                                hmmer3ParserSupport.addMatch(searchRecord, rawResults);
                                rawDomainCount += getSequenceMatchCount(searchRecord);
                                LOGGER.debug(sequenceDomainMatch.toString());
                            }else{
                                throw new ParseException("Domain coordinates error - sequenceId: " + currentSequenceIdentifier + sequenceDomainMatch.toString());
                            }
                        }
                        if (line.startsWith(END_OF_RECORD)) {
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                            LOGGER.debug("there are no sites for seq id: " + currentSequenceIdentifier);
                            currentSequenceIdentifier = null;
                        }
                        break;
//                    case LOOKING_FOR_SITE_SECTION:
//                        // Example: Sites:
//                        if (line.startsWith(SEQUENCE_SECTION_START)) {
////                            sites.clear();
//                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
//                        } else if (line.startsWith(SITE_SECTION_START)) {
//                            stage = ParsingStage.LOOKING_FOR_SITE_DATA_LINE;
//                            LOGGER.debug("Site Section ");
//                        } else {
//                            throw new ParseException("This line looks like a site header line, but it is not possible to parse the header.", null, line, lineNumber);
//                        }
//                        break;
                    case LOOKING_FOR_SITE_DATA_LINE:
                        Matcher sitesDataLineMatcher = SITES_LINE_PATTERN.matcher(line);
                        if (line.startsWith(END_OF_RECORD)) {
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                            currentSequenceIdentifier = null;
                        } else if (sitesDataLineMatcher.matches()) {
                            // E.g. line = "SFLDF00292	C91,C95,C98,Y99,C141	SFLD_Res01"
                            LOGGER.debug("Site line parse");

                            final String model = sitesDataLineMatcher.group(1);
                            final String sites = sitesDataLineMatcher.group(2);
                            final String description = sitesDataLineMatcher.group(3);

                            SFLDHmmer3RawSite rawSite = new SFLDHmmer3RawSite(currentSequenceIdentifier, description, sites, model, signatureLibraryRelease);
                            rawSites.add(rawSite);
                        }
                        break;

                }
            }
        }
        //TODO consider using the utilities methods
        Utilities.verboseLog(10, " RawResults.size : " + rawResults.size() + " domainCount: " + rawDomainCount);
        //Utilities.verboseLog(rawResults.values().toString());
        /*
        //can be used to check if we have the correct mtahces
        for (RawProtein<T> rawProtein : rawResults.values()) {
            for (T rawMatch : rawProtein.getMatches()) {
                Utilities.verboseLog(rawMatch.toString());
            }
        }
        */
//       LOGGER.debug(getTimeNow() + " RawResults.size : " + rawResults.size() + " domainCount: " +  rawDomainCount);

        for (RawProtein<T> rawProtein : rawResults.values()) {
            rawMatches.addAll((Collection<? extends SFLDHmmer3RawMatch>) rawProtein.getMatches());
        }
        return new MatchData(rawMatches, rawSites);
    }

    public int getSequenceMatchCount(HmmSearchRecord searchRecord) {
        int count = 0;
        for (SequenceMatch sequenceMatch : searchRecord.getSequenceMatches().values()) {
            count += sequenceMatch.getDomainMatches().size();
        }
        return count;
    }

    private class MatchData {
        final Set<SFLDHmmer3RawMatch> matches;
        final Set<SFLDHmmer3RawSite> sites;


        public MatchData(Set<SFLDHmmer3RawMatch> matches, Set<SFLDHmmer3RawSite> sites) {
            this.matches = matches;
            this.sites = sites;
        }

        public Set<SFLDHmmer3RawMatch> getMatches() {
            return matches;
        }

        public Set<SFLDHmmer3RawSite> getSites() {
            return sites;
        }
    }

    public boolean checkDomainCoordinates(SequenceDomainMatch sequenceDomainMatch){
        boolean domainCoordinatesCorrect = true;
        if (sequenceDomainMatch.getAliFrom() > sequenceDomainMatch.getAliTo()){
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo() );
        }
        if (sequenceDomainMatch.getHmmfrom() > sequenceDomainMatch.getHmmto()){
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo());
        }
        if (sequenceDomainMatch.getEnvFrom() > sequenceDomainMatch.getEnvTo()){
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo());
        }

        return  domainCoordinatesCorrect;
    }
}
