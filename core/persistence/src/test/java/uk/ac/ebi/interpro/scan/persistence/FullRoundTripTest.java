package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

import org.xml.sax.SAXException;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;


/**
 * Performs a full round-trip test of persistence
 * and XML marshalling / unmarshalling.
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @author Gift Nuka
 * @version $Id$
 * @see org.custommonkey.xmlunit.XMLUnit
 * @since 1.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
//@Disabled
public class FullRoundTripTest {

    private static final Logger LOGGER = LogManager.getLogger(FullRoundTripTest.class.getName());

    @Resource(name = "signatureMarshaller")
    private Marshaller signatureMarshaller;

    @Resource(name = "signatureUnmarshaller")
    private Unmarshaller signatureUnmarshaller;

    @Resource(name = "proteinMarshaller")
    private Marshaller proteinMarshaller;

    @Resource(name = "proteinUnmarshaller")
    private Unmarshaller proteinUnmarshaller;

    @Resource(name = "signatureDAO")
    private SignatureDAO signatureDAO;

    @Resource(name = "proteinDAO")
    private ProteinDAO proteinDAO;

    @Resource(name = "proteinXmls")
    private XmlsForTesting proteinXmls;

    @Resource(name = "signatureXmls")
    private XmlsForTesting signatureXmls;

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

    /**
     * Initializes XMLUnit so white space and comments are ignored.
     */
    @BeforeEach
    public void initializeXmlUnit() {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }


    /**
     * Test of <signature/> xml round trip.
     */
    //TODO: Investigate GenericJDBCException and remove @Disabled label when fixed
    @Disabled
    @Test
    public void newSignatureRoundTrip() {
        ObjectRetriever<Signature, SignatureDAO> retriever = new ObjectRetriever<Signature, SignatureDAO>() {
            public Signature getObjectByPrimaryKey(SignatureDAO dao, Long primaryKey) {
                return dao.getSignatureAndMethodsDeep(primaryKey);
            }

            public Long getPrimaryKey(Signature persistable) {
                return persistable.getId();
            }
        };
        roundTrip(signatureXmls,
                signatureDAO,
                retriever,
                signatureMarshaller,
                signatureUnmarshaller);
    }

    /**
     * Interface to simulate closures in the roundTrip method below.  Should be implemented
     * as an anonymous inner class in the method calling roundTrip.
     *
     * @param <P> being the class of model object.
     * @param <D> being the class extending GenericDAO for data access, corresponding to the model object above.
     */
    private interface ObjectRetriever<P, D extends GenericDAO> {
        P getObjectByPrimaryKey(D dao, Long primaryKey);

        Long getPrimaryKey(P persistable);
    }

    /**
     * Generic method to perform a full round-trip test on any xml type.
     *
     * @param testXMLs     which holds a Collection of xmls to be tested.
     * @param dao          being the specific DataAccessObject (extending GenericDAO) for
     *                     object persistence / retrieval.
     * @param retriever    implementing a closure for retrieving object primary keys and retrieving the required object map.
     * @param marshaller   to generate the XML from the object map.
     * @param unmarshaller to generate an object map from XML.
     * @param <T>          being the class of the object being unmarshalled / persisted.
     * @param <D>          being the class of the DAO, extending GenericDAO.
     */
    private <T, D extends GenericDAO> void roundTrip(XmlsForTesting testXMLs, D dao, ObjectRetriever<T, D> retriever, Marshaller marshaller, Unmarshaller unmarshaller) {
        for (String testXml : testXMLs.getXmls()) {
            String inputXml = testXml.trim();
            try {
                T persistableObject = (T) unmarshal(unmarshaller, inputXml);
                // First of all, test that round trip without persistence works...
                String unpersistedOutputXml = marshal(marshaller, persistableObject);
                Diff myDiff = new Diff(inputXml, unpersistedOutputXml);
                if (!myDiff.similar()) {
                    LOGGER.error("\nNot similar: ROUND-TRIP, UNPERSISTED:\n====================\nInput XML:\n" + inputXml + "\n\nOutput XML:\n" + unpersistedOutputXml);
                    LOGGER.error("\ntoString(): \n\n" + persistableObject.toString());
                } else if (!myDiff.identical()) {
                    LOGGER.error("\nNot identical: ROUND-TRIP, UNPERSISTED:\n====================\nInput XML:\n" + inputXml + "\n\nOutput XML:\n" + unpersistedOutputXml);
                    LOGGER.error("\ntoString(): \n\n" + persistableObject.toString());
                }
                assertTrue(myDiff.similar(), "Round trip XML (not persistence) should be similar." + myDiff);
                assertTrue(myDiff.identical(), "Round trip XML (not persistence) should be identical." + myDiff);

                // Now persist the Signature
                dao.insert(persistableObject);

                // Retrieve its primary key
                Long id = retriever.getPrimaryKey(persistableObject);
                assertNotNull(id, "The stored persistableObject should not have a null primary key·");
                LOGGER.debug("Primary key of persisted object (mid round-trip following insert): " + id);
                // And retrieve into a new reference
                T retrievedPersistable = retriever.getObjectByPrimaryKey(dao, id);

                assertNotNull(retrievedPersistable, "The retrieved object should not be null");
                assertEquals(persistableObject, retrievedPersistable, "The retrieved object should be equal to the original persistableObject.");

                // Finally unmarshall the retrieved persistableObject and compare the XML.
                String persistedOutputXML = marshal(marshaller, retrievedPersistable);
                myDiff = new Diff(inputXml, persistedOutputXML);
                if (!myDiff.similar()) {
                    LOGGER.error("\nNot similar: ROUND-TRIP, PERSISTED:\n====================\nInput XML:\n" + inputXml + "\n\nOutput XML:\n" + persistedOutputXML);
                } else if (!myDiff.identical()) {
                    LOGGER.error("\nNot identical: ROUND-TRIP, PERSISTED:\n====================\nInput XML:\n" + inputXml + "\n\nOutput XML:\n" + persistedOutputXML);

                }
                assertTrue(myDiff.similar(), "Round trip XML (persisted) should be similar." + myDiff);
                assertTrue(myDiff.identical(), "Round trip XML (persisted) should be identical." + myDiff);


            } catch (IOException e) {
                LOGGER.error("IOException thrown during XML round trip test:", e);
                fail("IOException thrown during XML round trip test (full stack trace logged):" + e.getMessage());
            } catch (SAXException e) {
                LOGGER.error("SAXExeption thrown when attempting comparison of XML files.", e);
                fail("SAXExeption thrown when attempting comparison of XML files (full stack trace logged): " + e.getMessage());
            } finally {
                dao.deleteAll();
            }
        }
    }

    /**
     * Marshalls an object to an XML, returned as a String instance.
     *
     * @param marshaller to perform the object -> XML marshalling
     * @param o          the object to marshall to XML
     * @return the XML in a String
     * @throws IOException in the event of an error writing out the XML to the StringWriter.
     */
    private String marshal(Marshaller marshaller, Object o) throws IOException {
        Writer writer = new StringWriter();
        marshaller.marshal(o, new StreamResult(writer));
        String xml = writer.toString();
        LOGGER.debug("\n" + xml);
        return xml;
    }

    /**
     * Unmarshalls an XML (passed in as a String) to an object map.
     *
     * @param unmarshaller to perform the XML -> object unmarshalling
     * @param xml          being the XML to unmarshall to an object
     * @return the object representing the XML contents.
     * @throws IOException in the event of an error reading from the StringReader.
     */
    private Object unmarshal(Unmarshaller unmarshaller, String xml) throws IOException {
        Object o = unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
        LOGGER.debug(o);
        return o;
    }

//    private boolean compare ()
}
