package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;

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

    private Properties propertiesFile;
    private Map<String, Integer> entryColourMap;

    /**
     * Initialise the singleton.
     */
    public void init() {
        entryColourMap = new HashMap<String, Integer>();
        for (Map.Entry<Object, Object> entryColour :  this.propertiesFile.entrySet()) {
            String entryAc = (String) entryColour.getKey();
            int colour = Integer.parseInt((String) entryColour.getValue());
            if (entryAc.startsWith("IPR")) {
                this.entryColourMap.put(entryAc, colour);
            }
            else {
                LOGGER.warn("Entry colours properties file contained an invalid entryAc - ignoring: " + entryAc);
            }
        }
    }

    public void setPropertiesFile(Properties propertiesFile) {
        this.propertiesFile = propertiesFile;
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
}
