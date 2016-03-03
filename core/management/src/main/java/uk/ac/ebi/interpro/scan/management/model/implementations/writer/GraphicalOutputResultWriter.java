package uk.ac.ebi.interpro.scan.management.model.implementations.writer;

import freemarker.template.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods common to all graphical output (e.g. HTML and SVG) writing classes.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class GraphicalOutputResultWriter {

    private static final Logger LOGGER = Logger.getLogger(GraphicalOutputResultWriter.class.getName());

    protected static final Charset characterSet = Charset.defaultCharset();

    protected static final int MAX_NUM_MATCH_DIAGRAM_SCALE_MARKERS = 10;

    protected Configuration freeMarkerConfig;

    protected String freeMarkerTemplate;

    protected AbstractApplicationContext appContext;

    protected EntryHierarchy entryHierarchy;

    protected static final Object EH_LOCK = new Object();

    protected String entryHierarchyBeanId;

    /* Please read the class comment if you are concerned about thread-safety.*/
    protected final List<Path> resultFiles = new ArrayList<>();

    protected String tempDirectory;

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
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public List<Path> getResultFiles() {
        return resultFiles;
    }

    protected void checkEntryHierarchy() {
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

    protected void checkTempDirectory(String tempDirectory) throws IOException {
        Path tempFileDirectory = Paths.get(tempDirectory);
        if (!Files.exists(tempFileDirectory)) {
            try {
                Files.createDirectories(tempFileDirectory);
            } catch (IOException e) {
                LOGGER.warn("Couldn't create temp directory " + tempDirectory);
                throw e;
            }
        } else if (LOGGER.isDebugEnabled()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Temp directory " + tempDirectory + " already exists, no need to create one.");
            }
        }
    }

}
