package uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * This class acts as a container to pass the list of server settings (defined in LANServerSettings class) between RunLANBroker and LANBroker.
 * (There may be a better, more 'spring-like' way of doing this, but this works for now!)
 * User: maslen
 * Date: Feb 10, 2010
 * Time: 1:55:31 PM
 */

public class LANServerListing {

    private List<LANServerSettings> listServerSettings;

    @Required
    public void setListServerSettings(List<LANServerSettings> listServerSettings) {
        this.listServerSettings = listServerSettings;
    }

    public List<LANServerSettings> getListServerSettings() {
        return listServerSettings;
    }
}
