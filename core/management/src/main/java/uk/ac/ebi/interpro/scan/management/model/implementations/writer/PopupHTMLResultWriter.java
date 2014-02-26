package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch;

import java.io.*;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PopupHTMLResultWriter extends GraphicalOutputResultWriter {

    private static final Logger LOGGER = Logger.getLogger(PopupHTMLResultWriter.class.getName());

    @Required
    public void setHtmlResourcesDir(String path) {
        if (path != null && path.length() > 0) {
            resultFiles.add(new File(path));
        }
    }

    public String write(final String superMatchPopupId, final SimpleSuperMatch superMatch, final String colourClass) throws IOException, TemplateException {
        if (superMatchPopupId == null || !superMatchPopupId.contains("popup-")) {
            throw new IllegalArgumentException("Invalid superMatchPopupId");
        }
        if (superMatch == null) {
            throw new IllegalArgumentException("Super match cannot be NULL");
        }
        if (colourClass == null || colourClass.equals("")) {
            throw new IllegalArgumentException("Colour class name must be supplied");
        }
        //checkEntryHierarchy();
        //Build model for FreeMarker
        final SimpleHash model = buildModelMap(superMatchPopupId, superMatch, colourClass);
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

    private SimpleHash buildModelMap(final String superMatchPopupId,
                                     final SimpleSuperMatch superMatch,
                                     final String colourClass) {
        final SimpleHash model = new SimpleHash();
        model.put("superMatchPopupId", superMatchPopupId);
        model.put("superMatch", superMatch);
        model.put("colourClass", colourClass);
        model.put("standalone", false);
        return model;
    }

}
