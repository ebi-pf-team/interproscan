package uk.ac.ebi.interpro.scan.io.unmarshal.xml.interpro;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.*;

/**
 * Parses out some of the InterPro XML format, extracting data from the following xpaths:
 * <p/>
 * /interprodb/interpro/@id                                        (IPR######)
 * /interprodb/interpro/name                                       (entry name)
 * /interprodb/interpro/member_list/db_xref/@db                    (member DB name)
 * /interprodb/interpro/member_list/db_xref/@dbkey                 (method accession)
 * /interprodb/interpro/member_list/db_xref/@name                  (method name)
 * /interprodb/interpro/class_list/classification/@id              (e.g. GO:012345)
 * /interprodb/interpro/class_list/classification/category         (Root term)
 * /interprodb/interpro/class_list/classification/description      (term)
 * /interprodb/interpro/class_list/classification/@class_type="GO" (FILTER ON THIS)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class InterProXMLUnmarshaller {

    private static final Logger LOGGER = LogManager.getLogger(InterProXMLUnmarshaller.class.getName());

    private static final String EL_INTERPRO = "interpro";
    private static final String EL_MEMBER_LIST = "member_list";
    private static final String EL_NAME = "name";
    private static final String EL_DB_XREF = "db_xref";
    private static final String EL_CLASS_LIST = "class_list";
    private static final String EL_CLASSIFICATION = "classification";
    private static final String EL_CATEGORY = "category";
    private static final String EL_DESCRIPTION = "description";

    private static final QName QNAME_AT_DB = new QName("db");
    private static final QName QNAME_AT_DB_KEY = new QName("dbkey");
    private static final QName QNAME_AT_ID = new QName("id");
    private static final QName QNAME_AT_CLASS_TYPE = new QName("class_type");

    private static final String GO = "GO";    // Used to filter for only GO classifications.

    // Mapping between member database name in XML and SignatureLibrary enum instance.
    private static final Map<String, SignatureLibrary> SIG_LIB_LOOKUP = new HashMap<String, SignatureLibrary>();

    /*

    Currently from the DTD:

    GENPROP | INTACT | BLOCKS | CATH | CAZY | COG | COMe | EC | GO | INTERPRO | IUPHAR | MEROPS | MSDsite |
     PANDIT | PDB | PFAM | PIRSF | PRINTS | PRODOM | PROFILE | PROSITE | PROSITEDOC | PUBMED | SCOP | SMART |
      SMODEL | SSF | SWISSPROT | TIGRFAMs | TIGRFAMS | TREMBL | PANTHER | GENE3D | HAMAP |TC | PRIAM
     */

    static {
        SIG_LIB_LOOKUP.put("PFAM", SignatureLibrary.PFAM);
        SIG_LIB_LOOKUP.put("PIRSF", SignatureLibrary.PIRSF);
        SIG_LIB_LOOKUP.put("PRINTS", SignatureLibrary.PRINTS);
        SIG_LIB_LOOKUP.put("PRODOM", SignatureLibrary.PRODOM);
        SIG_LIB_LOOKUP.put("PROFILE", SignatureLibrary.PROSITE_PROFILES);
        SIG_LIB_LOOKUP.put("PROSITE", SignatureLibrary.PROSITE_PATTERNS);
        SIG_LIB_LOOKUP.put("SMART", SignatureLibrary.SMART);
        SIG_LIB_LOOKUP.put("SSF", SignatureLibrary.SUPERFAMILY);
        SIG_LIB_LOOKUP.put("TIGRFAMs", SignatureLibrary.TIGRFAM);
        SIG_LIB_LOOKUP.put("TIGRFAMS", SignatureLibrary.TIGRFAM);
        SIG_LIB_LOOKUP.put("GENE3D", SignatureLibrary.GENE3D);
        SIG_LIB_LOOKUP.put("HAMAP", SignatureLibrary.HAMAP);
    }

    /**
     * This method attempts to unmarshall the InterPro XML file to produce
     * a lightweight date model that can be used to get the correct InterPro entry
     * for a particular method and any related GO terms.
     *
     * @param xmlIs The InputStream from the XML file.
     * @return a Map of SignatureLibrary to a List of member database accessions.
     * @throws javax.xml.stream.XMLStreamException
     *          in the event of a problem reading in the XML file.
     */
    public Map<SignatureLibrary, SignatureLibraryIntegratedMethods> unmarshal(InputStream xmlIs)
            throws XMLStreamException {
        final Map<SignatureLibrary, SignatureLibraryIntegratedMethods> libraryMethods = new HashMap<SignatureLibrary, SignatureLibraryIntegratedMethods>();
        XMLEventReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            reader = factory.createXMLEventReader(xmlIs);
            XMLEvent currentEvent;
            while (!(currentEvent = reader.nextEvent()).isEndDocument()) {
                if (currentEvent.isStartElement()) {
                    final StartElement startElement = currentEvent.asStartElement();
                    if (EL_INTERPRO.equals(startElement.getName().getLocalPart())) {
                        processEntryElement(startElement, reader, libraryMethods);
                    }
                }
            }
        }
        catch (NoSuchElementException nsee) {
            LOGGER.error("NoSuchElementException thrown", nsee);
            throw new IllegalStateException("The InterPro XML file that is being unmarshalled is incomplete - a NoSuchElementException has been thrown before the end tag has been reached.", nsee);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return libraryMethods;
    }

    /**
     * Look at the start of the <interpro> element, extracts the id attribute value and also the name
     * of the InterPro entry, allowing the creation of an InterProEntry object.
     *
     * @param interproStartElement being the start tag passed in from the unmarshall method.
     * @param reader               used to read the XML file
     * @param libraryMethods       being the Map of SignatureLibrary objects to SignatureLibraryIntegratedMethods.
     * @throws XMLStreamException in the event of a problem reading in the XML file.
     */
    private void processEntryElement(final StartElement interproStartElement,
                                     final XMLEventReader reader,
                                     final Map<SignatureLibrary, SignatureLibraryIntegratedMethods> libraryMethods)
            throws XMLStreamException {

        String interProEntryDescription = null;
        Set<GoTerm> goTerms = null;
        Map<SignatureLibrary, List<String>> sigLibToMethodAcMap = null;

        // The InterPro Entry ID is an attribute of the element that we are currently in.
        final String entryAccession = this.retrieveAttributeValue(interproStartElement, QNAME_AT_ID, true, "/interprodb/interpro/@id");

        XMLEvent currentEvent;
        boolean stillInEntryElement = true;
        while (stillInEntryElement) {
            currentEvent = reader.nextEvent();
            if (currentEvent.isEndElement()) {
                final EndElement endElement = currentEvent.asEndElement();
                // If have found the end element for the entry, set the "stillInEntryElement" flag to false.
                stillInEntryElement = !EL_INTERPRO.equals(endElement.getName().getLocalPart());
            } else if (currentEvent.isStartElement()) {
                final StartElement startElement = currentEvent.asStartElement();
                final String elementName = startElement.getName().getLocalPart();
                if (EL_NAME.equals(elementName)) {
                    interProEntryDescription = retrieveElementCdata(reader, true, "/interprodb/interpro/name");
                } else if (EL_MEMBER_LIST.equals(elementName)) {
                    // Only want to process x-refs in the "member_list" element.
                    sigLibToMethodAcMap = processMemberDatabaseXrefs(reader);
                } else if (EL_CLASS_LIST.equals(elementName)) {
                    goTerms = processGoTerms(reader);
                }
            }
        }
        // Finished parsing the <interpro/> element...


        // If there are some member database accessions integrated into the entry, then add to the data model.
        // If there are no integrated methods in this Entry, it is ignored.
        if (sigLibToMethodAcMap != null && sigLibToMethodAcMap.size() > 0) {
            if (interProEntryDescription == null || entryAccession == null) {
                throw new IllegalStateException("Finished parsing an 'interpro' element, but have not found the ID and name for the InterPro entry.");
            }
            // Note GO terms can be null...
            final InterProEntry interproEntry = new InterProEntry(entryAccession, interProEntryDescription, goTerms);

            for (SignatureLibrary sigLib : sigLibToMethodAcMap.keySet()) {
                // Put the Signature Library into the data model, if it's not already in there.
                if (!libraryMethods.keySet().contains(sigLib)) {
                    libraryMethods.put(sigLib, new SignatureLibraryIntegratedMethods(sigLib));
                }

                for (String methodAccession : sigLibToMethodAcMap.get(sigLib)) {
                    SignatureLibraryIntegratedMethods integratedMethods = libraryMethods.get(sigLib);
                    if (integratedMethods.containsAccession(methodAccession)) {  // Sanity check for duplicated methods.
                        throw new IllegalStateException("The method accession " + methodAccession + " appears in more than one InterPro Entry in the InterPro XML file.");
                    }
                    integratedMethods.addMethodEntryMapping(methodAccession, interproEntry);
                }
            }
        }

    }

    /**
     * Looks at the /interprodb/interpro/class_list element which may contain GO terms in classification elements.
     * This method just iterates over the child elements and calls the processGoTerm method to process each element.
     *
     * @param reader to read the XML file.
     * @return a Set of GoTerm objects (may be empty, but never null).
     * @throws XMLStreamException in the event of a problem reading in the XML file.
     */
    private Set<GoTerm> processGoTerms(final XMLEventReader reader) throws XMLStreamException {
        Set<GoTerm> goTerms = null;
        XMLEvent currentEvent;
        boolean stillInClassListElement = true;
        while (stillInClassListElement) {
            currentEvent = reader.nextEvent();

            if (currentEvent.isEndElement()) {
                final EndElement endElement = currentEvent.asEndElement();
                // If have found the end element for the entry, set the "stillInEntryElement" flag to false.
                stillInClassListElement = !EL_CLASS_LIST.equals(endElement.getName().getLocalPart());
            } else if (currentEvent.isStartElement()) {
                final StartElement startElement = currentEvent.asStartElement();
                final String elementName = startElement.getName().getLocalPart();
                if (EL_CLASSIFICATION.equals(elementName)) {
                    final GoTerm goTerm = processGoTerm(startElement, reader);
                    if (goTerm != null) {
                        if (goTerms == null) {
                            goTerms = new HashSet<GoTerm>();
                        }
                        goTerms.add(goTerm);
                    }
                }
            }
        }
        return (goTerms == null) ? Collections.<GoTerm>emptySet() : goTerms;
    }

    /**
     * Processes a single /interprodb/interpro/class_list/classification element to extract the details of a single
     * GO xref.
     *
     * @param startElement being a reference to the element found by the calling method.  This has two attributes that
     *                     must be read at this point.
     * @param reader       to read the XML file.
     * @return a single GoTerm object, or null if the classification element does not contain a GO reference.
     *         (Determined by looking at the classification/@class_type attribute, which should equal "GO")
     * @throws XMLStreamException in the event of a problem reading in the XML file.
     */
    private GoTerm processGoTerm(final StartElement startElement, final XMLEventReader reader) throws XMLStreamException {
        final String classificationClass = retrieveAttributeValue(startElement, QNAME_AT_CLASS_TYPE);
        if (!GO.equals(classificationClass)) {
            return null;
        }
        final String goAccession = retrieveAttributeValue(startElement, QNAME_AT_ID, true, "/interprodb/interpro/class_list/classification/@id");

        // If got this far, then the classification element is definitely a GO term, so also get the root term and term name.

        String goTerm = null;
        String goRoot = null;

        boolean stillInClassificationElement = true;

        while (stillInClassificationElement) {
            XMLEvent currentEvent = reader.nextEvent();

            if (currentEvent.isEndElement()) {
                final EndElement endElement = currentEvent.asEndElement();
                // If have found the end tag for the classification element, set the "stillInClassificationElement" flag to false.
                stillInClassificationElement = !EL_CLASSIFICATION.equals(endElement.getName().getLocalPart());
            } else if (currentEvent.isStartElement()) {
                final StartElement innerStartElement = currentEvent.asStartElement();
                final String elementName = innerStartElement.getName().getLocalPart();
                if (EL_CATEGORY.equals(elementName)) {
                    goRoot = retrieveElementCdata(reader, true, "/interprodb/interpro/class_list/classification/category");
                } else if (EL_DESCRIPTION.equals(elementName)) {
                    goTerm = retrieveElementCdata(reader, true, "/interprodb/interpro/class_list/classification/description");
                }
            }
        }
        if (goTerm == null || goRoot == null) {
            throw new IllegalStateException("Found a GO accession " + goAccession + " but no corresponding term and / or root term.");
        }
        return new GoTerm(goRoot, goAccession, goTerm);
    }

    /**
     * If the calling method finds itself in a /interprodb/interpro/member_list/ element, this method is called
     * to parse out any db_xref elements that contain signature database methods.
     *
     * @param reader to read the XML file.
     * @return a Map of SignatureLibrary objects on to a List of member database accessions.
     * @throws XMLStreamException in the event of a problem reading in the XML file.
     */
    private Map<SignatureLibrary, List<String>> processMemberDatabaseXrefs(final XMLEventReader reader)
            throws XMLStreamException {
        final Map<SignatureLibrary, List<String>> sigLibToMethodAcMap = new HashMap<SignatureLibrary, List<String>>();
        boolean stillInMemberListElement = true;
        while (stillInMemberListElement) {
            final XMLEvent currentEvent = reader.nextEvent();
            if (currentEvent.isEndElement()) {
                final EndElement endElement = currentEvent.asEndElement();
                // If have found the end element for the member_list, set the "stillInMemberListElement" flag to false.
                stillInMemberListElement = !EL_MEMBER_LIST.equals(endElement.getName().getLocalPart());
            } else if (currentEvent.isStartElement()) {
                final StartElement innerStartElement = currentEvent.asStartElement();
                final String elementName = innerStartElement.getName().getLocalPart();
                if (EL_DB_XREF.equals(elementName)) {
                    final String memberDBName = retrieveAttributeValue(innerStartElement, QNAME_AT_DB, true, "/interprodb/interpro/member_list/db_xref/@db");
                    final String accession = retrieveAttributeValue(innerStartElement, QNAME_AT_DB_KEY, true, "/interprodb/interpro/member_list/db_xref/@dbkey");
                    final SignatureLibrary sigLib = SIG_LIB_LOOKUP.get(memberDBName);
                    if (sigLib != null) {
                        if (!sigLibToMethodAcMap.containsKey(sigLib)) {
                            sigLibToMethodAcMap.put(sigLib, new ArrayList<String>());
                        }
                        sigLibToMethodAcMap.get(sigLib).add(accession);
                    }
                }
            }
        }
        return sigLibToMethodAcMap;
    }

    /**
     * Utility method to retrieve the CDATA contents of an element.  If the contents are mandatory, this method
     * can throw a suitable Exception if the contents are missing.
     *
     * @param reader    to read the XML file.
     * @param mandatory if the CDATA must be present
     * @param xPath     to allow a suitable error message to be generated
     * @return a String representation of the CDATA
     * @throws XMLStreamException in the event of a problem reading in the XML file.
     */
    private String retrieveElementCdata(final XMLEventReader reader, final boolean mandatory, final String xPath) throws XMLStreamException {
        final String elementCData = reader.getElementText();
        if (mandatory && elementCData == null || elementCData.trim().length() == 0) {
            throw new IllegalStateException("Found an " + xPath + " element with no content.");
        }
        return elementCData;
    }

    /**
     * Utility method to retrieve the value of an attribute.  If the contents are mandatory, this method
     * can throw a suitable Exception if the contents are missing.
     *
     * @param element        being the element in which the attribute is located
     * @param attributeQName being the QName object defining the attribute name.
     * @param mandatory      if the attribute value must be present
     * @param xPath          to allow a suitable error message to be generated
     * @return a String representation of the value of the attribute or null if the attribute is optional and not present.
     */
    private String retrieveAttributeValue(final StartElement element, final QName attributeQName, final boolean mandatory, final String xPath) {
        Attribute attribute = element.getAttributeByName(attributeQName);
        if (attribute == null) {
            if (mandatory) {
                throw new IllegalStateException("Looking for value for " + xPath + " but not present - flagged as mandatory.");
            }
            return null;
        } else {
            return attribute.getValue();
        }
    }

    /**
     * Overloaded method signature - only use for retrieving the contents of OPTIONAL attributes.  May return null.
     *
     * @param element        being the element in which the attribute is located
     * @param attributeQName being the QName object defining the attribute name.
     * @return a String representation of the value of the attribute or null if the attribute is not present.
     */
    private String retrieveAttributeValue(final StartElement element, final QName attributeQName) {
        return retrieveAttributeValue(element, attributeQName, false, null);
    }
}
