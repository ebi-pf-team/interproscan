package uk.ac.ebi.interpro.scan.jms.agent;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Gift Nuka, Phil Jones.
 */
public interface Manager extends Agent {

    /**
     * The "tier" is the distance from the Master.
     * The Master is 0.  Moving out from that, first level
     * SubMaster is 1, second level is 2 etc.
     *
     * @param tier an integer representing the distance of the Manager from the Master.
     */
    public void setTier(int tier);

    /**
     * This parameter is used to determine the type of Agent this Manager
     * should spawn. If the tier of this Manager equals the maxTier
     * value, then this Manager should spawn Workers, otherwise is spawns Managers.
     *
     * @param maxTier the maximum tier level for this network of brokers.
     */
    public void setMaxTier(int maxTier);
}
