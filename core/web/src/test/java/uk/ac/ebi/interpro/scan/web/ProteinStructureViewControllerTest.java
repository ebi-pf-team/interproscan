package uk.ac.ebi.interpro.scan.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.web.io.CreateSimpleProteinFromMatchData;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;
import uk.ac.ebi.interpro.scan.web.model.MatchDataSource;
import uk.ac.ebi.interpro.scan.web.model.SimpleLocation;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ProteinStructureViewController}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class ProteinStructureViewControllerTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Resource
    private CreateSimpleProteinFromMatchData matchData;

    @Test
    public void testProtein()    {
        ProteinStructureViewController c = new ProteinStructureViewController();
        c.setEntryHierarchy(entryHierarchy);
        c.setMatchData(matchData);
        c.proteinBody("P38398");
    }

    @Test
    public void testSimpleStructuralMatchSort() {

        /*
         * Example test data:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	MD5	CRC64	database_name	domain_id	class_id	pos_from	pos_to
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	4	14
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	362	446
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	CATH	1w91A01	2.60.40.1500	449	483
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	CATH	1w91A02	3.20.20.80	15	248
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	CATH	1w91A02	3.20.20.80	251	361
         */
        MatchDataSource cathDatabase = MatchDataSource.parseName("CATH");
        String cathDomainId1 = "1w91A01";
        String cathDomainId2 = "1w91A02";
        String cathClassId1 = "2.60.40.1500";
        String cathClassId2 = "3.20.20.80";
        SimpleLocation cathLocation1 = new SimpleLocation(4, 14);
        SimpleLocation cathLocation2 = new SimpleLocation(362, 446);
        SimpleLocation cathLocation3 = new SimpleLocation(449, 483);
        SimpleLocation cathLocation4 = new SimpleLocation(15, 248);
        SimpleLocation cathLocation5 = new SimpleLocation(251, 361);
        SimpleStructuralDatabase cath = new SimpleStructuralDatabase(cathDatabase);
        cath.addStructuralMatch(cathClassId1, cathDomainId1, cathLocation1);
        cath.addStructuralMatch(cathClassId1, cathDomainId1, cathLocation2);
        cath.addStructuralMatch(cathClassId1, cathDomainId1, cathLocation3);
        cath.addStructuralMatch(cathClassId2, cathDomainId2, cathLocation4);
        cath.addStructuralMatch(cathClassId2, cathDomainId2, cathLocation5);

        /*
         * Example test data:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	MD5	CRC64	database_name	domain_id	class_id	pos_from	pos_to
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91B	1w91	1	248
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91B	1w91	251	446
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91B	1w91	449	504
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91C	1w91	1	248
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91C	1w91	251	446
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	PDB	1w91C	1w91	449	504
         */
        MatchDataSource pdbDatabase = MatchDataSource.parseName("PDB");
        String pdbDomainId1 = "1w91B";
        String pdbDomainId2 = "1w91C";
        String pdbClassId = "1w91";
        SimpleLocation pdbLocation1 = new SimpleLocation(1, 248);
        SimpleLocation pdbLocation2 = new SimpleLocation(251, 446);
        SimpleLocation pdbLocation3 = new SimpleLocation(449, 504);
        SimpleStructuralDatabase pdb = new SimpleStructuralDatabase(pdbDatabase);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId1, pdbLocation1);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId1, pdbLocation2);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId1, pdbLocation3);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId2, pdbLocation1);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId2, pdbLocation2);
        pdb.addStructuralMatch(pdbClassId, pdbDomainId2, pdbLocation3);

        /*
         * Example test data:
         *
         * PROTEIN_ACCESSION	PROTEIN_ID	PROTEIN_LENGTH	MD5	CRC64	database_name	domain_id	class_id	pos_from	pos_to
         * Q9ZFM2	XYNB_GEOSE	504	08BF24654296C1128D571BD0780824EC	59518E75200A18B1	MODBASE	MB_Q9ZFM2	MB_Q9ZFM2	1	502
         */
        MatchDataSource modBaseDatabase = MatchDataSource.parseName("MODBASE");
        String modbaseClassId = "MB_P38398";
        String modbaseDomainId = "MB_P38398";
        SimpleLocation modbaseLocation = new SimpleLocation(1, 502);
        SimpleStructuralDatabase modBase = new SimpleStructuralDatabase(modBaseDatabase);
        modBase.addStructuralMatch(modbaseClassId, modbaseDomainId, modbaseLocation);

        // Add the databases to a list
        List<SimpleStructuralDatabase> structuralDatabases = new ArrayList<SimpleStructuralDatabase>();
        structuralDatabases.add(cath);
        structuralDatabases.add(pdb);
        structuralDatabases.add(modBase);

        // Test the structural matches sort correctly
        Collections.sort(structuralDatabases);
        assertEquals(3, structuralDatabases.size());
        assertEquals(cathDatabase, structuralDatabases.get(0).getDataSource());
        assertEquals(modBaseDatabase, structuralDatabases.get(1).getDataSource());
        assertEquals(pdbDatabase, structuralDatabases.get(2).getDataSource());
    }

}
