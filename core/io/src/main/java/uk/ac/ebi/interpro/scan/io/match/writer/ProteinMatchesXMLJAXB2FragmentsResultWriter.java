package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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

    private static final Logger LOGGER = LogManager.getLogger(ProteinMatchesXMLJAXB2FragmentsResultWriter.class.getName());

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

        try {
            bos = new BufferedOutputStream(Files.newOutputStream(path));
            writer = new StreamResult(bos);
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
    }

    public void write(final OutputListElement protein,  final String sequenceType, final boolean isSlimOutput) throws IOException, JAXBException {

        marshaller.marshal(protein , writer);

    }

    public void footer() throws IOException{

    }
    public void newLine() throws IOException{
    }

    public void close() throws XMLStreamException, IOException {
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
    }
}
