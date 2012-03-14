package uk.ac.ebi.interpro.scan.web.tag;

import uk.ac.ebi.interpro.scan.web.io.HierachyElementBuilder;
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
        StringBuilder result = new HierachyElementBuilder(supermatch).build();
        try {
            out.print(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }
}