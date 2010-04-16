package uk.ac.ebi.interpro.scan.jms.worker;

/**
 * The WorkerManager monitors the WorkerManagerTopic, used to monitor
 * the progress of Workers and to send them management messages
 * (such as shutdown, kill etc).
 *
 * @author Phil Jones
 * @version $Id: WorkerMonitor.java,v 1.2 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public interface WorkerMonitor{

    public static final String REQUESTEE_PROPERTY = "requestee";

}
