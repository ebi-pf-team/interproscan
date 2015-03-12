package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * <p/>
 * Interface defining a SubMaster to allow networks of JMS brokers to be
 * built for i5. This is to ensure that each broker has a limited number of connections.
 */
public interface SubMaster {


    public void setMaximumIdleTimeSeconds(Long maximumIdleTime);

    public void setMaximumLifeSeconds(Long maximumLife);


}
