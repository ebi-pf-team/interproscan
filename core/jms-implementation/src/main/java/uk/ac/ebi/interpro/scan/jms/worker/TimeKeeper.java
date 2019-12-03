package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TimeKeeper {

    private static final Logger LOGGER = Logger.getLogger(TimeKeeper.class.getName());

    private Long startUpTime;
    private Long maximumLifeMillis;

    public void start(Long startUpTime, Long maximumLifeMillis){
        this.startUpTime = startUpTime;
        this.maximumLifeMillis = maximumLifeMillis;
        checkIfEndOfLifeSpan();
    }
    /**
     *
     * @return  exceededLifespan
     */
    private void checkIfEndOfLifeSpan(){
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            public void run() {
                Long timeToSleep = maximumLifeMillis - (System.currentTimeMillis() - startUpTime);
                Utilities.verboseLog("TimeToLive: " + (maximumLifeMillis - (System.currentTimeMillis() - startUpTime))
                        + " maximumLifeMillis: " + maximumLifeMillis
                        + " Now: " + System.currentTimeMillis()
                        + " startUpTime: " + startUpTime
                        + " timetoSleep: " + timeToSleep);

                while(System.currentTimeMillis() - startUpTime < maximumLifeMillis){
                    timeToSleep = maximumLifeMillis - (System.currentTimeMillis() - startUpTime);
                    Utilities.verboseLog("Time To sleep/live: " + timeToSleep);
                    try {
                        Thread.sleep(timeToSleep + (10 * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                if(System.currentTimeMillis() - startUpTime > maximumLifeMillis){
                    LOGGER.warn("Force Worker to shutdown after failing to properly shutdown ");
                    System.err.println("Failed to properly shutdown the worker!");
                    System.exit(99);
                }else{
                   LOGGER.warn("Failed to properly shutdown the worker! ");
                    System.err.println("Failed to properly shutdown the worker!");
                    System.exit(99);
                }
            }
        });
    }


}
