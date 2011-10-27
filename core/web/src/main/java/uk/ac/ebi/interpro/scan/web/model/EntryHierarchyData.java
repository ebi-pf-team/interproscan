package uk.ac.ebi.interpro.scan.web.model;

import java.util.Set;

/**
 * Contains useful information about InterPro entry domain hierarchies.
 *
 * For a given entry accession, will be able to tell:
 * - At what level within the hierarchy is this entry? Level 1 is a root entry.
 * - What is this entries parent? Root entries have no parent.
 * - Which other entries are within the same hierarchy as this one?
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryHierarchyData {
    private final String entryAc;
    private final int hierarchyLevel;
    private final String parentEntryAc;
    private Set<String> entriesInSameHierarchy = null;

    public EntryHierarchyData(String entryAc, int hierarchyLevel, String parentEntryAc) {
        this.entryAc = entryAc;
        this.hierarchyLevel = hierarchyLevel;
        this.parentEntryAc = parentEntryAc;
    }

    public String getEntryAc() {
        return entryAc;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public String getParentEntryAc() {
        return parentEntryAc;
    }

    public Set<String> getEntriesInSameHierarchy() {
        return entriesInSameHierarchy;
    }

    public void setEntriesInSameHierarchy(Set<String> entriesInSameHierarchy) {
        this.entriesInSameHierarchy = entriesInSameHierarchy;
    }
}
