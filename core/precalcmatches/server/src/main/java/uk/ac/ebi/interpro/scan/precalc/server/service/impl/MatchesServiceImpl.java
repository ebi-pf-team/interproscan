package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.persist.EntityCursor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of service that uses the BerkeleyDB as a backend.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class MatchesServiceImpl implements MatchesService {

    private static final Logger LOGGER = Logger.getLogger(MatchesServiceImpl.class.getName());

    /**
     * Secondary Index to allow the BerkeleyDB (Sleepycat) to be queried
     * by MD5.
     */
    private BerkeleyMatchDBService berkeleyMatchDBService;

    /**
     * Primary index to allow the BerkeleyDB MD5 database to be
     * queried by MD5.
     */
    private BerkeleyMD5DBService berkeleyMD5Service;

    private String interproscanVersion;

    public MatchesServiceImpl(String interproscanVersion) {
        Assert.notNull(interproscanVersion, "Interproscan version cannot be null");
        this.interproscanVersion = interproscanVersion;
    }


    @Autowired
    public void setBerkeleyMatchDBService(BerkeleyMatchDBService berkeleyMatchDBService) {
        this.berkeleyMatchDBService = berkeleyMatchDBService;
    }

    @Autowired
    public void setBerkeleyMD5Service(BerkeleyMD5DBService berkeleyMD5Service) {
        this.berkeleyMD5Service = berkeleyMD5Service;
    }

    /**
     * Web service request for a set of matches, based upon
     * protein MD5 sequence checksums.
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of matches for these proteins.
     */
    public List<KVSequenceEntry> getMatches(List<String> proteinMD5s) {
        Assert.notNull(berkeleyMatchDBService.getMD5Index(), "The MD5 index must not be null.");
        List<KVSequenceEntry> matches = new ArrayList<>();

        for (String md5 : proteinMD5s) {
            EntityCursor<KVSequenceEntry> matchCursor = null;
            try {
                matchCursor = berkeleyMatchDBService.getMD5Index().entities(md5, true, md5, true);

                KVSequenceEntry currentMatch;
                while ((currentMatch = matchCursor.next()) != null) {
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
     * checksums where the protein sequence has been run through
     * the analysis pipeline and so should NOT be recalculated
     * (i.e. any returned MD5s should NOT be run against
     * the models).
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of MD5s for proteins that have been calculated previously.
     */
    public List<String> isPrecalculated(List<String> proteinMD5s) {
        Assert.notNull(berkeleyMD5Service, "The berkeleyMD5Service field is null.");
        Assert.notNull(berkeleyMD5Service.getPrimIDX(), "The berkeleyMD5Service.getPrimIDX() method is returning null.");
        List<String> md5ToCalculate = new ArrayList<String>();
        for (String md5 : proteinMD5s) {
            if (berkeleyMD5Service.getPrimIDX().get(md5) != null) {
                md5ToCalculate.add(md5);
            }
        }
        return md5ToCalculate;
    }

    /**
     * Web service request for the interproscan version on which the
     * lookup service is based. Necessary for the client to check if
     * it is in synch.
     * @return
     */
    public String getServerVersion() {
        return interproscanVersion;
    }

    /**
     * Cleanly shuts down the Berkeley DB environment.
     */
    @Override
    public void shutdown() {
        berkeleyMatchDBService.shutdown();
        berkeleyMD5Service.shutdown();
    }
}
