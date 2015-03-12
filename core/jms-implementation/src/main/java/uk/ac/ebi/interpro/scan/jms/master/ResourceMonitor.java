package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.commons.collections.functors.WhileClosure;
import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Gift Nuka
 *
 * this class track the resources the application is using on the LSF
 *
 * currently just tracking memory usage
 *
 */

public class ResourceMonitor implements  Runnable{

    private static final Logger LOGGER = Logger.getLogger(SingleSeqOptimisedBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    private String runId;

    private int timeDelay = 10;  //in seconds

    private Long maximumLifeMillis =  10 * 60 * 1000l; //Long.MAX_VALUE;

    private boolean verbose = false;


    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setTimeDelay(int timeDelay) {
        this.timeDelay = timeDelay;
    }

    public void setMaximumLifeMillis(Long maximumLifeMillis) {
        this.maximumLifeMillis = maximumLifeMillis;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @PostConstruct
    public void run(){
        displayMemoryUsage();
    }
    /**
     * display memory information
     */

    public void displayMemoryUsage(){
        final long startUpTime = System.currentTimeMillis();

        if(verbose){
            try {
                if (runId != null) {
                    Utilities.runBjobs(runId);
                }
                while (System.currentTimeMillis() - startUpTime > maximumLifeMillis){
                    System.out.println(Utilities.getTimeNow() +" ----------------------------------------------");
                    statsUtil.displayRunningJobs();
                    if (runId != null) {
                        Utilities.runBjobs(runId);
                    }
                    //statsUtil.displayMemInfo();
                    statsUtil.displayMemInfo();
                    statsUtil.displayRunningJobs();
                    if (runId != null) {
                        Utilities.runBjobs(runId);
                    }
                    //sleep for 5 seconds
                    Thread.sleep(timeDelay * 1000);
                }
            } catch (Exception ex) {
                LOGGER.warn(" Problems parsing bjobs command ...: " + ex);
            }
        }

    }
}
