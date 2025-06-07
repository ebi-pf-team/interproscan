package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer3;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer2.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class PirsfPostProcessor implements Serializable {

    private PirsfDatFileParser datFileParser;
    private Map<String, PirsfDatRecord> datRecords;

    final private double MIN_LENGTH_RATIO = 0.67;
    final private double MIN_OVERLAP = 0.8;
    final private double LENGTH_DEVIATION = 3.5;
    final private double MAX_LENGTH_DEVIATION = 50;

    @Required
    public void setDatFileParser(PirsfDatFileParser datFileParser) {
        this.datFileParser = datFileParser;
    }

    public Map<String, RawProtein<PirsfHmmer3RawMatch>> process(Map<String, RawProtein<PirsfHmmer3RawMatch>> proteinIdToRawMatchMap) throws IOException {
        if (datRecords == null) {
            datRecords = datFileParser.getRecords();
        }

        final Map<String, String> subfamilies = new HashMap<>();
        for (PirsfDatRecord record : datRecords.values()) {
            String parent = record.getModelAccession();
            for (String child: record.getSubfamilies()) {
                subfamilies.put(child, parent);
            }
        }

        final Map<String, RawProtein<PirsfHmmer3RawMatch>> proteinIdToRawProteinMap = new HashMap<>();
        if (proteinIdToRawMatchMap == null) {
            return proteinIdToRawProteinMap;
        }

        for (String proteinId : proteinIdToRawMatchMap.keySet()) {
            RawProtein<PirsfHmmer3RawMatch> rawProtein = proteinIdToRawMatchMap.get(proteinId);
            RawProtein<PirsfHmmer3RawMatch> filteredProtein = processProtein(rawProtein, subfamilies);
            proteinIdToRawProteinMap.put(proteinId, filteredProtein);
        }

        return proteinIdToRawProteinMap;
    }

    private RawProtein<PirsfHmmer3RawMatch> processProtein(final RawProtein<PirsfHmmer3RawMatch> rawProtein, final Map<String, String> subfamilies) {
        // Group matches by model
        Map<String, List<PirsfHmmer3RawMatch>> models = new HashMap<>();
        for (PirsfHmmer3RawMatch match : rawProtein.getMatches()) {
            if (!match.isSignificant())
                continue;
            else if (!models.containsKey(match.getModelId())) {
                models.put(match.getModelId(), new ArrayList<>());
            }
            models.get(match.getModelId()).add(match);
        }

        Set<PirsfHmmer3RawMatch> familyMatches = new HashSet<>();
        Set<PirsfHmmer3RawMatch> subfamilyMatches = new HashSet<>();

        // Merge multiple domains into one
        for (String modelId : models.keySet()) {
            int aliStart = Integer.MAX_VALUE;
            int aliEnd = Integer.MIN_VALUE;
            int hmmStart = Integer.MAX_VALUE;
            int hmmEnd = Integer.MIN_VALUE;
            int envStart = 0;
            int envEnd = 0;
            double score = 0;
            List<PirsfHmmer3RawMatch> matches = models.get(modelId);
            for (PirsfHmmer3RawMatch match : matches) {
                score += match.getLocationScore();
                if (match.getLocationStart() < aliStart && match.getHmmStart() < hmmStart) {
                    aliStart = match.getLocationStart();
                    hmmStart = match.getHmmStart();
                    envStart = match.getEnvelopeStart();
                }
                if (match.getLocationEnd() > aliEnd && match.getHmmEnd() > hmmEnd) {
                    aliEnd = match.getLocationEnd();
                    hmmEnd = match.getHmmEnd();
                    envEnd = match.getEnvelopeEnd();
                }
            }

            if (aliStart == Integer.MAX_VALUE) {
                continue;
            }

            PirsfHmmer3RawMatch match = createMatch(matches.get(0), aliStart, aliEnd, hmmStart, hmmEnd, envStart, envEnd, score);
            if (subfamilies.containsKey(match.getModelId())) {
                subfamilyMatches.add(match);
            } else {
                familyMatches.add(match);
            }
        }

        // Filter family matches
        Set<PirsfHmmer3RawMatch> filteredMatches = new HashSet<>();
        for (PirsfHmmer3RawMatch match : familyMatches) {
            PirsfDatRecord datRecord = datRecords.get(match.getModelId());

            double ovl = (double) (match.getLocationEnd() - match.getLocationStart() + 1) / match.getSequenceLength();
            double ld = Math.abs(match.getSequenceLength() - datRecord.getMeanSeqLen());
            double r = (double) (match.getHmmEnd() - match.getHmmStart() + 1) / (match.getLocationEnd() - match.getLocationStart() + 1);

            if (r > MIN_LENGTH_RATIO
                    && ovl >= MIN_OVERLAP
                    && match.getScore() >= datRecord.getMinScore()
                    && (ld < LENGTH_DEVIATION * datRecord.getStdDevSeqLen() || ld < MAX_LENGTH_DEVIATION)) {
                filteredMatches.add(match);
            }
        }

        // Select best family match
        PirsfHmmer3RawMatch familyMatch = null;
        for (PirsfHmmer3RawMatch match : filteredMatches) {
            if (familyMatch == null || match.getScore() > familyMatch.getScore()) {
                familyMatch = match;
            }
        }

        // Filter subfamily matches and select the best
        filteredMatches.clear();
        for (PirsfHmmer3RawMatch match : subfamilyMatches) {
            PirsfDatRecord datRecord = datRecords.get(match.getModelId());
            String parent = subfamilies.get(match.getModelId());

            if (familyMatch != null && !familyMatch.getModelId().equals(parent)) {
                // Only accept matches from subfamilies that belong to the best family
                continue;
            }

            PirsfHmmer3RawMatch parentMatch = null;
            for (PirsfHmmer3RawMatch match2 : familyMatches) {
                if (match2.getModelId().equals(parent)) {
                    parentMatch = match2;
                    break;
                }
            }

            if (parentMatch == null) {
                // Ignore matches from subfamilies whose parent family doesn't match the sequence
                continue;
            }

            double r = (double) (match.getHmmEnd() - match.getHmmStart() + 1) / (match.getLocationEnd() - match.getLocationStart() + 1);

            if (r > MIN_LENGTH_RATIO && match.getScore() >= datRecord.getMinScore()) {
                filteredMatches.add(match);
            }
        }

        // Select best subfamily match
        PirsfHmmer3RawMatch subfamilyMatch = null;
        for (PirsfHmmer3RawMatch match : filteredMatches) {
            if (subfamilyMatch == null || match.getScore() > subfamilyMatch.getScore()) {
                subfamilyMatch = match;
            }
        }

        if (familyMatch == null && subfamilyMatch != null) {
            // Promote parent
            String parent = subfamilies.get(subfamilyMatch.getModelId());
            for (PirsfHmmer3RawMatch match : familyMatches) {
                if (match.getModelId().equals(parent)) {
                    familyMatch = match;
                    break;
                }
            }
        }

        RawProtein<PirsfHmmer3RawMatch> filteredProtein = new RawProtein<PirsfHmmer3RawMatch>(rawProtein.getProteinIdentifier());

        if (familyMatch != null) {
            filteredProtein.addMatch(familyMatch);

            if (subfamilyMatch != null) {
                filteredProtein.addMatch(subfamilyMatch);
            }
        }

        return filteredProtein;
    }

    private PirsfHmmer3RawMatch createMatch(PirsfHmmer3RawMatch rawMatch,
                                            int aliStart, int aliEnd,
                                            int hmmStart, int hmmEnd,
                                            int envStart, int envEnd,
                                            double score) {
        String hmmBoundStart = hmmStart == 1 ? "[" : ".";
        String hmmBoundEnd= hmmEnd == rawMatch.getModelLength() ? "]" : ".";
        PirsfHmmer3RawMatch match = new PirsfHmmer3RawMatch(
                rawMatch.getSequenceIdentifier(),
                rawMatch.getModelId(),
                rawMatch.getSignatureLibrary(),
                rawMatch.getSignatureLibraryRelease(),
                aliStart,
                aliEnd,
                rawMatch.getEvalue(),
                score,
                hmmStart,
                hmmEnd,
                hmmBoundStart + hmmBoundEnd,
                score,
                envStart,
                envEnd,
                rawMatch.getExpectedAccuracy(),
                rawMatch.getFullSequenceBias(),
                0,
                rawMatch.getEvalue(),
                rawMatch.getFullSequenceBias(),
                true
        );
        match.setModelLength(rawMatch.getModelLength());
        match.setSequenceLength(rawMatch.getSequenceLength());
        return match;
    }
}
