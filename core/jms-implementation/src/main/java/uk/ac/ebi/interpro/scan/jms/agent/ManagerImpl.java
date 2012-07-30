package uk.ac.ebi.interpro.scan.jms.agent;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 30/07/12
 * Time: 15:48
 * To change this template use File | Settings | File Templates.
 */
public class ManagerImpl extends AbstractAgent implements Manager {

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The "tier" is the distance from the Master.
     * The Master is 0.  Moving out from that, first level
     * SubMaster is 1, second level is 2 etc.
     *
     * @param tier an integer representing the distance of the Manager from the Master.
     */
    public void setTier(int tier) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This parameter is used to determine the type of Agent this Manager
     * should spawn. If the tier of this Manager equals the maxTier
     * value, then this Manager should spawn Workers, otherwise is spawns Managers.
     *
     * @param maxTier the maximum tier level for this network of brokers.
     */
    public void setMaxTier(int maxTier) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
