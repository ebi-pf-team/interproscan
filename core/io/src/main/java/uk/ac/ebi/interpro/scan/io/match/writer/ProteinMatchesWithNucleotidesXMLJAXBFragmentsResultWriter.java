package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.OutputListElement;
import uk.ac.ebi.interpro.scan.model.Protein;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * Write matches as output for InterProScan user in JSON format.
 */
public class ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter.class.getName());

    //protected BufferedWriter fileWriter;

    private XMLStreamWriter writer;
    //private Result writer;

    private JAXBContext jaxbContext;
    private Jaxb2Marshaller jaxb2Marshaller;
    private Marshaller marshaller;

    StreamResult streamResult;
    protected BufferedWriter bufferedWriter;

    BufferedOutputStream bos;
    Path xmlPath;
    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter(Path path, boolean isSlimOutput) throws IOException, XMLStreamException, JAXBException {

        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            bos = new BufferedOutputStream(Files.newOutputStream(path));

            this.bufferedWriter = Files.newBufferedWriter(path, characterSet);
            xmlPath = path;

            this.writer = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(bufferedWriter);

            this.streamResult = new StreamResult(System.out);
            writer.setDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");




        }catch (IOException e){
            e.printStackTrace();
        }

        this.jaxbContext = JAXBContext.newInstance(NucleotideSequence.class);
        this.marshaller = jaxbContext.createMarshaller();

        this.jaxb2Marshaller =  new Jaxb2Marshaller();


        this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        System.out.println("jaxb.formatted.output.. 1: " + this.marshaller.getProperty("jaxb.formatted.output"));
    }

    public void header(String interProScanVersion) throws  XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement("nucleotide-sequence-matches");
        writer.writeAttribute("interProScanVersion", interProScanVersion);
        writer.writeDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {
        this.marshaller.marshal(protein, writer);
    }

    public String marshal(final OutputListElement outputListElement) throws JAXBException {
        StringWriter tmpWriter = new StringWriter();
        StreamResult result = new StreamResult(tmpWriter);
        this.marshaller.marshal(outputListElement, result);
        String xml = tmpWriter.toString();
        return xml;

    }
    public void footer() throws IOException{
    }
    public void newLine() throws IOException{
    }

    public void close() throws XMLStreamException, IOException {
        bufferedWriter.flush();
        writer.writeEndDocument();
        writer.close();
        System.out.println("");

        Transformer transformer = null;

        try {
            BufferedReader buf = Files.newBufferedReader(xmlPath, characterSet);
            String newPathName = xmlPath.toAbsolutePath().toString() + ".transform";
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            String xmlFileAsString = sb.toString();

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new StreamSource(new StringReader(xmlFileAsString))
                    ,new StreamResult(new File(newPathName)));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }


    }
    /**
     * Writes out a set of proteins to a JSON file
     *
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public void write(final IMatchesHolder matchesHolder,  final String sequenceType, final boolean isSlimOutput) throws IOException {

    }

    public int write(OutputListElement protein) throws IOException {

        return 0;
    }

    public int write(String outputString) throws IOException {

        return 0;
    }


    public void setMarshaller(Jaxb2Marshaller marshaller) {

    }
}
