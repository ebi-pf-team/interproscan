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
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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


    BufferedWriter bufferedWriter;
    BufferedOutputStream bos;
    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesXMLJAXBFragmentsResultWriter(Path path, boolean isSlimOutput) throws IOException, XMLStreamException, JAXBException {

        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");

        //
        try {

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



        this.jaxbContext = JAXBContext.newInstance(Protein.class);
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

    public void header(String interProScanVersion) throws  XMLStreamException {
       // writer.setDefaultNamespace("http://www.ebi.ac.uk");
        writer.writeStartDocument();
        writer.writeStartElement("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", "protein-matches");
        writer.writeAttribute("interProScanVersion", interProScanVersion);
        writer.writeNamespace("", "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        //writer.writeStartElement("protein-matches");

    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {
        //jaxb2Marshaller.marshal(writer);
        marshaller.marshal(protein , writer);

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