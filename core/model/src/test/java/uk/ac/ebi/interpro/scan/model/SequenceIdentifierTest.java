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

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test cases for SequenceIdentifier
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     SequenceIdentifier
 */
public class SequenceIdentifierTest extends TestCase {

    /**
     * Logger for Junit logging. Log messages will be associated with the SequenceIdentifierTest class.
     */
    private static Logger LOGGER = Logger.getLogger(SequenceIdentifierTest.class);

    private static final String XREF_1          = "Q12345";
    private static final String XREF_2          = "UPI1234567890";
    private static final String LOWER_CASE_MD5  = "9d380adca504b0b1a2654975c340af78";
    private static final String UPPER_CASE_MD5  = "9D380ADCA504B0b1A2654975C340AF78";

    private static final String[] GOOD_XREFS    = {XREF_1, XREF_2};
    private static final String[] GOOD_MD5      = {LOWER_CASE_MD5, UPPER_CASE_MD5};
    
    /**
     * Index 0: Not a hex string, but correct length.
     * Index 1: 1 character too many
     */
    private static final String[] BAD_MD5 = {"9D380ADCX504B0b1A2654975C340AF78", "9d380adca504b0b1a2654975c340af783"};

    @Test public void testFactoryCreateSequenceIdentifier(){
         // Should produce good XrefSequenceIdentifiers.
        for (String id : GOOD_XREFS){
            SequenceIdentifier identifier = SequenceIdentifier.Factory.createSequenceIdentifier(id);
            assertTrue("Xref '" + id + "' should return a XrefSequenceIdentifier.",
                    identifier instanceof XrefSequenceIdentifier);
        }

        for (String id : GOOD_MD5){
            SequenceIdentifier identifier = SequenceIdentifier.Factory.createSequenceIdentifier(id);
            assertTrue("GOOD MD5 '" + id + "' should return a MD5SequenceIdentifier.",
                    identifier instanceof MD5SequenceIdentifier);
        }

        for (String id : BAD_MD5){
            SequenceIdentifier identifier = SequenceIdentifier.Factory.createSequenceIdentifier(id);
            assertTrue("BAD MD5 '" + id + "' should return a XrefSequenceIdentifier.",
                    identifier instanceof XrefSequenceIdentifier);
        }

        // Testing how the factory responds to a null String being passed in as an identifier.
        try{
            SequenceIdentifier.Factory.createSequenceIdentifier(null);
        }
        catch (Exception e){
            assertTrue("SequenceIdentifier.Factory.createSequenceIdentifier(null) should throw IllegalArgumentException",
                    e instanceof IllegalArgumentException);
        }

    }

    @Test public void testAbstractSequenceIdentifierEquals(){

        // Test that two MD5SequenceIdentifiers that are created with upper or lower case representations
        // of the same MD5 are equal.
        SequenceIdentifier upperMD5 = SequenceIdentifier.Factory.createSequenceIdentifier(UPPER_CASE_MD5);
        SequenceIdentifier lowerMD5 = SequenceIdentifier.Factory.createSequenceIdentifier(LOWER_CASE_MD5);
        assertEquals("Two MD5SequenceIdentifier objects instantiated with same checksum in mixed cases should be equal",
                upperMD5, lowerMD5);

        // Test that an MD5SequenceIdentifier and an XrefSequenceIdentifier are not equal,
        // even if they contain the same identifier text.
        // (N.B. Only possible to do from within the same package, as the constructor for XrefSequenceIdentifier is
        //  package private).
        // Using upper case, to ensure the string is the same.
        XrefSequenceIdentifier xrefSeqIdentContainingMd5 = new XrefSequenceIdentifier(UPPER_CASE_MD5);
        assertFalse("MD5SequenceIdentifier and XrefSequenceIdentifier should never be equal, irrespective of data they contain.",
                upperMD5.equals(xrefSeqIdentContainingMd5) || xrefSeqIdentContainingMd5.equals(upperMD5));

    }

    @Test public void testRemoveMatch()    {
        SequenceIdentifier identifier = SequenceIdentifier.Factory.createSequenceIdentifier(XREF_1);
        RawMatch match = identifier.addRawMatch(new RawHmmMatch(new Model("PF00155"), 0.035, 4.3e-61));
        assertEquals("Sequence identifier should have one match", 1, identifier.getRawMatches().size());
        identifier.removeRawMatch(match);
        assertEquals("Sequence identifier should have no matches", 0, identifier.getRawMatches().size());
    }      

}
