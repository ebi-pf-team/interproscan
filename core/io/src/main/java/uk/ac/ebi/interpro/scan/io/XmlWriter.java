package uk.ac.ebi.interpro.scan.io;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.ac.ebi.interpro.scan.model.IMatchesHolder;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public void writeMatches(final File file, final IMatchesHolder matchesHolder) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("The file " + file.getAbsolutePath() + " already exists and cannot be deleted.");
            }
        }
        BufferedOutputStream bos = null;
        LOGGER.debug("About to start writing out match XML.");
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            Result result = new StreamResult(bos);
            marshaller.marshal(matchesHolder, result);
            LOGGER.debug("Finished writing out match XML.");
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }
}