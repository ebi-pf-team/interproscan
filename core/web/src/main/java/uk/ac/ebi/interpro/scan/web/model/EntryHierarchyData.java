package uk.ac.ebi.interpro.scan.web.model;

import java.util.Set;

/**
 * Contains useful information about InterPro entry domain hierarchies.
 *
 * For a given entry accession, will be able to tell:
 * - At what level within the hierarchy is this entry? Level 0 is a root entry.
 * - Which other entries are within the same hierarchy as this one?
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryHierarchyData {
    private final String entryAc;
    private final int hierarchyLevel;
    private Set<String> entriesInSameHierarchy = null;

    public EntryHierarchyData(String entryAc, int hierarchyLevel) {
        this.entryAc = entryAc;
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getEntryAc() {
        return entryAc;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public Set<String> getEntriesInSameHierarchy() {
        return entriesInSameHierarchy;
    }

    public void setEntriesInSameHierarchy(Set<String> entriesInSameHierarchy) {
        this.entriesInSameHierarchy = entriesInSameHierarchy;
    }
}
