package uk.ac.ebi.interpro.scan.batch.partition.proactive;

/**
 * Utility class for writing messages to log file.
 *
 * @author  Antony Quinn
 * @version $Id: LogSupport.java,v 1.1 2009/06/18 15:08:37 aquinn Exp $
 * @since   1.0
 */
class LogSupport {

    private static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(LogSupport.class);

    public void info(String message)  {
        LOGGER.info(message);
    }

}