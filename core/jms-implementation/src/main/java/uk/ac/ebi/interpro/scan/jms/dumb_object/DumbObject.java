package uk.ac.ebi.interpro.scan.jms.dumb_object;

import java.util.HashMap;
import java.util.Random;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: 09-Nov-2009
 * Time: 11:41:15
 */

public class DumbObject implements Serializable {
    private HashMap<Long, String> dumbHash = new HashMap<Long, String>();

    public DumbObject() {
        for (int i = 0; i < 10; i++) {
            Random aRand = new Random();
            Long randomLong = aRand.nextLong();
            int stringLength = 128;
            String randomString = generateRandomString(stringLength);
            dumbHash.put(randomLong, randomString);
        }
    }

    public int getSize() {
        return dumbHash.size();
    }

    private String generateRandomString(int n) {
        StringBuffer sb = new StringBuffer(n);
        int c = 'A';
        int r1 = 0;

        for (int i = 0; i < n; i++) {
            r1 = (int) (Math.random() * 3);
            switch (r1) {
                case 0:
                    c = '0' + (int) (Math.random() * 10);
                    break;
                case 1:
                    c = 'a' + (int) (Math.random() * 26);
                    break;
                case 2:
                    c = 'A' + (int) (Math.random() * 26);
                    break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

}



