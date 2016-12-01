package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
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
 * Write matches as output for InterProScan user.
 * <p/>
 * Please note: This class might be not thread-safe because of the resultFiles list array, but as I5 runs this instance
 * only once for all proteins at the end this OK.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesHTMLResultWriter extends GraphicalOutputResultWriter {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesHTMLResultWriter.class.getName());

    @Required
    public void setHtmlResourcesDir(String path) {
        if (path != null && path.length() > 0) {
            resultFiles.add(Paths.get(path));
        }
    }

    /**
     * Returns the protein view for a given SimpleProtein object as a String (standalone mode by default).
     *
     * @param simpleProtein for which to return a view
     * @return the view as a String
     * @throws IOException
     * @throws TemplateException
     */
    public String write(final SimpleProtein simpleProtein) throws IOException, TemplateException {
        return write(simpleProtein, true);
    }

    /**
     * Returns the protein view for a given SimpleProtein object as a String.
     *
     * @param simpleProtein for which to return a view
     * @param standalone InterProScan standalone mode output?
     * @return the view as a String
     * @throws IOException
     * @throws TemplateException
     */
    public String write(final SimpleProtein simpleProtein, final boolean standalone) throws IOException, TemplateException {
        if (simpleProtein != null) {
            checkEntryHierarchy();
            //Build model for FreeMarker
            final SimpleHash model = buildModelMap(simpleProtein, entryHierarchy, standalone);
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
        return null;
    }

    /**
     * Writes out protein view to an zipped and compressed HTML file (standalone mode by default).
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(final Protein protein) throws IOException {
        return write(protein, true);
    }

    /**
     * Writes out protein view to an zipped and compressed HTML file.
     *
     * @param protein containing matches to be written out
     * @param standalone InterProScan standalone mode output?
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(final Protein protein, final boolean standalone) throws IOException {
        checkEntryHierarchy();
        if (entryHierarchy != null) {
            for (ProteinXref xref : protein.getCrossReferences()) {
                final SimpleProtein simpleProtein = SimpleProtein.valueOf(protein, xref, entryHierarchy);
                if (simpleProtein != null) {
                    //Build model for FreeMarker
                    final SimpleHash model = buildModelMap(simpleProtein, entryHierarchy, standalone);
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
                        final Path newResultFile = Paths.get(tempDirectory + urlFriendlyId + ".html");
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

    private SimpleHash buildModelMap(final SimpleProtein p, final EntryHierarchy entryHierarchy, final boolean standalone) {
        final SimpleHash model = new SimpleHash();
        if (p != null) {

            final int proteinLength = p.getLength();
            final List<SimpleEntry> entries = p.getAllEntries();

            model.put("protein", p);
            model.put("condensedView", new CondensedView(entries, proteinLength));
            model.put("entryColours", entryHierarchy.getEntryColourMap());
            model.put("standalone", standalone);
            model.put("scale", ProteinViewHelper.generateScaleMarkers(p.getLength(), MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS));
        }
        return model;
    }
}
