package uk.ac.ebi.interpro.scan.jms.broker;

/**
 * An attempt to factor out the EmbeddedBroker concept, so there is no
 * direct dependency upon HornetQ in the code.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface EmbeddedBroker {

    void runBroker();

    void shutDownBroker();
}
