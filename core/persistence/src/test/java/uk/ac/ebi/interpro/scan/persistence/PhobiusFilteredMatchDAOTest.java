package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;

//import org.junit.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;

import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.PhobiusRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;


/**
 * Test of the Phobius Filtered Match DAO class.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Disabled ("TODO remove after fixing the errors")
public class PhobiusFilteredMatchDAOTest {

    private static final Logger LOGGER = Logger.getLogger(PhobiusFilteredMatchDAOTest.class.getName());

    @Resource (name = "phobiusFilteredMatchDAO")
    private PhobiusFilteredMatchDAO phobiusFilteredMatchDAO;

    @Resource //(name = "proteinDAO")
    private ProteinDAO proteinDAO;

    private static final String[] SEQUENCES = new String[]{
            "ABC",
            "DEF",
            "GHI",
            "KLM",
            "NPQ"
    };

    private static final String[][] XREFS = new String[][]{
            {"Bob", "Henry"},
            {"Geoff", "George"},
            {"Dave", "Dennis"},
            {"Sheelah", "Alice"},
            {"Derek", "Gustav"}

    };

    private static final String[][] PHOBIUS_FEATURE_LINES = new String[][]{
            {
                    "FT   DOMAIN        1    240       CYTOPLASMIC.",
                    "FT   TRANSMEM    241    259",
                    "FT   DOMAIN      260    292       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    293    312",
                    "FT   DOMAIN      313    318       CYTOPLASMIC."
            },
            {
                    "FT   SIGNAL        1     23",
                    "FT   DOMAIN        1      3       N-REGION.",
                    "FT   DOMAIN        4     15       H-REGION.",
                    "FT   DOMAIN       16     23       C-REGION.",
                    "FT   DOMAIN       24    117       NON CYTOPLASMIC."
            },
            {
                    "FT   DOMAIN        1      5       NON CYTOPLASMIC.",
                    "FT   TRANSMEM      6     27",
                    "FT   DOMAIN       28     47       CYTOPLASMIC.",
                    "FT   TRANSMEM     48     73",
                    "FT   DOMAIN       74     92       NON CYTOPLASMIC.",
                    "FT   TRANSMEM     93    117",
                    "FT   DOMAIN      118    128       CYTOPLASMIC.",
                    "FT   TRANSMEM    129    147",
                    "FT   DOMAIN      148    181       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    182    204",
                    "FT   DOMAIN      205    213       CYTOPLASMIC."
            },
            {
                    "FT   SIGNAL        1     19",
                    "FT   DOMAIN        1      3       N-REGION.",
                    "FT   DOMAIN        4     14       H-REGION.",
                    "FT   DOMAIN       15     19       C-REGION.",
                    "FT   DOMAIN       20    388       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    389    410",
                    "FT   DOMAIN      411    421       CYTOPLASMIC.",
                    "FT   TRANSMEM    422    443",
                    "FT   DOMAIN      444    466       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    467    487",
                    "FT   DOMAIN      488    507       CYTOPLASMIC.",
                    "FT   TRANSMEM    508    531",
                    "FT   DOMAIN      532    550       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    551    576",
                    "FT   DOMAIN      577    596       CYTOPLASMIC.",
                    "FT   TRANSMEM    597    619",
                    "FT   DOMAIN      620    630       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    631    651",
                    "FT   DOMAIN      652    728       CYTOPLASMIC."
            },
            {
                    "FT   DOMAIN        1     19       NON CYTOPLASMIC.",
                    "FT   TRANSMEM     20     45",
                    "FT   DOMAIN       46     65       CYTOPLASMIC.",
                    "FT   TRANSMEM     66     88",
                    "FT   DOMAIN       89     99       NON CYTOPLASMIC.",
                    "FT   TRANSMEM    100    118",
                    "FT   DOMAIN      119    200       CYTOPLASMIC.",
                    "FT   TRANSMEM    201    218",
                    "FT   DOMAIN      219    236       NON CYTOPLASMIC."
            }
    };


    @Test
    public void phobiusMatchStoreTest() { // Build some proteins and persist them.
        //TODO: This needs further investigation
        //Unexpectedly the protein dao contains already 3 entries right from the start.
        if(proteinDAO.count() > 0){
            proteinDAO.deleteAll();
        }
        List<Protein> proteins = new ArrayList<Protein>(SEQUENCES.length);
        for (int i = 0, SEQUENCESLength = SEQUENCES.length; i < SEQUENCESLength; i++) {
            String sequence = SEQUENCES[i];
            final Protein protein = new Protein(sequence);
            protein.addCrossReferences(XREFS[i]);
            proteins.add(protein);
        }
        proteins = insertProteinsInTransaction(proteins);
        // Assert that the Proteins have primary keys (and have therefore been persisted)
        for (Protein protein : proteins) {
            assertNotNull(protein.getId(), "Protein Id is unexpectedly NULL!");
            LOGGER.debug("Protein primary key: " + protein.getId());
        }
        insertMatchesInTransaction(proteins);
        // Now test the contents of the database following commit.
        assertEquals(new Long(SEQUENCES.length), proteinDAO.count(), "The protein count is not the expected one!");
    }

    private List<Protein> insertProteinsInTransaction(final List<Protein> proteins) {
        ProteinDAO.PersistedProteins persistedProteins = proteinDAO.insertNewProteins(proteins);
        // None of these proteins should have been pre-existing.
        assertTrue(persistedProteins.getPreExistingProteins() == null || persistedProteins.getPreExistingProteins().size() == 0,
                "None of these proteins should have been pre-existing!");
        return new ArrayList<Protein>(persistedProteins.getNewProteins());
    }


    private void insertMatchesInTransaction(List<Protein> proteins) {
        Set<RawProtein<PhobiusRawMatch>> phobiusRawMatches = new HashSet();
        //Set<PhobiusProtein> phobiusProteins = new HashSet<PhobiusProtein>(proteins.size());
        int proteinIndex = 0;
        long featureCount = 0L;
        for (Protein protein : proteins) {
            LOGGER.debug("PHOBIUS: Building new PhobiusProtein with ID " + protein.getId());
            String proteinAccession = protein.getId().toString();

            RawProtein<PhobiusRawMatch> rawProtein = new RawProtein<>(protein.getId().toString());
            PhobiusProtein phobProt = new PhobiusProtein(protein.getId().toString());
            String[] features = PHOBIUS_FEATURE_LINES[proteinIndex++];

            for (String feature : features) {
                featureCount++;
                LOGGER.debug("PHOBIUS: Adding feature from line: " + feature);
                Matcher matcher = PhobiusFeature.FT_LINE_PATTERN.matcher(feature);
                if (!matcher.matches()) {
                    fail("The PhobiusFeature.FT_LINE_PATTERN regex should match: " + feature);
                }
                PhobiusFeature phobiusFeature = new PhobiusFeature(matcher);

                boolean isSP = isSignalFeature(phobiusFeature.getFeatureType());
                boolean isTM = isTransmembraneFeature(phobiusFeature.getFeatureType());
                PhobiusRawMatch phobiusRawMatch = new PhobiusRawMatch(proteinAccession, phobiusFeature.getFeatureType().getAccession(),
                        phobiusFilteredMatchDAO.getSignatureLibraryRelease().getLibrary(), phobiusFilteredMatchDAO.getPhobiusReleaseVersion(),
                        phobiusFeature.getStart(), phobiusFeature.getStop(), phobiusFeature.getFeatureType(), isSP, isTM);
                phobiusRawMatches.add(rawProtein);
            }

            //phobiusProteins.add(phobProt);
        }
        // Store the PhobiusProteins that have just been created.
        assertEquals( proteins.size(), phobiusRawMatches.size(), "The size of phobius proteins doesn't match the proteins size.");
        assertEquals(proteins.size(), PHOBIUS_FEATURE_LINES.length, "The count of Phobius feature lines doesn't match the protein size!");
        if (phobiusRawMatches != null) {
            phobiusFilteredMatchDAO.persist(phobiusRawMatches);
            //phobiusDAO.persist(phobiusProteins);
        }

        // Now try to retrieve PhobiusMatches from the database to check they exist.
        assertTrue(featureCount > 0L, "Feature count isn't bigger then 0!");
        assertEquals( featureCount, phobiusFilteredMatchDAO.count(), "The count of phobius entries doesn't match the feature count!");

        List<Protein> retrievedProteins = proteinDAO.getProteinsAndMatchesAndCrossReferencesBetweenIds(0, Long.MAX_VALUE);
        for (Protein retrieved : retrievedProteins) {
            LOGGER.info("Protein ID: " + retrieved.getId());
            LOGGER.info("Number of Xrefs: " + retrieved.getCrossReferences().size());
            for (ProteinXref xref : retrieved.getCrossReferences()) {
                LOGGER.info("\tXref: " + xref.getIdentifier());
            }
            LOGGER.info("Number of Matches " + retrieved.getMatches().size());
            for (Match match : retrieved.getMatches()) {
                LOGGER.info("\tMatch: " + match.getSignature().getAccession());
                Set<Location> locations = match.getLocations();
                for (Location location : locations) {
                    LOGGER.info("\tStart: " + location.getStart());
                    LOGGER.info("\tStop: " + location.getEnd());
                }
            }

        }
    }

    public boolean isSignalFeature(PhobiusFeatureType featureType){
        return PhobiusFeatureType.SIGNAL_PEPTIDE == featureType ||
                PhobiusFeatureType.SIGNAL_PEPTIDE_C_REGION == featureType ||
                PhobiusFeatureType.SIGNAL_PEPTIDE_N_REGION == featureType ||
                PhobiusFeatureType.SIGNAL_PEPTIDE_H_REGION == featureType;
    }

    public boolean isTransmembraneFeature(PhobiusFeatureType featureType){
        return PhobiusFeatureType.TRANSMEMBRANE == featureType;
    }

}