package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Sep 15, 2010
 */

public interface SmartHmmer2RawMatchDAO
        extends RawMatchDAO<SmartRawMatch>{

    public Map<String, RawProtein<SmartRawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease);

}
