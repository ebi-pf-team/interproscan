package uk.ac.ebi.interpro.scan.jms.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * @author Phil Jones
 *         Date: 20/09/11
 *         This implementation does nothing and will be thrown away after initialization.
 *         <p/>
 *         This is for use in the command-line distributed worker, which creates
 *         a ConnectionFactory at runtime, based upon parameters passed in on
 *         the command line.  The instance of this class will then be replaced with
 *         a real ConnectionFactory, hence the NOOP method implementations.
 */
public class PlaceholderConnectionFactory implements ConnectionFactory {
    @Override
    public Connection createConnection() throws JMSException {
        return null;
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        return null;
    }
}
