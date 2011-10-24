package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A singleton that contains information about the InterPro domain entry hierarchy.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryHierarchy {

    private static final Logger LOGGER = Logger.getLogger(EntryHierarchy.class.getName());

    private Properties entryColourPropertiesFile;
    private Map<String, Integer> entryColourMap;
    private Resource entryHierarchyDataResource;
    private EntryHierarchyDataResourceReader entryHierarchyDataResourceReader;
    private Map<String, EntryHierarchyData> entryHierarchyDataMap = null;

    /**
     * Initialise the singleton.
     */
    public void init() {
        // Build entry colour map
        entryColourMap = new HashMap<String, Integer>();
        for (Map.Entry<Object, Object> entryColour :  this.entryColourPropertiesFile.entrySet()) {
            String entryAc = (String) entryColour.getKey();
            int colour = Integer.parseInt((String) entryColour.getValue());
            if (entryAc.startsWith("IPR")) {
                this.entryColourMap.put(entryAc, colour);
            }
            else {
                LOGGER.warn("Entry colours properties file contained an invalid entryAc - ignoring: " + entryAc);
            }
        }

        // Build entry hierarchy data map
        try {
            entryHierarchyDataMap = entryHierarchyDataResourceReader.read(entryHierarchyDataResource);
        }
        catch (IOException e) {
            LOGGER.warn("Problem reading entry hierarchy data resource: " + e.getMessage());
        }
    }

    public void setEntryColourPropertiesFile(Properties entryColourPropertiesFile) {
        this.entryColourPropertiesFile = entryColourPropertiesFile;
    }

    public void setEntryHierarchyDataResource(Resource entryHierarchyDataResource) {
        this.entryHierarchyDataResource = entryHierarchyDataResource;
    }

    public void setEntryHierarchyDataResourceReader(EntryHierarchyDataResourceReader entryHierarchyDataResourceReader) {
        this.entryHierarchyDataResourceReader = entryHierarchyDataResourceReader;
    }

    /**
     * Return the entry accession to colour map (unmodifiable).
     * @return The unmodifiable entry accession to colour map
     */
    public Map<String, Integer> getEntryColourMap() {
        return Collections.unmodifiableMap(this.entryColourMap);
    }

    /**
     * Get the colour for an InterPro entry.
     * @param ac Entry accession
     * @return The colour for the specified entry accession (or -1 if not found)
     */
    public int getEntryColour(String ac) {
        if (this.entryColourMap.containsKey(ac)) {
            return this.entryColourMap.get(ac);
        }
        return -1;
    }

    /**
     * Return the entry accession to hierarchy data map (unmodifiable).
     * @return The unmodifiable entry accession to hierarchy data map
     */
    public Map<String, EntryHierarchyData> getEntryHierarchyDataMap() {
        return Collections.unmodifiableMap(this.entryHierarchyDataMap);
    }

    /**
     * Get the associated hierarchy data for a supplied InterPro entry.
     * @param ac Entry accession
     * @return The hierarchy data for the specified entry accession (or NULL if not found)
     */
    public EntryHierarchyData getEntryHierarchyData(String ac) {
        if (this.entryHierarchyDataMap.containsKey(ac)) {
            return this.entryHierarchyDataMap.get(ac);
        }
        return null;
    }
}
