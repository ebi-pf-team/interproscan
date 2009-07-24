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

package uk.ac.ebi.interpro.scan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

/**
 * Test cases for {@link Signature}
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SignatureTest extends AbstractTest<Signature> {

    // TODO: Add Xrefs and Entry to Signature
    // TODO: Add Model MD5?

    @Test public void testBuilder()   {
        final String AC       = "SIG001";
        final String NAME     = "test";
        final String TYPE     = "domain";
        final String DESC     = NAME;
        final String ABSTRACT = NAME;
        SignatureDatabaseRelease release = new SignatureDatabaseRelease(new SignatureProvider("TST"), "1.0");
        Set<Model> models = new HashSet<Model>();
        models.add(new Model("MOD001"));
        models.add(new Model("MOD002"));
        int numModels = models.size();
        Signature signature = new Signature.Builder(AC)
                .name(NAME)
                .type(TYPE)
                .description(NAME)
                .abstractText(NAME)
                .signatureDatabaseRelease(release)
                .models(models)
                .build();
        assertEquals(AC, signature.getAccession());
        assertEquals(NAME, signature.getName());
        assertEquals(TYPE, signature.getType());
        assertEquals(DESC, signature.getDescription());
        assertEquals(ABSTRACT, signature.getAbstract());
        assertEquals(release, signature.getSignatureDatabaseRelease());
        assertEquals(numModels, signature.getModels().size());
    }       

    @Test public void testRemoveModel()   {
        Signature signature = new Signature("SIG001");
        Model m1 = signature.addModel(new Model("MOD001"));
        Model m2 = signature.addModel(new Model("MOD002"));
        // Should be OK
        assertEquals(2, signature.getModels().size());
        // Should be OK (key not recognised, so just ignores)
        signature.removeModel(new Model("UNKNOWN"));
        assertEquals(2, signature.getModels().size());
        // Should fail
        try {
            signature.removeModel(null);
        }
        catch (Exception e)    {
            assertTrue(e instanceof NullPointerException);
        }
        // Should be OK
        signature.removeModel(m1);
        assertEquals(1, signature.getModels().size());
        signature.removeModel(m2);
        assertEquals(0, signature.getModels().size());
    }

    @Test public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(Signature.class);
        super.testXmlRoundTrip();
    }

    // TODO: Re-enable test when fixed "org.hibernate.exception.GenericJDBCException: Could not execute JDBC batch update"
    /**
     * This test is currently not correct.  It can be made to work be setting CascadeType.DELETE (or CascadeType.ALL)
     * on Signature.models.  This would not be correct however, as Models can have an independent life
     * to Signatures, so should not be cascade-deleted when a Signature is deleted.  The
     * testJpaXmlObjects() method that this test calls is attempting to delete both the Signature and
     * its Models in one go. (However, I do not understand why this is causing the error:
     *
     * Could not synchronize database state with session [org.hibernate.event.def.AbstractFlushingEventListener]
     * org.hibernate.exception.GenericJDBCException: Could not execute JDBC batch update
     *
     * PJ.
     */
    @Test
    @Ignore ("This test is currently failing, and in its current form, should fail.  See comment on test method.")
    public void testJpa() {
        super.testJpaXmlObjects(new ObjectRetriever<Signature>(){
            public Signature getObjectByPrimaryKey(GenericDAO<Signature, Long> dao, Long primaryKey) {
                return dao.readDeep(primaryKey, "models");
            }

            public Long getPrimaryKey(Signature signature) {
                return signature.getId();
            }
        });
    }

}
