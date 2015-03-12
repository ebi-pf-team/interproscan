package uk.ac.ebi.interpro.scan.web.io;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a text file containing information about InterPro entry domain hierarchies.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EntryHierarchyDataResourceReader {

    private static final Logger LOGGER = Logger.getLogger(EntryHierarchyDataResourceReader.class.getName());

    private static final Pattern LINE_PATTERN = Pattern.compile("^IPR\\d{6},\\s+\\d+,\\s+(.*)$");

    public Map<String, EntryHierarchyData> read(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists()) {
            throw new IllegalStateException(resource.getURL() + " does not exist");
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }

        /*
         * Example text to parse:
         *
         * IPR000014, 1, None
         * IPR013655, 2, IPR000014
         * IPR013656, 2, IPR000014
         * IPR013767, 2, IPR000014
         * IPR000020, 1, None
         * IPR018081, 2, IPR000020
         * IPR001840, 3, IPR018081
         */
        Map<String, EntryHierarchyData> entryHierarchyMap = new HashMap<String, EntryHierarchyData>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String line;
            String entryAc;
            int hierarchyLevel;
            String parentEntryAc;
            Set<String> entriesInSameHierarchy = null;

            // Loop through and parse each line in the text file
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                Matcher lineMatcher = LINE_PATTERN.matcher(line);
                if (lineMatcher.matches()) {
                    String[] lineValues = line.split(", ");
                    if (lineValues.length == 3) {
                        // Now can get entryAc and hierarchy level from this line of text, e.g. "IPR000014, 1, None"
                        // but also need to build a list of entries that share a hierarchy too!
                        entryAc = lineValues[0];
                        hierarchyLevel = Integer.parseInt(lineValues[1]);
                        parentEntryAc = lineValues[2];
                        if (parentEntryAc.equals("None")) {
                            // This is a root entry, has no parent
                            parentEntryAc = null;
                        }
                        if (hierarchyLevel == 1) { // && parentEntryAc == null
                            // Moving on to a new hierarchy, e.g. "IPR000014" or "IPR000020"
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
                        } else {
                            // Part way through a hierarchy, e.g. "IPR013655"
                            entriesInSameHierarchy.add(entryAc);
                        }
                        final EntryHierarchyData data = new EntryHierarchyData(entryAc, hierarchyLevel, parentEntryAc);


                        entryHierarchyMap.put(entryAc, data);

                        if (hierarchyLevel == 1) {
                            data.setRootEntry(data);
                        } else {
                            // Get the parent
                            for (String rootAc : entriesInSameHierarchy) {
                                EntryHierarchyData candidate = entryHierarchyMap.get(rootAc);
                                if (candidate != null && candidate.getHierarchyLevel() == 1) {
                                    data.setRootEntry(candidate);
                                    break;
                                }
                            }
                        }

                        if (parentEntryAc != null) {
                            // This entry has a parent.  Add this entry to the parents immediate children.
                            EntryHierarchyData parent = entryHierarchyMap.get(parentEntryAc);
                            if (parent == null) {
                                throw new IllegalStateException("Attempting to retrieve a parent Entry that should have appeared in the entry hierarchy file first - however it cannot be found.");
                            }
                            parent.addImmediateChild(data);
                        }

                    } else {
                        LOGGER.warn("Ignoring line in unexpected format: " + line);
                    }
                } else {
                    if (!line.isEmpty()) {
                        LOGGER.warn("Ignoring line in unexpected format: " + line);
                    }
                }
            }
            // End of the text file, don't forget to add the final hierarchy!
            if (entriesInSameHierarchy != null) {
                for (String ac : entriesInSameHierarchy) {
                    if (entryHierarchyMap.containsKey(ac)) {
                        entryHierarchyMap.get(ac).setEntriesInSameHierarchy(entriesInSameHierarchy);
                    }
                }
            } else {
                LOGGER.warn("Resource file line format not recognised, entry hierarchy data parsing failed");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return Collections.unmodifiableMap(entryHierarchyMap);
    }
}
