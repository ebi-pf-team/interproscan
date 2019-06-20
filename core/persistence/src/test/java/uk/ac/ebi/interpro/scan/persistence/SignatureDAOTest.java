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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 01-Jul-2009
 * Time: 11:26:27
 *
 * @author Phil Jones, EMBL-EBI
 * @author Gift Nuka
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class SignatureDAOTest {

    private static final Long LONG_ZERO = 0L;

    @Resource(name= "signatureDAO")
    private GenericDAO<Signature, Long> dao;

    public void setDao(GenericDAO<Signature, Long> dao) {
        this.dao = dao;
    }

    @BeforeEach
    @AfterEach
    public void emptySignatureTable(){
        dao.deleteAll();
        assertEquals(LONG_ZERO, dao.count(),"There should be no Signatures in the Signature table following a call to dao.deleteAll");
    }

    //TODO: Investigate GenericJDBCException and remove @Disabled label when fixed
    @Disabled
    @Test
    public void storeAndRetrieveSignature(){
        //TODO: Why calling it explicitly, if we use @BeforeEach and @After annotation
        //emptySignatureTable();
        Signature signature = new Signature
                .Builder(SIGNATURE_ACCESSION)
                .name(SIGNATURE_NAME)
                .description(SIGNATURE_DESCRIPTION)
                .type(SIGNATURE_TYPE)
                .abstractText(SIGNATURE_ABSTRACT)
                .build();
        assertNotNull(dao, "The dao has not been injected and is null.");
        // Persist the Signature
        dao.insert(signature);

        // Persist a second Signature to check retrieval by primary key works correctly.
        dao.insert(new Signature("Dummy"));

        // Check there are two stored signatures.
        List<Signature> shouldContainTwo = dao.retrieveAll();
        assertEquals(2, shouldContainTwo.size(), "There should be two signatures in the database.");


        // Get the assigned primary key
        Long pk = signature.getId();

        // Now retrieve it using read method
        Signature retrievedSignature = dao.read(pk);
        assertNotNull(retrievedSignature, "No signature has been retrieved.");
        assertEquals(SIGNATURE_ACCESSION, retrievedSignature.getAccession());
        assertEquals(SIGNATURE_NAME, retrievedSignature.getName());
        assertEquals(SIGNATURE_DESCRIPTION, retrievedSignature.getDescription());
        assertEquals(SIGNATURE_TYPE, retrievedSignature.getType());
        assertEquals(SIGNATURE_ABSTRACT, retrievedSignature.getAbstract());

        // Test getSignatureAndMethodsDeep, specific to the SignatureDAO object!
        retrievedSignature = ((SignatureDAO)dao).getSignatureAndMethodsDeep(pk);
        assertNotNull(retrievedSignature, "No signature has been retrieved.");
        assertEquals(SIGNATURE_ACCESSION, retrievedSignature.getAccession());
        assertEquals(SIGNATURE_NAME, retrievedSignature.getName());
        assertEquals(SIGNATURE_DESCRIPTION, retrievedSignature.getDescription());
        assertEquals(SIGNATURE_TYPE, retrievedSignature.getType());
        assertEquals(SIGNATURE_ABSTRACT, retrievedSignature.getAbstract());

        // Test getSignaturesAndMethodsDeep, specific to the SignatureDAO object!
        Set<String> signatureAcs = new HashSet<String>();
        signatureAcs.add("PF02316");
        signatureAcs.add("Dummy");
        Collection<Signature> retrievedSignatures = ((SignatureDAO)dao).getSignaturesAndMethodsDeep(signatureAcs);
        assertNotNull(retrievedSignatures, "No signatures have been retrieved.");
        assertEquals(2, retrievedSignatures.size(), "Expected 2 signatures to be returned by query");

        // Finally delete it.
        dao.delete(retrievedSignature);

        // And check it is gone.
        List<Signature> shouldContainOne = dao.retrieveAll();
        assertEquals(1, shouldContainOne.size(), "There should be one in the database.");
    }


    /*
     Representative data for a Signature.
     */
    private static final String SIGNATURE_ACCESSION = "PF02316";
    private static final String SIGNATURE_NAME = "Mu_DNA_bind";
    private static final String SIGNATURE_DESCRIPTION = "This family consists of MuA-transposase and repressor protein CI. " +
            "These proteins contain homologous DNA-binding domains at their N-termini which compete for the same " +
            "DNA site within the Mu bacteriophage genome.";
    private static final String SIGNATURE_TYPE = "Family";
    private static final String SIGNATURE_ABSTRACT = " Interpro entry IPR003314 " +
            "This family consists of MuA-transposase and repressor protein CI. " +
            "The Bacteriophage Mu transposase is essential for integration, replication-transposition," +
            " and excision of Mu DNA. The N-terminus of the Mu transposase has considerable sequence homology " +
            "with the Mu repressor and with the NH2 terminus of the transposase of the Mu-like Bacteriophage " +
            "D108. These three proteins are known to share binding sites on DNA. An internal sequence in the " +
            "Mu A protein also shares these features PUBMED:2999776. " +
            "The repressor protein of Bacteriophage Mu establishes and maintains lysogeny by shutting down " +
            "transposition functions needed for phage DNA replication. It interacts with several repeated " +
            "DNA sequences within the early operator, preventing transcription from two divergent promoters. " +
            "It also directly represses transposition by competing with the MuA transposase for an internal " +
            "activation sequence (IAS) that is coincident with the operator and required for efficient " +
            "transposition. The transposase and repressor proteins compete for the operator/IAS region" +
            " using homologous DNA-binding domains located at their amino termini PUBMED:10387082. " +
            "More information about these proteins can be found at Protein of the Month: Transposase PUBMED:.";
}
