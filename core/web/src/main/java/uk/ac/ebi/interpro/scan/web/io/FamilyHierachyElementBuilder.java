package uk.ac.ebi.interpro.scan.web.io;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.IOException;
import java.util.*;

/**
 * Builder class that can create a set of nested ul / li for
 * a hierarchy of families.
 *
 * @author Phil Jones,EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FamilyHierachyElementBuilder extends AbstractHierarchyElementBuilder {

    private final List<List<SimpleEntry>> groupedEntries = new ArrayList<List<SimpleEntry>>();
    private Map<String, SimpleEntry> familyAccessions = new HashMap<String, SimpleEntry>();

    public FamilyHierachyElementBuilder(SimpleProtein simpleProtein) {
        if (simpleProtein != null) {
            Set<SimpleEntry> familyEntries = simpleProtein.getFamilyEntries();
            if (familyEntries != null && familyEntries.size() > 0) {
                // Place families into a List of Lists grouped by hierarchy.
                outerLoop:
                for (SimpleEntry familyEntry : familyEntries) {
                    familyAccessions.put(familyEntry.getAc(), familyEntry);
                    for (List<SimpleEntry> existingList : groupedEntries) {
                        if (existingList.size() > 0) {
                            if (SimpleEntry.getEntryHierarchy().areInSameHierarchy(familyEntry, existingList.get(0))) {
                                existingList.add(familyEntry);
                                continue outerLoop;
                            }
                        }
                    }
                    // This entry is not in the same hierarchy as any of the entries already considered,
                    // so create a new list.
                    final List<SimpleEntry> newList = new ArrayList<SimpleEntry>();
                    newList.add(familyEntry);
                    groupedEntries.add(newList);
                }
            }
        }
    }

    public StringBuilder build() {
        StringBuilder result = new StringBuilder();
        try {
            for (List<SimpleEntry> hierarchy : groupedEntries) {
                EntryHierarchyData root = hierarchy.get(0).getHierarchyData();
                if (root == null) {
                    // Flat - just spit out this Entry on its own.
                    result.append("<ul>");
                    appendEntry(hierarchy.get(0), result);
                    result.append("</li></ul>");
                } else {
                    StringBuilder list = siblings(root);
                    if (list.indexOf("<li>") == 0) {
                        result.append("<ul>");
                        result.append(list);
                        result.append("</ul>");
                    } else {
                        result.append(list);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected SimpleEntry entryDataMatched(EntryHierarchyData ehd) {
        return familyAccessions.get(ehd.getEntryAc());
    }

}
