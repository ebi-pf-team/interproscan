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
    WRITE_FASTA_COILS,
    WRITE_FASTA_CDD,
    WRITE_FASTA_MOBIDB,
    WRITE_FASTA_PHOBIUS,
    WRITE_FASTA_SIGNALP_EUK,
    WRITE_FASTA_SIGNALP_GRAM_POS,
    WRITE_FASTA_SIGNALP_GRAM_NEG,
    WRITE_FASTA_FILE_TMHMM,
    LOAD_PROTEINS,
    LOAD_NUCLEIC_ACID,
    MATCH_LOOKUP,
    PARSE_ANTIFAM,
    PARSE_CDD,
    PARSE_COILS,
    PARSE_GENE3D,
    PARSE_HAMAP,
    PARSE_MOBIDB,
    PARSE_PANTHER,
    PARSE_PFAM_A,
    PARSE_PHOBIUS,
    PARSE_PIRSF,
    PARSE_PIRSR,
    PARSE_PRINTS,
    PARSE_PRODOM,
    PARSE_PROSITE_PROFILES,
    PARSE_PROSITE_PATTERNS,
    PARSE_SFLD,
    PARSE_SIGNALP,
    PARSE_SMART,
    PARSE_SUPERFAMILY,
    PARSE_TIGRFAM,
    PARSE_TMHMM,
    RPSBLAST_POST_PROCESSING,
    PRINTS_POST_PROCESSING,
    PANTHER_POST_PROCESSING,
    HMMER2_POST_PROCESSING,
    HMMER3_POST_PROCESSING,
    PROFILE_SCAN_POST_PROCESSING
}
