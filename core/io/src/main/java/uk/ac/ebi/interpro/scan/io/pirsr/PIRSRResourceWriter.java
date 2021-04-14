package uk.ac.ebi.interpro.scan.io.pirsr;

import uk.ac.ebi.interpro.scan.io.AbstractResourceWriter;

public final class PIRSRResourceWriter extends AbstractResourceWriter<String> {

    @Override protected String createLine(String record) {
        return record;
    }

}
