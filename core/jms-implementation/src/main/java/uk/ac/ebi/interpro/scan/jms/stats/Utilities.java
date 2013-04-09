package uk.ac.ebi.interpro.scan.jms.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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


    /**
     * display time now
     * @return
     */
    public static String getTimeNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        String currentDate = sdf.format(cal.getTime());
        return currentDate;
    }
}
