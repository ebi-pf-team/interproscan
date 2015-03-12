package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a simple PIRSF data holder class (thread-safe).
 */
public class PirsfDatFileInfoHolder {

    private final Map<String, PirsfDatRecord> data;

    public PirsfDatFileInfoHolder(Resource pirsfDatFileResource) {
        try {
            this.data = PirsfDatFileParser.parse(pirsfDatFileResource);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse the PIRSF dat file!");
        }
    }

    public Map<String, PirsfDatRecord> getData() {
        return data;
    }
}