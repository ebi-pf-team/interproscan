package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
//import org.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
//import org.fasterxml.jackson.map.JsonMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.RuleSite;
import uk.ac.ebi.interpro.scan.io.SimpleDomainMatch;
import uk.ac.ebi.interpro.scan.io.SimpleSequenceMatch;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.io.match.MatchAndSiteParser;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.HmmSearchRecord;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceDomainMatch;
import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.parsemodel.SequenceMatch;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parser for PIRSR HMMER3 output
 */
public class PIRSRHmmer3MatchParser<T extends RawMatch> implements MatchAndSiteParser {

    private static final Logger LOGGER = LogManager.getLogger(PIRSRHmmer3MatchParser.class.getName());

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

    private static final Pattern SITES_LINE_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S+)(\\s+.*)?$");
    private final SignatureLibrary signatureLibrary;
    private final String signatureLibraryRelease;
    /**
     * This interface has a single method that
     * takes the HmmsearchOutputMethod object, containing sequence
     * and domain matches and converts it to RawProtein
     * objects.  The converter MAY perform additional steps, such as
     * filtering the raw results by specific criteria, such as GA value
     * cutoff.
     */
    private Hmmer3ParserSupport<T> hmmer3ParserSupport;

    //private ProteinDAO proteinDAO;

    private PIRSRHmmer3MatchParser() {
        signatureLibrary = null;
        signatureLibraryRelease = null;
    }

    public PIRSRHmmer3MatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
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

    public MatchSiteData parseMatchesAndSites(InputStream is) throws IOException {

        Map<String, RawProtein<PIRSRHmmer3RawMatch>> rawProteinMap = new HashMap<>();
        Map<String, RawProtein<PIRSRHmmer3RawMatch>> filtertedRawProteinMap = new HashMap<>();
        MatchData matchData = parseFileInput(is);
        Set<PIRSRHmmer3RawMatch> rawMatches = matchData.getMatches();

        Utilities.verboseLog(50,  "Parsed  match count: " + rawMatches.size());

        for (PIRSRHmmer3RawMatch rawMatch : rawMatches) {
            String sequenceId = rawMatch.getSequenceIdentifier();
            if (rawProteinMap.containsKey(sequenceId)) {
                RawProtein<PIRSRHmmer3RawMatch> rawProtein = rawProteinMap.get(sequenceId);
                rawProtein.addMatch(rawMatch);
            } else {
                RawProtein<PIRSRHmmer3RawMatch> rawProtein = new RawProtein<>(sequenceId);
                rawProtein.addMatch(rawMatch);
                rawProteinMap.put(sequenceId, rawProtein);
            }
        }

        Map<String, Set<PIRSRHmmer3RawMatch>> rawMatchGroups = new HashMap<>();
        for (RawProtein<PIRSRHmmer3RawMatch> rawProtein : rawProteinMap.values()) {
            String sequenceIdentifier = rawProtein.getProteinIdentifier();
            Collection<PIRSRHmmer3RawMatch> filteredRawMatches = rawProtein.getMatches();

            for (PIRSRHmmer3RawMatch rawMatch : filteredRawMatches) {
                String modelAc = rawMatch.getModelId();
                String key = sequenceIdentifier + "_" + modelAc;
                //update Scope
                Utilities.verboseLog(50,  "matchgroup_key " + key + " :" + rawMatch.toString());
                if (rawMatchGroups.keySet().contains(key)) {
                    Set<PIRSRHmmer3RawMatch> matchesForKey = rawMatchGroups.get(key);
                    matchesForKey.add(rawMatch);
                } else {
                    Set<PIRSRHmmer3RawMatch> matchesForKey = new HashSet<>();
                    matchesForKey.add(rawMatch);
                    rawMatchGroups.put(key, matchesForKey);
                }
            }
        }

        //deal with sites
        Map<String, RawProteinSite<PIRSRHmmer3RawSite>> rawProteinSiteMap = new HashMap<>();
        Set<PIRSRHmmer3RawSite> rawSites = matchData.getSites();
        Set<PIRSRHmmer3RawSite> filteredRawSites = new HashSet<>();

        int siteCount = rawSites.size();
        Utilities.verboseLog(50,  "Parsed site count: " + siteCount);
        Utilities.verboseLog(50,  "rawMatchGroups #: " + rawMatchGroups.size());
        int promotedSiteCont = 0;
        int correctSiteCoordinatesCount = 0;
        for (PIRSRHmmer3RawSite rawSite : rawSites) {
            Utilities.verboseLog(50,  "Consider RawSite : " + rawSite.toString());
            if (siteInMatchLocation(rawSite, rawMatchGroups)) {
                Utilities.verboseLog(50,  "RawSite : " + rawSite.toString());

                // add to the sites
                //filteredRawSites.add(rawSite);
                String sequenceId = rawSite.getSequenceIdentifier();
                if (rawProteinSiteMap.containsKey(sequenceId)) {
                    RawProteinSite<PIRSRHmmer3RawSite> rawProteinSite = rawProteinSiteMap.get(sequenceId);
                    rawProteinSite.addSite(rawSite);
                } else {
                    RawProteinSite<PIRSRHmmer3RawSite> rawProteinSite = new RawProteinSite<>(sequenceId);
                    rawProteinSite.addSite(rawSite);
                    rawProteinSiteMap.put(sequenceId, rawProteinSite);
                }

                correctSiteCoordinatesCount++;
            } else {
                Utilities.verboseLog(50,  "Site NOT withing match location site - " + rawSite.toString());
            }

        }
        int totalSiteCount = promotedSiteCont + siteCount;
        Utilities.verboseLog(50,  "Sites within match location site :" + correctSiteCoordinatesCount + " out of " + totalSiteCount);

        //print the matches with sites
        Utilities.verboseLog(25, "Matches and sites --- ooo ---");
        if (Utilities.verboseLogLevel >= 25) {
            for (RawProtein<PIRSRHmmer3RawMatch> rawProtein : filtertedRawProteinMap.values()) {
                String sequenceIdentifier = rawProtein.getProteinIdentifier();
                Collection<PIRSRHmmer3RawMatch> allRawMatches = rawProtein.getMatches();
                for (PIRSRHmmer3RawMatch rawMatch : allRawMatches) {
                    StringBuffer outMatch = new StringBuffer("match: ")
                            .append(sequenceIdentifier).append(" ")
                            .append(rawMatch.getModelId()).append(" ")
                            .append(rawMatch.getLocationStart()).append(" ")
                            .append(rawMatch.getLocationEnd()).append(" ");
                    Utilities.verboseLog(outMatch.toString());
                    for (RawProteinSite<PIRSRHmmer3RawSite> rawProteinSite : rawProteinSiteMap.values()) {
                        if (rawProteinSite.getProteinIdentifier().equals(sequenceIdentifier)) {
                            Set<PIRSRHmmer3RawSite> allRawSites = (HashSet<PIRSRHmmer3RawSite>) rawProteinSite.getSites();
                            for (PIRSRHmmer3RawSite rawSite : rawSites) {
                                if (rawSite.getModelId().equals(rawMatch.getModelId())) {
                                    StringBuffer outSite = new StringBuffer("site: ")
                                            .append(rawSite.getSequenceIdentifier()).append(" ")
                                            .append(rawSite.getResidues()).append(" ")
                                            .append(rawSite.getFirstStart()).append(" ")
                                            .append(rawSite.getLastEnd());
                                    Utilities.verboseLog(outSite.toString());
                                }
                            }
                        }
                    }


                }
            }
        }
        //instead of the filterd group use protein
        return new MatchSiteData<>(new HashSet<>(rawProteinMap.values()), new HashSet<>(rawProteinSiteMap.values()));
    }

    /**
     * @param modelMatch
     * @return
     */
    private String getMatchDetails(PIRSRHmmer3RawMatch modelMatch) {
        final List<String> mappingFields = new ArrayList<>();
        mappingFields.add(modelMatch.getSequenceIdentifier());
        mappingFields.add(modelMatch.getModelId());
        mappingFields.add(Integer.toString(modelMatch.getLocationStart()));
        mappingFields.add(Integer.toString(modelMatch.getLocationEnd()));
        mappingFields.add(Integer.toString(modelMatch.getHmmStart()));
        mappingFields.add(Integer.toString(modelMatch.getHmmEnd()));
        mappingFields.add(Integer.toString(modelMatch.getEnvelopeStart()));
        mappingFields.add(Integer.toString(modelMatch.getEnvelopeEnd()));
        mappingFields.add(Double.toString(modelMatch.getScore()));
        mappingFields.add(Double.toString(modelMatch.getEvalue()));

        return mappingFields.toString();
    }

    /**
     * @param rawMatches
     * @return
     */
    public Map<String, Set<PIRSRHmmer3RawMatch>> getMatchGroups(Collection<PIRSRHmmer3RawMatch> rawMatches) {
        Map<String, Set<PIRSRHmmer3RawMatch>> matchGroups = new HashMap<>();
        for (PIRSRHmmer3RawMatch rawMatch : rawMatches) {
            String modelAc = rawMatch.getModelId();
            if (matchGroups.keySet().contains(modelAc)) {
                Set<PIRSRHmmer3RawMatch> modelMatches = matchGroups.get(modelAc);
                modelMatches.add(rawMatch);
            } else {
                Set<PIRSRHmmer3RawMatch> modelMatches = new HashSet<>();
                modelMatches.add(rawMatch);
                matchGroups.put(modelAc, modelMatches);
            }
        }
        return matchGroups;
    }

    private boolean siteInMatchLocation(PIRSRHmmer3RawSite rawSite, Map<String, Set<PIRSRHmmer3RawMatch>> rawMatchGroups) {

        String key = rawSite.getSequenceIdentifier() + "_" + rawSite.getModelId();
        int firstStart = rawSite.getFirstStart();
        int lastEnd = rawSite.getLastEnd();
        Set<PIRSRHmmer3RawMatch> rawMatches = rawMatchGroups.get(key);
        Utilities.verboseLog(50,  "group raw matches key -> " + key);
        if (rawMatches != null) {
            Utilities.verboseLog(50,  "raw matches -> " + rawMatches.size());
            for (PIRSRHmmer3RawMatch rawMatch : rawMatches) {
                Utilities.verboseLog(50, firstStart + "-" + lastEnd + " vs " + rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd());
                if (!(firstStart > rawMatch.getLocationEnd() || rawMatch.getLocationStart() > lastEnd)) {
                    return true;
                }
            }
        }

        return false;

    }

//    private Set<PIRSRHmmer3RawMatch> getPromotedRawMatches(PIRSRHmmer3RawMatch rawMatch, Set<String> parents) {
//        Set<PIRSRHmmer3RawMatch> promotedRawMatches = new HashSet();
//        String childModelId = rawMatch.getModelId();
//        //Utilities.verboseLog(1100, "Promoted match for " + childModelId + " with parents: " + parents);
//        for (String modelAc : parents) {
//            if (!childModelId.equals(modelAc)) {
//                promotedRawMatches.add(rawMatch.getNewRawMatch(modelAc));
//            }
//        }
//        return promotedRawMatches;
//    }
//
//    public PIRSRHmmer3RawSite getRawSite(PIRSRHmmer3RawSite rawSite, String modelAc) {
//        //Utilities.verboseLog( "Get promoted sites for : " + rawSite.getModelId() + " modelAc: " + modelAc);
//        PIRSRHmmer3RawSite promotedRawSite = new PIRSRHmmer3RawSite(
//                rawSite.getSequenceIdentifier(),
//                rawSite.getTitle(),
//                rawSite.getResidues(),
//                rawSite.getLabel(),
//                rawSite.getFirstStart(),
//                rawSite.getLastEnd(),
//                rawSite.getHmmStart(),
//                rawSite.getHmmEnd(),
//                rawSite.getGroup(),
//                modelAc,
//                rawSite.getSignatureLibraryRelease());
//
//        //Utilities.verboseLog(1100, "Promoted site for " + rawSite.getModelId() + " with new model: " + modelAc + " ::::- " + promotedRawSite);
//        return promotedRawSite;
//    }

    public MatchData parseFileInput(InputStream is) throws IOException {
        Map<String, RawProtein<T>> rawResults = new HashMap<>();
        Set<PIRSRHmmer3RawMatch> rawMatches = new HashSet<>();
        Set<PIRSRHmmer3RawSite> rawSites = new HashSet<>();

        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            //SimpleSequenceMatch sequenceMatch = new SimpleSequenceMatch();

            /*
            // convert JSON file to map
            //Map<?, ?> map = mapper.readValue(Paths.get("book.json").toFile(), Map.class);
            Map<?, ?> map = mapper.readValue(is, Map.class);

            // print map entries
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Utilities.verboseLog(50,  entry.getKey() + "=" + entry.getValue());
            }
            */


            //TODO remove this test
            //String jsonInString2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createSequenceMatches());

            //Utilities.verboseLog(50,  jsonInString2);

            //new try
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, SimpleSequenceMatch> jsonMap = new HashMap();

            //jsonMap2 = mapper.readValue(jsonInString2, new TypeReference<Map<String, SimpleSequenceMatch>>(){});
            jsonMap = mapper.readValue(is, new TypeReference<Map<String, SimpleSequenceMatch>>() {
            });


            for (String key : jsonMap.keySet()) {
                SimpleSequenceMatch simpleObject = (SimpleSequenceMatch) jsonMap.get(key);
                int count = 0;
                for (Map.Entry<String, List<SimpleDomainMatch>> domainEntry : simpleObject.getDomainMatches().entrySet()) {
                    count++;
                    String sequenceId = key;
                    String modelId = domainEntry.getKey();
                    List<SimpleDomainMatch> domainModelMatches = (List<SimpleDomainMatch>) domainEntry.getValue();
                    for (SimpleDomainMatch simpleDomainMatch : domainModelMatches) {
                        PIRSRHmmer3RawMatch pirsrHmmer3RawMatch =
                                new PIRSRHmmer3RawMatch(
                                        sequenceId, modelId,
                                        signatureLibrary,
                                        signatureLibraryRelease,
                                        simpleDomainMatch.getSeqFrom(),
                                        simpleDomainMatch.getSeqTo(),
                                        simpleDomainMatch.getDomEvalue(), //evalue,
                                        simpleDomainMatch.getDomScore(), //score,
                                        simpleDomainMatch.getHmmFrom(),
                                        simpleDomainMatch.getHmmTo(),
                                        "[]",
                                        simpleDomainMatch.getDomScore(), //locationScore,
                                        1, //envStart,
                                        2, //envEnd,
                                        0, //expectedAcuracy,
                                        0, //sequenceBias,
                                        0, //domainCeVale,
                                        simpleDomainMatch.getDomEvalue(), //domainIeValue,
                                        1,  //domainBias
                                        simpleDomainMatch.getScope().toString(),
                                        "",
                                        ""
                                );
                        // rawMatches.add(pirsrHmmer3RawMatch);
                        List<RuleSite> ruleSites = simpleDomainMatch.getRuleSites();
                        String seqAlignment = simpleDomainMatch.getSeqAlign();
                        String hmmAlign = simpleDomainMatch.getHmmAlign();

                        Utilities.verboseLog(50,  "sequenceAlignmentPositionMap" + simpleDomainMatch.getSeqFrom() + " - " + simpleDomainMatch.getSeqTo());
                        Map<Integer, Integer> sequenceAlignmentPositionMap = getPositionMap(seqAlignment, simpleDomainMatch.getSeqFrom());
                        //lets reverse the sequence alignment map to get actual positions
                        Map<Integer, Integer> sequenceAlignmentReversePositionMap = new HashMap<>();
                        for(Map.Entry<Integer, Integer> entry : sequenceAlignmentPositionMap.entrySet()){
                            sequenceAlignmentReversePositionMap.put(entry.getValue(), entry.getKey());
                        }
                        Utilities.verboseLog(50,  "hmmAlignmentPositionMap: " + simpleDomainMatch.getHmmFrom() + " - " + simpleDomainMatch.getHmmTo());
                        Map<Integer, Integer> hmmAlignmentPositionMap = getPositionMap(hmmAlign, simpleDomainMatch.getHmmFrom());

                        int site_count = 0;
                        for (RuleSite ruleSite : ruleSites) {
                            Utilities.verboseLog(50,  "Consider --> " + ruleSite.toString());
                            int residueStart = 0;
                            int residueEnd = 0;
                            String residues = null;

                            Utilities.verboseLog(50,  "seqAlignment: (" + seqAlignment.length() + ") " + seqAlignment + "\n");
                            Utilities.verboseLog(50,  "hmmAlignment: (" + hmmAlign.length() + ") " + hmmAlign + "\n");

                            String condition = ruleSite.getCondition();
                            Utilities.verboseLog(50,  "Condition: " + condition + " length : " + condition.length());

                            Utilities.verboseLog(50,  "Check : - seqFrom(): "+ simpleDomainMatch.getSeqFrom() +
                                   " seqTo(): "+ simpleDomainMatch.getSeqTo()  +
                                    " startKey: " + ruleSite.getHmmStart() + " endKey: " + ruleSite.getHmmEnd());
                            //use the sequencePositionMap
                            Integer startKey = ruleSite.getHmmStart();
                            boolean residueCoordinatesFound = false;
                            if (hmmAlignmentPositionMap.containsKey(startKey)) {
                                int residueStartOnSeqAlignFromMap = hmmAlignmentPositionMap.get(startKey);
                                int endKey = ruleSite.getHmmEnd();
                                if (hmmAlignmentPositionMap.containsKey(endKey)) {
                                    int residueEndOnSeqAlignFromMap = hmmAlignmentPositionMap.get(endKey);

                                    String residuesFromMap = seqAlignment.substring(residueStartOnSeqAlignFromMap, residueEndOnSeqAlignFromMap + 1);
                                    residues = residuesFromMap;
                                    if (! (sequenceAlignmentReversePositionMap.containsKey(residueStartOnSeqAlignFromMap)
                                                && sequenceAlignmentReversePositionMap.containsKey(residueEndOnSeqAlignFromMap))){
                                        Utilities.verboseLog(50,  "Position Start not found: " + residueStartOnSeqAlignFromMap);
                                        Utilities.verboseLog(50,  "Position End not found: " + residueEndOnSeqAlignFromMap);
                                        Utilities.verboseLog(50,  "Position not found: " + pirsrHmmer3RawMatch.toString());
                                    }else {
                                        residueStart = sequenceAlignmentReversePositionMap.get(residueStartOnSeqAlignFromMap); //simpleDomainMatch.getSeqFrom() + residueStartOnseqAlignFromMap;
                                        residueEnd = sequenceAlignmentReversePositionMap.get(residueEndOnSeqAlignFromMap); //simpleDomainMatch.getSeqFrom() + residueEndOnseqAlignFromMap;
                                        Utilities.verboseLog(50,  "residues from sequencePositionMap: " + residuesFromMap +
                                                " -- position on the alignment: " + residueStartOnSeqAlignFromMap + "-" + residueEndOnSeqAlignFromMap +
                                                " -- position on the sequence: " + residueStart + "-" + residueEnd
                                        );
                                        //int residueStartOnSequence = sequencePositionMap.get() ...;
                                        residueCoordinatesFound = true;
                                    }
                                }


                            }
                            if (!residueCoordinatesFound) {
                                Utilities.verboseLog(50,  "Problem parsing the alignment for residues, most likely position out of range ");
                                continue;
                            }
                            if (!checkCondition(residues, condition)) {
                                Utilities.verboseLog(50,  "Residues fail the check : " + residues + " not in  " + condition);
                                continue;
                            }

                            PIRSRHmmer3RawSite pirsrHmmer3RawSite = new PIRSRHmmer3RawSite(
                                    sequenceId,
                                    ruleSite.getDesc(),
                                    residues.toUpperCase(),
                                    ruleSite.getLabel(),
                                    residueStart,
                                    residueEnd,
                                    ruleSite.getHmmStart(),
                                    ruleSite.getHmmEnd(),
                                    Integer.parseInt(ruleSite.getGroup()),
                                    ruleSite.getStart(),
                                    ruleSite.getEnd(),
                                    condition,
                                    modelId,
                                    getSignatureLibraryRelease()
                            );
                            if (siteInMatchLocation(pirsrHmmer3RawSite, pirsrHmmer3RawMatch)) {
                                rawSites.add(pirsrHmmer3RawSite);
                                site_count++;
                            } else {
                                LOGGER.warn("Site not in Location" + pirsrHmmer3RawSite.toString() + " \n"
                                        + pirsrHmmer3RawMatch.toString());
                            }
                            Utilities.verboseLog(50,  "pirsrHmmer3RawSite:" + pirsrHmmer3RawSite.toString());
                        }
                        // only add match if we have sites
                        if (site_count > 0) {
                            rawMatches.add(pirsrHmmer3RawMatch);
                        }
                    }

                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int rawDomainCount = 0;

        Utilities.verboseLog(50,  " RawResults.size : " + rawResults.size() + " domainCount: " + rawDomainCount);
        Utilities.verboseLog(50,  " Raw Results: site Count: " + rawSites.size());

        return new MatchData(rawMatches, rawSites);
    }

    public int getSequenceMatchCount(HmmSearchRecord searchRecord) {
        int count = 0;
        for (SequenceMatch sequenceMatch : searchRecord.getSequenceMatches().values()) {
            count += sequenceMatch.getDomainMatches().size();
        }
        return count;
    }

    public boolean checkDomainCoordinates(SequenceDomainMatch sequenceDomainMatch) {
        boolean domainCoordinatesCorrect = true;
        if (sequenceDomainMatch.getAliFrom() > sequenceDomainMatch.getAliTo()) {
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo());
        }
        if (sequenceDomainMatch.getHmmfrom() > sequenceDomainMatch.getHmmto()) {
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo());
        }
        if (sequenceDomainMatch.getEnvFrom() > sequenceDomainMatch.getEnvTo()) {
            domainCoordinatesCorrect = false;
            LOGGER.error("Domain Aligments coordinates error :- from: " + sequenceDomainMatch.getAliFrom()
                    + " to: " + sequenceDomainMatch.getAliTo());
        }

        return domainCoordinatesCorrect;
    }

    private Map<String, SimpleSequenceMatch> createStaticSequenceMatches() {
        Map<String, SimpleSequenceMatch> simpleSequenceMatchMap = new LinkedHashMap<>();
        for (int index = 1; index < 4; index++) {
            String key = String.valueOf(index);
            SimpleSequenceMatch simpleSequenceMatch = new SimpleSequenceMatch();
            Map<String, SimpleDomainMatch> domainMatches = new LinkedHashMap<>();
            String model1 = "PIRSR016496"; //"PIRSR016496-1";
            SimpleDomainMatch sdm1 = new SimpleDomainMatch(
                    4.5,
                    1.2e10,
                    1,
                    10,
                    ",HmmAlign = hmmAlign;",
                    4,
                    6,
                    "mathAlign = mathAlign;",
                    null,
                    null
            );
            List<RuleSite> ruleSites = new ArrayList<>();
            RuleSite rulesite1 = new RuleSite(
                    "Lipid",
                    "they are lipids",
                    2, 2,
                    5, 5,
                    "3",
                    "[GAST]"
            );
            ruleSites.add(rulesite1);
            RuleSite rulesite2 = new RuleSite(
                    "Lipid",
                    "dna binding",
                    2, 2,
                    5, 5,
                    "3",
                    "[GAST2]"
            );
            ruleSites.add(rulesite2);
            sdm1.setRuleSites(ruleSites);
            List<String> scope = new ArrayList<>();
            scope.add("bacteria");
            scope.add("algea");
            sdm1.setScope(scope);
            domainMatches.put(model1, sdm1);
            domainMatches.put("PIRSR016496", sdm1); //PIRSR016496-2
            //simpleSequenceMatch.setDomainMatches(domainMatches);
            //simpleSequenceMatchMap.put(key, simpleSequenceMatch);
        }

        return simpleSequenceMatchMap;
    }

    private boolean siteInMatchLocation(PIRSRHmmer3RawSite rawSite, PIRSRHmmer3RawMatch rawMatch) {

        String sequenceIdentifier = rawSite.getSequenceIdentifier();
        String modelId = rawSite.getModelId();
        int firstStart = rawSite.getFirstStart();
        int lastEnd = rawSite.getLastEnd();
        if (sequenceIdentifier.equalsIgnoreCase(rawMatch.getSequenceIdentifier()) &&
                modelId.equalsIgnoreCase(rawMatch.getModelId())) {
            if (!(firstStart > rawMatch.getLocationEnd() || rawMatch.getLocationStart() > lastEnd)) {
                return true;
            }
        }

        return false;

    }

    private Map<Integer, Integer> getPositionMap(String alignment, int aliStart) {
        Map<Integer, Integer> positionMap = new HashMap();
        int sequenceAlignmentPosition = aliStart;
        Integer positionkey = aliStart;
        Utilities.verboseLog(50,  "The alignment being positioned: " + alignment);
        for (int index = 0, alignmentLength = alignment.length(); index < alignmentLength; index++) {
            char sequenceChar = alignment.charAt(index);
            if (Character.isAlphabetic(sequenceChar)) {
                //sequenceAlignmentPosition ++
                positionMap.put(positionkey, index);
                positionkey++;
            } else {
                Utilities.verboseLog(50,  "Skip position " + "positionkey: " + positionkey + " index: " + index);//skip
            }
            if (! Character.isAlphabetic(sequenceChar)){
                Utilities.verboseLog(50,  " this was not alpha " + Character.toString(sequenceChar));
            }

        }
        //Utilities.verboseLog(50,  positionMap.toString());
        return positionMap;
    }

    private boolean checkCondition(String residues, String condition) {
        condition = condition.replace("-", "");
        condition = condition.replace("(", "{");
        condition = condition.replace(")", "}");
        condition = condition.replace("x", ".");
        Utilities.verboseLog(50,  "condition: " + condition);
        residues = residues.replace("-", "");

        //less strict check
        boolean residueChecksCondition = false;
        Pattern pattern = Pattern.compile(condition);
        Matcher matcher = pattern.matcher(residues);
        if (matcher.matches()) {
            residueChecksCondition = true;
        } else {
            // make another check, so as not be too strict
            boolean secondCheckPass = false;
            for (int index = 0; index < residues.length(); index++) {
                String searchString = String.valueOf(residues.charAt(index));
                if (condition.contains(searchString)) {
                    residueChecksCondition = true;
                } else {
                    residueChecksCondition = false;
                    break;
                }
            }
        }

        return residueChecksCondition;
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

    private class MatchData {
        final Set<PIRSRHmmer3RawMatch> matches;
        final Set<PIRSRHmmer3RawSite> sites;


        public MatchData(Set<PIRSRHmmer3RawMatch> matches, Set<PIRSRHmmer3RawSite> sites) {
            this.matches = matches;
            this.sites = sites;
        }

        public Set<PIRSRHmmer3RawMatch> getMatches() {
            return matches;
        }

        public Set<PIRSRHmmer3RawSite> getSites() {
            return sites;
        }
    }
}
