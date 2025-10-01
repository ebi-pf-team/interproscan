package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.io.match.MatchAndSiteParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.DomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SFLDHmmer3MatchParser<T extends RawMatch> implements MatchAndSiteParser {
    private static final String END_OF_RECORD = "//";

    private static final String SEQUENCE_SECTION_START = "Sequence:";

    private static final Pattern SEQUENCE_SECTION_START_PATTERN = Pattern.compile("^Sequence:\\s+(\\S+).*$");

    private static final String DOMAIN_SECTION_START = "Domains:";

    public static final Pattern DOMAIN_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+).*$");

    private static final String SITE_SECTION_START = "Sites:";

    private static final Pattern SITES_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+(.*)?$");
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
    private String sfldHierarchyFilePath;

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

    public String getSfldHierarchyFilePath() {
        return sfldHierarchyFilePath;
    }

    @Required
    public void setSfldHierarchyFilePath(String sfldHierarchyFilePath) {
        this.sfldHierarchyFilePath = sfldHierarchyFilePath;
    }

    /**
     * Enum of states that the parser may be in - used to minimise parsing time.
     */
    private enum ParsingStage {
        LOOKING_FOR_SEQUENCE_MATCHES,
        LOOKING_FOR_DOMAINS_SECTION,
        LOOKING_FOR_DOMAIN_DATA_LINE,
        LOOKING_FOR_SITE_DATA_LINE,
    }

    public MatchSiteData<SFLDHmmer3RawMatch, SFLDHmmer3RawSite> parseMatchesAndSites(InputStream is) throws IOException {
        // Parse the matches and sites
        MatchData matchData = parseFileInput(is);
        // Load the SFLD hierarchy
        Map<String, Set<String>> hierarchyInformation = getHierarchyInformation();

        // Group matches by sequence
        Map<String, RawProtein<SFLDHmmer3RawMatch>> rawProteins = new HashMap<>();
        for (SFLDHmmer3RawMatch rawMatch : matchData.getMatches()) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (rawProteins.containsKey(sequenceId)) {
                RawProtein<SFLDHmmer3RawMatch> rawProtein = rawProteins.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<SFLDHmmer3RawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                rawProteins.put(sequenceId, rawProtein);
            }
        }

        // Post-processing, one sequence at a time
        for (RawProtein<SFLDHmmer3RawMatch> rawProtein: rawProteins.values()) {
            if (rawProtein.getMatches().size() > 1) {
                // Select most specific, non-overlapping matches
                Set<SFLDHmmer3RawMatch> filteredMatches = resolveOverlappingMatches(rawProtein.getMatches(), hierarchyInformation);
                rawProtein.setMatches(filteredMatches);
            }

            // Promote matches, i.e. a match from a family is promoted all the way to the superfamily
            Set<SFLDHmmer3RawMatch> promotedRawMatches = new HashSet<>(rawProtein.getMatches());
            for (SFLDHmmer3RawMatch rawMatch: rawProtein.getMatches()) {
                Set<String> parents = hierarchyInformation.getOrDefault(rawMatch.getModelId(), new HashSet<>());
                if (!parents.isEmpty()) {
                    for (String parent: parents) {
                        promotedRawMatches.add(rawMatch.getNewRawMatch(parent));
                    }
                }
            }

            // Sort matches by boundaries (start asc, end desc)
            List<SFLDHmmer3RawMatch> sortedMatches = new ArrayList<>(promotedRawMatches);
            sortedMatches.sort(
                    Comparator.comparing(SFLDHmmer3RawMatch::getLocationStart)
                            .thenComparing(Comparator.comparingInt(SFLDHmmer3RawMatch::getLocationEnd).reversed())
            );

            // Get rid of "nested" matches from the same model (usually occurs due to promotions)
            Set<SFLDHmmer3RawMatch> filteredMatches = new HashSet<>();
            for (SFLDHmmer3RawMatch rawMatch: sortedMatches) {
                boolean isContained = false;
                for (SFLDHmmer3RawMatch other: filteredMatches) {
                    if (rawMatch.getModelId().equals(other.getModelId())
                            && other.getLocationStart() <= rawMatch.getLocationStart()
                            && other.getLocationEnd() >= rawMatch.getLocationEnd()) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained) {
                    filteredMatches.add(rawMatch);
                }
            }

            // Let's not forget to update the matches
            rawProtein.setMatches(filteredMatches);
        }

        // Now let's take care of sites
        Map<String, RawProteinSite<SFLDHmmer3RawSite>> rawProteinSites = new HashMap<>();
        for (SFLDHmmer3RawSite site: matchData.getSites()) {
            String sequenceId = site.getSequenceIdentifier();
            if (rawProteins.containsKey(sequenceId)) {
                // We need to make sure that the site is found in a match
                boolean foundInMatch = false;
                for (SFLDHmmer3RawMatch match: rawProteins.get(sequenceId).getMatches()) {
                    if (site.getModelId().equals(match.getModelId())
                            && !(site.getFirstStart() > match.getLocationEnd() || site.getLastEnd() < match.getLocationStart())) {
                        foundInMatch = true;
                        break;
                    }
                }

                if (foundInMatch) {
                    if (rawProteinSites.containsKey(sequenceId)) {
                        rawProteinSites.get(sequenceId).addSite(site);
                    } else {
                        RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = new RawProteinSite<>(sequenceId);
                        rawProteinSite.addSite(site);
                        rawProteinSites.put(sequenceId, rawProteinSite);
                    }
                }
            }
        }

        return new MatchSiteData<>(new HashSet<>(rawProteins.values()), new HashSet<>(rawProteinSites.values()));
    }

    public Set<SFLDHmmer3RawMatch> resolveOverlappingMatches(Collection<SFLDHmmer3RawMatch> rawMatches, Map<String, Set<String>> hierarchyInformation) {
        Set<SFLDHmmer3RawMatch> filtered = new HashSet<>();
        Set<SFLDHmmer3RawMatch> ignored = new HashSet<>();
        List<SFLDHmmer3RawMatch> matches = new ArrayList<>(rawMatches);

        for (SFLDHmmer3RawMatch match : matches) {
            if (match.getModelId().startsWith("SFLDF")) {
                // This match belongs to a family: more specific so preffered
                filtered.add(match);
                continue;
            }

            boolean ignore = false;
            for (SFLDHmmer3RawMatch other : matches) {
                if (match.getModelId().equals(other.getModelId())
                        || ignored.contains(other)
                        || !match.overlapsWith(other))
                    continue;

                // Matches are from different models and overlap
                Set<String> otherParents = hierarchyInformation.getOrDefault(other.getModelId(), Collections.emptySet());
                if (otherParents.contains(match.getModelId())) {
                    // The current match belongs to an ancestor of the other match
                    // Ignore the current match (other match more specific)
                    ignore = true;
                    break;
                }
            }

            if (ignore) {
                ignored.add(match);
            } else {
                filtered.add(match);
            }
        }
        return filtered;
    }

    public MatchData parseFileInput(InputStream is) throws IOException {
        Map<String, RawProtein<T>> rawResults = new HashMap<>();
        Set<SFLDHmmer3RawSite> rawSites = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Matcher matcher;
            HmmSearchRecord searchRecord;
            SequenceMatch sequenceMatch;

            ParsingStage stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
            int lineNumber = 0;
            String line;
            String currentSequenceIdentifier = null;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                switch (stage) {
                    case LOOKING_FOR_SEQUENCE_MATCHES:
                        if (line.startsWith(SEQUENCE_SECTION_START)) {
                            matcher = SEQUENCE_SECTION_START_PATTERN.matcher(line);
                            if (matcher.matches()) {
                                currentSequenceIdentifier = matcher.group(1);
                            } else {
                                throw new ParseException("This line looks like a domain section header line, but it is not possible to parse out the sequence id.", null, line, lineNumber);
                            }
                            stage = ParsingStage.LOOKING_FOR_DOMAINS_SECTION;
                        }
                        if (line.startsWith(END_OF_RECORD)) {
                            //the end of the record might be at the start of the output
                            continue;
                        }
                        break;
                    case LOOKING_FOR_DOMAINS_SECTION:
                        if (line.startsWith(DOMAIN_SECTION_START)) {
                            stage = ParsingStage.LOOKING_FOR_DOMAIN_DATA_LINE;
                        } else {
                            throw new ParseException("This line looks like a domain header line, but it is not possible to parse the header.", null, line, lineNumber);
                        }
                        break;
                    case LOOKING_FOR_DOMAIN_DATA_LINE:
                        matcher = DOMAIN_LINE_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            String modelAccession = matcher.group(1);
                            double sequenceEvalue = Double.parseDouble(matcher.group(2));
                            double sequenceScore = Double.parseDouble(matcher.group(3));
                            double sequenceBias = Double.parseDouble(matcher.group(4));
                            int hmmFrom = Integer.parseInt(matcher.group(5));
                            int hmmTo = Integer.parseInt(matcher.group(6));
                            String hmmBounds = "..";  // Not reported by the post-processing binary
                            double score = Double.parseDouble(matcher.group(7));
                            int aliFrom = Integer.parseInt(matcher.group(8));
                            int aliTo = Integer.parseInt(matcher.group(9));
                            int envFrom = Integer.parseInt(matcher.group(10));
                            int envTo = Integer.parseInt(matcher.group(11));
                            double cEvalue = Double.parseDouble(matcher.group(12));
                            double iEvalue = Double.parseDouble(matcher.group(13));
                            double acc = Double.parseDouble(matcher.group(14));
                            double bias = Double.parseDouble(matcher.group(15));

                            if (aliFrom <= aliTo && hmmFrom <= hmmTo && envFrom <= envTo) {
                                searchRecord = new HmmSearchRecord(modelAccession);
                                sequenceMatch = new SequenceMatch(currentSequenceIdentifier, sequenceEvalue, sequenceScore, sequenceBias);
                                searchRecord.addSequenceMatch(sequenceMatch);
                                DomainMatch domainMatch = new DomainMatch(score, bias, cEvalue, iEvalue, hmmFrom, hmmTo, hmmBounds, aliFrom, aliTo, envFrom, envTo, acc);
                                searchRecord.addDomainMatch(currentSequenceIdentifier, domainMatch);
                                hmmer3ParserSupport.addMatch(searchRecord, rawResults);
                            } else {
                                throw new ParseException("Domain coordinates error", null, line, lineNumber);
                            }
                        } else if (line.startsWith(SITE_SECTION_START)) {
                            stage = ParsingStage.LOOKING_FOR_SITE_DATA_LINE;
                        } else if (line.startsWith(END_OF_RECORD)) {
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                            currentSequenceIdentifier = null;
                        } else {
                            throw new ParseException("Unexpected line", null, line, lineNumber);
                        }
                        break;
                    case LOOKING_FOR_SITE_DATA_LINE:
                        matcher = SITES_LINE_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            final String model = matcher.group(1);
                            final String sites = matcher.group(2);
                            final String description = matcher.group(3);
                            SFLDHmmer3RawSite rawSite = new SFLDHmmer3RawSite(currentSequenceIdentifier, description, sites, model, signatureLibraryRelease);
                            rawSites.add(rawSite);
                        } else if (line.startsWith(END_OF_RECORD)) {
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                            currentSequenceIdentifier = null;
                        } else {
                            throw new ParseException("Unexpected line", null, line, lineNumber);
                        }
                        break;
                }
            }
        }

        Set<SFLDHmmer3RawMatch> rawMatches = new HashSet<>();
        for (RawProtein<T> rawProtein : rawResults.values()) {
            rawMatches.addAll((Collection<? extends SFLDHmmer3RawMatch>) rawProtein.getMatches());
        }
        return new MatchData(rawMatches, rawSites);
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

    /**
     * get HierarchyInformation information child - parent relationships
     *
     * @return sfldHierarchyInformation
     */
    public Map<String, Set<String>> getHierarchyInformation() throws IOException {
        Map<String, Set<String>> sfldHierarchyInformation = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sfldHierarchyFilePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] nodes = line.split("\t");
                for (int i = 1; i < nodes.length; i++) {
                    String child = nodes[i];
                    Set<String> parents = sfldHierarchyInformation.computeIfAbsent(child, k -> new HashSet<>());
                    for (int j = 0; j < i; j++) {
                        String ancestor = nodes[j];
                        if (!ancestor.equals(child)) {
                            parents.add(ancestor);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to parse " + sfldHierarchyFilePath, e);
        }
        return sfldHierarchyInformation;
    }


}
