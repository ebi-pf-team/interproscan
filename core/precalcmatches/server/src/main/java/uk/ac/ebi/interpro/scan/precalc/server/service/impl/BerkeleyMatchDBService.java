package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

/**
 * Initializes the SleepyCat database for read only.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyMatchDBService extends AbstractDBService {

    private SecondaryIndex<String, Long, BerkeleyMatch> secIDX = null;

    public BerkeleyMatchDBService(String databasePath) {
        super(databasePath);
    }

    SecondaryIndex<String, Long, BerkeleyMatch> getMD5Index() {
        return secIDX;
    }

    protected void initializeIndex() {
        super.initializeIndexCommon();
        PrimaryIndex<Long, BerkeleyMatch> primIDX = store.getPrimaryIndex(Long.class, BerkeleyMatch.class);
        secIDX = store.getSecondaryIndex(primIDX, String.class, "proteinMD5");
    }
}
