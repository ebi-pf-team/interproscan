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
 * Write matches as protein view output (SVG version) for InterProScan user.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesSVGResultWriter {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesSVGResultWriter.class.getName());

    private Configuration freeMarkerConfig;

    private String freeMarkerTemplate;

    private AbstractApplicationContext appContext;

    private EntryHierarchy entryHierarchy;

    private static final Object EH_LOCK = new Object();

    private String entryHierarchyBeanId;

    private List<File> resultFiles = new ArrayList<File>();

    private String outputDirectory;

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
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public List<File> getResultFiles() {
        return resultFiles;
    }

    /**
     * Writes out protein view to an zipped and compressed HTML file.
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(final Protein protein) throws IOException {
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
                        checkTempDirectory(outputDirectory);
                        if (!outputDirectory.endsWith("/")) {
                            outputDirectory = outputDirectory + "/";
                        }

                        UrlFriendlyIdGenerator gen = UrlFriendlyIdGenerator.getInstance();
                        String urlFriendlyId = gen.generate(xref.getIdentifier());
                        final File newResultFile = new File(outputDirectory + urlFriendlyId + ".svg");
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

    private void checkTempDirectory(String tempDirectory) throws IOException {
        File tempFileDirectory = new File(tempDirectory);
        if (!tempFileDirectory.exists()) {
            boolean isCreated = tempFileDirectory.mkdirs();
            if (!isCreated) {
                LOGGER.warn("Couldn't create temp directory " + tempDirectory);
            }

        } else {
            LOGGER.debug("Temp directory already exists, no need to create one.");
        }
    }

    private SimpleHash buildModelMap(SimpleProtein p, EntryHierarchy entryHierarchy) {
        final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;
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
