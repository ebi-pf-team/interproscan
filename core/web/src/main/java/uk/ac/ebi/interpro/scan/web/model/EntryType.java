package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 * @see    uk.ac.ebi.interpro.scan.model.EntryType
 */
public enum EntryType {

    // Note: protein page orders matches according to order of enums here!
    FAMILY("Family"),
    DOMAIN("Domain"),
    REGION("Region"),    
    REPEAT("Repeat"),
    SITE("Active_site", "Binding_site", "Conserved_site", "PTM"),
    UNKNOWN("Unknown");

    private final String name;
    private final List<String> alternativeNames = new ArrayList<String>();

    private EntryType(String name) {
        this.name = name;
    }
    
    private EntryType(String... names) {
        this.name = name(); // Default name (see java.lang.Enum)
        // Store list of alternative names
        this.alternativeNames.addAll(Arrays.asList(names));
    }

    public static EntryType parseName(String name)  {
        for (EntryType m : EntryType.values()) {
            if (name.equals(m.toString()))   {
                return m;
            }
            if (m.alternativeNames.contains(name)) {
                return m;
            }
        }
        // In rare cases, may get IPR that's not been released yet, for example, the following
        // returned "IPR001438 does not exist. IPR001438 is not released" on 20 October 2011:
        // http://www.ebi.ac.uk/interpro/IEntry?ac=IPR001438
        return UNKNOWN;
    }

    @Override public String toString() {
        return name;
    }

}
