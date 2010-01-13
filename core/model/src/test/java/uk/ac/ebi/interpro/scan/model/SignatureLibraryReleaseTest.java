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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.net.MalformedURLException;

/**
 * Test cases for {@link SignatureLibraryRelease}
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SignatureLibraryReleaseTest extends AbstractTest<SignatureLibraryRelease> {

    @Test public void testRemoveSignature()   {
        SignatureLibraryRelease sdr = new SignatureLibraryRelease(new SignatureLibrary("Pfam"), "23.0");
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

    @Test public void testXml() throws IOException, SAXException {
        super.testSupportsMarshalling(SignatureLibraryRelease.class);
        super.testXmlRoundTrip();
    }

    // TODO: Fails with org.hibernate.id.IdentifierGenerationException: ids for this class must be manually assigned before calling save(): uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease
//    @Test public void testJpa() {
//        super.testJpaXmlObjects();
//    }

    public static void main(String[] args) throws IOException {
        //http://www.bioinf.manchester.ac.uk/dbbrowser/xmlprints/PR00460.xml
        if (args.length < 1)    {
            System.err.println("Usage: SignatureLibraryReleaseTest <signatures-xml-file>");
        }
        String signaturesFile = args[0];
        org.springframework.core.io.Resource resource;
        if (signaturesFile.startsWith("http"))  {
            resource = new UrlResource(signaturesFile);
        }
        else    {
            resource = new FileSystemResource(signaturesFile);
        }
        ApplicationContext ctx = new ClassPathXmlApplicationContext("uk/ac/ebi/interpro/scan/model/oxm-context.xml");
        // Unmarshall
        Unmarshaller unmarshaller = (Unmarshaller)ctx.getBean("unmarshaller");
        SignatureLibraryRelease slr = 
                (SignatureLibraryRelease) unmarshaller.unmarshal(new StreamSource(resource.getInputStream()));
        // Marshall
        // Marshaller marshaller     = (Marshaller)ctx.getBean("marshaller");
        //Writer writer = new StringWriter();
        //marshaller.marshal(slr, new StreamResult(writer));
        // Print
        System.out.println("Received:");
        System.out.println(slr.toString());
        //System.out.println(writer.toString());
    }

}
