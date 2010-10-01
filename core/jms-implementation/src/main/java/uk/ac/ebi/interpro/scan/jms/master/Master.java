package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Interface for the Master application.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */
public interface Master extends Runnable {
    void setFastaFilePath(String fastaFilePath);

    void setOutputFile(String outputFile);

    void setOutputFormat(String outputFormat);

    void setAnalyses(String[] analyses);

    void setMapToInterProEntries(boolean mapToInterPro);

    void setMapToGOAnnotations(boolean mapToGO);

    public void setCleanDatabase(boolean clean);
}
