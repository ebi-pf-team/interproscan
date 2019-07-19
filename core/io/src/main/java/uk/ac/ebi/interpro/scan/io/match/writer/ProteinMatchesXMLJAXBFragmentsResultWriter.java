package uk.ac.ebi.interpro.scan.io.match.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Write matches as output for InterProScan user in JSON format.
 */
public class ProteinMatchesXMLJAXBFragmentsResultWriter implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesXMLJAXBFragmentsResultWriter.class.getName());

    //protected BufferedWriter fileWriter;

    private XMLStreamWriter writer;
    //private Result writer;

    private JAXBContext jaxbContext;
    private Jaxb2Marshaller jaxb2Marshaller;
    private Marshaller marshaller;

    Path xmlPath;

    BufferedWriter bufferedWriter;
    BufferedOutputStream bos;
    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesXMLJAXBFragmentsResultWriter(Path path, Class classToBind, boolean isSlimOutput) throws IOException, XMLStreamException, JAXBException {

        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");

        //
        try {
            this.xmlPath = path;
            bos = new BufferedOutputStream(Files.newOutputStream(path));
            bufferedWriter = Files.newBufferedWriter(path, characterSet);

            this.writer = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(bufferedWriter);

            //this.writer = XMLOutputFactory.newFactory()
            //       .createXMLStreamWriter(bos);

//            writer = new StreamResult(bos);
//                    .createXMLStreamWriter(System.out);
            writer.setDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        }catch (IOException e){
            e.printStackTrace();
        }

        this.jaxbContext = JAXBContext.newInstance(classToBind);
        //this.jaxbContext = JAXBContext.newInstance(NucleotideSequence.class);
        this.marshaller = jaxbContext.createMarshaller();

        this.jaxb2Marshaller =  new Jaxb2Marshaller();

        this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.setProperty("jaxb.formatted.output", true);

        //Map<String,Object> propertiesMap = new HashMap<String,Object>();
        //propertiesMap.put("jaxb.formatted.output", true);
        //propertiesMap.put("jaxb.fragment", true);

        //this.jaxb2Marshaller.setMarshallerProperties(propertiesMap);

    }

    public void header(String interProScanVersion, String localname) throws XMLStreamException, IOException {
       // writer.setDefaultNamespace("http://www.ebi.ac.uk");
        writer.writeStartDocument();
        //bufferedWriter.write("\n");
        writer.writeStartElement("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", localname);
        writer.writeAttribute("interProScanVersion", interProScanVersion);
        writer.writeNamespace("", "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        //writer.writeStartElement("protein-matches");

    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {
        //jaxb2Marshaller.marshal(writer);
        marshaller.marshal(protein , writer);

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
        //writer.writeComment("");
    }

    public void close() throws XMLStreamException, IOException {
        writer.writeEndDocument();
        writer.close();
        System.out.println("");
        //bufferedWriter.close();

        Transformer transformer = null;

        try {
            BufferedReader buf = Files.newBufferedReader(xmlPath, characterSet);
            String newPathName = xmlPath.toAbsolutePath().toString() + ".transform";
            /*
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            String xmlFileAsString = sb.toString();

            */

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            //transformer.transform(new StreamSource(new StringReader(xmlFileAsString))
            transformer.transform(new StreamSource(buf)
                    ,new StreamResult(new File(newPathName)));
            //rename the new file

            Path sourcePath = Paths.get(newPathName);
            Path targetPath = Paths.get(xmlPath.toAbsolutePath().toString());
            Utilities.verboseLog(20,"Moving/Renaming the xmls file temp xml file:  " + sourcePath.toAbsolutePath().toString()
                    + " - with - " + targetPath.toAbsolutePath().toString());
            Files.move(sourcePath, targetPath, REPLACE_EXISTING);

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