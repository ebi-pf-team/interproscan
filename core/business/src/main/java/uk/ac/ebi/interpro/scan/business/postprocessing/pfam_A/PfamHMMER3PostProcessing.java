package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import uk.ac.ebi.interpro.scan.model.DCStatus;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.*;
import java.util.*;

/**
 * This class performs post processing of HMMER3 output for
 * Pfam.
 *
 * @author Phil Jones
 * @author Matthias Blum
 * @version $Id: PfamHMMER3PostProcessing.java,v 1.10 2009/11/09 13:35:50 craigm Exp $
 * @since 1.0
 */
public class PfamHMMER3PostProcessing implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(PfamHMMER3PostProcessing.class.getName());

    private Map<String, Set<String>> nestedModels;

    private String pfamDatPath;

    private int minMatchLength;

    @Required
    public void setPfamDatPath(String pfamDatPath) {
        this.pfamDatPath = pfamDatPath;
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
        if (nestedModels == null) {
            nestedModels = getNestedModels();
        }
        final Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<PfamHmmer3RawMatch>>();
        if (proteinIdToRawMatchMap == null) {
            return proteinIdToRawProteinMap;
        }

        for (String proteinId : proteinIdToRawMatchMap.keySet()) {
            proteinIdToRawProteinMap.put(proteinId, processProtein(proteinIdToRawMatchMap.get(proteinId)));
        }
        return proteinIdToRawProteinMap;
    }

    /**
     * Implementation of Rob Finn's algorithm for post processing, translated from Perl to Java.
     * <p/>
     * Also includes additional code to ensure seed alignments are included as matches, regardless of
     * score.
     *
     * @param rawProtein being a List of the raw matches to filter
     * @return a List of filtered matches.
     */
    private RawProtein<PfamHmmer3RawMatch> processProtein(final RawProtein<PfamHmmer3RawMatch> rawProtein) {
        // Sort matches
        final Set<PfamHmmer3RawMatch> rawMatches = new TreeSet<PfamHmmer3RawMatch>(rawProtein.getMatches());
        Set<PfamHmmer3RawMatch> filteredMatches = new HashSet<>();
        for (PfamHmmer3RawMatch candidateMatch : rawMatches) {
            if ((candidateMatch.getLocationEnd() - candidateMatch.getLocationStart() + 1) < this.getMinMatchLength()) {
                continue;
            }

            boolean passes = true;

            // Compare the candidate match with matches already selected
            for (PfamHmmer3RawMatch filteredMatch : filteredMatches) {
                if (matchesOverlap(candidateMatch, filteredMatch)) {
                    // Matches are overlapping

                    if (!matchesAreEnclosed(candidateMatch, filteredMatch)
                            || !matchesAreNested(candidateMatch, filteredMatch)) {
                        passes = false;
                        break;
                    }
                }
            }

            if (passes) {
                filteredMatches.add(candidateMatch);
            }
        }

        RawProtein<PfamHmmer3RawMatch> filteredProtein = new RawProtein<>(rawProtein.getProteinIdentifier());
        for (PfamHmmer3RawMatch match : filteredMatches) {
            Set<String> nestedInMatch = nestedModels.get(match.getModelId());
            if (nestedInMatch != null && !nestedInMatch.isEmpty()) {
                // Find all matches that are nested in the current match

                List<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> fragments = new ArrayList<>();
                for (PfamHmmer3RawMatch otherMatch: filteredMatches) {
                    if (nestedInMatch.contains(otherMatch.getModelId())
                            && matchesAreEnclosed(match, otherMatch)) {
                        fragments.add(new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(
                                otherMatch.getLocationStart(), otherMatch.getLocationEnd()));
                    }
                }

                Collections.sort(fragments);

                int start = match.getLocationStart();
                List<PfamHmmer3RawMatch> tmpMatches = new ArrayList<>();

                UUID splitGroup = UUID.randomUUID();

                for (Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment fragment : fragments) {
                    if (fragment.getStart() > start &&  fragment.getEnd() < match.getLocationEnd()) {
                        DCStatus status = tmpMatches.isEmpty() ? DCStatus.C_TERMINAL_DISC : DCStatus.NC_TERMINAL_DISC;
                        PfamHmmer3RawMatch tmpMatch = createNewMatch(match, start, fragment.getStart() - 1, status);
                        tmpMatch.setSplitGroup(splitGroup);
                        tmpMatches.add(tmpMatch);
                        start = fragment.getEnd() + 1;
                    }
                }

                if (tmpMatches.isEmpty()) {
                    tmpMatches.add(match);
                } else {
                    PfamHmmer3RawMatch tmpMatch = createNewMatch(match, start, match.getLocationEnd(), DCStatus.N_TERMINAL_DISC);
                    tmpMatch.setSplitGroup(splitGroup);
                    tmpMatches.add(tmpMatch);
                }

                for (PfamHmmer3RawMatch tmpMatch: tmpMatches) {
                    filteredProtein.addMatch(tmpMatch);
                }
            } else {
                filteredProtein.addMatch(match);
            }
        }

        return filteredProtein;
    }

    /**
     * Determines if two domains overlap.
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches overlap.
     */
    boolean matchesOverlap(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        return regionsOverlap(one.getLocationStart(), one.getLocationEnd(), two.getLocationStart(), two.getLocationEnd());
    }

    boolean regionsOverlap(int startRegionOne, int endRegionOne, int startRegionTwo, int endRegionTwo) {
        return Math.max(startRegionOne, startRegionTwo) <= Math.min(endRegionOne,endRegionTwo);
    }

    /**
     * Determines if two Pfam families are nested (in either direction)
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches are nested.
     */
    boolean matchesAreNested(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        Set<String> nestedInOne = nestedModels.get(one.getModelId());
        Set<String> nestedInTwo = nestedModels.get(two.getModelId());

        return ((nestedInOne != null &&nestedInOne.contains(two.getModelId()))
                || (nestedInTwo != null && nestedInTwo.contains(one.getModelId())));
    }

    boolean matchesAreEnclosed(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        return ((one.getLocationStart() <= two.getLocationStart()
                && one.getLocationEnd() >= two.getLocationEnd())
                || (two.getLocationStart() <= one.getLocationStart()
                && two.getLocationEnd() >= one.getLocationEnd()));
    }

    private PfamHmmer3RawMatch createNewMatch(PfamHmmer3RawMatch rawMatch, int start, int end, DCStatus dcStatus) {
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

    public Map<String, Set<String>> getNestedModels() throws IOException {
        Map<String, String> nameToAccession = new HashMap<>();
        Map<String, Set<String>> modelToNested = new HashMap<>();
        BufferedReader reader = null;

        /*
        # STOCKHOLM 1.0
        #=GF ID   ZZ
        #=GF AC   PF00569.23
        #=GF DE   Zinc finger, ZZ type
        #=GF GA   21.4; 21.4;
        #=GF TP   Domain
        #=GF ML   45
        #=GF CL   CL0229
        //
         */

        try {
            String accession = null, name = null;
            Set<String> nestedDomains = new HashSet<>();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(pfamDatPath)));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    if (line.startsWith("#=GF ID")) {
                        name = line.split("\\s+")[2];
                    } else if (line.startsWith("#=GF AC")) {
                        // Pfam accession has the version, e.g. PF00569.23
                        accession = line.split("\\s+")[2].split("\\.")[0];
                    } else if (line.startsWith("#=GF NE")) {
                        String nestedDomain = line.split("\\s+")[2];
                        nestedDomains.add(nestedDomain);
                    } else if (line.startsWith("//")) {
                        if (accession != null) {
                            nameToAccession.put(name, accession);
                            modelToNested.put(accession, nestedDomains);
                        }
                        accession = null;
                        name = null;
                        nestedDomains = new HashSet<>();
                    };
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        Map<String, Set<String>> pfamData = new HashMap<>();
        for (String accession: modelToNested.keySet()) {
            Set<String> nestedDomainNames = modelToNested.get(accession);
            Set<String> nestedDomainAccessions = new HashSet<>();
            for (String nestedDomainName: nestedDomainNames) {
                String nestedDomainAccession = nameToAccession.get(nestedDomainName);
                nestedDomainAccessions.add(nestedDomainAccession);
            }
            pfamData.put(accession, nestedDomainAccessions);
        }
        return pfamData;
    }
}
