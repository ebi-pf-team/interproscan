package uk.ac.ebi.interpro.scan.management.model;

/**
 * Enum of 'Serial Groups'.  These are groups
 * of Steps where StepInstances in the same group
 * cannot be run (submitted) concurrently.
 *
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
    PFAM_A_POST_PROCESSING,
    GENE_3D_FILTERING
    
}
