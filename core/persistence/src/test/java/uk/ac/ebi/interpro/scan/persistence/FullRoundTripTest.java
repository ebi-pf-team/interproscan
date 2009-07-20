package uk.ac.ebi.interpro.scan.persistence;

import static junit.framework.TestCase.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Performs a full round-trip test of persistence
 * and XML marshalling / unmarshalling.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     org.custommonkey.xmlunit.XMLUnit
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/springconfig/spring-FullRoundTripTest-config.xml"})
public class FullRoundTripTest {

    private static final Log logger = LogFactory.getLog(FullRoundTripTest.class);

    @Resource (name="signatureMarshaller")
    private Marshaller signatureMarshaller;

    @Resource (name="signatureUnmarshaller")
    private Unmarshaller signatureUnmarshaller;

    @Resource (name="proteinMarshaller")
    private Marshaller proteinMarshaller;

    @Resource (name="proteinUnmarshaller")
    private Unmarshaller proteinUnmarshaller;

    @Resource (name="signatureDAO")
    private SignatureDAO signatureDAO;

    @Resource (name="proteinDAO")
    private ProteinDAO proteinDAO;

    public void setSignatureMarshaller(Marshaller signatureMarshaller) {
        this.signatureMarshaller = signatureMarshaller;
    }

    public void setSignatureUnmarshaller(Unmarshaller signatureUnmarshaller) {
        this.signatureUnmarshaller = signatureUnmarshaller;
    }

    public void setProteinMarshaller(Marshaller proteinMarshaller) {
        this.proteinMarshaller = proteinMarshaller;
    }

    public void setProteinUnmarshaller(Unmarshaller proteinUnmarshaller) {
        this.proteinUnmarshaller = proteinUnmarshaller;
    }

    public void setSignatureDAO(SignatureDAO signatureDAO) {
        this.signatureDAO = signatureDAO;
    }

    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    private String[] signatureFiles = new String[]{"testXML/signature1.xml", "testXML/signature2.xml", "testXML/signature3.xml", "testXML/signature4.xml"};

    private String[] proteinFiles = new String[]{"testXML/protein1.xml", "testXML/protein2.xml", "testXML/protein3.xml", "testXML/protein4.xml"};


    /**
     * Initializes XMLUnit so white space and comments are ignored.
     */
    @Before
    public void initializeXmlUnit(){
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }


    @Test
    public void newSignatureRoundTrip(){
        ObjectRetriever<Signature, SignatureDAO> retriever = new ObjectRetriever<Signature, SignatureDAO>() {
            public Signature getObjectByPrimaryKey(SignatureDAO dao, Long primaryKey) {
                return dao.getSignatureAndMethodsDeep(primaryKey);
            }

            public Long getPrimaryKey(Signature persistable) {
                return persistable.getId();
            }
        };
        roundTrip (signatureFiles,
                signatureDAO,
                retriever,
                signatureMarshaller,
                signatureUnmarshaller);
    }

    @Test
    public void newProteinRoundTrip(){
        ObjectRetriever<Protein, ProteinDAO> retriever = new ObjectRetriever<Protein, ProteinDAO>() {
            public Protein getObjectByPrimaryKey(ProteinDAO dao, Long primaryKey) {
                return dao.getProteinAndMatchesById(primaryKey);
            }

            public Long getPrimaryKey(Protein persistable) {
                return persistable.getId();
            }
        };
        roundTrip (proteinFiles,
                proteinDAO,
                retriever,
                proteinMarshaller,
                proteinUnmarshaller);
    }

    private interface ObjectRetriever<P, D extends GenericDAO>{
        P getObjectByPrimaryKey(D dao, Long primaryKey);
        Long getPrimaryKey(P persistable);
    }


    private <T, D extends GenericDAO> void roundTrip(String[] fileNames, D dao, ObjectRetriever<T, D> retriever, Marshaller marshaller, Unmarshaller unmarshaller){
        for (String inputFileName : fileNames){
            try{
                String inputXml = readFile(inputFileName);
                logger.debug("Input XML:\n" + inputXml);
                T persistableObject = (T) unmarshal(unmarshaller, inputXml);
                // First of all, test that round trip without persistence works...
                String unpersistedOutputXml = marshal(marshaller, persistableObject);
                logger.debug("Unpersisted output XML:\n" + unpersistedOutputXml);
                Diff myDiff = new Diff(inputXml, unpersistedOutputXml);
                assertTrue("Round trip XML (not persistence) should be similar." + myDiff, myDiff.similar());
                assertTrue("Round trip XML (not persistence) should be identical." + myDiff, myDiff.identical());

                // Now persist the Signature
                dao.insert(persistableObject);

                // Retrieve its primary key
                Long id = retriever.getPrimaryKey(persistableObject);
                assertNotNull("The stored persistableObject should not have a null primary key·", id);
                logger.debug("Primary key of persisted object (mid round-trip following insert): "+ id);
                // And retrieve into a new reference
                T retrievedPersistable = retriever.getObjectByPrimaryKey(dao, id);

                assertNotNull("The retrieved object should not be null", retrievedPersistable);
                assertEquals("The retrieved object should be equal to the original persistableObject.", persistableObject, retrievedPersistable);

                // Finally unmarshall the retrieved persistableObject and compare the XML.
                String persistedOutputXML = marshal(marshaller, retrievedPersistable);
                logger.debug("Persisted output XML:\n" + persistedOutputXML);
                myDiff = new Diff(inputXml, persistedOutputXML);
                assertTrue("Round trip XML (persisted) should be similar." + myDiff, myDiff.similar());
                assertTrue("Round trip XML (persisted) should be identical." + myDiff, myDiff.identical());


            } catch (IOException e) {
                logger.error("IOException thrown during XML round trip test:" , e);
                fail("IOException thrown during XML round trip test (full stack trace logged):" + e.getMessage());
            } catch (SAXException e) {
                logger.error("SAXExeption thrown when attempting comparison of XML files.", e);
                fail ("SAXExeption thrown when attempting comparison of XML files (full stack trace logged): " + e.getMessage());
            }
        }
    }


    /**
     * Reads in a file and writes its contents to a String.
     * @param fileName being the file to be read in.
     * @return a String containing the contents of the file or null if the file does not exist.
     * @throws IOException if a problem occurs when closing streams.
     */
    private String readFile(String fileName) throws IOException{
        BufferedReader reader = null;
        StringWriter writer = null;
        InputStream resourceStream = null;
        try{
            resourceStream = FullRoundTripTest.class.getClassLoader().getResourceAsStream(fileName);
            if (resourceStream == null){
                return null;
            }
            reader = new BufferedReader (
                    new InputStreamReader(FullRoundTripTest.class.getClassLoader().getResourceAsStream(fileName))
            );
            writer = new StringWriter ();
            while (reader.ready()){
                writer.write(reader.readLine());
                writer.write('\n');
            }
            return writer.toString();
        }
        finally{
            if (resourceStream != null){
                resourceStream.close();
            }
            if (reader != null){
                reader.close();
            }
            if (writer != null){
                writer.close();
            }
        }
    }

    private String marshal(Marshaller marshaller, Object o) throws IOException  {
        Writer writer = new StringWriter();
        marshaller.marshal(o, new StreamResult(writer));
        String xml = writer.toString();
        logger.debug("\n" + xml);
        return xml;
    }

    private Object unmarshal(Unmarshaller unmarshaller, String xml) throws IOException  {
        Object o = unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
        logger.debug(o);
        return o;
    }

//    private boolean compare ()
}
