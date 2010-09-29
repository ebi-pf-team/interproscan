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
public final class GoTerm implements Serializable {

    private final GoRoot root;

    private final String accession;

    private final String termName;


    GoTerm(String goRootName, String accession, String termName) {
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
            return rootNameToGoRoot.get(rootName);
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

        if (root != goTerm.root) return false;
        if (!accession.equals(goTerm.accession)) return false;
        if (!termName.equals(goTerm.termName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = root.hashCode();
        result = 31 * result + accession.hashCode();
        result = 31 * result + termName.hashCode();
        return result;
    }
}
