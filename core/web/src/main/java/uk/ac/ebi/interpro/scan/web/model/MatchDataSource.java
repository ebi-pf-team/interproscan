package uk.ac.ebi.interpro.scan.web.model;

/**
 * TODO: Add description
 *
 * @author Antony Quinn
 * @version $Id$
 */
public enum MatchDataSource {

    // Signature databases (descriptions from user manual appendices: http://www.ebi.ac.uk/interpro/user_manual.html)
    
    HAMAP("Members of HAMAP families are identified using PROSITE profile collections. " +
            "HAMAP profiles are manually created by expert curators and they identify proteins " +
            "that are part of well-conserved bacterial, archaeal and plastid-encoded proteins families " +
            "or subfamilies. The aim of HAMAP is to propagate manually generated annotation to all " +
            "members of a given protein family in an automated and controlled way using " +
            "very strict criteria.", 0),

    PFAM("Pfam", "Pfam is a collection of protein family alignments which were constructed semi-automatically " +
            "using hidden Markov models (HMMs). Sequences that were not covered by Pfam were clustered and aligned " +
            "automatically, and are released as Pfam-B. Pfam families have permanent accession numbers and contain " +
            "functional annotation and cross-references to other databases, while Pfam-B families are re-generated " +
            "at each release and are unannotated."),

    PFAM_B("PfamB", PFAM.description),

    PRINTS("PRINTS is a compendium of protein fingerprints. " +
            "A fingerprint is a group of conserved motifs used to characterise a protein family; " +
            "its diagnostic power is refined by iterative scanning of OWL. Usually the motifs do not overlap, " +
            "but are separated along a sequence, though they may be contiguous in 3D-space. " +
            "Fingerprints can encode protein folds and functionalities more flexibly and powerfully than can " +
            "single motifs: the database thus provides a useful adjunct to PROSITE.", 0),

    // PROSITE patterns' or db == 'PROSITE profiles
    PROSITE("PROSITE consists of documentation entries describing protein domains, families and functional sites, " +
            "as well as associated patterns and profiles to identify them. Profiles and patterns are constructed " +
            "from manually edited seed alignments. PROSITE is complemented by ProRule, a collection of rules based " +
            "on profiles and patterns, which increases the discriminatory power of profiles and patterns by providing " +
            "additional information about functionally and/or structurally critical amino acids.", 0),

    PROSITE_PATTERNS("PROSITE patterns", PROSITE.description),

    PROSITE_PROFILES("PROSITE profiles", PROSITE.description),

    // TODO: Add other signature databases

    // Structural features
    CATH, SCOP, PDB,

    // Structural predictions
    MODBASE,
    SWISSMODEL("SWISS-MODEL", ""),
    
    // Other
    UNKNOWN;

    private final String name;
    private final String description;

    private MatchDataSource() {
        this.name = name(); // Default name (see java.lang.Enum)
        this.description = "";
    }

    private MatchDataSource(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Dummt int just allows us to specify description without having to give name
    private MatchDataSource(String description, int dummy) {
        this.name = name(); // Default name (see java.lang.Enum)
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static MatchDataSource parseName(String name)  {
        for (MatchDataSource m : MatchDataSource.values()) {
            if (name.equals(m.toString()))   {
                return m;
            }
        }
        return UNKNOWN;
    }

    public static boolean isStructuralFeature(String name) {
        return (name.equals(CATH.toString()) || name.equals(SCOP.toString()) || name.equals(PDB.toString()));
    }

    public static boolean isStructuralPrediction(String name) {
        return (name.equals(MODBASE.toString()) || name.equals(SWISSMODEL.toString()));
    }

    @Override public String toString() {
        return name;
    }    

}
