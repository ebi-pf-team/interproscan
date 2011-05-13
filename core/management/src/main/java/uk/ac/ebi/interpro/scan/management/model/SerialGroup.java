package uk.ac.ebi.interpro.scan.management.model;

/**
 * Enum of 'Serial Groups'.  These are groups
 * of Steps where StepInstances in the same group
 * cannot be run (submitted) concurrently.
 * <p/>
 * The judgement about whether steps should be in the same
 * serial group depends upon database locking concerns.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public enum SerialGroup {

    LOAD_PROTEINS,
    WRITE_FASTA_FILE,
    PARSE_HMMER_3,
    PARSE_PHOBIUS,
    PARSE_PRINTS,
    PARSE_HAMAP,
    PARSE_SMART,
    PARSE_TIGRFAM,
    PARSE_PANTHER,
    PARSE_PIRSF,
    PARSE_PRODOM,
    PARSE_PROSITE_PROFILES,
    PARSE_PROSITE_PATTERNS,
    PFAM_A_POST_PROCESSING,
    PRINTS_POST_PROCESSING,
    HAMAP_POST_PROCESSING,
    PANTHER_POST_PROCESSING,
    PIRSF_POST_PROCESSING,
    PRODOM_POST_PROCESSING,
    PROSITE_PROFILES_POST_PROCESSING,
    PROSITE_PATTERNS_POST_PROCESSING,
    TIGRFAM_POST_PROCESSING,
    SMART_POST_PROCESSING,
    GENE_3D_FILTERING

}
