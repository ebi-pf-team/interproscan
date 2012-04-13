package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.persist.PrimaryIndex;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyConsideredProtein;

/**
 * Initializes the SleepyCat database for read only.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyMD5DBService extends AbstractDBService {

    private PrimaryIndex<String, BerkeleyConsideredProtein> primIDX = null;

    public BerkeleyMD5DBService(String databasePath) {
        super(databasePath);
    }

    public PrimaryIndex<String, BerkeleyConsideredProtein> getPrimIDX() {
        return primIDX;
    }

    protected void initializeIndex() {
        super.initializeIndexCommon();
        primIDX = store.getPrimaryIndex(String.class, BerkeleyConsideredProtein.class);
    }
}
