package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.util.*;

public class MatchDAOImpl extends GenericKVDAOImpl<Match> implements MatchDAO<Match> {

    private static final Logger LOGGER = LogManager.getLogger(MatchDAOImpl.class.getName());

    public MatchDAOImpl() {
        super(Match.class);
    }


    @Override
    @Transactional
    public void persist(String key,  Match match) {
        ///byte[] keyInBytes = levelDBStore.serialize(key);
        LOGGER.warn("storing data for seq: " + key + " matchid: " + match.getId()
                + " match: " +  match
                + "  to kv db .... " + dbStore.getDbName());
        byte[]  data = serialize(match);
        if (data == null){
            LOGGER.error("match has problems: key " + key + " match : " + match.toString());
        }
        dbStore.put(key, data);
        LOGGER.warn("data for seq: " + key + " stored to kv db ...." + dbStore.getDbName());
    }

    @Override
    public Match get(String key){
        byte [] byteMatch = dbStore.get(key);
        if(byteMatch != null) {
            return dbStore.asMatch(byteMatch);
        }
        return null;
    }

    @Override
    public void persist(String key, Set<Match> matches) {
        //check if this is valid
        byte[] byteMatches = dbStore.serialize((HashSet<Match>) matches);
        dbStore.put(key,byteMatches);
    }

    @Transactional(readOnly = true)
    public Set<Match> getMatchSet(String key) {
        byte[] byteMatchSet = dbStore.get(key);
        if (byteMatchSet != null){
            return dbStore.asMatchSet(byteMatchSet);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Map<String, Set<Match>> getMatchesForEachProtein() throws Exception{
        Map<String, Set<Match>> keyToMatchMap = new HashMap<>();
        List<Match> allMatches = new ArrayList<>();
        Map<byte[], byte[]> allElements = dbStore.getAllElements();
        for (byte[] byteKey : allElements.keySet()){
            byte[] byteData = allElements.get(byteKey);
            String key = dbStore.asString(byteKey);
            Set<Match> matches = dbStore.asMatchSet(byteData);
            keyToMatchMap.put(key, matches);
        }

        return keyToMatchMap;
    }

    @Transactional(readOnly = true)
    public  Set<Match> getMatches() throws Exception{
        Set<Match> allMatches = new HashSet<>();
        Map<byte[], byte[]> allElements = dbStore.getAllElements();
        for (byte[] byteKey : allElements.keySet()){
            byte[] byteData = allElements.get(byteKey);
            String key = dbStore.asString(byteKey);
            Match match = dbStore.asMatch(byteData);
            allMatches.add(match);
        }

        return allMatches;
    }


    /*
    @Transactional
    public void persist(byte[] key,  byte[] match) {
        if (key == null || match == null){
            LOGGER.error("Match(es) or key has problems:  "  );
            LOGGER.error("Problem - key: " + dbStore.asString(key));
//                    + " math or matchSet " + dbStore.(match).toString());
        }else {
            //Utilities.verboseLog(1100, "Try1 key: " + (String) SerializationUtils.deserialize( key));
            //Utilities.verboseLog(1100, "Try1 match: " + ((HashSet<Match>) SerializationUtils.deserialize( match)));
            dbStore.put(key, match);

        }
    }

    */
}
