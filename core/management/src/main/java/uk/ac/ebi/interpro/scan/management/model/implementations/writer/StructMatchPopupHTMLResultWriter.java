package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.web.model.MatchDataSource;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;

/**
 * Prepare HTML for a structural match information popup.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StructMatchPopupHTMLResultWriter extends PopupHTMLResultWriter {
    // DO NOT DELETE - This class is not used in InterProScan 5, but is used as a dependency in InterPro web 6

    private static final Logger LOGGER = Logger.getLogger(StructMatchPopupHTMLResultWriter.class.getName());

    public String write(final String structPopupId,
                        final SimpleLocation location,
                        final Map<String, SortedSet<String>> locationDataMap,
                        final MatchDataSource databaseMetadata) throws IOException, TemplateException {
        // Validate inputs
        if (structPopupId == null || !structPopupId.contains("popup-")) {
            throw new IllegalArgumentException("Invalid structPopupId");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be NULL");
        }
        if (locationDataMap == null) {
            throw new IllegalArgumentException("Location data map cannot be NULL");
        }
        if (databaseMetadata == null) {
            throw new IllegalArgumentException("Structural database metadata cannot be NULL");
        }

        // Build model for FreeMarker
        final SimpleHash model = buildModelMap();
        model.put("structPopupId", structPopupId);
        model.put("location", location);
        model.put("locationDataMap", locationDataMap);
        model.put("databaseMetadata", databaseMetadata);

        // Now prepare the HTML
        return writePopupHTML(model);
    }

}
