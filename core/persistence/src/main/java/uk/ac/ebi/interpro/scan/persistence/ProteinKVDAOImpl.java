package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Protein;

import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;

/**
 * Filtered match data KV access object.
 *
 * @author Gift Nuka
 * @version $Id$
 */
public class ProteinKVDAOImpl extends GenericDAOImpl<Protein, Long> implements ProteinKVDAO  {

    protected LevelDBStore levelDBStore;

    public ProteinKVDAOImpl() {
        super(Protein.class);
    }

    /**
     * Persists filtered protein matches.
     *
     * @param keyToProteinMap Filtered protein matches with their keyIds.
     */
    @Transactional
    public void persist(final Map<String, Protein> keyToProteinMap){

    }

    @Transactional
    @Override
    public void insert(String key, Protein protein) {
        levelDBStore.put(key,levelDBStore.serialize(protein));
        //return protein;
    }

    @Transactional
    public void persist(byte[] key, byte[] protein){
        levelDBStore.put(key,protein);
    }
 
    @Transactional
    public Protein getProtein(String key){
        return levelDBStore.asProtein(levelDBStore.get(key));
    }

    @Transactional(readOnly = true)
    public List<Protein> getProteins(){
        List<Protein> proteins = new ArrayList<Protein>();
        Collection<byte[]> allByteProteins = levelDBStore.getAllElements().values();
        for (byte[] byteProtein: allByteProteins){
	  Protein protein = levelDBStore.asProtein(byteProtein);
          proteins.add(protein);
	}
        return proteins;
    }

    @Transactional(readOnly = true)
    public Map<String, Protein> getKeyToProteinMap(){
        Map<String, Protein> keyToProteinMap = new HashMap<>();
	Map<byte[], byte[]> allByteProteins = levelDBStore.getAllElements();
        Iterator it = allByteProteins.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = levelDBStore.asString((byte[]) pair.getKey()); 
            Protein protein = levelDBStore.asProtein((byte[]) pair.getValue());
            //Utilities.verboseLog(" key:" + key + " protein: " + protein.getId());
            keyToProteinMap.put(key, protein);
        }              
        return keyToProteinMap;
    }

    public void setLevelDBStore(LevelDBStore levelDBStore) {
        this.levelDBStore = levelDBStore;
    }

    public void closeDB(){
        levelDBStore.close();
    }

}
