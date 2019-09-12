package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import uk.ac.ebi.interpro.scan.model.Protein;

import org.iq80.leveldb.DB;
import uk.ac.ebi.interpro.scan.persistence.kvstore.KVDB;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    public Protein getProteinById(Long proteinId) {
        return null;
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

    @Override
    public DB getLevelDBStore(){
        return null;
    }

    @Override
    public void checkKVDBStores(){

    }

    public void closeKVDBStores(){
        
    }
    @Override
    public List<Protein> getProteinsWithoutLookupHitBetweenIds(long bottom, long top) {
        return null;
    }

    @Override
    public Set<Protein> getProteinsWithoutLookupHit() {
        return null;
    }

    @Override
    public void setProteinIdsWithoutLookupHit(Map<Long, Protein> proteinIdsWithoutLookupHit) {

    }

    @Override
    public Map<String, Protein> getKeyToProteinMap()  throws Exception{
        return null;
    }

    @Override
    public List<Protein> getProteins(long bottom, long top) {
        return null;
    }

    @Override
    public List<Protein> getProteinsNotInLookup() throws Exception{
        return null;
    }

    @Override
    public List<Protein> getProteins() throws Exception{
        return null;
    }

    @Override
    public Protein getProteinNotInLookup(String key) {
        return null;
    }

    @Override
    public Protein getProtein(String key) {
        return null;
    }

    @Override
    public void persistProteinNotInLookup(byte[] key, byte[] protein) {

    }

    @Override
    public void persist(byte[] key, byte[] protein){

    }

    @Override
    public void insertProteinNotInLookup(String key, Protein protein) {

    }

    @Override
    public void insert(String key, Protein protein) {

    }

    @Override
    public void persist(final Map<String, Protein> keyToProteinMap) {

    }

    @Override
    public void persist(String key, Protein value){

    }
    //
    @Override
    public KVDB getDbStore(){
        return null;
    }
//
    @Override
    public Protein get(String key){
        return null;
    }

}
