package uk.ac.ebi.interpro.scan.jms.main;

import java.util.EnumSet;

/**
 * InterProScan 5 run mode (standalone, convert etc).
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum Mode {
    //Mode for InterPro production
    MASTER("master", "spring/jms/master/production-master-context.xml"),
    WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-context.xml"),
    DISTRIBUTED_WORKER("distributedWorkerController", "spring/jms/worker/distributed-worker-context.xml"),
    HIGHMEM_WORKER("distributedWorkerController", "spring/jms/activemq/activemq-distributed-worker-highmem-context.xml"),
    //Default mode. Mode to run the I5 black box version
    STANDALONE("standalone", "spring/jms/master/standalone-master-context.xml"),
    //This mode allows spawning of distributed workers on demand using the new i5jms architecture
    DISTRIBUTED_MASTER("distributedMaster", "spring/jms/master/distributed-master-context.xml"),
    CLUSTER("distributedMaster", "spring/jms/master/distributed-master-context.xml"),
    SINGLESEQ("ssOptimisedBlackBoxMaster", "spring/jms/master/singleseq-optimised-master-context.xml"),
    // Use this internal mode for creating the H2 in-memory database
    INSTALLER("installer", "spring/installer/installer-context.xml"),
    // Use this mode for creating the test database that lives in /jms-implementation/src/test/resources/
    EMPTY_INSTALLER("installer", "spring/installer/empty-installer-context.xml"),
    //This mode is for converting I5 XML files into an other output format supported by I5 and I4 XML as well (additional option)
    CONVERT("convert", "spring/converter/converter-context.xml"),
    //Use this mode to send shutdown commands to the master (monitoring or cluster version) or to get runtime statistics like submitted/finished jobs.
    MONITOR("monitorApplication", "spring/jms/monitoring/monitor-context.xml");

    public static final EnumSet<Mode> SET_OF_NO_MODES = EnumSet.noneOf(Mode.class);
    public static final EnumSet<Mode> SET_OF_CONVERT_MODE_ONLY = EnumSet.range(Mode.CONVERT, Mode.CONVERT);
    public static final EnumSet<Mode> SET_OF_STANDARD_MODES = EnumSet.complementOf(SET_OF_CONVERT_MODE_ONLY);
    public static final EnumSet<Mode> SET_OF_ALL_MODES = EnumSet.allOf(Mode.class);

    private String contextXML;

    private String runnableBean;

    private static String commaSepModeList;

    static {
        StringBuilder sb = new StringBuilder();
        for (Mode mode : Mode.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(mode.toString().toLowerCase());
        }
        commaSepModeList = sb.toString();
    }

    /**
     * Constructor for modes.
     *
     * @param runnableBean Optional bean that implements Runnable.
     * @param contextXml   being the Spring context.xml file to load.
     */
    private Mode(String runnableBean, String contextXml) {
        this.runnableBean = runnableBean;
        this.contextXML = contextXml;
    }

    public String getRunnableBean() {
        return runnableBean;
    }

    public String getContextXML() {
        return contextXML;
    }

    public static String getCommaSepModeList() {
        return commaSepModeList;
    }
}
