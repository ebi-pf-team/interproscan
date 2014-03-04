package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.web.model.SimpleSuperMatch;

import java.io.IOException;

/**
 * Prepare HTML for a supermatch information popup.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperMatchPopupHTMLResultWriter extends PopupHTMLResultWriter {

    private static final Logger LOGGER = Logger.getLogger(SuperMatchPopupHTMLResultWriter.class.getName());

    public String write(final String superMatchPopupId, final SimpleSuperMatch superMatch, final String colourClass) throws IOException, TemplateException {
        // Validate inputs
        if (superMatchPopupId == null || !superMatchPopupId.contains("popup-")) {
            throw new IllegalArgumentException("Invalid superMatchPopupId");
        }
        if (superMatch == null) {
            throw new IllegalArgumentException("Super match cannot be NULL");
        }
        if (colourClass == null || colourClass.equals("")) {
            throw new IllegalArgumentException("Colour class name must be supplied");
        }

        // Build model for FreeMarker
        final SimpleHash model = buildModelMap();
        model.put("superMatchPopupId", superMatchPopupId);
        model.put("superMatch", superMatch);
        model.put("colourClass", colourClass);

        // Now prepare the HTML
        return writePopupHTML(model);
    }

}
