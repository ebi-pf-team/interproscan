package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public class DummyProteinDAOImpl implements uk.ac.ebi.interpro.scan.persistence.ProteinDAO {
    @Override
    public Protein getProteinAndCrossReferencesByProteinId(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Protein> getProteinsBetweenIds(long bottom, long top) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Protein> getProteinsByIds(Set<Long> proteinIds) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PersistedProteins insertNewProteins(Collection<Protein> newProteins) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Protein> getProteinsAndMatchesAndCrossReferencesBetweenIds(long bottom, long top) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Protein insert(Protein newInstance) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Protein> insert(Collection<Protein> newInstances) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(Protein modifiedInstance) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Protein read(Long id) {
        return null;
    }

    @Override
    public Protein readDeep(Long id, String... deepFields) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(Protein persistentObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long count() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Protein> retrieveAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int deleteAll() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getMaximumPrimaryKey() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void flush() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
