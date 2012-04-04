package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 16:27
 *         Comprises the lines for the condensed view and is responsible for
 *         building this structure.
 */
public class CondensedView implements Serializable {

    private static final Logger LOG = Logger.getLogger(CondensedView.class.getName());

    private final SimpleProtein protein;

    private static final List<EntryType> INCLUDED_TYPES = Arrays.asList(EntryType.DOMAIN, EntryType.REPEAT);

    // The CondensedLines in this Set are ordered by their lineNumber,
    // 0 indexed.
    private Set<CondensedLine> lines;

    public CondensedView(final SimpleProtein protein) {
        this.protein = protein;
        // First of all, need to build SuperMatches.
        final List<SimpleSuperMatch> superMatches = buildSuperMatchList();

        // Second, need to build "SuperMatchBucket" objects.  This process also merges
        // matches to entries in the same hierarchy.
        final List<SuperMatchBucket> buckets = buildBuckets(superMatches);

        // Fix any SuperMatchBuckets that have overlaps within them.
        fixOverlaps(buckets);

        // Finally, add the buckets to the lines, aiming for the least number of lines possible.
        buildLines(buckets);
    }

    private void fixOverlaps(List<SuperMatchBucket> buckets) {
        List<SuperMatchBucket> newBuckets = new ArrayList<SuperMatchBucket>();
        for (SuperMatchBucket bucket : buckets) {
            newBuckets.addAll(bucket.ensureNoOverlaps());
        }
        buckets.addAll(newBuckets);
    }

    /**
     * Very dumb method - just makes "SimpleSuperMatch" objects out of SimpleEntry
     * objects - however at this point they are not Supermatches - that is the job
     * of the next method (buildBuckets).
     * <p/>
     * Note - only includes features of the types allowed in the INCLUDED_TYPES list.
     *
     * @return a List of SimpleSuperMatch objects, one for each Entry / location.
     */
    private List<SimpleSuperMatch> buildSuperMatchList() {
        final List<SimpleSuperMatch> superMatchList = new ArrayList<SimpleSuperMatch>();
        // Initially the SimpleSuperMatches are just matches - the merging occurs in the next method call.
        for (final SimpleEntry entry : protein.getAllEntries()) {
            if (INCLUDED_TYPES.contains(entry.getType())) {
                for (final SimpleLocation location : entry.getLocations()) {
                    superMatchList.add(new SimpleSuperMatch(entry, location));
                }
            }
        }

        return superMatchList;
    }

    /**
     * Iterates over the supermatches and merges / buckets them according to their
     * relationships in the hierarchy.
     *
     * @param superMatches to be merged & bucketed.
     * @return a List of SuperMatchBuckets.
     */
    private List<SuperMatchBucket> buildBuckets(final List<SimpleSuperMatch> superMatches) {
        List<SuperMatchBucket> superMatchBucketList = new ArrayList<SuperMatchBucket>();
        for (SimpleSuperMatch superMatch : superMatches) {
            boolean inList = false;
            for (final SuperMatchBucket bucket : superMatchBucketList) {
                // addIfSameHierarchyMergeIfOverlap also merges matches into supermatches.
                inList = bucket.addIfSameHierarchyMergeIfOverlap(superMatch);
                if (inList) break; // Will be only one bucket per hierarchy, so no need to go further.
            }
            if (!inList) {
                // Need a new Bucket.
                superMatchBucketList.add(new SuperMatchBucket(superMatch));
            }
        }
        return superMatchBucketList;
    }

    /**
     * Considering each bucket in turn, attempt to add the buckets to a line, minimising the
     * number of lines and attempting to add the
     *
     * @param buckets
     */
    private void buildLines(List<SuperMatchBucket> buckets) {
        //While building, don't try to sort.  This should speed things up
        // as well as prevent sort order errors due to mutable objects.
        final Set<CondensedLine> unsortedLines = new HashSet<CondensedLine>();
        for (SuperMatchBucket bucket : buckets) {
            boolean bucketFoundAHome = false;
            // Check if this bucket can be added to any existing lines
            for (CondensedLine line : unsortedLines) {  // This will give the lines in the correct order, as they are in a TreeSet.
                bucketFoundAHome = line.addSuperMatchesSameTypeWithoutOverlap(bucket);
                if (bucketFoundAHome) {
                    break; // out of the Condensed line loop - stop trying to add this bucket to any more lines.
                }
            }
            // if the bucket has still not found a line to live on, need to create a new line for it.
            if (!bucketFoundAHome) {
                unsortedLines.add(new CondensedLine(bucket));
            }
        }
        // Sort them when finished building, by placing into a TreeSet.
        lines = new TreeSet<CondensedLine>(unsortedLines);
    }

    public Set<CondensedLine> getLines() {
        return lines;
    }
}
