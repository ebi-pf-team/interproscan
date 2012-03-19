package uk.ac.ebi.interpro.scan.web.tag;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;

import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 13/03/12
 */
public abstract class AbstractHierarchyTag extends TagSupport {

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
     * Utility method to create the list item for an entry.
     * NOTE: Does NOT append the closing &lt;/li&gt; element as there may
     * children of this entry to squeeze in.
     *
     * @param entry to be rendered
     * @param sb    the StringBuilder to append this list item to.
     */
    protected void appendEntry(SimpleEntry entry, StringBuilder sb) {

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
     * Processes a "sibling", i.e. a single Entry.  If this entry is matched,
     * renders the entry.  The method then recurses into the children of the
     * entry to consider them in turn.
     *
     * @param sibling being an EntryHierarchyData object to consider
     * @return a StringBuilder (possibly empty) containing the details of this entry
     * @throws java.io.IOException
     */
    protected StringBuilder siblings(EntryHierarchyData sibling) throws IOException {
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
     * This method takes a parent entry and iterates over its children, recursing into them
     * via the siblings method.
     *
     * @param parent for which children should be considered
     * @return a StringBuilder object containing an &lt;ul/&gt; for any rendered children
     * @throws java.io.IOException
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
