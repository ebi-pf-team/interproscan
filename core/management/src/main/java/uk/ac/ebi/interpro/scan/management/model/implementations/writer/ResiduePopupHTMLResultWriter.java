package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.web.model.SimpleSite;
import uk.ac.ebi.interpro.scan.web.model.SimpleSiteLocation;

import java.io.IOException;

/**
 * Prepare HTML for a residue match information popup.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ResiduePopupHTMLResultWriter extends PopupHTMLResultWriter {
    // DO NOT DELETE - This class is not used in InterProScan 5, but is used as a dependency in InterPro web 6

    private static final Logger LOGGER = Logger.getLogger(ResiduePopupHTMLResultWriter.class.getName());

    public String write(final String residuePopupId,
                        final SimpleSiteLocation residueLocation,
                        final SimpleSite site,
                        final String colourClass) throws IOException, TemplateException {
        // Validate inputs
        if (residuePopupId == null || !residuePopupId.contains("popup-")) {
            throw new IllegalArgumentException("Invalid matchPopupId");
        }
        if (residueLocation == null) {
            throw new IllegalArgumentException("Site residue location cannot be NULL");
        }
        if (site == null) {
            throw new IllegalArgumentException("Site cannot be NULL");
        }
        if (colourClass == null) {
            throw new IllegalArgumentException("Colour class name must be supplied");
        }

        // Build model for FreeMarker
        final SimpleHash model = buildModelMap();
        model.put("residuePopupId", residuePopupId);
        model.put("residueLocation", residueLocation);
        model.put("site", site);
        model.put("colourClass", colourClass);

        // Now prepare the HTML
        return writePopupHTML(model);
    }

}
