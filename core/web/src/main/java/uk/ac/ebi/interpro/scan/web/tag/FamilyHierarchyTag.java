package uk.ac.ebi.interpro.scan.web.tag;

import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 13/03/12
 *         Time: 17:45
 */
public class FamilyHierarchyTag extends AbstractHierarchyTag {

    private SimpleProtein simpleProtein;

    public void setSimpleProtein(SimpleProtein simpleProtein) {
        this.simpleProtein = simpleProtein;
    }

    /**
     * Default processing of the start tag, returning SKIP_BODY.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        if (simpleProtein != null) {
            Set<SimpleEntry> familyEntries = simpleProtein.getFamilyEntries();
            if (familyEntries != null && familyEntries.size() > 0) {
                // Place families into a List of Lists grouped by hierarchy.
                List<List<SimpleEntry>> groupedEntries = new ArrayList<List<SimpleEntry>>();
                for (SimpleEntry entry : familyEntries) {
                    for (List<SimpleEntry> existingList : groupedEntries) {
//                        if ()
                    }
                }
            }
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
    @Override
    protected SimpleEntry entryDataMatched(EntryHierarchyData ehd) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
