package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;

import java.io.IOException;
import java.util.*;

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
    private Map<String, Integer> entryColourMap = null;
    private Resource entryHierarchyDataResource;
    private EntryHierarchyDataResourceReader entryHierarchyDataResourceReader;
    private Map<String, EntryHierarchyData> entryHierarchyDataMap = null;

    /**
     * Initialise the singleton.
     */
    public void init() {
        // Build entry colour map
        entryColourMap = buildEntryColourMap();

        // Build entry hierarchy data map
        try {
            entryHierarchyDataMap = entryHierarchyDataResourceReader.read(entryHierarchyDataResource);
        } catch (IOException e) {
            LOGGER.warn("Problem reading entry hierarchy data resource: " + e.getMessage());
        }
    }

    /**
     * Build a map of InterPro entry accessions to colour ID numbers from the configured properties file.
     *
     * @return The map
     */
    private Map<String, Integer> buildEntryColourMap() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Map.Entry<Object, Object> entryColour : this.entryColourPropertiesFile.entrySet()) {
            String entryAc = (String) entryColour.getKey();
            int colour = Integer.parseInt((String) entryColour.getValue());
            if (entryAc.startsWith("IPR")) {
                map.put(entryAc, colour);
            } else {
                LOGGER.warn("Entry colours properties file contained an invalid entryAc - ignoring: " + entryAc);
            }
        }
        return map;
    }

    /**
     * Re-initialise the singleton whilst the application is running.
     *
     * @return True if application singleton data re-initialisation succeeded, otherwise false.
     */
    public boolean reinit() {
        // Re-build entry colour map
        Map<String, Integer> newEntryColourMap = buildEntryColourMap();
        if (newEntryColourMap == null || newEntryColourMap.size() < 1) {
            // Something went wrong - leave previous data un-touched
            return false;
        }

        // Re-build entry hierarchy data map
        Map<String, EntryHierarchyData> newEntryHierarchyDataMap;
        try {
            newEntryHierarchyDataMap = entryHierarchyDataResourceReader.read(entryHierarchyDataResource);
        } catch (IOException e) {
            LOGGER.warn("Problem reading entry hierarchy data resource: " + e.getMessage());
            return false;
        }
        if (newEntryHierarchyDataMap == null || newEntryHierarchyDataMap.size() < 1) {
            // Something went wrong - leave previous data un-touched
            return false;
        }

        // All looks OK with the new data, proceed with re-initialisation
        entryColourMap = newEntryColourMap;
        entryHierarchyDataMap = newEntryHierarchyDataMap;
        return true;
    }


    @Required
    public void setEntryColourPropertiesFile(Properties entryColourPropertiesFile) {
        this.entryColourPropertiesFile = entryColourPropertiesFile;
    }

    @Required
    public void setEntryHierarchyDataResource(Resource entryHierarchyDataResource) {
        this.entryHierarchyDataResource = entryHierarchyDataResource;
    }

    @Required
    public void setEntryHierarchyDataResourceReader(EntryHierarchyDataResourceReader entryHierarchyDataResourceReader) {
        this.entryHierarchyDataResourceReader = entryHierarchyDataResourceReader;
    }

    /**
     * Return the entry accession to colour map (unmodifiable).
     *
     * @return The unmodifiable entry accession to colour map
     */
    public Map<String, Integer> getEntryColourMap() {
        return Collections.unmodifiableMap(this.entryColourMap);
    }

    /**
     * Get the colour for an InterPro entry.
     *
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
     *
     * @return The unmodifiable entry accession to hierarchy data map
     */
    public Map<String, EntryHierarchyData> getEntryHierarchyDataMap() {
        return Collections.unmodifiableMap(this.entryHierarchyDataMap);
    }

    /**
     * Get the associated hierarchy data for a supplied InterPro entry.
     *
     * @param ac Entry accession
     * @return The hierarchy data for the specified entry accession (or NULL if not found)
     */
    public EntryHierarchyData getEntryHierarchyData(String ac) {
        if (this.entryHierarchyDataMap.containsKey(ac)) {
            return this.entryHierarchyDataMap.get(ac);
        }
        return null;
    }

    /**
     * Are the two entries part of the same hierarchy?
     *
     * @param ac1 First entry accession
     * @param ac2 Second entry accession
     * @return True if part of the same hierarchy, otherwise false
     */
    public boolean areInSameHierarchy(String ac1, String ac2) {
        // Are they the same entry?  If so, they are in the same hierarchy by definition...
        if (ac1.equals(ac2)) {
            return true;
        }
        if (this.entryHierarchyDataMap.containsKey(ac1)) {
            final EntryHierarchyData data = this.entryHierarchyDataMap.get(ac1);
            final Set<String> entriesInSameHierarchy = data.getEntriesInSameHierarchy();
            return entriesInSameHierarchy != null && entriesInSameHierarchy.contains(ac2);
        }
        return false;
    }

    public boolean areInSameHierarchy(SimpleEntry se1, SimpleEntry se2) {
        return se1 != null && se2 != null && areInSameHierarchy(se1.getAc(), se2.getAc());
    }

    /**
     * Compare the hierarchy level of two entries
     *
     * @param ac1 First entry accession
     * @param ac2 Second entry accession
     * @return 0 if the same, -1 or 1
     */
    public int compareHierarchyLevels(String ac1, String ac2) {
        if (this.entryHierarchyDataMap.containsKey(ac1) && this.entryHierarchyDataMap.containsKey(ac2)) {
            EntryHierarchyData data1 = this.entryHierarchyDataMap.get(ac1);
            EntryHierarchyData data2 = this.entryHierarchyDataMap.get(ac2);
            Integer level1 = data1.getHierarchyLevel();
            Integer level2 = data2.getHierarchyLevel();
            return level1.compareTo(level2);
        }
        return 0;
    }
}
