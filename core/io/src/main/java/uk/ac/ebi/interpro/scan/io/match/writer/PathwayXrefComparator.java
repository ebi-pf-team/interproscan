package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.PathwayXref;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 21/03/13
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class PathwayXrefComparator implements Comparator<PathwayXref> {

    @Override
    public int compare(PathwayXref o1, PathwayXref o2) {
        if (o1.equals(o2)) {
            return 0;
        } else {
            return o1.getIdentifier().compareTo(o2.getIdentifier());
        }

    }
}
