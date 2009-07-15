package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 10:52:32
 *
 * @author Phil Jones, EMBL-EBI
 */
public enum TransactionPriorityCriteria {

    /**
     * Choose proteins that have been in the database the 'longest'
     * (i.e. have the lowest primary key value)
     *
     * This is the default choice as used in production / "Onion" mode.
     */
    LOWEST_PROTEIN_ID,

    /**
     * Choose the most urgent proteins and models.  Could be used
     * to drive InterProScan mode, where submitted searches are given
     * a high priority.
     */
    HIGHEST_URGENCY
}
