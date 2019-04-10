package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesWithNucleotidesXMLJAXBFragmentsResultWriter.class.getName());

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

        //
        try {
            bos = new BufferedOutputStream(Files.newOutputStream(path));

            this.bufferedWriter = Files.newBufferedWriter(path, characterSet);
            xmlPath = path;

            this.writer = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(bufferedWriter);

            //this.writer = XMLOutputFactory.newFactory()
            //        .createXMLStreamWriter(bos);

            this.streamResult = new StreamResult(System.out);
//            writer = new StreamResult(bos);
//                    .createXMLStreamWriter(System.out);


            writer.setDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");




        }catch (IOException e){
            e.printStackTrace();
        }



        //this.jaxbContext = JAXBContext.newInstance(Protein.class);
        this.jaxbContext = JAXBContext.newInstance(NucleotideSequence.class);
        this.marshaller = jaxbContext.createMarshaller();

        this.jaxb2Marshaller =  new Jaxb2Marshaller();

        this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        System.out.println("jaxb.formatted.output.. 1: " + this.marshaller.getProperty("jaxb.formatted.output"));
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty("jaxb.formatted.output", true);

        //Map<String,Object> propertiesMap = new HashMap<String,Object>();
        //propertiesMap.put("jaxb.formatted.output", true);
        //propertiesMap.put("jaxb.fragment", true);

        //this.jaxb2Marshaller.setMarshallerProperties(propertiesMap);

    }

    public void header(String interProScanVersion) throws  XMLStreamException {
       // writer.setDefaultNamespace("http://www.ebi.ac.uk");
        writer.writeStartDocument();

        writer.writeStartElement("nucleotide-sequence-matches");
        writer.writeAttribute("interProScanVersion", interProScanVersion);
        writer.writeDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");

        //bufferedWriter.write("\n");


        //writer.writeNamespace("", "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        //writer.writeAttribute("interProScanVersion", interProScanVersion);
        //writer.writeEndElement();

        //writer.writeStartElement("nucleotide-sequence-matches",  "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");

        //writer.writeStartElement("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", "nucleotide-sequence-matches",  "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        //writer.writeNamespace("", "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        //writer.writeAttribute("interProScanVersion", interProScanVersion);
        //

        //writer.writeEndElement();
        //writer.writeStartElement("protein-matches");

    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {
        //jaxb2Marshaller.marshal(writer);


//        StringWriter tmpWriter = new StringWriter();
//        StreamResult result = new StreamResult(tmpWriter);
//        this.marshaller.marshal(protein, result);
//        String xml = tmpWriter.toString();
//        tmpWriter.close();

//        System.out.println("xml: " + xml);
//        try {
//            writer.flush();
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
//        }

        //bufferedWriter.write(xml);
       // bufferedWriter.write("\n");
        this.marshaller.marshal(protein, writer);

        //this.marshaller.marshal(protein, this.streamResult);

        //System.out.println("jaxb.formatted.output.. 2: " + this.marshaller.getProperty("jaxb.formatted.output"));
        //System.out.println("jaxb.fragment.. 2: " + this.marshaller.getProperty("jaxb.fragment"));



        //System.out.println(xml);
        //fileWriter.write(xml);

    }

    public void footer() throws IOException{

    }
    public void newLine() throws IOException{
        //writer.writeComment("");
    }

    public void close() throws XMLStreamException, IOException {
        bufferedWriter.flush();
        //writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        System.out.println("");
        //bufferedWriter.close();


        //fileWriter.close();

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
        //this.marshaller = marshaller;
    }
}


/*
public class JMain {

    public static void main(String[] args) throws Exception {


        writer.writeStartDocument();
        writer.writeStartElement("http://www.java2s.com", "Import");
        writer.writeNamespace("", "http://www.java2s.com");
        writer.writeStartElement("WorkSets");

        m.marshal(new WorkSet(), writer);

        writer.writeEndDocument();
        writer.close();
        System.out.println("");
    }

    @XmlRootElement(name = "WorkSet", namespace = "http://www.java2s.com")
    public static class WorkSet {

    }
}

*/