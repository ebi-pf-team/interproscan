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
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

@XmlRootElement(name = "kvSequenceEntryXML")
@XmlType(name = "KVSequenceEntryType")
public class KVSequenceEntryXML {

    private List<KVSequenceEntry> matches = new ArrayList<>();

    public KVSequenceEntryXML() {
    }

    public KVSequenceEntryXML(List<KVSequenceEntry> matches) {
        this.matches = matches;
    }

    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "matches")
    // XmlElement sets the name of the entities
    @XmlElement(name = "match")
    public List<KVSequenceEntry> getMatches() {
        return matches;
    }
}
