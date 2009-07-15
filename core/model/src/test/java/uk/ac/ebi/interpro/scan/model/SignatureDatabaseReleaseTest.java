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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test cases for {@link SignatureDatabaseRelease}
 *
 * @author  Antony Quinn
 * @version $Id: SignatureDatabaseReleaseTest.java,v 1.6 2009/07/13 14:21:41 aquinn Exp $
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SignatureDatabaseReleaseTest   {

    @Test public void testRemoveSignature()   {
        SignatureDatabaseRelease sdr = new SignatureDatabaseRelease(new SignatureProvider("Pfam"), "23.0");
        Signature s1 = sdr.addSignature(new Signature("PF00001"));
        Signature s2 = sdr.addSignature(new Signature("PF00002"));
        // Should be OK
        assertEquals("Should have two signatures", 2, sdr.getSignatures().size());
        // Should be OK (key not recognised, so just ignores)
        sdr.removeSignature(new Signature("??"));
        assertEquals("Should have two signatures", 2, sdr.getSignatures().size());
        // Should fail
        try {
            sdr.removeSignature(null);
        }
        catch (Exception e)    {
            assertTrue("Should be NullPointerException", e instanceof NullPointerException);
        }
        // Should be OK
        sdr.removeSignature(s1);
        assertEquals("Should have one signature", 1, sdr.getSignatures().size());
        sdr.removeSignature(s2);
        assertEquals("Should have no signatures", 0, sdr.getSignatures().size());
    }

    // TODO: Add SuperFamily SSF53098    

}
