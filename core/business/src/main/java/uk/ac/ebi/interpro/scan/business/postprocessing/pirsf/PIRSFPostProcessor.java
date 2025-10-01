package uk.ac.ebi.interpro.scan.business.postprocessing.pirsf;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatFileParser;
import uk.ac.ebi.interpro.scan.io.pirsf.PirsfDatRecord;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Read in PIRSF raw matches, perform post-processing and persist filtered matches.
 */
public class PIRSFPostProcessor implements Serializable {

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

    public Set<RawProtein<PIRSFHmmer3RawMatch>> process(Set<RawProtein<PIRSFHmmer3RawMatch>> proteins) throws IOException {
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

        final Set<RawProtein<PIRSFHmmer3RawMatch>> filteredProteins = new HashSet<>();
        if (proteins == null) {
            return filteredProteins;
        }

        for (RawProtein<PIRSFHmmer3RawMatch> protein : proteins) {
            RawProtein<PIRSFHmmer3RawMatch> filteredProtein = processProtein(protein, subfamilies);
            filteredProteins.add(filteredProtein);
        }

        return filteredProteins;
    }

    private RawProtein<PIRSFHmmer3RawMatch> processProtein(final RawProtein<PIRSFHmmer3RawMatch> rawProtein, final Map<String, String> subfamilies) {
        // Group matches by model
        Map<String, List<PIRSFHmmer3RawMatch>> models = new HashMap<>();
        for (PIRSFHmmer3RawMatch match : rawProtein.getMatches()) {
            // System.out.printf("post-processor\tprocessing\t%s\t%s\t%d-%d\t%s\n",
            //         rawProtein.getProteinIdentifier(), match.getModelId(),
            //         match.getLocationStart(), match.getLocationEnd(), match.isSignificant() ? "yes" : "no");
            if (!match.isSignificant())
                continue;
            else if (!models.containsKey(match.getModelId())) {
                models.put(match.getModelId(), new ArrayList<>());
            }
            models.get(match.getModelId()).add(match);
        }

        Set<PIRSFHmmer3RawMatch> familyMatches = new HashSet<>();
        Set<PIRSFHmmer3RawMatch> subfamilyMatches = new HashSet<>();

        // Merge multiple domains into one
        for (String modelId : models.keySet()) {
            int aliStart = Integer.MAX_VALUE;
            int aliEnd = Integer.MIN_VALUE;
            int hmmStart = Integer.MAX_VALUE;
            int hmmEnd = Integer.MIN_VALUE;
            int envStart = 0;
            int envEnd = 0;
            double score = 0;
            List<PIRSFHmmer3RawMatch> matches = models.get(modelId);
            matches.sort(Comparator.comparingInt(PIRSFHmmer3RawMatch::getLocationStart));
            for (PIRSFHmmer3RawMatch match : matches) {
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
                // System.out.printf("post-processor\tskipped\t%s\t%s\talistart max\n", rawProtein.getProteinIdentifier(), modelId);
                continue;
            }

            PIRSFHmmer3RawMatch match = createMatch(matches.get(0), aliStart, aliEnd, hmmStart, hmmEnd, envStart, envEnd, score);
            if (subfamilies.containsKey(match.getModelId())) {
                subfamilyMatches.add(match);
            } else {
                familyMatches.add(match);
            }
        }

        // Filter family matches
        Set<PIRSFHmmer3RawMatch> filteredMatches = new HashSet<>();
        for (PIRSFHmmer3RawMatch match : familyMatches) {
            PirsfDatRecord datRecord = datRecords.get(match.getModelId());

            double ovl = (double) (match.getLocationEnd() - match.getLocationStart() + 1) / match.getSequenceLength();
            double ld = Math.abs(match.getSequenceLength() - datRecord.getMeanSeqLen());
            double r = (double) (match.getHmmEnd() - match.getHmmStart() + 1) / (match.getLocationEnd() - match.getLocationStart() + 1);

            boolean ok = false;
            if (r > MIN_LENGTH_RATIO
                    && ovl >= MIN_OVERLAP
                    && match.getScore() >= datRecord.getMinScore()
                    && (ld < LENGTH_DEVIATION * datRecord.getStdDevSeqLen() || ld < MAX_LENGTH_DEVIATION)) {
                filteredMatches.add(match);
                ok = true;
            }

            // System.out.printf("post-processor\tfamily\t%s\t%s\t%d-%d (%d-%d)\tl=%d\tr=%f, ovl=%f, %f>=%f, %f, %f\t%s\n",
            //         rawProtein.getProteinIdentifier(), match.getModelId(),
            //         match.getLocationStart(), match.getLocationEnd(),
            //         match.getHmmStart(), match.getHmmEnd(),
            //         match.getSequenceLength(),
            //         r, ovl, match.getScore(), datRecord.getMinScore(),
            //         ld, datRecord.getStdDevSeqLen(),
            //         ok ? "yes" : "no"
            // );
        }

        // Select best family match
        PIRSFHmmer3RawMatch familyMatch = null;
        for (PIRSFHmmer3RawMatch match : filteredMatches) {
            if (familyMatch == null || match.getScore() > familyMatch.getScore()) {
                familyMatch = match;
            } else {
                // System.out.printf("post-processor\tskipped\t%s\t%s\tlower score\t%f < %f\n",
                //         rawProtein.getProteinIdentifier(), match.getModelId(), match.getScore(), familyMatch.getScore());
            }
        }

        // Filter subfamily matches and select the best
        filteredMatches.clear();
        for (PIRSFHmmer3RawMatch match : subfamilyMatches) {
            PirsfDatRecord datRecord = datRecords.get(match.getModelId());
            String parent = subfamilies.get(match.getModelId());

            if (familyMatch != null && !familyMatch.getModelId().equals(parent)) {
                // Only accept matches from subfamilies that belong to the best family
                // System.out.printf("post-processor\tskipped\t%s\t%s\tnot in family\t%s\n",
                //         rawProtein.getProteinIdentifier(), match.getModelId(), familyMatch.getModelId());
                continue;
            }

            PIRSFHmmer3RawMatch parentMatch = null;
            for (PIRSFHmmer3RawMatch match2 : familyMatches) {
                if (match2.getModelId().equals(parent)) {
                    parentMatch = match2;
                    break;
                }
            }

            if (parentMatch == null) {
                // Ignore matches from subfamilies whose parent family doesn't match the sequence
                // System.out.printf("post-processor\tskipped\t%s\t%s\tparent without hit\t%s\n",
                //         rawProtein.getProteinIdentifier(), match.getModelId(), parent);
                continue;
            }

            double r = (double) (match.getHmmEnd() - match.getHmmStart() + 1) / (match.getLocationEnd() - match.getLocationStart() + 1);

            // System.out.printf("post-processor\tsubfamily\t%s\t%s\t%d-%d (%d-%d)\tl=%d\tr=%f, %f>=%f\n",
            //         rawProtein.getProteinIdentifier(), match.getModelId(),
            //         match.getLocationStart(), match.getLocationEnd(),
            //         match.getHmmStart(), match.getHmmEnd(),
            //         match.getSequenceLength(),
            //         r, match.getScore(), datRecord.getMinScore()
            // );

            if (r > MIN_LENGTH_RATIO && match.getScore() >= datRecord.getMinScore()) {
                filteredMatches.add(match);
            }
        }

        // Select best subfamily match
        PIRSFHmmer3RawMatch subfamilyMatch = null;
        for (PIRSFHmmer3RawMatch match : filteredMatches) {
            if (subfamilyMatch == null || match.getScore() > subfamilyMatch.getScore()) {
                subfamilyMatch = match;
            } else {
                // System.out.printf("post-processor\tskipped\t%s\t%s\tlower score\t%f < %f\n",
                //         rawProtein.getProteinIdentifier(), match.getModelId(), match.getScore(), subfamilyMatch.getScore());
            }
        }

        if (familyMatch == null && subfamilyMatch != null) {
            // Promote parent
            String parent = subfamilies.get(subfamilyMatch.getModelId());
            for (PIRSFHmmer3RawMatch match : familyMatches) {
                if (match.getModelId().equals(parent)) {
                    familyMatch = match;
                    break;
                }
            }
        }

        RawProtein<PIRSFHmmer3RawMatch> filteredProtein = new RawProtein<PIRSFHmmer3RawMatch>(rawProtein.getProteinIdentifier());

        if (familyMatch != null) {
            // System.out.printf("post-processor\tsaved\t%s\t%s\n", rawProtein.getProteinIdentifier(), familyMatch.getModelId());
            filteredProtein.addMatch(familyMatch);

            if (subfamilyMatch != null) {
                // System.out.printf("post-processor\tsaved\t%s\t%s\n", rawProtein.getProteinIdentifier(), subfamilyMatch.getModelId());
                filteredProtein.addMatch(subfamilyMatch);
            }
        } else {
            // System.out.printf("post-processor\tunannotated\t%s\n", rawProtein.getProteinIdentifier());
        }

        return filteredProtein;
    }

    private PIRSFHmmer3RawMatch createMatch(PIRSFHmmer3RawMatch rawMatch,
                                            int aliStart, int aliEnd,
                                            int hmmStart, int hmmEnd,
                                            int envStart, int envEnd,
                                            double score) {
        String hmmBoundStart = hmmStart == 1 ? "[" : ".";
        String hmmBoundEnd= hmmEnd == rawMatch.getModelLength() ? "]" : ".";
        PIRSFHmmer3RawMatch match = new PIRSFHmmer3RawMatch(
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
