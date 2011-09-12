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
        // Update raw matches with values from DomainFinder
        return filter(rawProteins, domainFinderRecords);
    }

    // Update raw matches with values from DomainFinder
    private Set<RawProtein<Gene3dHmmer3RawMatch>> filter(final Set<RawProtein<Gene3dHmmer3RawMatch>> rawProteins,
                                                         final Collection<DomainFinderRecord> domainFinderRecords) {
        final Set<RawProtein<Gene3dHmmer3RawMatch>> filteredProteins = new HashSet<RawProtein<Gene3dHmmer3RawMatch>>();
        final Set<String> matchKeys = new HashSet<String>();
        for (RawProtein<Gene3dHmmer3RawMatch> p : rawProteins) {
            String id = p.getProteinIdentifier();
            RawProtein<Gene3dHmmer3RawMatch> filteredProtein = new RawProtein<Gene3dHmmer3RawMatch>(id);
            // TODO: Sort p.getMatches() by location.start so we always get consistent results if need to choose between raw matches
            List<Gene3dHmmer3RawMatch> matchList = new ArrayList<Gene3dHmmer3RawMatch>(p.getMatches());
            Collections.sort(matchList, new Comparator<Gene3dHmmer3RawMatch>() {
                public int compare(Gene3dHmmer3RawMatch record1, Gene3dHmmer3RawMatch record2) {
                    return Integer.valueOf(record1.getLocationStart()).compareTo(record2.getLocationStart());
                }
            }
            );
            //for (Gene3dHmmer3RawMatch match : p.getMatches())   {
            for (Gene3dHmmer3RawMatch match : matchList) {
                if (id.equals(match.getSequenceIdentifier())) {
                    addRecord(filteredProtein, match, domainFinderRecords, matchKeys);
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
                           final Collection<DomainFinderRecord> domainFinderRecords,
                           final Set<String> matchKeys) {
        for (DomainFinderRecord r : domainFinderRecords) {
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
            String matchKey = new StringBuilder()
                    .append(m.getSequenceIdentifier())
                    .append('-')
                    .append(m.getModelId())
                    .append('-')
                    .append(r.getSegmentBoundaries())
                    .toString();
            if (m.getSequenceIdentifier().equals(r.getSequenceId()) &&
                    m.getModelId().equals(r.getModelId()) &&
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
                // segments[0]=10, segments[1]=20, segments[2]=30, segments[4]=40
                // The even numbered array indexes contain the start position, and
                // the odd numbered indexes contain the end position
                int splitDomainStart = 0;
                for (int i = 0; i < segments.length; i++) {
                    int number = Integer.valueOf(segments[i]);
                    int splitDomainEnd;
                    // Even numbers (array index 0, 2, 4 ...etc) = start
                    if (i % 2 == 0) {
                        splitDomainStart = number;
                    }
                    // Odd numbers (array index 1, 3, 5 ...etc) = end
                    else {
                        splitDomainEnd = number;
                        // Create match for each split domain
                        if (splitDomainEnd > splitDomainStart) {  // Exclude split domain matches where the length is 1.
                            Gene3dHmmer3RawMatch match = new Gene3dHmmer3RawMatch(
                                    m.getSequenceIdentifier(),
                                    m.getModelId(),
                                    m.getSignatureLibraryRelease(),
                                    splitDomainStart,
                                    splitDomainEnd,
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
            }
        }
    }
}
