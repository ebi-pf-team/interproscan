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
public class HierachyElementBuilder {

    // Required parameters
    private final SimpleSuperMatch superMatch;

    public HierachyElementBuilder(SimpleSuperMatch superMatch) {
        this.superMatch = superMatch;
    }

    public StringBuilder build() {
        StringBuilder result = new StringBuilder();
        try {
            EntryHierarchyData entryHierarchyData = superMatch.getRootEntryData();
            if (entryHierarchyData != null) {
                StringBuilder list = siblings(entryHierarchyData);

                if (list.indexOf("<li>") == 0) {
                    result.append("<ul>");
                    result.append(list);
                    result.append("</ul>");
                } else {
                    result.append(list);
                }

            } else {
                // This entry is not in any hierarchy - just spit it out flat.
                result.append("<ul>");
                appendEntry(superMatch.getFirstEntry(), result);
                result.append("</li></ul>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Processes a "sibling", i.e. a single Entry.  If this entry is matched,
     * renders the entry.  The method then recurses into the children of the
     * entry to consider them in turn.
     *
     * @param sibling being an EntryHierarchyData object to consider
     * @return a StringBuilder (possibly empty) containing the details of this entry
     * @throws java.io.IOException
     */
    private StringBuilder siblings(EntryHierarchyData sibling) throws IOException {
        StringBuilder siblings = new StringBuilder();
        final SimpleEntry includedEntry = entryDataMatched(sibling);
        if (includedEntry != null) {
            appendEntry(includedEntry, siblings);
        }
        siblings.append(children(sibling));
        if (includedEntry != null) {
            siblings.append("</li>");
        }
        return siblings;
    }

    /**
     * Utility method to create the list item for an entry.
     * NOTE: Does NOT append the closing &lt;/li&gt; element as there may
     * children of this entry to squeeze in.
     *
     * @param entry to be rendered
     * @param sb    the StringBuilder to append this list item to.
     */
    private void appendEntry(SimpleEntry entry, StringBuilder sb) {
        if (entry == null) return;
        sb.append("<li>");
        sb.append("<a href=\"http://www.ebi.ac.uk/interpro/IEntry?ac=");
        sb.append(entry.getAc());
        sb.append("\" class=\"neutral\">");
        sb.append(entry.getName());
        sb.append("<span>(");
        sb.append(entry.getAc());
        sb.append(")</span></a>");
    }

    /**
     * Utility method - when iterating over the hierarchy, determine if the entry
     * currently being considered is actually matched.  If it is, return the
     * corresponding SimpleEntry object so the details can be displayed
     *
     * @param ehd being the point in the hierarchy being considered
     * @return the SimpleEntry object if this node in the hierarchy is matched
     *         and should therefore be displayed.
     */
    private SimpleEntry entryDataMatched(EntryHierarchyData ehd) {
        for (SimpleEntry entry : superMatch.getEntries()) {
            if (ehd != null && entry != null && ehd.getEntryAc().equals(entry.getAc())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * This method takes a parent entry and iterates over its children, recursing into them
     * via the siblings method.
     *
     * @param parent for which children should be considered
     * @return a StringBuilder object containing an &lt;ul/&gt; for any rendered children
     * @throws IOException
     */
    private StringBuilder children(EntryHierarchyData parent) throws IOException {
        StringBuilder children = new StringBuilder();

        // Iterate over siblings
        for (final EntryHierarchyData child : parent.getImmediateChildren()) {
            children.append(siblings(child));
        }
        if (children.length() > 0) {
            final StringBuilder temp = new StringBuilder();
            temp.append("<ul>");
            temp.append(children);
            children = temp;
            children.append("</ul>");
        }
        return children;
    }
}
