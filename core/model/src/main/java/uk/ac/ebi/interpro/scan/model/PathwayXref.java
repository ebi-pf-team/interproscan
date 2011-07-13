package uk.ac.ebi.interpro.scan.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Defines a cross reference to a external pathway entry.</br>
 * E.g. UPA00098 Amino-acid biosynthesis; L-proline biosynthesis; L-glutamate 5-semialdehyde from L-glutamate: step 2/2
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@XmlType(name = "PathwayXrefType")
public class PathwayXref extends Xref implements Serializable {

    public PathwayXref(String databaseName, String identifier, String name) {
        super(databaseName, identifier, name);
    }
}