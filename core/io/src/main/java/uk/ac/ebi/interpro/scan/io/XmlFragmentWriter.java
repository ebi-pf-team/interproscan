package uk.ac.ebi.interpro.scan.io;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;
import uk.ac.ebi.interpro.scan.model.Protein;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Phil Jones
 *         Date: 14/06/11
 *         Time: 17:49
 */
public class XmlFragmentWriter {

    private static final Logger LOGGER = LogManager.getLogger(XmlFragmentWriter.class.getName());

    private Jaxb2Marshaller marshaller;

    private BufferedOutputStream bos;

    Result result;

    @Required
    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setBos(BufferedOutputStream bos) {
        this.bos = bos;
    }

    public void setOutputStream(final Path path) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path));
        setBos(bos);
        result = new StreamResult(bos);

    }

    public void header(String interProScanVersion) throws XMLStreamException {

    }

    public void writeMatches(final Path path, final IMatchesHolder matchesHolder) throws IOException {

        LOGGER.debug("About to start writing out match XML.");
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
            Result result = new StreamResult(bos);
            marshaller.marshal(matchesHolder, result);
//            marshaller.marshal();
            LOGGER.debug("Finished writing out match XML.");
        }

    }

    public void write(Protein protein){
        marshaller.marshal(protein, result);
    }

}