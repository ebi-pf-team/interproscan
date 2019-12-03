package uk.ac.ebi.interpro.scan.business.postprocessing.gene3d;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderRecord;
import uk.ac.ebi.interpro.scan.io.gene3d.DomainFinderResourceReader;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Antony Quinn
 * @author Phil Jones
 *         Date: 08/09/11
 *         Time: 14:53
 */
public class Gene3DPostProcessing implements Serializable {

    public Set<RawProtein<Gene3dHmmer3RawMatch>> filter(Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins, String ssfFilePath) {
        final Resource ssfFile = new FileSystemResource(ssfFilePath);
        // Parse DF3 results
        final DomainFinderResourceReader resourceReader = new DomainFinderResourceReader();
        final Collection<DomainFinderRecord> domainFinderRecords;
        try {
            domainFinderRecords = resourceReader.read(ssfFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        // Map of sequence ID's to the set of DomainFinderRecord's for each particular sequence
        Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap = new HashMap<>(rawProteins.size());
        for (DomainFinderRecord dfr : domainFinderRecords) {
            String id = dfr.getSequenceId();
            if (domainFinderRecordsMap.containsKey(id)) {
                domainFinderRecordsMap.get(id).add(dfr);
            }
            else {
                Set<DomainFinderRecord> newSet = new HashSet<>();
                newSet.add(dfr);
                domainFinderRecordsMap.put(id, newSet);
            }
        }

        // Update raw matches with values from DomainFinder
        return filter(rawProteins, domainFinderRecordsMap);
    }

    // Update raw matches with values from DomainFinder
    private Set<RawProtein<Gene3dHmmer3RawMatch>> filter(final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins,
                                                         final Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap) {

        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        final Set<String> matchKeys = new HashSet<>();
        for (final RawProtein<Gene3dHmmer3RawMatch> p : rawProteins) {
            final String id = p.getProteinIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> filteredProtein = new RawProtein<>(id);
            // Sort p.getMatches() by location.start so we always get consistent results if need to choose between raw matches
            List<Gene3dHmmer3RawMatch> matchList = new ArrayList<Gene3dHmmer3RawMatch>(p.getMatches());
            if (matchList.size() > 1) {
                Collections.sort(matchList, new Comparator<Gene3dHmmer3RawMatch>() {
                    public int compare(Gene3dHmmer3RawMatch record1, Gene3dHmmer3RawMatch record2) {
                        return Integer.valueOf(record1.getLocationStart()).compareTo(record2.getLocationStart());
                    }
                });
            }
            for (final Gene3dHmmer3RawMatch match : matchList) {
                if (id.equals(match.getSequenceIdentifier())) {
                    addRecord(filteredProtein, match, domainFinderRecordsMap, matchKeys);
                }
            }
            if (!filteredProtein.getMatches().isEmpty()) {
                filteredProteins.add(filteredProtein);
            }
        }
        return filteredProteins;
    }

    private void addRecord(final RawProtein<Gene3dHmmer3RawMatch> filteredProtein,
                           final Gene3dHmmer3RawMatch m,
                           final Map<String, Set<DomainFinderRecord>> domainFinderRecordsMap,
                           final Set<String> matchKeys) {
        final String mSeqId = m.getSequenceIdentifier();
        final String mModelId = m.getModelId();
        final String matchKeyPrefix = mSeqId + '-' + mModelId + '-';
        final Set<DomainFinderRecord> domainFinderRecords = domainFinderRecordsMap.get(mSeqId);
        if (domainFinderRecords != null) {
            for (final DomainFinderRecord r : domainFinderRecords) {
                // Parse segment boundaries
                String s = r.getSegmentBoundaries();
                String[] segments = s.split(DomainFinderRecord.SEGMENT_BOUNDARY_SEPARATOR);
                int lowestBoundary = Integer.valueOf(segments[0]);
                int highestBoundary = Integer.valueOf(segments[segments.length - 1]);
                // Match up the DomainFinder record with the corresponding raw match
                // Check for "greater than" or "less than" positions, because DomainFinder may have split the domain
                // match into smaller chunks.
                // In this example, the original raw match as reported by HMMER (position 4-41) is split into
                // two matches (positions 6-10 and 20-26). To marry up the raw match with the
                // filtered matches (so we can get the evalue, score ...etc),  we need to check that the raw match
                // start (4) is equal to or less than the lowest filtered match boundary (6) and
                // the end (41) is equal to or greater than the highest filtered match boundary (26).
                //                     4                                  41
                // raw match:      |---####################################--------|
                //                       6   10        20    26
                // filtered match: |-----#####---------#######---------------------|
                // ... where #=domain
                // Track matches that we've added in case there are two raw matches with the same e-value that fall within
                // the greater-than/less-than boundaries but have different start and end positions -- in these cases
                // "just take the first match" (Source: Craig McAnulla, June 2010)
                String matchKey = matchKeyPrefix + r.getSegmentBoundaries();
                if (mModelId.equals(r.getModelId()) &&
                        m.getLocationStart() <= lowestBoundary &&
                        m.getLocationEnd() >= highestBoundary &&
                        m.getDomainIeValue() == r.getDomainIeValue() &&
                        !matchKeys.contains(matchKey)) {
                    // We should never find more than one raw match
                    if (filteredProtein.getMatches().contains(m)) {
                        throw new IllegalStateException("Found duplicate filtered match: " + m);
                    }
                    // Get start and end coordinates for each split domain
                    // For example "10:20:30:40" represents two domains: (start=10,end=20) and (start=30,end=40)
                    // The domain string has already been split, so we have:
                    // segments[0]=10, ssfsegments[1]=20, segments[2]=30, segments[4]=40
                    // The even numbered array indexes contain the start position, and
                    // the odd numbered indexes contain the end position
                    int splitDomainStart = 0;
                    for (int i = 0; i < segments.length; i++) {
                        int number = Integer.valueOf(segments[i]);
                        // Even numbers (array index 0, 2, 4 ...etc) = start
                        if (i % 2 == 0) {
                            splitDomainStart = number;
                        }
                        // Odd numbers (array index 1, 3, 5 ...etc) = end
                        else {
                            // Create match for each split domain
                            if (number > splitDomainStart) {  // Exclude split domain matches where the length is 1.
                                Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                                        mSeqId,
                                        mModelId,
                                        m.getSignatureLibraryRelease(),
                                        splitDomainStart,
                                        number,
                                        m.getEvalue(),
                                        m.getScore(),
                                        m.getHmmStart(),       // TODO: What should HMM start and end be for split domains?
                                        m.getHmmEnd(),
                                        m.getHmmBounds(),
                                        m.getLocationScore(),
                                        m.getEnvelopeStart(),  // TODO: What should env start and end be for split domains?
                                        m.getEnvelopeEnd(),
                                        m.getExpectedAccuracy(),
                                        m.getFullSequenceBias(),
                                        m.getDomainCeValue(),
                                        m.getDomainIeValue(),
                                        m.getDomainBias(),
                                        m.getCigarAlignment());
                                // Add match
                                filteredProtein.addMatch(match);
                                matchKeys.add(matchKey);
                            }
                        }
                    }
                    break; // Found the DomainFinder record with the corresponding raw match
                }
            }
        }
    }
}
