package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Gift Nuka
 *
 */
public class ClusterState implements Serializable {

    /**
     *   number of jobs allowed for this grid
     *   (currently for the project)
     */
    private int gridLimit = 0;

    /**
     * total number of jobs for this cluster
     */
    private int allClusterJobsCount;

    /**
     * total number of jobs pending for this cluster
     */
    private int pendingClusterJobsCount;

    /**
     * total number of jobs for this project
     */
    private int allClusterProjectJobsCount;

    /**
     * total number of jobs pending for this project
     */
    private int pendingClusterProjectJobsCount;

    /**
     * timestamp of the current cluster state
     *
     */

    private Long lastUpdated = System.currentTimeMillis();

    /**
     *
     * @param gridLimit
     * @param allClusterProjectJobsCount
     * @param pendingClusterProjectJobsCount
     */
    public ClusterState(int gridLimit, int allClusterProjectJobsCount, int pendingClusterProjectJobsCount) {
        this.gridLimit = gridLimit;
        this.allClusterProjectJobsCount = allClusterProjectJobsCount;
        this.pendingClusterProjectJobsCount = pendingClusterProjectJobsCount;
        lastUpdated = System.currentTimeMillis();
    }

    public void setGridLimit(int gridLimit) {
        this.gridLimit = gridLimit;
    }

    public int getGridLimit() {
        return gridLimit;
    }

    public int getAllClusterJobsCount() {
        return allClusterJobsCount;
    }

    public void setAllClusterJobsCount(int allClusterJobsCount) {
        this.allClusterJobsCount = allClusterJobsCount;
    }

    public int getPendingClusterJobsCount() {
        return pendingClusterJobsCount;
    }

    public void setPendingClusterJobsCount(int pendingClusterJobsCount) {
        this.pendingClusterJobsCount = pendingClusterJobsCount;
    }

    public int getAllClusterProjectJobsCount() {
        return allClusterProjectJobsCount;
    }

    public void setAllClusterProjectJobsCount(int allClusterProjectJobsCount) {
        this.allClusterProjectJobsCount = allClusterProjectJobsCount;
    }

    public int getPendingClusterProjectJobsCount() {
        return pendingClusterProjectJobsCount;
    }

    public void setPendingClusterProjectJobsCount(int pendingClusterProjectJobsCount) {
        this.pendingClusterProjectJobsCount = pendingClusterProjectJobsCount;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * if more than 10% of all the jobs on the cluster are not running, donnot submit any more jobs
     * @return
     */
    public boolean canSubmitToCluster(){
        int runningJobs = allClusterProjectJobsCount - pendingClusterProjectJobsCount;
        if (allClusterProjectJobsCount >= gridLimit
                ||(runningJobs == 0 && pendingClusterProjectJobsCount > 5)
                ||(pendingClusterProjectJobsCount > (runningJobs * 0.1) && allClusterProjectJobsCount > 10)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer clusterState = new StringBuffer("ClusterState: " + Utilities.getTimeNow()).append("\n");
        clusterState.append( "gridJobsLimit: " + gridLimit).append(" ");
        clusterState.append("activeJobs: " + allClusterProjectJobsCount).append(" ");
        clusterState.append("pendingJobs: " + pendingClusterProjectJobsCount).append(" ");
        DateFormat df = new SimpleDateFormat("dd:MM:yy HH:mm:ss");
        clusterState.append("timestamp: " + df.format(new Date(lastUpdated)));
        return clusterState.toString();
    }

}
