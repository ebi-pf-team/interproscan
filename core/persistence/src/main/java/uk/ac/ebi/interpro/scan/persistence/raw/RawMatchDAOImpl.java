package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

/**
 * Performs the job of inserting raw matches contained in a RawProtein object into the database.
 *
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Nov 30, 2009
 * Time: 5:29:35 PM
 */
public class RawMatchDAOImpl implements RawMatchDAO{

    protected EntityManager entityManager;

    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected RawMatchDAOImpl() {

    }

    /**
     * DAO method that inserts RawMatches contained within a RawProtein object.
     * Note that the RawProtein object is NOT persisted.
     *
     * @param parsedResults being a Set of RawProtein objects.  These objects
     *                      contain a Collection of RawMatch objects to be persisted.
     */
    @Transactional
    public void insertRawSequenceIdentifiers(Set<RawProtein> parsedResults) {
        for (RawProtein rawSeqIdentifier : parsedResults){
            for (RawMatch newRawMatch : rawSeqIdentifier.getMatches()){
                if (entityManager.contains(newRawMatch)){
                    throw new IllegalArgumentException ("EntityManager.insert has been called on a RawMatch " + newRawMatch + " that has already been persisted.");
                }
                entityManager.persist(newRawMatch);
            }
        }
    }
}
