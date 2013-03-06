package uk.ac.ebi.interpro.scan.web.io;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;

import java.io.IOException;

/**
 * @author Phil Jones
 *         Date: 15/03/12
 */
public abstract class AbstractHierarchyElementBuilder {

    /**
     * Processes a "sibling", i.e. a single Entry.  If this entry is matched,
     * renders the entry.  The method then recurses into the children of the
     * entry to consider them in turn.
     *
     * @param sibling being an EntryHierarchyData object to consider
     * @return a StringBuilder (possibly empty) containing the details of this entry
     * @throws java.io.IOException
     */
    protected StringBuilder siblings(EntryHierarchyData sibling) throws IOException {
        return siblings(sibling, false);
    }

    /**
     * Processes a "sibling", i.e. a single Entry.  If this entry is matched,
     * renders the entry.  The method then recurses into the children of the
     * entry to consider them in turn.
     *
     * @param sibling being an EntryHierarchyData object to consider
     * @param isPopup true if we are building this specifically for a popup
     * @return a StringBuilder (possibly empty) containing the details of this entry
     * @throws java.io.IOException
     *
     */
    protected StringBuilder siblings(EntryHierarchyData sibling, boolean isPopup) throws IOException {
        StringBuilder siblings = new StringBuilder();
        final SimpleEntry includedEntry = entryDataMatched(sibling);
        if (includedEntry != null) {
            appendEntry(includedEntry, siblings, isPopup);
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
    protected void appendEntry(SimpleEntry entry, StringBuilder sb) {
        appendEntry(entry, sb, false);
    }

    /**
     * Utility method to create the list item for an entry.
     * NOTE: Does NOT append the closing &lt;/li&gt; element as there may
     * children of this entry to squeeze in.
     *
     * @param entry to be rendered
     * @param sb    the StringBuilder to append this list item to.
     */
    protected void appendEntry(SimpleEntry entry, StringBuilder sb, boolean isPopup) {
        if (entry == null) return;
        sb.append("<li");
        if (isPopup) {
            sb.append(" class=\"");
            sb.append(entry.getType().toString());
            sb.append("\" ");
        }
        sb.append(">");
        sb.append("<a href=\"http://www.ebi.ac.uk/interpro/entry/");
        sb.append(entry.getAc());
        sb.append("\">");
        sb.append(entry.getName());
        sb.append("</a><span> (");
        sb.append(entry.getAc());
        sb.append(")</span>");
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
    protected abstract SimpleEntry entryDataMatched(EntryHierarchyData ehd);

    /**
     * This method takes a parent entry and iterates over its children, recursing into them
     * via the siblings method.
     *
     * @param parent for which children should be considered
     * @return a StringBuilder object containing an &lt;ul/&gt; for any rendered children
     * @throws java.io.IOException
     */
    private StringBuilder children(EntryHierarchyData parent) throws IOException {
        StringBuilder children = new StringBuilder();

        // Iterate over children
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
