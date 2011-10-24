package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Parse a text file containing information about InterPro entry domain hierarchies.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryHierarchyDataResourceReader {

    private static final Logger LOGGER = Logger.getLogger(EntryHierarchyDataResourceReader.class.getName());

    public Map<String, EntryHierarchyData> read(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists()) {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }

        /*
         * Example text to parse:
         *
         * IPR000014::PAS::
         * --IPR013655::PAS fold-3::
         * --IPR013656::PAS fold-4::
         * --IPR013767::PAS fold::
         * IPR000020::Anaphylatoxin/fibulin::
         * --IPR018081::Anaphylatoxin::
         * ----IPR001840::Complement C3a/C4a/C5a anaphylatoxin::
         */
        Map<String, EntryHierarchyData> entryHierarchyMap = new HashMap<String, EntryHierarchyData>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line;
            String entryAc;
            int hierarchyLevel;
            Set<String> entriesInSameHierarchy = null;

            // Loop through and parse each line in the text file
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if ((line.startsWith("IPR") || line.startsWith("--")) && line.contains("IPR") && line.contains(":")) {
                    line = line.substring(0, line.indexOf(':'));
                    // Now can get entryAc and hierarchy level from this line of text, e.g. "IPR000014" or "--IPR013655"
                    // but also need to build a list of entries that share a hierarchy too!
                    if (line.startsWith("IPR")) {
                        // Moving on to a new hierarchy, e.g. "IPR000014" or "IPR000020"
                        entryAc = line;
                        hierarchyLevel = 0;
                        if (entriesInSameHierarchy != null) {
                            // Starting on a new hierarchy, e.g. "IPR000020", done with the previous hierarchy so tidy up
                            for (String ac : entriesInSameHierarchy) {
                                if (entryHierarchyMap.containsKey(ac)) {
                                    entryHierarchyMap.get(ac).setEntriesInSameHierarchy(entriesInSameHierarchy);
                                }
                            }
                        } // Else first entry in the text file, e.g. "IPR000014"
                        entriesInSameHierarchy = new HashSet<String>();
                        entriesInSameHierarchy.add(entryAc);
                    }
                    else {
                        // Part way through a hierarchy, e.g. "--IPR013655"
                        entryAc = line.substring(line.indexOf("IPR"));
                        hierarchyLevel = line.indexOf("IPR") / 2;
                        entriesInSameHierarchy.add(entryAc);
                    }
                    EntryHierarchyData data = new EntryHierarchyData(entryAc, hierarchyLevel);
                    entryHierarchyMap.put(entryAc, data);
                }
                else {
                    LOGGER.warn("Ignoring line in unexpected format: " + line);
                }
            }
            // End of the text file, don't forget to add the final hierarchy!
            for (String ac : entriesInSameHierarchy) {
                if (entryHierarchyMap.containsKey(ac)) {
                    entryHierarchyMap.get(ac).setEntriesInSameHierarchy(entriesInSameHierarchy);
                }
            }

        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }


        return Collections.unmodifiableMap(entryHierarchyMap);
    }
}
