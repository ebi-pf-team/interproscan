package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.web.ProteinViewHelper;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * Write matches as protein view output (SVG version) for InterProScan user.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesSVGResultWriter extends GraphicalOutputResultWriter {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesSVGResultWriter.class.getName());

    /**
     * Writes out protein view to an zipped and compressed HTML file.
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(final Protein protein) throws IOException {
        checkEntryHierarchy();

        if (entryHierarchy != null) {
            for (ProteinXref xref : protein.getCrossReferences()) {
                final SimpleProtein simpleProtein = SimpleProtein.valueOf(protein, xref, entryHierarchy);
                if (simpleProtein != null) {
                    //Build model for FreeMarker
                    final SimpleHash model = buildModelMap(simpleProtein, entryHierarchy);
                    //Render template and write result to a file
                    Writer writer = null;
                    try {
                        final Template temp = freeMarkerConfig.getTemplate(freeMarkerTemplate);
                        checkTempDirectory(tempDirectory);
                        if (!tempDirectory.endsWith(File.separator)) {
                            tempDirectory = tempDirectory + File.separator;
                        }

                        UrlFriendlyIdGenerator gen = UrlFriendlyIdGenerator.getInstance();
                        String urlFriendlyId = gen.generate(xref.getIdentifier());
                        final Path newResultFile = Paths.get(tempDirectory + urlFriendlyId + ".svg");
                        resultFiles.add(newResultFile);
                        writer = Files.newBufferedWriter(newResultFile, characterSet);
                        temp.process(model, writer);
                        writer.flush();
                    } catch (TemplateException e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
            }
        }
        return 0;
    }

    private SimpleHash buildModelMap(SimpleProtein p, EntryHierarchy entryHierarchy) {
        SimpleHash model = new SimpleHash();
        if (p != null) {
            final int proteinLength = p.getLength();
            final List<SimpleEntry> entries = p.getAllEntries();
            final CondensedView condensedView = new CondensedView(entries, proteinLength);

            model.put("protein", p);
            model.put("condensedView", condensedView);
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
            model.put("svgDocumentHeight", ProteinViewHelper.calculateSVGDocumentHeight(p, condensedView, 30, 180, 18, 19, 30));
        }
        return model;
    }
}
