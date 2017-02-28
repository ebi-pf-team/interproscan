package uk.ac.ebi.interpro.scan.io;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;

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
public class XmlWriter {

    private static final Logger LOGGER = Logger.getLogger(XmlWriter.class.getName());

    private Jaxb2Marshaller marshaller;

    @Required
    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void writeMatches(final Path path, final IMatchesHolder matchesHolder) throws IOException {

        LOGGER.debug("About to start writing out match XML.");
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
            Result result = new StreamResult(bos);
            marshaller.marshal(matchesHolder, result);
            LOGGER.debug("Finished writing out match XML.");
        }

    }
}