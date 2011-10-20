package uk.ac.ebi.interpro.scan.web.model;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 */
public enum MatchDataSources {

    // Structural features
    CATH, SCOP, PDB,

    // Structural predictions
    MODBASE,
    SWISSMODEL {
        @Override public String toString() {
            return "SWISS-MODEL";
        }
    };

    public static boolean isStructuralFeature(String name) {
        return (name.equals(CATH.toString()) || name.equals(SCOP.toString()) || name.equals(PDB.toString()));
    }

    public static boolean isStructuralPrediction(String name) {
        return (name.equals(MODBASE.toString()) || name.equals(SWISSMODEL.toString()));
    }

}
