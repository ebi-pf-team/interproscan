package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import uk.ac.ebi.interpro.scan.model.DCStatus;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClanData;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamModel;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.util.*;

/**
 * This class performs post processing of HMMER3 output for
 * Pfam.
 *
 * @author Phil Jones
 * @version $Id: PfamHMMER3PostProcessing.java,v 1.10 2009/11/09 13:35:50 craigm Exp $
 * @since 1.0
 */
public class PfamHMMER3PostProcessing implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(PfamHMMER3PostProcessing.class.getName());

    private PfamClanData clanData;

    private ClanFileParser clanFileParser;

    private SeedAlignmentDataRetriever seedAlignmentDataRetriever;

    private String pfamHmmDataPath;

    private int minMatchLength;

    @Required
    public void setClanFileParser(ClanFileParser clanFileParser) {
        this.clanFileParser = clanFileParser;
    }

    /**
     * TODO: Will eventually be 'required', but not till after milestone one.
     *
     * @param seedAlignmentDataRetriever to retrieve seed alignment data for
     *                                   a range of proteins.
     */
    public void setSeedAlignmentDataRetriever(SeedAlignmentDataRetriever seedAlignmentDataRetriever) {
        this.seedAlignmentDataRetriever = seedAlignmentDataRetriever;
    }

    @Required
    public void setPfamHmmDataPath(String pfamHmmDataPath) {
        this.pfamHmmDataPath = pfamHmmDataPath;
    }

    public int getMinMatchLength() {
        return minMatchLength;
    }

    public void setMinMatchLength(int minMatchLength) {
        this.minMatchLength = minMatchLength;
    }

    /**
     * Post-processes raw results for Pfam HMMER3 in the batch requested.
     *
     * @param proteinIdToRawMatchMap being a Map of protein IDs to a List of raw matches
     * @return a Map of proteinIds to a List of filtered matches.
     */
    public Map<String, RawProtein<PfamHmmer3RawMatch>> process(Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToRawMatchMap) throws IOException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Pfam A Post Processing: Number of proteins being considered: " + ((proteinIdToRawMatchMap == null) ? 0 : proteinIdToRawMatchMap.size()));
        }
        if (clanData == null) {
            clanData = clanFileParser.getClanData();
        }
        final Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<PfamHmmer3RawMatch>>();
        if (proteinIdToRawMatchMap == null) {
            return proteinIdToRawProteinMap;
        }
        long startNanos = System.nanoTime();
        // Iterate over UniParc IDs in range and processBatch them
        SeedAlignmentDataRetriever.SeedAlignmentData seedAlignmentData = null;
        if (seedAlignmentDataRetriever != null) {
            seedAlignmentData = seedAlignmentDataRetriever.retrieveSeedAlignmentData(proteinIdToRawMatchMap.keySet());
        }
        Map<String, Set<String>> nestedModelsMap = new HashMap<>();

        try {
            nestedModelsMap = getPfamHmmData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String proteinId : proteinIdToRawMatchMap.keySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Pfam A post processing: processing protein " + proteinId);
            }
            List<SeedAlignment> seedAlignments = null;
            if (seedAlignmentData != null) {
                seedAlignments = seedAlignmentData.getSeedAlignments(proteinId);
            }

            Utilities.verboseLog(120,"Pfam A post processing: processing protein " + proteinId);
            proteinIdToRawProteinMap.put(proteinId, processProtein(proteinIdToRawMatchMap.get(proteinId), nestedModelsMap, seedAlignments));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append("Batch containing").append(proteinIdToRawMatchMap.size()).append(" proteins took ").append(((double) (System.nanoTime() - startNanos)) / 1.0e9d).append(" s to run.").toString());
        }
        return proteinIdToRawProteinMap;
    }

    /**
     * Implementation of Rob Finn's algorithm for post processing, translated from Perl to Java.
     * <p/>
     * Also includes additional code to ensure seed alignments are included as matches, regardless of
     * score.
     *
     * @param rawProteinUnfiltered being a List of the raw matches to filter
     * @param seedAlignments       being a Collection of SeedAlignment objects, to check for matches to
     *                             methods where this protein was part of the seed alignment.
     * @return a List of filtered matches.
     */
    private RawProtein processProtein(final RawProtein<PfamHmmer3RawMatch> rawProteinUnfiltered, final Map<String, Set<String>> nestedModelsMap, final List<SeedAlignment> seedAlignments) {
        int localVerboseLevel = 120;
        Utilities.verboseLog(localVerboseLevel,"Start processProtein ---oo--");
        RawProtein<PfamHmmer3RawMatch> filteredMatches = new RawProtein<PfamHmmer3RawMatch>(rawProteinUnfiltered.getProteinIdentifier());
        RawProtein<PfamHmmer3RawMatch> filteredRawProtein = new RawProtein<PfamHmmer3RawMatch>(rawProteinUnfiltered.getProteinIdentifier());

        // First of all, place any rawProteinUnfiltered to methods for which this protein was a seed
        // into the filteredMatches collection.
        final Set<PfamHmmer3RawMatch> seedMatches = new HashSet<PfamHmmer3RawMatch>();

        if (seedAlignments != null) {        // TODO This check can be removed, once the seed alignment stuff has been sorted.
            Utilities.verboseLog(localVerboseLevel,"seedAlignments count:" + seedAlignments.size());
            for (final SeedAlignment seedAlignment : seedAlignments) {
                for (final PfamHmmer3RawMatch candidateMatch : rawProteinUnfiltered.getMatches()) {

                    if (!seedMatches.contains(candidateMatch)) {
                        if (seedAlignment.getModelAccession().equals(candidateMatch.getModelId()) &&
                                seedAlignment.getAlignmentStart() <= candidateMatch.getLocationStart() &&
                                seedAlignment.getAlignmentEnd() >= candidateMatch.getLocationEnd()) {
                            // Found a match to a seed, where the coordinates fall within the seed alignment.
                            // Add it directly to the filtered rawProteinUnfiltered...
                            Utilities.verboseLog(localVerboseLevel,"found match to a seed - candidateMatch and seedMatch: " + candidateMatch);
                            filteredMatches.addMatch(candidateMatch);
                            seedMatches.add(candidateMatch);
                        }
                    }
                }
            }
        }

        // Then iterate over the non-seed raw rawProteinUnfiltered, sorted in order ievalue ASC score DESC
        final Set<PfamHmmer3RawMatch> unfilteredByEvalue = new TreeSet<PfamHmmer3RawMatch>(rawProteinUnfiltered.getMatches());

        Utilities.verboseLog(localVerboseLevel,"unfilteredByEvalue count: " + unfilteredByEvalue.size());
        Utilities.verboseLog(localVerboseLevel,"unfilteredByEvalue: " + unfilteredByEvalue);
        for (final RawMatch rawMatch : unfilteredByEvalue) {
            final PfamHmmer3RawMatch candidateMatch = (PfamHmmer3RawMatch) rawMatch;
            Utilities.verboseLog(localVerboseLevel,"consider match - candidateMatch: " + candidateMatch);
            if (!seedMatches.contains(candidateMatch)) {
                final PfamClan candidateMatchClan = clanData.getClanByModelAccession(candidateMatch.getModelId());

                boolean passes = true;   // Optimistic algorithm!
                Utilities.verboseLog(localVerboseLevel,"candidateMatchClan: " + candidateMatchClan);
                if (candidateMatchClan != null) {
                    // Iterate over the filtered rawProteinUnfiltered (so far) to check for passes
                    for (final PfamHmmer3RawMatch match : filteredMatches.getMatches()) {
                        final PfamClan passedMatchClan = clanData.getClanByModelAccession(match.getModelId());
                        // Are both the candidate and the passedMatch in the same clan?
                        if (candidateMatchClan.equals(passedMatchClan)) {
                            // Both in the same clan, so check for overlap.  If they overlap
                            // and are NOT nested, then set passes to false and break out of the inner for loop.
                            if (matchesOverlap(candidateMatch, match)) {
                                if (!matchesAreNested(candidateMatch, match)) {
                                    passes = false;
                                    break;  // out of loop over filtered rawProteinUnfiltered.
                                } else {
                                    Utilities.verboseLog(localVerboseLevel,"nested match: candidateMatch - " + candidateMatch
                                            + " other match:- " + match);
                                }
                            }
                        }
                    }
                }

                if (passes) {
                    // Add filtered match to collection
                    filteredMatches.addMatch(candidateMatch);
                }
            }
        }


//        Utilities.verboseLog(1100, "nestedModelsMap count:" + nestedModelsMap.keySet().size());
//        for (String testKey : nestedModelsMap.keySet()) {
//            if (testKey.contains("PF01193")) {
//                Utilities.verboseLog(1100, "testKey: " + testKey + " ne models: " + nestedModelsMap.get(testKey).toString());
//            }
//        }
        Utilities.verboseLog(localVerboseLevel,"The matches found so far: ");
        for (PfamHmmer3RawMatch pfamHmmer3RawMatch : filteredMatches.getMatches()) {
            Utilities.verboseLog(localVerboseLevel,pfamHmmer3RawMatch.getModelId() + " [" +
                    pfamHmmer3RawMatch.getLocationStart() + "-" + pfamHmmer3RawMatch.getLocationEnd() + "]");
        }
        Utilities.verboseLog(localVerboseLevel,"  --ooo--- ");
        for (PfamHmmer3RawMatch pfamHmmer3RawMatch : filteredMatches.getMatches()) {
            String modelId = pfamHmmer3RawMatch.getModelId();
            Utilities.verboseLog(localVerboseLevel,"ModelId to consider: " + modelId + " region: [" +
                    pfamHmmer3RawMatch.getLocationStart() + "-" + pfamHmmer3RawMatch.getLocationEnd() + "]");

            Set<String> nestedModels = nestedModelsMap.get(modelId);
            Utilities.verboseLog(localVerboseLevel,"nestedModels: " + nestedModels);
            if (nestedModels != null && ! nestedModels.isEmpty()) {
                final UUID splitGroup = UUID.randomUUID();
                pfamHmmer3RawMatch.setSplitGroup(splitGroup);
                //get new regions
                List<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> locationFragments = new ArrayList<>();
                int nestedFragments = 0;
                for (PfamHmmer3RawMatch rawMatch : filteredMatches.getMatches()) {
                    if (nestedModels.contains(rawMatch.getModelId()) &&
                            (matchesOverlap(rawMatch, pfamHmmer3RawMatch))) {
                        locationFragments.add(new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(
                                rawMatch.getLocationStart(), rawMatch.getLocationEnd()));
                        nestedFragments ++;
                    }
                }
                Utilities.verboseLog(localVerboseLevel,"locationFragments to consider:  (# " + nestedFragments + ")" + locationFragments.toString());
                //the following is for testing only should be removed in the main code later
//                locationFragments.add(new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(
//                        380, 395));
                //sort these according to the start and stop positions
                Collections.sort(locationFragments);

                DCStatus fragmentDCStatus = DCStatus.CONTINUOUS;

                List<PfamHmmer3RawMatch> rawDiscontinuousMatches  = new ArrayList<>();
                rawDiscontinuousMatches.add(pfamHmmer3RawMatch);
                if (nestedFragments > 1){
                    Utilities.verboseLog(localVerboseLevel,"nestedFragments > 1 require special investigation ");
                }
                for (Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment fragment : locationFragments) {
                    List<PfamHmmer3RawMatch> newMatchesFromFragment  = new ArrayList<>();
                    for (PfamHmmer3RawMatch rawDiscontinuousMatch: rawDiscontinuousMatches) {
                        Utilities.verboseLog(localVerboseLevel,"rawDiscontinuousMatch to consider: " + rawDiscontinuousMatch.toString());
                        int newLocationStart = rawDiscontinuousMatch.getLocationStart();
                        int newLocationEnd = rawDiscontinuousMatch.getLocationEnd();
                        int finalLocationEnd = rawDiscontinuousMatch.getLocationEnd();
                        if (! regionsOverlap(newLocationStart,newLocationEnd, fragment.getStart(), fragment.getEnd())){
                            newMatchesFromFragment.add(rawDiscontinuousMatch);  // we add this match as previously processed
                            continue;
                        }
                        if (fragment.getStart() <= newLocationStart && fragment.getEnd() >= newLocationEnd){
                            fragmentDCStatus = DCStatus.NC_TERMINAL_DISC;
                            rawDiscontinuousMatch.setLocFragmentDCStatus(fragmentDCStatus.getSymbol());
                            newMatchesFromFragment.add(rawDiscontinuousMatch);
                            continue;
                        }

//                        if (fragment.getStart() < newLocationStart) {
//                            newLocationStart =
//                        }
//                        if (fragment.getEnd() > newLocationEnd) {
//                            newLocationStart =
//                        }

                        if(fragmentDCStatus ==  DCStatus.CONTINUOUS){
                            fragmentDCStatus = null;
                        }
                        boolean twoAtualRegions = false;
                        Utilities.verboseLog(localVerboseLevel,"region to consider: " + fragment.toString());
                        if (fragment.getStart() <= newLocationStart) {
                            newLocationStart = fragment.getEnd() + 1;
                            fragmentDCStatus = DCStatus.N_TERMINAL_DISC;
                        } else if (fragment.getEnd() >= newLocationEnd) {
                            newLocationEnd = fragment.getStart() - 1;
                            fragmentDCStatus = DCStatus.getNewDCStatus(fragmentDCStatus, DCStatus.C_TERMINAL_DISC);
                        } else if (fragment.getStart() > newLocationStart && fragment.getEnd() < newLocationEnd) {
                            //we have two new fragments
                            newLocationEnd = fragment.getStart() - 1;
                            twoAtualRegions = true;
                            fragmentDCStatus = DCStatus.getNewDCStatus(fragmentDCStatus,  DCStatus.C_TERMINAL_DISC);
                        }
                        Utilities.verboseLog(localVerboseLevel,"New Region: " + newLocationStart + "-" + newLocationEnd);
                        PfamHmmer3RawMatch pfMatchRegionOne = getTempPfamHmmer3RawMatch(pfamHmmer3RawMatch, newLocationStart, newLocationEnd, fragmentDCStatus);
                        pfMatchRegionOne.setSplitGroup(splitGroup);
                        pfMatchRegionOne.setLocFragmentDCStatus(fragmentDCStatus.getSymbol());
                        newMatchesFromFragment.add(pfMatchRegionOne);
                        newLocationStart = fragment.getEnd() + 1;
                        Utilities.verboseLog(localVerboseLevel," New Match for Region One  :" + pfMatchRegionOne.toString());
                        if (twoAtualRegions) {
                            //deal with final region
                            fragmentDCStatus = DCStatus.N_TERMINAL_DISC;
                            Utilities.verboseLog(localVerboseLevel,"The Last new Region: " + newLocationStart + "-" + finalLocationEnd);
                            PfamHmmer3RawMatch pfMatchRegionTwo = getTempPfamHmmer3RawMatch(pfamHmmer3RawMatch, newLocationStart, finalLocationEnd, fragmentDCStatus);
                            pfMatchRegionTwo.setSplitGroup(splitGroup);
                            pfMatchRegionTwo.setLocFragmentDCStatus(fragmentDCStatus.getSymbol());
                            newMatchesFromFragment.add(pfMatchRegionTwo);
                            Utilities.verboseLog(localVerboseLevel," New Match for Region Two :" + pfMatchRegionTwo.toString());
                        }
                    }
                    rawDiscontinuousMatches = newMatchesFromFragment;
                }
                //now add the processed discontinuous matches for further post processing or filtering into actual matches
                for (PfamHmmer3RawMatch rawDiscontinuousMatch: rawDiscontinuousMatches) {
                    int matchLength = rawDiscontinuousMatch.getLocationEnd() - rawDiscontinuousMatch.getLocationStart() + 1;
                    if (matchLength >= this.getMinMatchLength()) {
                        filteredRawProtein.addMatch(rawDiscontinuousMatch);
                    }
                }
            } else if (pfamHmmer3RawMatch.getLocationEnd() - pfamHmmer3RawMatch.getLocationStart() + 1 >= this.getMinMatchLength()) {
                filteredRawProtein.addMatch(pfamHmmer3RawMatch);
            }
        }

        //return filteredMatches;
        return filteredRawProtein;
    }

    /**
     * Determines if two domains overlap.
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches overlap.
     */
    boolean matchesOverlap(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        return Math.max(one.getLocationStart(),two.getLocationStart()) <= Math.min(one.getLocationEnd(),two.getLocationEnd());
//        return !
//                ((one.getLocationStart() > two.getLocationEnd()) ||
//                        (two.getLocationStart() > one.getLocationEnd()));
    }

    boolean regionsOverlap(int startRegionOne, int endRegionOne, int startRegionTwo, int endRegionTwo) {
        boolean regionsOverlap = false;
        return Math.max(startRegionOne,startRegionTwo) <= Math.min(endRegionOne,endRegionTwo);

    }

    /**
     * Determines if two Pfam families are nested (in either direction)
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches are nested.
     */
    boolean matchesAreNested(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        PfamModel oneModel = clanData.getModelByModelAccession(one.getModelId());
        PfamModel twoModel = clanData.getModelByModelAccession(two.getModelId());

        return !(oneModel == null || twoModel == null) &&
                (oneModel.isNestedIn(twoModel) || twoModel.isNestedIn(oneModel));

    }

    private PfamHmmer3RawMatch getTempPfamHmmer3RawMatch(PfamHmmer3RawMatch rawMatch, int start, int end, DCStatus dcStatus) {
        final PfamHmmer3RawMatch match = new PfamHmmer3RawMatch(
                rawMatch.getSequenceIdentifier(),
                rawMatch.getModelId(),
                rawMatch.getSignatureLibrary(),
                rawMatch.getSignatureLibraryRelease(),
                start,
                end,
                rawMatch.getEvalue(),
                rawMatch.getScore(),
                rawMatch.getHmmStart(),
                rawMatch.getHmmEnd(),
                rawMatch.getHmmBounds(),
                rawMatch.getScore(),
                rawMatch.getEnvelopeStart(),
                rawMatch.getEnvelopeEnd(),
                rawMatch.getExpectedAccuracy(),
                rawMatch.getFullSequenceBias(),
                rawMatch.getDomainCeValue(),
                rawMatch.getDomainIeValue(),
                rawMatch.getDomainBias()
        );
        match.setLocFragmentDCStatus(dcStatus.getSymbol());

        return match;
    }


    public Map<String, Set<String>> getPfamHmmData() throws IOException {
        LOGGER.debug("Starting to parse hmm data file.");
        Utilities.verboseLog(1100, "Starting to parse hmm data file -- " + pfamHmmDataPath);
        Map<String, String> domainNameToAccesstion = new HashMap<>();
        Map<String, Set<String>> pfamHmmData = new HashMap<>();
        BufferedReader reader = null;
        try {
            String accession = null, identifier = null, name = null, clan = null, description = null;
            Set<String> nestedDomains = new HashSet<>();
            Integer length = null;
            StringBuffer modelBuf = new StringBuffer();
            //reader = new BufferedReader(new InputStreamReader(new FileInputStream(domTblOutputFileName)));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(pfamHmmDataPath)));
            int lineNumber = 0;
            String line;
            //
//            # STOCKHOLM 1.0
//            #=GF ID   7tm_1
//            #=GF AC   PF00001.20
//            #=GF DE   7 transmembrane receptor (rhodopsin family)
//            #=GF GA   23.80; 23.80;
//            #=GF TP   Family
//            #=GF ML   268
//            #=GF NE   HTH_Tnp_Tc3_2
//            #=GF NE   DDE_Tnp_4
//            #=GF CL   CL0192


            String[] gfLineCases = {"#=GF ID", "#=GF AC", "#=GF NE", "#=GF CL", "//"};

            while ((line = reader.readLine()) != null) {
                if (lineNumber++ % 10000 == 0) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Parsed " + lineNumber + " lines of the HMM file.");
                        LOGGER.debug("Parsed " + " domething here ????" + " signatures.");
                    }
                    Utilities.verboseLog(1100, "Parsed " + lineNumber + " lines .. at line :" + line);
                }

                line = line.trim();
                // Load the model line by line into a temporary buffer.
                // TODO - won't break anything, but needs some work.  Need to grab the hmm file header first!
                modelBuf.append(line);
                modelBuf.append('\n');
                // Speed things up a LOT - there are lots of lines we are not
                // interested in parsing, so just check the first char of each line

                if (line.length() > 0) {
                    int i;
                    for (i = 0; i < gfLineCases.length; i++) {
                        if (line.startsWith(gfLineCases[i])) {
                            break;
                        }
                    }

                    if (line.split("\\s+").length >= 3) {
                        String value = line.split("\\s+")[2];

//                        Utilities.verboseLog(1100, "accession: " + accession + " id:" +  name + " nestedDomains: "
//                                + nestedDomains +  " case: " + i + " lineL " + line);

                        switch (i) {
                            case 0: //"#=GF ID"
                                if (name == null) {
                                    name = value;
                                }
                                break;
                            case 1: //""#=GF AC"
                                if (accession == null) {
                                    accession = value.split("[ .]")[0];
                                }
                                break;
                            case 2: //"#=GF NE"
                                String domainName = value;
                                nestedDomains.add(domainName);
                                break;
                            case 3: //"#=GF CL"
                                if (clan == null) {
                                    clan = value;
                                }
                                break;
                            case 4: //"//"
                                //we shouldnt get here
                                // Looks like an end of record marker - just to check:
                                if (accession != null) {
                                    domainNameToAccesstion.put(name, accession);
                                    pfamHmmData.put(accession, nestedDomains);

                                }
                                accession = null;
                                name = null;
                                description = null;
                                length = null;
                                nestedDomains = new HashSet<>();
                                if (accession.contains("PF01193")) {
                                    Utilities.verboseLog(1100, "accession (PF01193): " + accession + " ne domains : " + nestedDomains);
                                }
                                break;
                        }
                    } else {
                        //this is a special line like end of record marker
                        if (line.startsWith("//")) {
                            // Looks like an end of record marker - just to check:
                            if (accession != null) {
                                domainNameToAccesstion.put(name, accession);
                                pfamHmmData.put(accession, nestedDomains);
                                if (accession.contains("PF01193")) {
                                    Utilities.verboseLog(1100, "accession (PF01193): " + accession + " ne domains : " + nestedDomains);
                                }
                            }
                            accession = null;
                            name = null;
                            description = null;
                            length = null;
                            nestedDomains = new HashSet<>();
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        Utilities.verboseLog(1100, "pfamHmmData #: " + pfamHmmData.keySet().size());
        Map<String, Set<String>> altPfamHmmData = new HashMap<>();
        Iterator it = pfamHmmData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String accession = (String) pair.getKey();
            Set<String> domainAccessions = new HashSet<>();
            Set<String> nestedDomains = (Set<String>) pair.getValue();
            for (String domainName : nestedDomains) {
                String acc = domainNameToAccesstion.get(domainName);
                domainAccessions.add(acc);
            }
            altPfamHmmData.put(accession, domainAccessions);
        }
        return altPfamHmmData;
    }
}
