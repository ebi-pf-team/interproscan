package uk.ac.ebi.interpro.scan.batch.partition.proactive;

/**
 * Utility class for writing messages to log file.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
class LogSupport {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(LogSupport.class.getName());

    public void info(String message) {
        LOGGER.info(message);
    }

}
