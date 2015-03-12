package uk.ac.ebi.interpro.scan.persistence.oracle;

import org.hibernate.dialect.Oracle10gDialect;

import java.sql.Types;

/**
 * Conforms to the Oracle10gDialect except that Java doubles are mapped to
 * to the Oracle type BINARY_DOUBLE
 * Necessary to avoid underflow exceptions when storing evalues lower than 1e-38
 * Must be used in conjunction with OracleCustomDriver
 *
 *  @author Craig McAnulla
 * @version $Id$
 */
public class OracleCustomDialect extends Oracle10gDialect {


    public OracleCustomDialect()   {

        super();
        registerColumnType(Types.DOUBLE, "binary_double");

    }

}
