package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.web.ProteinViewHelper;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;

import java.io.*;

/**
 * A class to render HTML for the condensed view only. Less complicated than {@link ProteinMatchesHTMLResultWriter}
 * which deals with both the condensed view and also other protein page objects too!
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CondensedViewHTMLResultWriter extends GraphicalOutputResultWriter {
    // DO NOT DELETE - This class is not used in InterProScan 5, but is used as a dependency in InterPro web 6

    private static final Logger LOGGER = Logger.getLogger(CondensedViewHTMLResultWriter.class.getName());

    @Required
    public void setHtmlResourcesDir(String path) {
        if (path != null && path.length() > 0) {
            resultFiles.add(new File(path));
        }
    }

    public String write(CondensedView condensedView) throws IOException, TemplateException {
        return write(condensedView, null);
    }

    /**
     *
     * @param condensedView The condensed view data
     * @param viewId An additional identifier for the condensed view (a useful extra prefix for the HTML elements when
     *               rendering multiple condensed views on the same page).
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String write(CondensedView condensedView, String viewId) throws IOException, TemplateException {
        if (viewId == null) {
            viewId = "";
        }
        if (condensedView != null) {
            // Don't need the hierarchy information to render an empty condensed view
            checkEntryHierarchy();
        }
        //Build model for FreeMarker
        final SimpleHash model = buildModelMap(condensedView, viewId);
        Writer writer = null;
        try {
            StringWriter stringWriter = new StringWriter();
            writer = new BufferedWriter(stringWriter);
            final Template temp = freeMarkerConfig.getTemplate(freeMarkerTemplate);
            temp.process(model, writer);
            writer.flush();
            return stringWriter.toString();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private SimpleHash buildModelMap(final CondensedView condensedView, final String viewId) {
        final SimpleHash model = new SimpleHash();
        if (condensedView != null) {
            int proteinLength = condensedView.getProteinLength();
            model.put("condensedView", condensedView);
            model.put("viewId", viewId);
            model.put("proteinLength", proteinLength);
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("scale", ProteinViewHelper.generateScaleMarkers(proteinLength, MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        }
        model.put("standalone", false); // Never used in InterProScan 5 standalone mode
        return model;
    }

}
