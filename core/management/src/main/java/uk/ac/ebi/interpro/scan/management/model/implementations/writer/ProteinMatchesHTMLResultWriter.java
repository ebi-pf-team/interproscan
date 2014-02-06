package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.web.ProteinViewHelper;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.CondensedView;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import java.io.*;
import java.util.ArrayList;
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
public class ProteinMatchesHTMLResultWriter {

    private static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesHTMLResultWriter.class.getName());

    private Configuration freeMarkerConfig;

    private String freeMarkerTemplate;

    private AbstractApplicationContext appContext;

    private EntryHierarchy entryHierarchy;

    private static final Object EH_LOCK = new Object();

    private String entryHierarchyBeanId;

    /* Please read the class comment if you are concerned about thread-safety.*/
    private final List<File> resultFiles = new ArrayList<File>();

    private String tempDirectory;

    @Required
    public void setEntryHierarchyBeanId(String entryHierarchyBeanId) {
        this.entryHierarchyBeanId = entryHierarchyBeanId;
    }

    @Required
    public void setFreeMarkerConfig(Configuration freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    @Required
    public void setApplicationContextConfigLocation(String applicationContextConfigLocation) {
        if (applicationContextConfigLocation != null) {
            this.appContext = new FileSystemXmlApplicationContext(applicationContextConfigLocation);
        }
    }

    @Required
    public void setFreeMarkerTemplate(String freeMarkerTemplate) {
        this.freeMarkerTemplate = freeMarkerTemplate;
    }

    @Required
    public void setHtmlResourcesDir(String path) {
        if (path != null && path.length() > 0) {
            resultFiles.add(new File(path));
        }
    }

    @Required
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public List<File> getResultFiles() {
        return resultFiles;
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
        checkEntryHierarchy();
        if (simpleProtein != null) {
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
                        if (!tempDirectory.endsWith("/")) {
                            tempDirectory = tempDirectory + "/";
                        }

                        UrlFriendlyIdGenerator gen = UrlFriendlyIdGenerator.getInstance();
                        String urlFriendlyId = gen.generate(xref.getIdentifier());
                        final File newResultFile = new File(tempDirectory + urlFriendlyId + ".html");
                        resultFiles.add(newResultFile);
                        writer = new PrintWriter(new FileWriter(newResultFile));
                        temp.process(model, writer);
                        writer.flush();
                    } catch (TemplateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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

    private void checkEntryHierarchy() {
        if (entryHierarchy == null) {
            synchronized (EH_LOCK) {
                if (entryHierarchy == null) {
                    if (appContext != null && entryHierarchyBeanId != null) {
                        this.entryHierarchy = (EntryHierarchy) appContext.getBean(entryHierarchyBeanId);
                    } else {
                        if (LOGGER.isEnabledFor(Level.WARN)) {
                            LOGGER.warn("Application context or entry hierarchy bean aren't initialised successfully!");
                        }
                    }
                }
            }
        }
    }

    private void checkTempDirectory(String tempDirectory) throws IOException {
        File tempFileDirectory = new File(tempDirectory);
        if (!tempFileDirectory.exists()) {
            boolean isCreated = tempFileDirectory.mkdirs();
            if (!isCreated) {
                LOGGER.warn("Couldn't create temp directory " + tempDirectory);
            }

        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Temp directory already exists, no need to create one.");
        }
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
