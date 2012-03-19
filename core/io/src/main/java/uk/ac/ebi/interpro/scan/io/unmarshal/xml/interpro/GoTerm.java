package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight model to hold details of a GO term,
 * sufficient only to allow basics of a GO mapping
 * to be output with match data, limited to Root ontology name,
 * GO accession and term description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public final class GoTerm implements Serializable, Comparable<GoTerm> {

    private final GoRoot root;

    private final String accession;

    private final String termName;


    public GoTerm(String goRootName, String accession, String termName) {
        if (goRootName == null || accession == null || termName == null) {
            throw new IllegalArgumentException("No parameters may be null when instantiating a new GoTerm object. rootName: " +
                    goRootName + ", accession: " + accession + ", termName: " + termName);
        }
        this.root = GoRoot.getRootByName(goRootName);
        if (this.root == null) {
            throw new IllegalArgumentException("The GO root name " + goRootName + " is not a recognised GO root.");
        }
        this.accession = accession;
        this.termName = termName;
    }

    public GoRoot getRoot() {
        return root;
    }

    public String getAccession() {
        return accession;
    }

    public String getTermName() {
        return termName;
    }

    public int compareTo(GoTerm that) {
        if (this == that) {
            return 0;
        }
        return this.getAccession().compareTo(that.getAccession());
    }

    public static enum GoRoot {
        BIOLOGICAL_PROCESS("Biological Process"),
        CELLULAR_COMPONENT("Cellular Component"),
        MOLECULAR_FUNCTION("Molecular Function");

        private static Map<String, GoRoot> rootNameToGoRoot = new HashMap<String, GoRoot>();

        static {
            for (GoRoot goRoot : GoRoot.values()) {
                rootNameToGoRoot.put(goRoot.getRootName(), goRoot);
            }
        }

        private GoRoot(String rootName) {
            this.rootName = rootName;
        }

        private String rootName;

        public String getRootName() {
            return rootName;
        }

        public static GoRoot getRootByName(String rootName) {
            GoRoot root = rootNameToGoRoot.get(rootName);
            if (root != null) {
                return root;
            }
            // Try a more general approach approach
            for (GoRoot candidate : GoRoot.values()) {
                if (candidate.getRootName().toLowerCase().contains(rootName.toLowerCase())) {
                    return candidate;
                }
            }
            // Tried every which way - cannot determine which root has been requested.
            throw new IllegalArgumentException("Cannot determine which GO root is required from the String " + rootName);
        }
    }


    @Override
    public String toString() {
        return String.format("%s:%s (%s)", root.getRootName(), termName, accession);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoTerm goTerm = (GoTerm) o;

        if (!accession.equals(goTerm.accession)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return accession.hashCode();
    }
}
