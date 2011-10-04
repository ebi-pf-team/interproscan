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
    LOAD_NUCLEIC_ACID,
    PARSE_GENE3D,
    PARSE_PFAM_A,
    PARSE_PHOBIUS,
    PARSE_COILS,
    PARSE_PRINTS,
    PARSE_HAMAP,
    PARSE_SMART,
    PARSE_TIGRFAM,
    PARSE_PANTHER,
    PARSE_PIRSF,
    PARSE_PRODOM,
    PARSE_PROSITE_PROFILES,
    PARSE_PROSITE_PATTERNS,
    PARSE_SIGNALP,
    PARSE_SUPERFAMILY,
    PARSE_TMHMM,
    PRINTS_POST_PROCESSING,
    PANTHER_POST_PROCESSING,
    HMMER2_POST_PROCESSING,
    HMMER3_POST_PROCESSING,
    PROFILE_SCAN_POST_PROCESSING
}
