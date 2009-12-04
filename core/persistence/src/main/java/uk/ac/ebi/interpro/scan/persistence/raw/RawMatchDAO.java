package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Nov 30, 2009
 * Time: 5:28:56 PM
 */
public interface RawMatchDAO{


    /**
     * DAO method that inserts RawMatches contained within a RawProtein object.
     * Note that this object is NOT persisted.
     * @param parsedResults being a Set of RawProtein objects.  These objects
     * contain a Collection of RawMatch objects to be persisted.
     */
    @Transactional
    public <T extends RawMatch> void insertRawSequenceIdentifiers(Set<RawProtein<T>> parsedResults);
}
