package uk.ac.ebi.interpro.scan.jms.lsf;

/**
 * Represents a generic job scheduler option which is used within the abstract {@link GenericJobCommand}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id: IJobOption.java,v 1.2 2012/08/13 16:02:36 craigm Exp $
 * @since 1.0-SNAPSHOT
 */
//TODO: Should be an abstract class?
public interface IJobOption {

    public String getShortOpt();

    public String getDescription();

    public boolean isArgumentRequired();
}
