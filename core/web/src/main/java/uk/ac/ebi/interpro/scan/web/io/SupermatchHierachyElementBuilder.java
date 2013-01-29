package uk.ac.ebi.interpro.scan.web.io;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch;

import java.io.IOException;

/**
 * Builder class that can create a set of nested ul / li for
 * a hierarchy of domains or families.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Phil Jones,EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SupermatchHierachyElementBuilder extends AbstractHierarchyElementBuilder {

    // Required parameters
    private final SimpleSuperMatch superMatch;

    public SupermatchHierachyElementBuilder(SimpleSuperMatch superMatch) {
        this.superMatch = superMatch;
    }

    public StringBuilder build() {
       return build(false);
    }

    public StringBuilder build(boolean isPopup) {
        StringBuilder result = new StringBuilder();
        try {
            EntryHierarchyData entryHierarchyData = superMatch.getRootEntryData();
            if (entryHierarchyData != null) {
                StringBuilder list = siblings(entryHierarchyData, isPopup);

                if (list.indexOf("<li") == 0) {
                    result.append("<ul>");
                    result.append(list);
                    result.append("</ul>");
                } else {
                    result.append(list);
                }

            } else {
                // This entry is not in any hierarchy - just spit it out flat.
                result.append("<ul>");
                appendEntry(superMatch.getFirstEntry(), result, isPopup);
                result.append("</li></ul>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public StringBuilder buildPopup() {
        return build(true);
    }

    @Override
    protected SimpleEntry entryDataMatched(EntryHierarchyData ehd) {
        for (SimpleEntry entry : superMatch.getEntries()) {
            if (ehd != null && entry != null && ehd.getEntryAc().equals(entry.getAc())) {
                return entry;
            }
        }
        return null;
    }



}
