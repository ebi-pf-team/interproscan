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

        Map<String, RawProtein<PIRSRHmmer3RawMatch>> rawProteinMap = new HashMap<>();
        Map<String, RawProtein<PIRSRHmmer3RawMatch>> filtertedRawProteinMap = new HashMap<>();
        MatchData matchData = parseFileInput(is);
        Set<PIRSRHmmer3RawMatch> rawMatches = matchData.getMatches();

        Utilities.verboseLog(30, "Parsed  match count: " + rawMatches.size());

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
                Utilities.verboseLog(30, "matchgroup_key " + key + " :" + rawMatch.toString());
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
        Utilities.verboseLog(30, "Parsed site count: " + siteCount);
        Utilities.verboseLog(30, "rawMatchGroups #: " + rawMatchGroups.size());
        int promotedSiteCont = 0;
        int correctSiteCoordinatesCount = 0;
        for (PIRSRHmmer3RawSite rawSite : rawSites) {
            Utilities.verboseLog(30, "Consider RawSite : " + rawSite.toString());
            if (siteInMatchLocation(rawSite, rawMatchGroups)) {
                Utilities.verboseLog(30, "RawSite : " + rawSite.toString());

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
                Utilities.verboseLog(30, "Site NOT withing match location site - " + rawSite.toString());
            }

        }
        int totalSiteCount = promotedSiteCont + siteCount;
        Utilities.verboseLog(30, "Sites within match location site :" + correctSiteCoordinatesCount + " out of " + totalSiteCount);

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

    private boolean siteInMatchLocation(PIRSRHmmer3RawSite rawSite, Map<String, Set<PIRSRHmmer3RawMatch>> rawMatchGroups) {

        String key = rawSite.getSequenceIdentifier() + "_" + rawSite.getModelId();
        int firstStart = rawSite.getFirstStart();
        int lastEnd = rawSite.getLastEnd();
        Set<PIRSRHmmer3RawMatch> rawMatches = rawMatchGroups.get(key);
        Utilities.verboseLog(30, "group raw matches key -> " + key);
        if (rawMatches != null) {
            Utilities.verboseLog(30, "raw matches -> " + rawMatches.size());
            for (PIRSRHmmer3RawMatch rawMatch : rawMatches) {
                Utilities.verboseLog(firstStart + "-" + lastEnd + " vs " + rawMatch.getLocationStart() + "-" + rawMatch.getLocationEnd());
                if (!(firstStart > rawMatch.getLocationEnd() || rawMatch.getLocationStart() > lastEnd)) {
                    return true;
                }
            }
        }

        return false;

    }

//    private Set<PIRSRHmmer3RawSite> getPromotedRawSites(PIRSRHmmer3RawSite rawSite, Set<String> parents) {
//        Set<PIRSRHmmer3RawSite> promotedRawSites = new HashSet();
//        String childModelId = rawSite.getModelId();
//        //Utilities.verboseLog( "Get promoted sites for : " + childModelId + " with parents: " + parents);
//        for (String modelAc : parents) {
//            if (!childModelId.equals(modelAc)) {
//                promotedRawSites.add(getRawSite(rawSite, modelAc));
//            }
//        }
//        return promotedRawSites;
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
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
            */


            //TODO remove this test
            //String jsonInString2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createSequenceMatches());

            //System.out.println(jsonInString2);

            //new try
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            Map<String, SimpleSequenceMatch> jsonMap = new HashMap();

            //jsonMap2 = mapper.readValue(jsonInString2, new TypeReference<Map<String, SimpleSequenceMatch>>(){});
            jsonMap = mapper.readValue(is, new TypeReference<Map<String, SimpleSequenceMatch>>() {
            });


            for (String key : jsonMap.keySet()) {
                //SimpleSequenceMatch object = (SimpleSequenceMatch) jsonMap.get(key);
//                SimpleSequenceMatch object = (SimpleSequenceMatch) jsonMap.get(key);
                System.out.println("jsonMap Primary key (SequenceID)::" + key);
                //Class obj = jsonMap.getClass();
                //System.out.println("type::" + obj.toString());
                //System.out.println("type canon::" + obj.getTypeName());
                //System.out.println("type of this object with key " +  key  + " is: " + jsonMap.get(key).toString());
                //Class object = jsonMap.get(key).getClass();
                //System.out.println("object type::" + object.toString());
                //System.out.println("object type canon::" + object.getTypeName());

                SimpleSequenceMatch simpleObject = (SimpleSequenceMatch) jsonMap.get(key);
                System.out.println("jsonMap object for key " + key + " : SimpleSequenceMatch : " + simpleObject.toString());
                /*
                for(Map.Entry<String, SimpleDomainMatch> domainEntry : simpleObject.getDomainMatches().entrySet()){
                    System.out.println("map item : " + domainEntry.getKey()+" "+ domainEntry.getValue());
                    System.out.println("try again : " + domainEntry.getValue().toString());
                    SimpleDomainMatch simpleDomainMatch = (SimpleDomainMatch) domainEntry.getValue();
                    //SimpleDomainMatch sdm = new ObjectMapper().readValue(m.getValue().toString(), SimpleDomainMatch.class);
                    System.out.println("simpleDomainMatch : " + simpleDomainMatch.toString());
                }

                 */

                //Map<String, SimpleSequenceMatch> simpleSequenceMatchMap = (Map<String, SimpleSequenceMatch>) jsonMap.get(key);

                //Map<String, SimpleSequenceMatch> simpleSequenceMatchMap = (Map<String, SimpleSequenceMatch>) jsonMap.get(key);;
                //System.out.println("type of simpleSequenceMatchMap: " + simpleSequenceMatchMap.toString());
                //System.out.println("This is a(n) " + simpleSequenceMatchMap.getClass().getSimpleName());
                //if (! key.equalsIgnoreCase("6")){
                //    continue;
                //}
                int count = 0;
                for (Map.Entry<String, SimpleDomainMatch> domainEntry : simpleObject.getDomainMatches().entrySet()) {
                    count ++;
                    String sequenceId = key;
                    String modelId = domainEntry.getKey();
                    System.out.println(count + ": Secondary key (ModelId)::" + modelId);


                    System.out.println(count + ": type of this object with entry key " + sequenceId + " is: " + domainEntry.getValue().toString());
                    SimpleDomainMatch simpleDomainMatch = (SimpleDomainMatch) domainEntry.getValue();
                    System.out.println(count + ": This simpleDomainMatch is a(n) " + domainEntry.getClass().getClass().getSimpleName());
                    modelId = modelId.split("-")[0];

                    System.out.println(count + ": simpleDomainMatch : " + simpleDomainMatch.toString());
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
                                    simpleDomainMatch.getScope().toString()
                            );
                    rawMatches.add(pirsrHmmer3RawMatch);
                    List<RuleSite> ruleSites = simpleDomainMatch.getRuleSites();
                    System.out.println(count + ": process sites : " + ruleSites.toString());
                    for (RuleSite ruleSite : ruleSites) {
                        String condition = ruleSite.getCondition();
                        Utilities.verboseLog(30, "Condition: " + condition + " len : " + condition.length());
                        if (condition.contains("[")){
                            condition =  condition.replace("[", "").replace("]", "");
                            condition = condition.replace("'", "");
                        }
                        String residues = condition;
                        Utilities.verboseLog(30, "Condition (residues): " + residues + " -> " + ruleSite.getStart() + "-" +
                                ruleSite.getEnd());
                        PIRSRHmmer3RawSite pirsrHmmer3RawSite = new PIRSRHmmer3RawSite(
                                sequenceId,
                                ruleSite.getDesc(),
                                residues,
                                ruleSite.getLabel(),
                                ruleSite.getStart(),
                                ruleSite.getEnd(),
                                ruleSite.getHmmStart(),
                                ruleSite.getHmmEnd(),
                                Integer.parseInt(ruleSite.getGroup()),
                                modelId,
                                getSignatureLibraryRelease()
                        );
                        rawSites.add(pirsrHmmer3RawSite);
                        Utilities.verboseLog(30, "pirsrHmmer3RawSite:" + pirsrHmmer3RawSite.toString());
                    }


                }
                //Map<String, SimpleDomainMatch> simpleDomainMatches = simpleSequenceMatch.getDomainMatches();
                //System.out.println(key + ": " + simpleDomainMatches.toString());
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int rawDomainCount = 0;

        //TODO consider using the utilities methods
        Utilities.verboseLog(30, " RawResults.size : " + rawResults.size() + " domainCount: " + rawDomainCount);
        Utilities.verboseLog(30, " Raw Results: site Count: " + rawSites.size());

//        for (RawProtein<T> rawProtein : rawResults.values()) {
//            rawMatches.addAll((Collection<? extends PIRSRHmmer3RawMatch>) rawProtein.getMatches());
//        }
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

    private Map<String, SimpleSequenceMatch> createSequenceMatches() {
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
            simpleSequenceMatch.setDomainMatches(domainMatches);
            simpleSequenceMatchMap.put(key, simpleSequenceMatch);
        }

        return simpleSequenceMatchMap;
    }
}
