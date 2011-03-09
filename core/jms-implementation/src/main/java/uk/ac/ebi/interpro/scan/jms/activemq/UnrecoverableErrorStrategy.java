package uk.ac.ebi.interpro.scan.jms.activemq;

import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface UnrecoverableErrorStrategy {

    void failed(StepInstance instance, Jobs jobs);
}
