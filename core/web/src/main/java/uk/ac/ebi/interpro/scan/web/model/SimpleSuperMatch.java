package uk.ac.ebi.interpro.scan.web.model;


import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import java.util.*;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 13:44
 *         <p/>
 *         Captures super matches that are displayed on the protein overview tracks
 */
public class SimpleSuperMatch implements Comparable<SimpleSuperMatch> {

    private String type;

    private SimpleLocation location;

    /**
     * Deliberately not using TreeSet - the ordering is not important
     * and the Comparator for SimpleEntry is expensive.
     */
    private final Set<SimpleEntry> entries = new HashSet<SimpleEntry>();

    public SimpleSuperMatch(SimpleEntry firstEntry, SimpleLocation location) {
        if (location == null || firstEntry == null) {
            throw new IllegalArgumentException("The type, location and firstEntry are all mandatory when constructing a SimpleSuperMatch object.");
        }
        this.type = firstEntry.getType();
        this.location = location;
        entries.add(firstEntry);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public Set<SimpleEntry> getEntries() {
        return entries;
    }

    public Set<SimpleEntry> getEntriesHierarchyOrder() {
        final Set<SimpleEntry> hierarchySortedSet = new TreeSet<SimpleEntry>(HIERARCHY_COMPARATOR);
        hierarchySortedSet.addAll(entries);
        return hierarchySortedSet;
    }

    private static final Comparator<SimpleEntry> HIERARCHY_COMPARATOR = new Comparator<SimpleEntry>() {

        public int compare(SimpleEntry e1, SimpleEntry e2) {
            if ((e1 == null || e2 == null) || (e1 == e2) || (e1.equals(e2))) {
                return 0;
            }
            EntryHierarchy eh = SimpleEntry.getEntryHierarchy();
            int comparison = eh.compareHierarchyLevels(e1, e2);
            if (comparison == 0) {
                // Siblings!
                comparison = e1.getName().compareTo(e2.getName());
            }
            if (comparison == 0) {
                comparison = e1.getAc().compareTo(e2.getAc());
            }
            return comparison;
        }

    };

    public void addEntry(SimpleEntry entry) {
        this.entries.add(entry);
    }

    public void addEntries(Collection<SimpleEntry> entries) {
        this.entries.addAll(entries);
    }

    /**
     * This is a difficult method.  For a SuperMatch, either there is only one match
     * or all of the matches contained are mapped to Entries ALL of which must be
     * in the same Hierarchy.  There is a bit of cross-product going on here though...
     * <p/>
     * To be on the safe side and to test the assumption, the first version will iterate
     * over the cross-product to make sure the answer is consistent.
     * <p/>
     * TODO - To improve performance, just compare with the first Supermatch in the collection,
     * TODO - no need to iterate over all of them.  Do this once satisfied that the method works correctly.
     *
     * @param superMatch
     * @return
     */
    public boolean inSameHierarchy(SimpleSuperMatch superMatch) {
        Boolean inSameHierarchy = null;
        for (final SimpleEntry thisEntry : entries) {
            for (final SimpleEntry thatEntry : superMatch.entries) {
                if (thisEntry != null && thatEntry != null) {
                    inSameHierarchy = SimpleEntry.getEntryHierarchy().areInSameHierarchy(thisEntry, thatEntry);
                    // If any of the Entries are in a different hierarchy - barf out straight away.
                    if (!inSameHierarchy) return false;
                }
            }
        }
        return (inSameHierarchy == null) ? false : inSameHierarchy;
    }

    public int compareTo(SimpleSuperMatch o) {
        if (this == o || this.equals(o)) {
            return 0;
        }
        return this.getLocation().compareTo(o.getLocation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleSuperMatch that = (SimpleSuperMatch) o;

        if (!entries.equals(that.entries)) return false;
        if (!location.equals(that.location)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    public SimpleEntry getFirstEntry() {
        if (entries == null || entries.size() == 0) {
            return null;
        }
        return entries.iterator().next();
    }

    /**
     * Determines if domains overlap overlap.
     *
     * @param that SimpleSuperMatch to compare with.
     * @return true if the two domain matches overlap.
     */
    public boolean matchesOverlap(SimpleSuperMatch that) {
        return !
                ((this.getLocation().getStart() > that.getLocation().getEnd()) ||
                        (that.getLocation().getStart() > this.getLocation().getEnd()));
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + entries.hashCode();
        return result;
    }

    /**
     * Merges candidate with this SuperMatch if they overlap.
     *
     * @param candidate to Merge with this SuperMatch.
     * @return true if the mergeIfOverlap criteria are met and the candidate is merged. false if
     *         the criteria are not met.
     */
    public SimpleSuperMatch mergeIfOverlap(SimpleSuperMatch candidate) {
        if (!this.type.equals(candidate.getType())) {
            return null;
        }
        if (!this.inSameHierarchy(candidate)) {
            return null;
        }
        if (!this.matchesOverlap(candidate)) {
            return null;
        }
        // Reset location to widest bounds of overlapping matches
        int start = (candidate.getLocation().getStart() < this.getLocation().getStart())
                ? candidate.getLocation().getStart()
                : this.getLocation().getStart();

        int end = (candidate.getLocation().getEnd() > this.getLocation().getEnd())
                ? candidate.getLocation().getEnd()
                : this.getLocation().getEnd();

        this.setLocation(new SimpleLocation(start, end));

        // Add entries from the candidate
        this.entries.addAll(candidate.getEntries());
        return this;
    }
}
