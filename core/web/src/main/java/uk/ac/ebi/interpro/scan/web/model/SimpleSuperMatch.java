package uk.ac.ebi.interpro.scan.web.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + entries.hashCode();
        return result;
    }
}
