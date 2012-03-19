package uk.ac.ebi.interpro.scan.web.model;

import uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro.GoTerm;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import java.io.Serializable;
import java.util.*;

/**
 * Contains basic information about an InterPro entry.
 *
 * @author Antony Quinn
 * @author Matthew Fraser
 * @version $Id$
 */
public final class SimpleEntry implements Comparable<SimpleEntry>, Serializable {

    private final String ac;
    private final String shortName;
    private final String name;
    private final String type;
    private Integer hierarchyLevel;
    private final EntryHierarchyData hierarchyData;
    private List<SimpleLocation> locations = new ArrayList<SimpleLocation>(); // super matches
    private Map<String, SimpleSignature> signatures = new HashMap<String, SimpleSignature>();
    private static EntryHierarchy entryHierarchy;

    private static final Object ehLock = new Object();

    public SimpleEntry(String ac, String shortName, String name, String type, final EntryHierarchy entryHierarchy) {
        this.ac = ac;
        this.shortName = shortName;
        this.name = name;
        this.type = type;

        if (entryHierarchy != null && SimpleEntry.entryHierarchy == null) {
            synchronized (ehLock) {
                if (SimpleEntry.entryHierarchy == null) {
                    SimpleEntry.entryHierarchy = entryHierarchy;
                }
            }
        }

        if (entryHierarchy != null) {
            this.hierarchyLevel = entryHierarchy.getHierarchyLevel(ac);
            this.hierarchyData = entryHierarchy.getEntryHierarchyData(ac);
        } else {
//            this.hierarchyLevel = 1;  // TODO - Is this desirable?  Current algorithm probably allows for this being null.
            this.hierarchyData = new EntryHierarchyData(ac, 1, ac);
        }
    }

    public String getAc() {
        return ac;
    }

    /**
     * Returns the hierarchy level for the entry, or null if it is not in any hierarchy.
     *
     * @return the hierarchy level for the entry, or null if it is not in any hierarchy.
     */
    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public EntryHierarchyData getHierarchyData() {
        return hierarchyData;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static EntryHierarchy getEntryHierarchy() {
        return entryHierarchy;
    }

    public List<SimpleLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<SimpleLocation> locations) {
        this.locations = locations;
    }

    public Collection<SimpleSignature> getSignatures() {
        return signatures.values();
    }

    public void setSignatures(Map<String, SimpleSignature> signatures) {
        this.signatures = signatures;
    }

    public Map<String, SimpleSignature> getSignaturesMap() {
        return signatures;
    }

    // Tautology: all entries are composed of integrated signatures. Better to *not* put integrated signatures here!!
    public boolean isIntegrated() {
        return (ac != null && !ac.equals(""));
    }

    @Override
    public int compareTo(SimpleEntry that) {

        if (this == that) {
            return 0;
        }

        // No supermatch locations (un-integrated signatures)
        if (this.ac == null || this.ac.equals("")) {
            return 1;
        } else if (that.ac == null || that.ac.equals("")) {
            return -1;
        }

        // Entry type
        final EntryType thisType = EntryType.parseName(this.getType());
        final EntryType thatType = EntryType.parseName(that.getType());
        if (thisType != null && thatType != null) {
            final int compare = thisType.compareTo(thatType);
            if (compare != 0) {
                return compare;
            }
        } else if (thisType != null) {
            return -1;
        } else if (thatType != null) {
            return 1;
        }

        // Order by entry accession whilst considering if the entries are in the same hierarchy
        if (!this.ac.equals(that.ac)) {
            if (SimpleEntry.entryHierarchy.areInSameHierarchy(this.ac, that.ac)) {
                // Sort based upon level in hierarchy
                final int hierarchyComparison = SimpleEntry.entryHierarchy.compareHierarchyLevels(this, that);
                if (hierarchyComparison != 0) {
                    return hierarchyComparison;
                }
            }
        }


        return Collections.min(this.locations).compareTo(Collections.min(that.locations));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimpleEntry))
            return false;
        return this.ac.equals(((SimpleEntry) o).ac);
    }

    @Override
    public String toString() {
        return ac;
    }

    public List<GoTerm> getGoTerms() {
        List<GoTerm> terms = SimpleEntry.getEntryHierarchy().getGoTerms(this.getAc());
        if (terms == null) {
            return Collections.emptyList();
        }
        return terms;
    }
}
