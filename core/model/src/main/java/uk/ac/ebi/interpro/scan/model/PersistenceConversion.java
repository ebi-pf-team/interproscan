package uk.ac.ebi.interpro.scan.model;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Helper class to assist persistence of model values.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
public final class PersistenceConversion {

    private static final double ACCEPTABLE_RATIO_DIFFERENCE = 1E-10d;

    /**
     * Returns log(x) so x can be safely persisted.
     *
     * @param   x Value 
     * @return log(x)
     */
    public static double set(double x)  {
        return Math.log10(x);
    }

    /**
     * Returns antilog(x), the original value that was converted for persistence.
     * Rounded to 7 sig fig.
     *
     * @param   x Value
     * @return antilog(x)
     */
    public static double get(double x)  {
        BigDecimal bd = new BigDecimal(Math.pow(10, x));
        bd = bd.round(MathContext.DECIMAL32);
        return bd.doubleValue();
    }

    /**
     * For use in equals methods, where the value may have been converted to log10 and
     * back again, causing rounding errors.  Returns true if the values are only
     * minutely different.
     * @param a first value
     * @param b second value
     * @return true if the different in ratio is less than ACCEPTABLE_RATIO_DIFFERENCE
     */
    public static boolean equivalent(double a, double b){
        return Math.abs(1d - (a / b)) < ACCEPTABLE_RATIO_DIFFERENCE;
    }

}
