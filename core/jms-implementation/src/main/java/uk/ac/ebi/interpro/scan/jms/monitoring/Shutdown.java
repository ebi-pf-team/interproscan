package uk.ac.ebi.interpro.scan.jms.monitoring;

import java.io.Serializable;

/**
 *
 *
 */
public class Shutdown implements Serializable {

    private Long timestamp = System.currentTimeMillis();

    public Long getTimestamp() {
        return timestamp;
    }
}
