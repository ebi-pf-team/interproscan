package uk.ac.ebi.interpro.scan.web.tag;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * @author Phil Jones
 *         Date: 29/02/12
 *         Time: 17:42
 *         <p/>
 *         Tag class that can create a set of nested ul / li for
 *         a hierarchy of domains or families.
 *         <p/>
 *         Implemented in Java as requires recursion.
 */
public class HierarchyTag extends TagSupport {

    private SimpleSuperMatch supermatch;

    public void setSupermatch(SimpleSuperMatch supermatch) {
        this.supermatch = supermatch;
    }

    /**
     * Processes a hierarchy of SimpleEntry objects and renders using
     * ul and li tags appropriately.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        //Get the writer object for output.
        final JspWriter out = pageContext.getOut();
        try {
            EntryHierarchyData entryHierarchyData = supermatch.getRootEntryData();
            if (entryHierarchyData != null) {
                StringBuilder list = siblings(entryHierarchyData);

                if (list.indexOf("<li>") == 0) {
                    out.print("<ul>");
                    out.print(list);
                    out.print("</ul>");
                } else {
                    out.print(list);
                }

            } else {
                // This entry is not in any hierarchy - just spit it out flat.
                final StringBuilder sb = new StringBuilder("<ul>");
                appendEntry(supermatch.getFirstEntry(), sb);
                sb.append("</li></ul>");
                out.print(sb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
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
        for (SimpleEntry entry : supermatch.getEntries()) {
            if (ehd != null && entry != null && ehd.getEntryAc().equals(entry.getAc())) {
                return entry;
            }
        }
        return null;
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
     * Processes a "sibling", i.e. a single Entry.  If this entry is matched,
     * renders the entry.  The method then recurses into the children of the
     * entry to consider them in turn.
     *
     * @param sibling being an EntryHierarchyData object to consider
     * @return a StringBuilder (possibly empty) containing the details of this entry
     * @throws IOException
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
