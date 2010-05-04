package uk.ac.ebi.interpro.scan.jms.activemq;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.Message;

/**
 * This class performs the execution in a transaction.  This has been factored out of the AmqInterProScanWorker class
 * to ensure that the transaction semantics work...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface StepExecutionTransaction {

    @Transactional
    void executeInTransaction(StepExecution stepExecution, Message message);

}
