package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.persist.EntityCursor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of service that uses the BerkeleyDB as a backend.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MatchesServiceImpl implements MatchesService {

    private static final Logger LOGGER = Logger.getLogger(MatchesServiceImpl.class.getName());

    /**
     * Secondary Index to allow the BerkeleyDB (Sleepycat) to be queried
     * by MD5. Needs to be configured elsewhere.. (Spring?)
     */
    private BerkeleyDBService berkeleyDBService;

    @Autowired
    public MatchesServiceImpl(BerkeleyDBService berkeleyDBService) {
        Assert.notNull(berkeleyDBService, "'berkeleyDBService' bean must not be null");
        this.berkeleyDBService = berkeleyDBService;
    }

    /**
     * Web service request for a set of matches, based upon
     * protein MD5 sequence checksums.
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of matches for these proteins.
     */
    public List<BerkeleyMatch> getMatches(List<String> proteinMD5s) {
        EntityCursor<BerkeleyMatch> matchCursor = null;
        List<BerkeleyMatch> matches = new ArrayList<BerkeleyMatch>();

        for (String md5 : proteinMD5s) {
            System.out.println("Looking for matches for protein MD5: " + md5);
            try {
                matchCursor = berkeleyDBService.getMD5Index().entities(md5, true, md5, true);

                BerkeleyMatch currentMatch;
                while ((currentMatch = matchCursor.next()) != null) {
                    System.out.println("Found a match...");
                    matches.add(currentMatch);
                }
            } finally {
                if (matchCursor != null) {
                    matchCursor.close();
                }
            }
        }

        return matches;
    }

    /**
     * Web service request for a List of protein sequence MD5
     * checksums where the protein sequence is not
     * considered in the source of precalculated matches
     * (i.e. any returned MD5s should be run against
     * the models).
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of MD5s for proteins that have not
     *         been considered previously.
     */
    public List<String> notPrecalculated(List<String> proteinMD5s) {
        LOGGER.warn("notPrecalculated method not implemented - just returns an empty List.");
        return Collections.emptyList();
    }
}
