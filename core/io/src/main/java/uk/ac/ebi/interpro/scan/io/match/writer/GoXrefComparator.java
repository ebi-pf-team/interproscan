package uk.ac.ebi.interpro.scan.io.match.writer;

import java.util.Comparator;
import uk.ac.ebi.interpro.scan.model.*;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 21/03/13
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */


public class GoXrefComparator implements Comparator<GoXref> {

    @Override
    public int compare(GoXref o1, GoXref o2) {
        if (o1.equals(o2)) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }  else {
            return o1.getIdentifier().compareTo(o2.getIdentifier());
        }
    }
}
