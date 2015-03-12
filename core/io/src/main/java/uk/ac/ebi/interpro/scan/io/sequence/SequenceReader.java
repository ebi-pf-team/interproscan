package uk.ac.ebi.interpro.scan.io.sequence;

import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.IOException;

import uk.ac.ebi.interpro.scan.io.sequence.SequenceRecord;

/**
 * Represents a sequence reader.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface SequenceReader {

    public void read(Resource resource) throws IOException;

    public void read(InputStream stream) throws IOException;

    /**
     * Allows sequence records to be mapped to objects.
     */
    public interface Listener {

        public void mapRecord(SequenceRecord record);

    }
    
}
