package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;

/**
 * Contains common code shared by all types of HTML information popups.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class PopupHTMLResultWriter extends GraphicalOutputResultWriter {

    private static final Logger LOGGER = Logger.getLogger(PopupHTMLResultWriter.class.getName());

    @Required
    public void setHtmlResourcesDir(String path) {
        if (path != null && path.length() > 0) {
            resultFiles.add(new File(path));
        }
    }

    protected String writePopupHTML(SimpleHash model) throws IOException, TemplateException {
        // Now prepare the HTML
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

    protected SimpleHash buildModelMap() {
        final SimpleHash model = new SimpleHash();
        // No popups are  currently used in InterProScan 5 standalone mode, only used by the InterPro web application
        // (through AJAX calls)
        model.put("standalone", false);
        return model;
    }

}
