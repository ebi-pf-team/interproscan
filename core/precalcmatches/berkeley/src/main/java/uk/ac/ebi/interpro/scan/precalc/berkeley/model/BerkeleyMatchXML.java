package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is NOT persisted to the Berkeley database.
 * <p/>
 * It is <b>only</b> used to structure the XML for serialising the
 * match data from the match lookup webservice.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@XmlRootElement(name = "berkeleyMatchXML")
@XmlType(name = "BerkeleyMatchXMLType")
public class BerkeleyMatchXML {

    private List<BerkeleyMatch> matches = new ArrayList<BerkeleyMatch>();

    public BerkeleyMatchXML() {
    }

    public BerkeleyMatchXML(List<BerkeleyMatch> matches) {
        this.matches = matches;
    }

    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "matches")
    // XmlElement sets the name of the entities
    @XmlElement(name = "match")
    public List<BerkeleyMatch> getMatches() {
        return matches;
    }
}
