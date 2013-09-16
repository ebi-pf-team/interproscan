package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Interface for the Master application.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */
public interface Master extends Runnable {

    /**
     * Optionally, set the analyses that should be run.
     * If not set, or set to null, all analyses will be run.
     *
     * @param analyses a comma separated list of analyses (job names) that should be run. Null for all jobs.
     */
    void setAnalyses(String[] analyses);

    /**
     * Get the customised temporary directory (could be default, set on the command line or in the
     * interproscan.properties).
     * @return The temporary directory
     */
    String getTemporaryDirectory();

    /**
     * Parameter passed in on command line to set a customised temporary directory.
     *
     * @param temporaryDirectory Specified as a command-line option. Will overwrite the default ([I5-home]/temp) one.
     */
    void setTemporaryDirectory(String temporaryDirectory);

}
