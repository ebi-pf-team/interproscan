package uk.ac.ebi.interpro.scan.jms.lsf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents generic job scheduler command for job submission, job monitoring etc.
 * <p/>
 * Synopsis
 * <p/>
 * job scheduler systemCmd [options] systemCmd [arguments]
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id: GenericJobCommand.java,v 1.7 2012/09/18 12:11:18 craigm Exp $
 * @since 1.0-SNAPSHOT
 */
public abstract class GenericJobCommand<T extends IJobOption> implements Serializable {

    private final String jobSchedulerCommand;

    private final Map<T, Object> options = new HashMap<T, Object>();

    // for serialization
    //protected GenericJobCommand() {}


    protected GenericJobCommand(String jobSchedulerCmd) {
        this(jobSchedulerCmd, null);
    }

    protected GenericJobCommand(String jobSchedulerCmd, Map<T, Object> options) {
        this.jobSchedulerCommand = jobSchedulerCmd;

        if (options != null) {
            this.options.putAll(options);
        }
    }

    public String getJobSchedulerCommand() {
        return jobSchedulerCommand;
    }


    public Map<T, Object> getOptions() {
        return options;
    }

    public Object getOptionValue(T optionKey) {
        return this.options.get(optionKey);
    }

    //TODO: Think if this fits the design strategy of immutable attributes
    public void putCommandOption(T key, Object value) {
        this.options.put(key, value);
    }

    //List representation of this command
    //TODO: make generic implementation (works for all except bsub!)
    public abstract List<String> getCommand();

    public abstract String toString();
}