package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.persist.EntityCursor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.server.service.MatchesService;

import uk.ac.ebi.interpro.scan.util.Utilities;
import java.lang.InterruptedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import java.util.Timer;
import java.util.TimerTask;

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

    private BerkeleySiteDBService berkeleySiteDBService;

    /**
     * Primary index to allow the BerkeleyDB MD5 database to be
     * queried by MD5.
     */
    private BerkeleyMD5DBService berkeleyMD5Service;

    private String interproscanVersion;

    private String serviceName;
    private Timer timer = new Timer ();

    private AtomicInteger   md5TotalRequests;
    private AtomicInteger   newMD5TotalCount;
    private AtomicLong  md5TotalTimeToGetMatches;

    private AtomicInteger   totalRequests;
    private AtomicInteger   md5TotalCount;
    private AtomicLong  totalTimeToGetMatches;

    private AtomicInteger   sitesTotalRequests;
    private AtomicInteger   sitesTotalCount;
    private AtomicLong  totalTimeToGetSites;

    private ReentrantLock lock;

    public MatchesServiceImpl(String interproscanVersion) {
        System.out.println(Utilities.getTimeNow() + " Starting matchservice ...");

        Assert.notNull(interproscanVersion, "Interproscan version cannot be null");
        this.interproscanVersion = interproscanVersion;

        md5TotalRequests = new AtomicInteger();
        newMD5TotalCount = new AtomicInteger();
        md5TotalTimeToGetMatches  = new AtomicLong();


        totalRequests = new AtomicInteger();
        md5TotalCount = new AtomicInteger();
        totalTimeToGetMatches  = new AtomicLong();

        sitesTotalRequests = new AtomicInteger();
        sitesTotalCount = new AtomicInteger();
        totalTimeToGetSites  = new AtomicLong();

        lock = new ReentrantLock();

        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                resetCountRequests();
            }
        };
        TimerTask envStatsTask = new TimerTask () {
            @Override
            public void run () {
                berkeleyMatchDBService.displayServerStats();
                berkeleyMD5Service.displayServerStats();
                berkeleySiteDBService.displayServerStats();
            }
        };
        // schedule the task to run starting now and then every 24 hours ...
        timer.schedule (hourlyTask, 0l, 1000 * 60 * 60 * 24);
        timer.schedule (envStatsTask, 0l, 1000 * 60 * 60 * 1);
//        timer.schedule (hourlyTask, 0l, 1000*60*5);
    }


    @Autowired
    public void setBerkeleyMatchDBService(BerkeleyMatchDBService berkeleyMatchDBService) {
        this.berkeleyMatchDBService = berkeleyMatchDBService;
    }

    @Autowired
    public void setBerkeleySiteDBService(BerkeleySiteDBService berkeleySiteDBService) {
        this.berkeleySiteDBService = berkeleySiteDBService;
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

    public void countMatchesRequests(int md5Count, long timeToGetMatches){
        if (lock.isLocked()){
          //wait for few millis 
          try {
              Thread.sleep(1000);
          } catch (InterruptedException iexc){
              //we are not so much bothered by this interuption
              //System.out.println(Utilities.getTimeNow() + " countRequests Thread.currentThread().interrupt()");
          }         
        }
        totalRequests.incrementAndGet();
        md5TotalCount.addAndGet(md5Count);
        totalTimeToGetMatches.addAndGet(timeToGetMatches);
    }

    /**
     * Web service request for a set of sites, based upon
     * protein MD5 sequence checksums.
     *
     * @param proteinMD5s md5 checksum of sequences.
     * @return a List of site matches for these proteins.
     */
    public List<KVSequenceEntry> getSites(List<String> proteinMD5s) {
        Assert.notNull(berkeleySiteDBService.getMD5Index(), "The MD5 index must not be null.");
        List<KVSequenceEntry> sites = new ArrayList<>();

        for (String md5 : proteinMD5s) {
            EntityCursor<KVSequenceEntry> siteCursor = null;
            try {
                siteCursor = berkeleySiteDBService.getMD5Index().entities(md5, true, md5, true);

                KVSequenceEntry currentMatch;
                while ((currentMatch = siteCursor.next()) != null) {
                    sites.add(currentMatch);
                }
            } finally {
                if (siteCursor != null) {
                    siteCursor.close();
                }
            }
        }

        return sites;
    }


    @Override
    public void countMD5Requests(int md5Count, long timeToGetMatches){
        if (lock.isLocked()){
            //wait for few millis
            try {
                Thread.sleep(1000);
            } catch (InterruptedException iexc){
                //we are not so much bothered by this interuption
                //System.out.println(Utilities.getTimeNow() + " countRequests Thread.currentThread().interrupt()");
            }
        }
        md5TotalRequests.incrementAndGet();
        newMD5TotalCount.addAndGet(md5Count);
        md5TotalTimeToGetMatches.addAndGet(timeToGetMatches);
    }

    public void resetCountRequests(){

        int hourlyMD5TotalRequests = 0;
        int hourlyMD5Md5TotalCount = 0;
        long hourlyMD5TotalTimeToGetMatches = 0l;

       int hourlyTotalRequests = 0;
       int hourlyMd5TotalCount = 0;
       long hourlyTotalTimeToGetMatches = 0l;

       lock.lock();
       try {
         hourlyTotalRequests = totalRequests.getAndSet(0);
         hourlyMd5TotalCount = md5TotalCount.getAndSet(0);
         hourlyTotalTimeToGetMatches = totalTimeToGetMatches.getAndSet(0);

//         int ihourlyTotalRequests = totalRequests.get();
//         int ihourlyMd5TotalCount = md5TotalCount.get();
//         long ihourlyTotalTimeToGetMatches = totalTimeToGetMatches.get();

//         String outMessage1 = Utilities.getTimeNow() + " test... " + this.serviceName + " request_count: " + ihourlyTotalRequests + " " + ihourlyMd5TotalCount  + " " + ihourlyTotalTimeToGetMatches;

         //System.out.println(outMessage1);
       } finally {
         lock.unlock();
       }

        lock.lock();
        try {
            hourlyMD5TotalRequests = md5TotalRequests.getAndSet(0);
            hourlyMD5Md5TotalCount = newMD5TotalCount.getAndSet(0);
            hourlyMD5TotalTimeToGetMatches = md5TotalTimeToGetMatches.getAndSet(0);

//            int ihourlyMD5TotalRequests = md5TotalRequests.get();
//            int ihourlyMD5Md5TotalCount = newMD5TotalCount.get();
//            long ihourlyMD5TotalTimeToGetMatches = md5TotalTimeToGetMatches.get();
//
//            String outMessage2 = Utilities.getTimeNow() + " test... " + this.serviceName + " request_count: " + ihourlyMD5TotalRequests + " " + ihourlyMD5Md5TotalCount  + " " + ihourlyMD5TotalTimeToGetMatches;
//
//            System.out.println(outMessage2);
        } finally {
            lock.unlock();
        }

       //log the hourly values
        // " " + this.serviceName +
       String outMessage = " match_counts: " + hourlyTotalRequests + " " + hourlyMd5TotalCount  + " " + hourlyTotalTimeToGetMatches +
               " md5_counts: " + hourlyMD5TotalRequests + " " + hourlyMD5Md5TotalCount  + " " + hourlyMD5TotalTimeToGetMatches;
        if (hourlyTotalRequests > 0) {
            //not yet for jetty, but should it not say its alive
            System.out.println(Utilities.getTimeNow() + outMessage);
        }


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
        berkeleySiteDBService.shutdown();
    }


    public void setName(String serviceName){
        this.serviceName = serviceName;
    }
}
