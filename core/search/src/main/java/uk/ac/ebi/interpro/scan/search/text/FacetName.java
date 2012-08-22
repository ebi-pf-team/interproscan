package uk.ac.ebi.interpro.scan.search.text;

/**
 * Recognised EBI search facet names.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum FacetName {

    DOMAIN_SOURCE("domain_source", "EBI search domain source - e.g. INTERPRO for only searching within InterPro!", false),
    TYPE("type" , "Entry type", true),
    DB("db", "Member database code", true);

    private final String name; // As required by external services EBI search web service (indexing based on provided InterPro XML)
    private final String description;
    private final boolean isInterProSpecific; // Is it an InterPro facet or external services facet?

    FacetName(String name, String description, boolean isInterProSpecific) {
        this.name = name;
        this.description = description;
        this.isInterProSpecific = isInterProSpecific;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInterProSpecific() {
        return isInterProSpecific;
    }

    @Override
    public String toString() {
        return name;
    }
}
