package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.io.FileInputStream;
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

    private static final Logger LOGGER = LogManager.getLogger(SFLDHmmer3MatchParser.class.getName());

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
        Map<String, RawProtein<SFLDHmmer3RawMatch>> filtertedRawProteinMap = new HashMap<>();
        MatchData matchData = parseFileInput(is);
        Set<SFLDHmmer3RawMatch> rawMatches = matchData.getMatches();

        Map<String, Set<String>> hierarchyInformation = getHierarchyInformation();

        Utilities.verboseLog(1100, "Parsed  match count: " + rawMatches.size());

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

        int promotedTentativeCount = 0;
        //deal with overlaps
        Set<String> seqIds = rawProteinMap.keySet();
        int nonOverlapCount = 0;
        for (String key : seqIds) {
            RawProtein<SFLDHmmer3RawMatch> rawProtein = rawProteinMap.get(key);
            Collection<SFLDHmmer3RawMatch> proteinRawMatches = rawProtein.getMatches();
            if (proteinRawMatches.size() == 1) {
                nonOverlapCount++;
                continue;
            } else {
                //continue;
                int originalProteinRawMatchesCount = proteinRawMatches.size();
                Set<SFLDHmmer3RawMatch> resolvedOverlappingMatches = resolveOverlappingMatches(proteinRawMatches, hierarchyInformation);
                //rawProtein.setMatches(resolvedOverlappingMatches);
                promotedTentativeCount = resolvedOverlappingMatches.size() - proteinRawMatches.size();
//                Utilities.verboseLog(1100, "Match count: " + promotedTentativeCount + " resolvedOverlappingMatches: " + resolvedOverlappingMatches.size() +
//                        " proteinRawMatches : " + originalProteinRawMatchesCount);
                rawProtein.setMatches(resolvedOverlappingMatches);
                nonOverlapCount += resolvedOverlappingMatches.size();

            }
        }
        Utilities.verboseLog(1100, "Overlap resolved  match count: " + nonOverlapCount + " from original " + rawMatches.size() + " matches");

        //Utilities.verboseLog(1100, "Parsed and Promotted matches ...");
        int seqIdsCount = seqIds.size();
        int idxCount = 0;
        int matchCount = 0;
        int totalOriginalMatchCount = 0;
        int totalPromotedRawMatchesCount = 0;
        for (String key : seqIds) {
            idxCount++;
            RawProtein<SFLDHmmer3RawMatch> originalRawProtein = rawProteinMap.get(key);
            RawProtein<SFLDHmmer3RawMatch> filteredRawProtein = new RawProtein<>(key);
            Set<SFLDHmmer3RawMatch> initialRawMatches = (HashSet<SFLDHmmer3RawMatch>) originalRawProtein.getMatches();

            //check for overlaps and remove
            int originalMatchCount = initialRawMatches.size();
            totalOriginalMatchCount += originalMatchCount;
            int promotedRawMatchesCount = 0;
            // start problematic code
            if (initialRawMatches == null) {
                LOGGER.error("initialRawMatches == null!! ");
            }
            Utilities.verboseLog(15, "idxCount : " + idxCount + " of " + seqIdsCount + " with " + originalMatchCount + " raw matches originalMatchCount");
            Set<SFLDHmmer3RawMatch> seqPromotedRawMatches = new HashSet<>();
            for (SFLDHmmer3RawMatch rawMatch : initialRawMatches) {
                Set<String> parents = hierarchyInformation.get(rawMatch.getModelId());

                Set<SFLDHmmer3RawMatch> promotedRawMatches = null;
                if (parents != null && parents.size() > 0) {
                    promotedRawMatches = getPromotedRawMatches(rawMatch, parents);
                    promotedRawMatchesCount = promotedRawMatches.size();
                    totalPromotedRawMatchesCount += promotedRawMatchesCount;
                    //Utilities.verboseLog( "promotedRawMatches count: " + promotedRawMatches.size());
                    //filteredRawProtein.addAllMatches(promotedRawMatches);
                    boolean promotedContainsMatch = false;
                    boolean matchContainsPromted = false;
                    SFLDHmmer3RawMatch matchToRemove = null;
                    for (SFLDHmmer3RawMatch promotedMatch: promotedRawMatches){
                        for (SFLDHmmer3RawMatch seqPromotedMatch: seqPromotedRawMatches){
                           if (promotedMatch.getLocationStart() <= seqPromotedMatch.getLocationStart() &&
                                   promotedMatch.getLocationEnd() >= seqPromotedMatch.getLocationEnd() ){
                               promotedContainsMatch = true;
                               matchToRemove = seqPromotedMatch;
                           }else if (seqPromotedMatch.getLocationStart() <= promotedMatch.getLocationStart() &&
                                   seqPromotedMatch.getLocationEnd() >= promotedMatch.getLocationEnd() ){
                               matchContainsPromted = true;
                               break;
                               // no need to add the new promoted match
                           }
                        }
                        if (promotedContainsMatch){
                            seqPromotedRawMatches.remove(matchToRemove);
                        }
                        if (matchContainsPromted){
                            //do nothing
                            continue;
                        }else {
                            seqPromotedRawMatches.add(promotedMatch);
                        }
                    }

                    //Utilities.verboseLog(1100, "promotedRawMatches:" + promotedRawMatches);
                }
                matchCount = originalMatchCount + promotedRawMatchesCount;
            }
            if (seqPromotedRawMatches.size() > 0){
                //Utilities.verboseLog(25, "seqPromotedRawMatches:" + seqPromotedRawMatches);
                Set<SFLDHmmer3RawMatch> duplicateFreeRawMatches = resolveDuplicateMatches(seqPromotedRawMatches);
                //Utilities.verboseLog(25,"duplicateFreeRawMatches:" + duplicateFreeRawMatches);
                filteredRawProtein.addAllMatches(duplicateFreeRawMatches);

            }
            filteredRawProtein.addAllMatches(initialRawMatches);
            filtertedRawProteinMap.put(key, filteredRawProtein);
            // end problematic code
        }
        Utilities.verboseLog(1100, "Original Parsed match count: " + totalOriginalMatchCount);
        Utilities.verboseLog(1100, "Promotted match count: " + totalPromotedRawMatchesCount);

        Map<String, Set<SFLDHmmer3RawMatch>> rawMatchGroups = new HashMap<>();
        for (RawProtein<SFLDHmmer3RawMatch> rawProtein: filtertedRawProteinMap.values()){
            String sequenceIdentifier = rawProtein.getProteinIdentifier();
            Collection <SFLDHmmer3RawMatch> filteredRawMatches =  rawProtein.getMatches();
            for (SFLDHmmer3RawMatch rawMatch : filteredRawMatches) {
                String modelAc = rawMatch.getModelId();
                String key = sequenceIdentifier + "_" + modelAc;
                if (rawMatchGroups.keySet().contains(key)) {
                    Set<SFLDHmmer3RawMatch> matchesForKey = rawMatchGroups.get(key);
                    matchesForKey.add(rawMatch);
                }else{
                    Set<SFLDHmmer3RawMatch> matchesForKey =  new HashSet<>();
                    matchesForKey.add(rawMatch);
                    rawMatchGroups.put(key, matchesForKey);
                }
            }
        }

        //deal with sites
        Map<String, RawProteinSite<SFLDHmmer3RawSite>> rawProteinSiteMap = new HashMap<>();
        Set<SFLDHmmer3RawSite> rawSites = matchData.getSites();
        Set<SFLDHmmer3RawSite> filteredRawSites = new HashSet<>();

        int siteCount = rawSites.size();
        Utilities.verboseLog(1100, "Parsed site count: " + siteCount);
        int promotedSiteCont = 0;
        int correctSiteCoordinatesCount = 0;
        for (SFLDHmmer3RawSite rawSite : rawSites) {
            if (siteInMatchLocation(rawSite,  rawMatchGroups)){
                // add to the sites
                //filteredRawSites.add(rawSite);
                String sequenceId = rawSite.getSequenceIdentifier();
                if (rawProteinSiteMap.containsKey(sequenceId)) {
                    RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = rawProteinSiteMap.get(sequenceId);
                    rawProteinSite.addSite(rawSite);
                } else {
                    RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = new RawProteinSite<>(sequenceId);
                    rawProteinSite.addSite(rawSite);
                    rawProteinSiteMap.put(sequenceId, rawProteinSite);
                }

                correctSiteCoordinatesCount ++;
            }else{
                Utilities.verboseLog(1100, "Site NOT withing match location site - " + rawSite.toString());
            }

            /*
            There seems to be no good reason to promote sites

            Set<String> parents = hierarchyInformation.get(rawSite.getModelId());

            Set<SFLDHmmer3RawSite> promotedRawSites = null;
            if (parents != null && parents.size() > 0) {
                promotedRawSites = getPromotedRawSites(rawSite, parents);
                promotedSiteCont += promotedRawSites.size();
                //Utilities.verboseLog( "promotedRawSites count: " + promotedRawSites.size());
            }
            String sequenceId = rawSite.getSequenceIdentifier();
            if (rawProteinSiteMap.containsKey(sequenceId)) {
                RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = rawProteinSiteMap.get(sequenceId);
                rawProteinSite.addSite(rawSite);
                rawProteinSite.addAllSites(promotedRawSites);
            } else {
                RawProteinSite<SFLDHmmer3RawSite> rawProteinSite = new RawProteinSite<>(sequenceId);
                rawProteinSite.addSite(rawSite);
                rawProteinSite.addAllSites(promotedRawSites);
                rawProteinSiteMap.put(sequenceId, rawProteinSite);
            }
            */
        }
        int totalSiteCount = promotedSiteCont + siteCount;
        Utilities.verboseLog(1100, "Sites within match location site :" + correctSiteCoordinatesCount + " out of " + totalSiteCount);

        //promote the SFLD matched to the parents in the hierarchy

        //print the matches with sites
        Utilities.verboseLog(25,"Matches and sites --- ooo ---");
        if (Utilities.verboseLogLevel >= 25) {
            for (RawProtein<SFLDHmmer3RawMatch> rawProtein : filtertedRawProteinMap.values()) {
                String sequenceIdentifier = rawProtein.getProteinIdentifier();
                Collection<SFLDHmmer3RawMatch> allRawMatches = rawProtein.getMatches();
                for (SFLDHmmer3RawMatch rawMatch : allRawMatches) {
                    StringBuffer outMatch = new StringBuffer("match: ")
                            .append(sequenceIdentifier).append(" ")
                            .append(rawMatch.getModelId()).append(" ")
                            .append(rawMatch.getLocationStart()).append(" ")
                            .append(rawMatch.getLocationEnd()).append(" ");
                    Utilities.verboseLog(outMatch.toString());
                    for (RawProteinSite<SFLDHmmer3RawSite> rawProteinSite : rawProteinSiteMap.values()) {
                        if (rawProteinSite.getProteinIdentifier().equals(sequenceIdentifier)) {
                            Set<SFLDHmmer3RawSite> allRawSites = (HashSet<SFLDHmmer3RawSite>) rawProteinSite.getSites();
                            for (SFLDHmmer3RawSite rawSite : rawSites) {
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
        return new MatchSiteData<>(new HashSet<>(filtertedRawProteinMap.values()), new HashSet<>(rawProteinSiteMap.values()));
    }

    public Set<SFLDHmmer3RawMatch> resolveOverlappingMatches(Collection<SFLDHmmer3RawMatch> rawMatches, Map<String, Set<String>> hierarchyInformation) {
        // hmm_hit = [hmm_id, description, float(eVal), float(score), location]
        Set<SFLDHmmer3RawMatch> overlapFreeRawMatches = new HashSet<>();

        Collection<SFLDHmmer3RawMatch> allRawMatches = rawMatches;
        Map<String, Set<SFLDHmmer3RawMatch>> matchesPerModel = getMatchGroups(rawMatches);
        //Utilities.verboseLog(1100, "matchesPerModel: " + matchesPerModel.toString());
        if (matchesPerModel.keySet().size() == 1) {
            overlapFreeRawMatches.addAll(rawMatches);
            return overlapFreeRawMatches;
        }
        for (String key : matchesPerModel.keySet()) {
            Set<SFLDHmmer3RawMatch> modelMatches = matchesPerModel.get(key);
            //SFLDF models are more specific
            if (key.contains("SFLDF")) {
                overlapFreeRawMatches.addAll(modelMatches);
                continue;
            }

            //Utilities.verboseLog(1100, " modelMatches count: " + modelMatches.size());
            for (SFLDHmmer3RawMatch modelMatch : modelMatches) {
                boolean overlaps = false;
                SFLDHmmer3RawMatch baseMatch = modelMatch;
                Set<String> parents = hierarchyInformation.get(modelMatch.getModelId());
                for (SFLDHmmer3RawMatch otherMatch : allRawMatches) {
                    Set<String> otherParents = hierarchyInformation.get(otherMatch.getModelId());
                    if (modelMatch.equals(otherMatch)) {
                        continue;
                    }
                    if (modelMatch.getModelId().equals(otherMatch.getModelId())) {
                        continue;
                    }
                    String otherParentsStr = "";
                    if (modelMatch.overlapsWith(otherMatch)) {
                        if (otherParents != null) {
                            otherParentsStr = otherParents.toString();
                        }
                        //Utilities.verboseLog(1100, "modelMatch.overlapsWith otherMatch)  --- check if parents of otherMatch " + otherMatch.getModelId() + " [ " + otherParentsStr + "] contain modelMatch " + modelMatch.getModelId());
                        if (otherParents != null && otherParents.contains(modelMatch.getModelId())) {
//                            Utilities.verboseLog(1100, "modelMatch does overlapsWith(otherMatch) - modelMatch: " + modelMatch.getModelId()
//                                    + " otherMatch: " + otherMatch.getModelId());
                            overlaps = true;
                            break;
                        }
                    }
                }
                if (overlaps) {
                    allRawMatches.remove(modelMatch);
                } else {
                    overlapFreeRawMatches.add(modelMatch);
                }
            }
        }

        return overlapFreeRawMatches;
    }

    public Set<SFLDHmmer3RawMatch> resolveDuplicateMatches(Set<SFLDHmmer3RawMatch> rawMatches){
        Set<SFLDHmmer3RawMatch> duplicateFreeRawMatches = new HashSet<>();
        Collection<SFLDHmmer3RawMatch> allRawMatches = rawMatches;
        Map<String, Set<SFLDHmmer3RawMatch>> matchesPerModel = getMatchGroups(rawMatches);
        //Utilities.verboseLog(1100, "matchesPerModel: " + matchesPerModel.toString());
        /*if (matchesPerModel.keySet().size() == 1) {
            duplicateFreeRawMatches.addAll(rawMatches);
            return overlapFreeRawMatches;
        }
        */
        for (String key : matchesPerModel.keySet()) {
            Set<SFLDHmmer3RawMatch> modelMatches = matchesPerModel.get(key);

            /* debug
            for (SFLDHmmer3RawMatch modelMatch : modelMatches) {

                //Utilities.verboseLog(1100, "raw unfilt match : " + getMatchDetails(modelMatch));

            }
            */

            for (SFLDHmmer3RawMatch modelMatch : modelMatches) {
                boolean duplicate = false;
                SFLDHmmer3RawMatch baseMatch = modelMatch;
                //Utilities.verboseLog(1100, "Consider  match: " + getMatchDetails(modelMatch));
                for (SFLDHmmer3RawMatch otherMatch : allRawMatches) {
                    //Utilities.verboseLog(120,"Consider against match: " + getMatchDetails(otherMatch));
                    if (modelMatch.equals(otherMatch) || ! modelMatch.getModelId().equals(otherMatch.getModelId())) {
                        continue;
                    }

                    if (modelMatch.getLocationStart() == otherMatch.getLocationStart() && modelMatch.getLocationEnd() == otherMatch.getLocationEnd()) {
                        if (modelMatch.getScore() <= modelMatch.getScore() ) {
                           // Utilities.verboseLog(getMatchDetails(modelMatch) + " -- is duplicate .... of ..  -- " + getMatchDetails(otherMatch) );
                            duplicate = true;
                            break;
                            //check hmmstarts and envstarts;
                        }

                    }
                }
                if (duplicate) {
                    allRawMatches.remove(modelMatch);
                } else {
                    duplicateFreeRawMatches.add(modelMatch);
                }
            }
        }

        return duplicateFreeRawMatches;
    }

    /**
     *
     * @param modelMatch
     * @return
     */
    private String getMatchDetails(SFLDHmmer3RawMatch modelMatch){
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
     *
     * @param rawMatches
     * @return
     */
    public Map<String, Set<SFLDHmmer3RawMatch>> getMatchGroups(Collection<SFLDHmmer3RawMatch> rawMatches) {
        Map<String, Set<SFLDHmmer3RawMatch>> matchGroups = new HashMap<>();
        for (SFLDHmmer3RawMatch rawMatch : rawMatches) {
            String modelAc = rawMatch.getModelId();
            if (matchGroups.keySet().contains(modelAc)) {
                Set<SFLDHmmer3RawMatch> modelMatches = matchGroups.get(modelAc);
                modelMatches.add(rawMatch);
            } else {
                Set<SFLDHmmer3RawMatch> modelMatches = new HashSet<>();
                modelMatches.add(rawMatch);
                matchGroups.put(modelAc, modelMatches);
            }
        }
        return matchGroups;
    }
//
//    public Map<String, Set<SFLDHmmer3RawMatch>> getMatchGroups(Set<SFLDHmmer3RawMatch> rawMatches) {
//        Map<String, Set<SFLDHmmer3RawMatch>> matchGroups = new HashMap<>();
//        for (SFLDHmmer3RawMatch rawMatch : rawMatches) {
//            String modelAc = rawMatch.getModelId();
//            if (matchGroups.keySet().contains(modelAc)) {
//                Set<SFLDHmmer3RawMatch> modelMatches = matchGroups.get(modelAc);
//                modelMatches.add(rawMatch);
//            } else {
//                Set<SFLDHmmer3RawMatch> modelMatches = new HashSet<>();
//                modelMatches.add(rawMatch);
//                matchGroups.put(modelAc, modelMatches);
//            }
//        }
//        return matchGroups;
//    }

    private Set<SFLDHmmer3RawMatch> getPromotedRawMatches(SFLDHmmer3RawMatch rawMatch, Set<String> parents) {
        Set<SFLDHmmer3RawMatch> promotedRawMatches = new HashSet();
        String childModelId = rawMatch.getModelId();
        //Utilities.verboseLog(1100, "Promoted match for " + childModelId + " with parents: " + parents);
        for (String modelAc : parents) {
            if (!childModelId.equals(modelAc)) {
                promotedRawMatches.add(rawMatch.getNewRawMatch(modelAc));
            }
        }
        return promotedRawMatches;
    }

    public SFLDHmmer3RawSite getRawSite(SFLDHmmer3RawSite rawSite, String modelAc) {
        //Utilities.verboseLog( "Get promoted sites for : " + rawSite.getModelId() + " modelAc: " + modelAc);
        SFLDHmmer3RawSite promotedRawSite = new SFLDHmmer3RawSite(rawSite.getSequenceIdentifier(),
                rawSite.getTitle(), rawSite.getResidues(), modelAc, rawSite.getSignatureLibraryRelease());

        //Utilities.verboseLog(1100, "Promoted site for " + rawSite.getModelId() + " with new model: " + modelAc + " ::::- " + promotedRawSite);
        return promotedRawSite;
    }

    private boolean siteInMatchLocation(SFLDHmmer3RawSite rawSite, Map<String, Set<SFLDHmmer3RawMatch>> rawMatchGroups){

        String key = rawSite.getSequenceIdentifier() + "_" + rawSite.getModelId();
        int firstStart = rawSite.getFirstStart();
        int lastEnd = rawSite.getLastEnd();
        Set<SFLDHmmer3RawMatch> rawMatches = rawMatchGroups.get(key);
        if (rawMatches != null) {
            for (SFLDHmmer3RawMatch rawMatch : rawMatches){
                if (! (firstStart > rawMatch.getLocationEnd() || rawMatch.getLocationStart() > lastEnd )){
                    return true;
                }
            }
        }

        return false;

    }

    private Set<SFLDHmmer3RawSite> getPromotedRawSites(SFLDHmmer3RawSite rawSite, Set<String> parents) {
        Set<SFLDHmmer3RawSite> promotedRawSites = new HashSet();
        String childModelId = rawSite.getModelId();
        //Utilities.verboseLog( "Get promoted sites for : " + childModelId + " with parents: " + parents);
        for (String modelAc : parents) {
            if (!childModelId.equals(modelAc)) {
                promotedRawSites.add(getRawSite(rawSite, modelAc));
            }
        }
        return promotedRawSites;
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
//                Utilities.verboseLog(1100, "dealing with line: " + line + "  stage: " + stage.toString());
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
                            if (checkDomainCoordinates(sequenceDomainMatch)) {
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
                            } else {
                                throw new ParseException("Domain coordinates error - sequenceId: " + currentSequenceIdentifier + sequenceDomainMatch.toString());
                            }
                        }
                        if (line.startsWith(END_OF_RECORD)) {
                            stage = ParsingStage.LOOKING_FOR_SEQUENCE_MATCHES;
                            LOGGER.debug("there are no sites for seq id: " + currentSequenceIdentifier);
                            currentSequenceIdentifier = null;
                        }
                        break;
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
        Utilities.verboseLog(110, " RawResults.size : " + rawResults.size() + " domainCount: " + rawDomainCount);
        Utilities.verboseLog(110, " Raw Results: site Count: " + rawSites.size());

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


    /**
     * get HierarchyInformation information child - parent relationships
     *
     * @return sfldHierarchyInformation
     */
    public Map<String, Set<String>> getHierarchyInformation() {
        Map<String, Set<String>> sfldHierarchyInformation = new HashMap<>();
        try (FileInputStream is = new FileInputStream(sfldHierarchyFilePath)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                int lineNumber = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] modelWithParents = line.trim().split(":");

                    if (modelWithParents.length >= 2) {
                        String modelAc = modelWithParents[0];
                        String[] allParents = modelWithParents[1].split("\\s+");
                        Set<String> parents = new HashSet<>();
                        for (String parent : allParents) {
                            if (!(parent.trim().isEmpty() || parent.trim().equals(modelAc))) {
                                parents.add(parent.trim());
                            }
                        }
                        sfldHierarchyInformation.put(modelAc, parents);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sfldHierarchyInformation;
    }


}
