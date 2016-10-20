package uk.ac.ebi.interpro.scan.web.model;

import java.util.*;

/**
 * InterPro entry type.
 *
 * @author Antony Quinn
 * @version $Id$
 * @see uk.ac.ebi.interpro.scan.model.EntryType
 */
public enum EntryType {

    // Note: protein page orders matches according to order of enums here!
    FAMILY("Family"),
    DOMAIN("Domain"),
    REPEAT("Repeat"),
    SITE("Active_site", "Binding_site", "Conserved_site", "PTM"),
    UNKNOWN("Unknown");

    private final String name;
    private final List<String> alternativeNames = new ArrayList<String>();

    private static final Map<String, EntryType> TYPE_NAME_TO_TYPE = new HashMap<String, EntryType>();

    // Optimised lookup of types by name or alternative name.
    static {
        for (EntryType m : EntryType.values()) {
            TYPE_NAME_TO_TYPE.put(m.name(), m);
            TYPE_NAME_TO_TYPE.put(m.toString(), m);
            TYPE_NAME_TO_TYPE.put(m.toString().toLowerCase(), m);
            for (String alternativeName : m.alternativeNames) {
                TYPE_NAME_TO_TYPE.put(alternativeName, m);
                TYPE_NAME_TO_TYPE.put(alternativeName.toUpperCase(), m);
                TYPE_NAME_TO_TYPE.put(alternativeName.toLowerCase(), m);
            }
        }
    }


    EntryType(String name) {
        this.name = name;
    }

    /**
     * Method that takes any uk.ac.ebi.interpro.scan.model.EntryType and
     * returns the appropriate uk.ac.ebi.interpro.scan.web.model.EntryType
     *
     * @param modelEntryType to convert
     * @return the appropriate EntryType from the web module.
     */
    public static EntryType mapFromModelEntryType(uk.ac.ebi.interpro.scan.model.EntryType modelEntryType) {
        switch (modelEntryType) {
            case ACTIVE_SITE:
            case BINDING_SITE:
            case CONSERVED_SITE:
            case PTM:
                return SITE;

            case DOMAIN:
                return DOMAIN;

            case FAMILY:
                return FAMILY;

            case REPEAT:
                return REPEAT;

            case UNKNOWN:
            default:
                return UNKNOWN;
        }
    }

    EntryType(String... names) {
        this.name = name(); // Default name (see java.lang.Enum)
        // Store list of alternative names
        this.alternativeNames.addAll(Arrays.asList(names));
    }

    public static EntryType parseName(String name) {
        final EntryType type = TYPE_NAME_TO_TYPE.get(name);
        return (type == null) ? UNKNOWN : type;
        // In rare cases, may get IPR that's not been released yet, for example, the following
        // returned "IPR001438 does not exist. IPR001438 is not released" on 20 October 2011:
        // http://www.ebi.ac.uk/interpro/entry/IPR001438
    }

    @Override
    public String toString() {
        return name;
    }

    public List<String> getAlternativeNames() {
        return alternativeNames;
    }
}
