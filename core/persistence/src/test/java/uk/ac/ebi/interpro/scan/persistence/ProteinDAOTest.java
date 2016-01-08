/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.*;

/**
 * Developed using IntelliJ IDEA.
 * User: phil
 * Date: 19-Jun-2009
 * Time: 10:11:05
 *
 * @author Phil Jones, EMBL-EBI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore("Does not work on all OS.")
public class ProteinDAOTest {

    /**
     * Logger for Junit logging. Log messages will be associated with the ProteinPersistenceTest class.
     */
    private static final Logger LOGGER = Logger.getLogger(ProteinDAOTest.class.getName());

    private static final String[] ACCESSIONS = {"Q12345", "P99999", "IPI01234567", "ENSP120923423423234"};

    private static final String[] ACCESSIONS_2 = {"Q98765", "P23423", "IPI01234598", "ENSP120923423423268"};

    private static final Long LONG_ZERO = 0L;

    private static final Long LONG_ONE = 1L;

    // First line of UPI0000000001.fasta
    public static final String GOOD = "MGAAASIQTTVNTLSERISSKLEQEANASAQTKCDIEIGNFYIRQNHGCNLTVKNMCSAD";

    @Resource(name = "proteinDAO")
    private ProteinDAO dao;

    public void setDao(ProteinDAO dao) {
        this.dao = dao;
    }

    @Before
    @After
    public void emptyProteinTable() {
        dao.deleteAll();
        assertEquals("There should be no proteins in the Protein table following a call to dao.deleteAll", LONG_ZERO, dao.count());
    }

    /**
     * This test exercises
     * GenericDAOImpl<Protein, Long>
     * .insert(Protein protein)
     * .count()
     * .read(Long id)
     * .retrieveAll()
     * .delete(Protein protein)
     */
    @Test
    @Ignore("Does not work on all OS.")
    public void storeAndRetrieveProtein() {
        emptyProteinTable();
        Protein protein = new Protein(GOOD);
        assertNotNull("The ProteinDAOImpl object should be not-null.", dao);
        dao.insert(protein);
        Long id = protein.getId();
        assertEquals("The count of proteins in the database is not correct.", LONG_ONE, dao.count());
        Protein retrievedProtein = dao.read(id);
        assertEquals("The protein sequence of the retrieved object is not the same as the original object.", protein.getSequence(), retrievedProtein.getSequence());
        dao.delete(retrievedProtein);
        List<Protein> retrievedProteinList = dao.retrieveAll();
        assertEquals("There should be no proteins in the database, following removal of the single protein that was added.", 0, retrievedProteinList.size());
        assertEquals("The count of proteins in the database is not correct.", LONG_ZERO, dao.count());
        emptyProteinTable();
    }


    /**
     * This test exercises storage of XrefSequenceIdentifier objects that are
     * associated with a Protein object.
     * <p/>
     * It also exercises the GenericDAOImpl<Protein, Long>.update(Protein protein)
     * method in relation to XrefSequenceIdentifier objects
     */
    @Test
    @Ignore("Does not work on all OS.")
    public void storeAndRetrieveProteinWithXrefs() {
        emptyProteinTable();
        Protein protein = new Protein(GOOD);
        // First of all, insert the protein not including Xrefs.
        dao.insert(protein);
        Long id = protein.getId();
        assertNotNull("The protein ID (following persistence) should not be null", id);
        addXrefsToProtein(protein, ACCESSIONS);
        dao.update(protein);
        Long idAfterUpdate = protein.getId();
        assertEquals("The protein ID following update should be the same as before update", id, idAfterUpdate);
        Protein firstRetrievedProtein = dao.getProteinAndCrossReferencesByProteinId(id);
        // Check the retrieved protein
        assertNotNull("The retrieved protein should not be null", firstRetrievedProtein);
        assertEquals("The protein sequence of the retrieved object is not the same as the original object.", protein.getSequence(), firstRetrievedProtein.getSequence());
        assertEquals("The protein ID of the retrieved object should be the same as after the initial insert", id, firstRetrievedProtein.getId());

        // Check the number and content of the cross references.
        Set<ProteinXref> retrievedXRefs = firstRetrievedProtein.getCrossReferences();
        assertNotNull("The cross reference collection should not be null", retrievedXRefs);
        assertEquals("There should be " + ACCESSIONS.length + " cross references.", ACCESSIONS.length, retrievedXRefs.size());
        for (String accession : ACCESSIONS) {
            boolean foundAccession = false;
            for (ProteinXref ident : retrievedXRefs) {
                if (accession != null && accession.equals(ident.getIdentifier())) {
                    foundAccession = true;
                }
            }
            if (!foundAccession) {
                fail("The retrieved protein does not include the cross reference " + accession);
            }
        }

        // Now add an additional cross references and update the Protein.
        firstRetrievedProtein.addCrossReference(new ProteinXref("EXTRA"));
        dao.update(firstRetrievedProtein);

        // Retrieve the protein from the database again and check it has no Xrefs.
        Protein secondRetrievedProtein = dao.getProteinAndCrossReferencesByProteinId(id);
        assertNotNull("The second retrieved protein should not be null", secondRetrievedProtein);
        assertEquals("The second retrieved protein should have " + (ACCESSIONS.length + 1) + " cross references.", (ACCESSIONS.length + 1), secondRetrievedProtein.getCrossReferences().size());
    }


    /**
     * This test exercises storage of XrefSequenceIdentifier objects that are
     * associated with a Protein object.
     * <p/>
     * It also exercises the GenericDAOImpl<Protein, Long>.update(Protein protein)
     * method in relation to XrefSequenceIdentifier objects
     */
    @Test
    public void testGetProteinsAndMatchesAndCrossReferencesBetweenIds() {
        emptyProteinTable();
        final Protein protein1 = new Protein(GOOD);
        final Protein protein2 = new Protein(GOOD + "ACD");
        // First of all, insert the protein not including Xrefs.
        dao.insert(protein1);
        Long id = protein1.getId();
        assertNotNull("The protein ID (following persistence) should not be null", id);
        addXrefsToProtein(protein1, ACCESSIONS);
        addXrefsToProtein(protein2, ACCESSIONS_2);


        dao.update(protein1);

    }

    private void addXrefsToProtein(Protein protein1, final String[] accessions) {
        for (String accession : accessions) {
            ProteinXref xref = new ProteinXref(accession);
            protein1.addCrossReference(xref);
        }
    }


    /**
     * Test that a PersistenceException is thrown if an attempt is made to
     * insert the same Protein twice.
     */
    @Test
    public void testSecondInsertOfSameProtein() {
        emptyProteinTable();
        Protein protein1 = new Protein(GOOD);
        Protein protein2 = new Protein(GOOD);

        dao.insert(protein1);
        assertEquals(protein1.getId(), (dao.insert(protein2)).getId());
    }

    /**
     * Tests the GenericDAO.getMaximumPrimaryKey() method.
     */
    @Test
    public void testMaximumPrimaryKeyCount() {
        emptyProteinTable();
        String[] proteinSequences = new String[]{"ABCD", "QWERTY", "PLQPPY", "GHGHGHGHG", "GRUFF"};
        Long maxPrimaryKey = 0l;
        for (String sequence : proteinSequences) {
            Protein protein = new Protein(sequence);
            dao.insert(protein);
            if (protein.getId() > maxPrimaryKey) {
                maxPrimaryKey = protein.getId();
            }
        }
        assertEquals("The maximum primary key is not as expected", maxPrimaryKey, dao.getMaximumPrimaryKey());
    }

    /**
     * Tests the ProteinDAO.getProteinsInTransactionSlice() method.
     */
    @Test
    public void testGetProteinsInTransactionSlice() {
        emptyProteinTable();
        String[] proteinSequences = new String[]{"ABCD", "QWERTY", "PLQQPY", "GHGHGHGHG", "GRUFF", "SPLQD", "QUQUQUQU"};
        Long maxPrimaryKey = Long.MIN_VALUE;
        Long minPrimaryKey = Long.MAX_VALUE;
        for (String sequence : proteinSequences) {
            Protein protein = new Protein(sequence);
            dao.insert(protein);
            if (protein.getId() > maxPrimaryKey) {
                maxPrimaryKey = protein.getId();
            }
            if (protein.getId() < minPrimaryKey) {
                minPrimaryKey = protein.getId();
            }
        }
        assertEquals("The difference between the highest and lowest primary key values is not as expected.", (proteinSequences.length - 1), maxPrimaryKey - minPrimaryKey);
        List<Protein> retrievedProteins = dao.getProteinsBetweenIds(minPrimaryKey, maxPrimaryKey);
        assertEquals("The wrong number of proteins were returned from the database.", proteinSequences.length, retrievedProteins.size());

        // Now build a new, smaller slice and see if a smaller number of proteins are returned.
        retrievedProteins = dao.getProteinsBetweenIds(minPrimaryKey + 1, maxPrimaryKey - 1);
        assertEquals("The wrong number of proteins returned from the database for the smaller slice.", (proteinSequences.length - 2), retrievedProteins.size());
    }
}
