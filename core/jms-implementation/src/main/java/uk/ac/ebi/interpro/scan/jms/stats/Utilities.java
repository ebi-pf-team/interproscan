package uk.ac.ebi.interpro.scan.jms.stats;

/**
 * Created with IntelliJ IDEA.
 * User: nuka
 * Date: 14/09/12
 * Time: 16:42
 * To change this template use File | Settings | File Templates.
 */
public class Utilities {


    public static String createUniqueJobName(int jobNameLength) {
        StringBuffer sb = new StringBuffer();
        for (int x = 0; x < jobNameLength; x++) {
            sb.append((char) ((int) (Math.random() * 26) + 97));
        }
        return sb.toString();
    }

}
