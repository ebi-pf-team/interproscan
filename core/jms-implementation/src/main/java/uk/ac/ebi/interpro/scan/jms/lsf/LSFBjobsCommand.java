package uk.ac.ebi.interpro.scan.jms.lsf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Displays information about LSF jobs.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id: LSFBjobsCommand.java,v 1.4 2012/08/13 16:02:35 craigm Exp $
 * @since 1.0-SNAPSHOT
 */
public class LSFBjobsCommand extends GenericJobCommand<LSFBjobsOption> {


    public LSFBjobsCommand() {
        super("bjobs");
    }

    public LSFBjobsCommand(Map<LSFBjobsOption, Object> options) {
        super("bjobs", options );
    }

    @Override
    public List<String> getCommand() {
        List<String> result = new ArrayList<String>();
        result.add(getJobSchedulerCommand());
        for (LSFBjobsOption optionKey : getOptions().keySet()) {
            String lsfShortOption = optionKey.getShortOpt();
            result.add(lsfShortOption);
            if (optionKey.isArgumentRequired()) {
                result.add(String.valueOf(getOptionValue(optionKey)));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}