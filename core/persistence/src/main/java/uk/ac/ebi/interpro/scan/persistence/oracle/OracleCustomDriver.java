package uk.ac.ebi.interpro.scan.persistence.oracle;

//import oracle.jdbc.OracleDriver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Oracle driver that automatically sets the database to use binary values
 * for floating point numbers
 * Necessary to avoid underflow exceptions when storing evalues lower than 1e-38
 * Must be used in conjunction with OracleCustomDriver
 *
 *  @author Craig McAnulla
 * @version $Id$
 */
public class OracleCustomDriver {

    /**
     * *
     *      * TODO remove this after the relase
     *
public class OracleCustomDriver extends OracleDriver{




    public OracleCustomDriver() {
        super();

    }

    @Override
    public Connection connect(String s, Properties properties) throws SQLException {
        if (properties == null ) {
            properties = new Properties();
        }
        properties.setProperty(SetFloatAndDoubleUseBinary_string, "true");
        return super.connect(s, properties);
    }

     */

}
