package uk.ac.ebi.interpro.scan.model;

/**
 * Helper class to assist persistence of model values.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class PersistenceConversion {
    
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
     *
     * @param   x Value
     * @return antilog(x)
     */
    public static double get(double x)  {
        return Math.pow(10, x);
    }

    public static boolean equals(double a, double b){
        return Math.abs(1d - (a / b)) < ACCEPTABLE_RATIO_DIFFERENCE;
    }

}
