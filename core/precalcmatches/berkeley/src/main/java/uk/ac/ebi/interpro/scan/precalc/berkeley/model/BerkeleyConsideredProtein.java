package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class is used to hold a simple Collection of MD5s for which matches
 * have been calculated (so protein
 * sequences with no matches are not recalculated.)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
public class BerkeleyConsideredProtein {

    @PrimaryKey
    private String MD5;

    /**
     * Required by BerkeleyDB
     */
    public BerkeleyConsideredProtein() {

    }

    public BerkeleyConsideredProtein(String MD5) {
        this.MD5 = MD5;
    }

    public String getMD5() {
        return MD5;
    }
}
