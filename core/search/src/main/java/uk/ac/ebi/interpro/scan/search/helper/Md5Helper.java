package uk.ac.ebi.interpro.scan.search.helper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * MD5 helper class.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
public final class Md5Helper {

    private static final MessageDigest m;
    private static final int HEXADECIMAL_RADIX = 16;

    static {
        try {
            m = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot find MD5 algorithm", e);
        }
    }

    // Not instantiable
    private Md5Helper() {
    }

    public static String calculateMd5(String sequence) {

        String md5;

        // As using single instance of MessageDigest, make thread safe.
        // This should be much faster than creating a new MessageDigest object
        // each time this method is called.
        synchronized (m) {
            m.reset();
            m.update(sequence.getBytes(), 0, sequence.length());
            md5 = new BigInteger(1, m.digest()).toString(HEXADECIMAL_RADIX);
        }

        return (md5.toLowerCase(Locale.ENGLISH));
    }

}