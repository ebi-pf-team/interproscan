package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.log4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;
import uk.ac.ebi.interpro.scan.model.OutputListElement;
import uk.ac.ebi.interpro.scan.model.Protein;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.IOException;
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
public class ProteinMatchesXMLJAXB2FragmentsResultWriter implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesXMLJAXB2FragmentsResultWriter.class.getName());

    //protected BufferedWriter fileWriter;

    //private XMLStreamWriter writer;
    private Result writer;

    private JAXBContext jaxbContext;
    private Jaxb2Marshaller marshaller;
    //private Marshaller marshaller;



    BufferedOutputStream bos;
    protected DateFormat dmyFormat;
    protected static final Charset characterSet = Charset.defaultCharset();

    public ProteinMatchesXMLJAXB2FragmentsResultWriter(Path path, boolean isSlimOutput) throws IOException, XMLStreamException, JAXBException {

        this.dmyFormat = new SimpleDateFormat("dd-MM-yyyy");

        //
        try {
            bos = new BufferedOutputStream(Files.newOutputStream(path));

//            this.writer = XMLOutputFactory.newFactory()
//                    .createXMLStreamWriter(bos);
            writer = new StreamResult(bos);
//                    .createXMLStreamWriter(System.out);
            //writer.setDefaultNamespace("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        }catch (IOException e){
            e.printStackTrace();
        }



        this.jaxbContext = JAXBContext.newInstance(Protein.class);
        //this.marshaller = jaxbContext.createMarshaller();

       // marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("jaxb.formatted.output", true);
        map.put("jaxb.fragment", true);
        marshaller.setMarshallerProperties(map);

    }

    public void header(String interProScanVersion) throws  XMLStreamException {
       // writer.setDefaultNamespace("http://www.ebi.ac.uk");
        /*
        writer.writeStartDocument();
        writer.writeStartElement("http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5", "protein-matches");
        writer.writeAttribute("interProScanVersion", interProScanVersion);
        writer.writeNamespace("", "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5");
        */
        //writer.writeStartElement("protein-matches");

    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {

        marshaller.marshal(protein , writer);

    }

    public void footer() throws IOException{

    }
    public void newLine() throws IOException{
        //writer.writeComment("");
    }

    public void close() throws XMLStreamException, IOException {
        /*
        writer.writeEndDocument();
        writer.close();
        */

        System.out.println("");
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