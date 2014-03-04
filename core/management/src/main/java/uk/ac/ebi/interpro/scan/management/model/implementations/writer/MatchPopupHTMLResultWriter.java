package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleSignature;

import java.io.IOException;

/**
 * Prepare HTML for a signature match information popup.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchPopupHTMLResultWriter extends PopupHTMLResultWriter {

    private static final Logger LOGGER = Logger.getLogger(MatchPopupHTMLResultWriter.class.getName());

    public String write(final String matchPopupId, final SimpleSignature signature, final SimpleLocation location,  final String colourClass) throws IOException, TemplateException {
        // Validate inputs
        if (matchPopupId == null || !matchPopupId.contains("popup-")) {
            throw new IllegalArgumentException("Invalid matchPopupId");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be NULL");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be NULL");
        }
        if (colourClass == null || colourClass.equals("")) {
            throw new IllegalArgumentException("Colour class name must be supplied");
        }

        // Build model for FreeMarker
        final SimpleHash model = buildModelMap();
        model.put("matchPopupId", matchPopupId);
        model.put("signature", signature);
        model.put("location", location);
        model.put("colourClass", colourClass);

        // Now prepare the HTML
        return writePopupHTML(model);
    }

}
