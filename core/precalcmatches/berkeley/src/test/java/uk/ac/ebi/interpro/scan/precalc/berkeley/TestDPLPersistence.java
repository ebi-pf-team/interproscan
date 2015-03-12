package uk.ac.ebi.interpro.scan.precalc.berkeley;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.*;
import org.junit.Ignore;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.UUID;

/**
 * This test (just a main class, not a Junit test) has been developed
 * to test the scalability of BerkeleyDB for precalculated match lookup.
 * <p/>
 * Creates 20 million random matches and allows the speed of lookup to be tested.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Ignore
public class TestDPLPersistence {


    private static final int MATCH_NUMBER = 20 * 1000 * 1000; // The number of matches currently in Onion.
//    private static final int MATCH_NUMBER = 1000;

    private static final int SELECT_INTERVAL = 100 * 1000;

    private static final int MATCHES_PER_PROTEIN = 4;

    private static final NumberFormat format = DecimalFormat.getInstance();

    public static void main(String[] args) {


        Environment myEnv = null;
        EntityStore store = null;
        EntityCursor<BerkeleyMatch> matchCursor = null;

        try {
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();

            myEnvConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(false);

            File file = new File("./target/BIG");
            file.mkdirs();
            // Open the environment and entity store
            myEnv = new Environment(file, myEnvConfig);
            store = new EntityStore(myEnv, "EntityStore", storeConfig);


            PrimaryIndex<Long, BerkeleyMatch> primIDX = store.getPrimaryIndex(Long.class, BerkeleyMatch.class);
            SecondaryIndex<String, Long, BerkeleyMatch> secIDX = store.getSecondaryIndex(primIDX, String.class, "proteinMD5");

            String proteinMD5 = UUID.randomUUID().toString();

            // Need to pick a random accession.

            final Random randomizer = new Random();
            int randomValue = randomizer.nextInt(SELECT_INTERVAL);
            String randomlySelectedProteinMD5 = null;
            // Create some random Matches
            for (int i = 0; i < MATCH_NUMBER; i++) {
                if (i % MATCHES_PER_PROTEIN == 0) {
                    proteinMD5 = UUID.randomUUID().toString();
//                    System.out.println(proteinMD5);
                }
                if (i == randomValue) {
                    randomlySelectedProteinMD5 = proteinMD5;
                }
                BerkeleyMatch match = new BerkeleyMatch();
                match.setProteinMD5(proteinMD5);
                match.setSignatureAccession(UUID.randomUUID().toString().substring(0, 6));
                match.setSequenceEValue(randomizer.nextDouble());
                match.setSequenceScore(randomizer.nextDouble());
                for (int j = 0; j < 4; j++) {
                    BerkeleyLocation location = new BerkeleyLocation();
                    if (i % 50 == 0) {
                        location.setCigarAlignment(UUID.randomUUID().toString());
                    }
                    if (i % 2 == 0) {
                        location.seteValue(randomizer.nextDouble());
                    }
                    Random rand = new Random();
                    location.setStart(rand.nextInt());
                    location.setEnd(rand.nextInt());
                    match.addLocation(location);

                }
                primIDX.put(match);

                if (i % SELECT_INTERVAL == 0 && i > 0) {
                    System.out.println("Following storage of " + i + " matches:");
                    long startTimeMilli = System.currentTimeMillis();

                    matchCursor = secIDX.entities(randomlySelectedProteinMD5, true, randomlySelectedProteinMD5, true);
                    BerkeleyMatch currentMatch;

                    while ((currentMatch = matchCursor.next()) != null) {
                        System.out.println("currentMatch = " + currentMatch);
                    }

                    long endTimeMilli = System.currentTimeMillis();
                    System.out.println("Time for complete query & iteration: " + (endTimeMilli - startTimeMilli) + " ms.");
                    matchCursor.close();
                    // Pick a random protein out of the next lot being inserted.
                    randomValue = randomizer.nextInt(SELECT_INTERVAL) + i;
                }
            }

//            System.out.println(randomlySelectedProteinMD5 + " key found? " + secIDX.contains(randomlySelectedProteinMD5));

//            System.out.println("secIDX.count() = " + secIDX.count());


        } catch (DatabaseException dbe) {
            System.err.println("Error opening environment and store: " +
                    dbe.toString());
            System.exit(-1);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (DatabaseException dbe) {
                    System.err.println("Error closing store: " +
                            dbe.toString());
                    System.exit(-1);
                }
            }

            if (myEnv != null) {
                try {
                    // Finally, close environment.
                    myEnv.close();
                } catch (DatabaseException dbe) {
                    System.err.println("Error closing MyDbEnv: " +
                            dbe.toString());
                    System.exit(-1);
                }
            }
        }
    }
}
