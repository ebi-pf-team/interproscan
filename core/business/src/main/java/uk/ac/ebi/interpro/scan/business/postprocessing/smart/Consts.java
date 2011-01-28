//package uk.ac.ebi.interpro.scan.business.postprocessing.smart;
//// getAnalysisTypeId(mode);
//
//import uk.ac.ebi.uniparc.database.ProteinRow;
//
//import java.util.*;
//import java.io.*;
//import java.sql.*;
//
//import oracle.jdbc.driver.OracleResultSet;
//
//import umontreal.iro.lecuyer.util.Num;
//
///**
// * Globals and util methods.
// *
// * @author  Robert Petry
// * @author  Antony Quinn
// * @author  Craig McAnulla
// * @version $Id: Consts.java,v 1.72 2010/02/10 13:47:06 craigm Exp $
// * @since   1.0
// */
//public final class Consts {
//
//    static final int ARRAY_POS_UNKNOWN = -1;
//
//    // Used for analyses where running a job sequence by sequence is necessary
//    static final String SINGLE_SEQ_POST = "_SSEQ";
//
//    // Valid lsf job stati
//    static enum JobStatus {
//        PENDING, RUNNING, COMPLETED_SUCCESSFULLY, FAILED, STATUS_UNKNOWN
//    }
//    // Onion speed-audit constants
//    static enum SpeedAudit {
//        START_PROCESSING_BATCH, END_PROCESSING_BATCH
//    }
//
//
//    // Environmental samples related constants
//    static final String ENV = "_env";
//    static final int SEQ_LINE_LEN_IN_FASTA = 4000; // length of single sequence line in fasta files
//    static final String ENV_PROT_ID_PRE = ">JCVI_PEP_"; // String preceding the protein id in ENV fasta files from CAMERA
//    static final String ENV_PROT_ID_POST = " /"; // String which follows the protein id in ENV fasta files from CAMERA
//    static final String ENVSEQS_DIR = "ENV/";
//    static final String ENVSEQS_FASTA = "pred_prot_env.fasta";
//    static final long MIN_ENV_PROTEIN_ID = 1096665652461l;
//
//    // Java related constants
//    static final String javaExeOn32BitLinux = "/ebi/research/software/Linux/opt/java/jdk1.5/bin/java";
//    static final String javaExeOn64BitLinux = "/ebi/research/software/Linux_x86_64/opt/java/jdk1.5/bin/java";
//    static final String liveOnionBase = "/ebi/sp/pro1/interpro/programmers/rpetry/Onion/";
//
//    // LSF-related constants
//    static final String RESEARCH_FARM_LSF_QUEUE = "long";
//    static final String PRIMARY_LSF_QUEUE = "research";
//    static final String ANTHILL_PRIMARY_LSF_QUEUE = "default_batch";
//    static final String SECONDARY_LSF_QUEUE = "research_long";
//    static final String PRODUCTION_LSF_QUEUE = "production";
//    static final String LSF_OUTPUT_FILE_WRITTEN_OUT = "The output (if any)";
//    static final String LSF_JOB_COMPLETED_SUCCESSFULLY = "Successfully completed.";
//    static final String LSF_JOB_NEED_MORE_MEMORY = "killed after reaching LSF memory usage limit";
//    static final String LSF_OUT_EXT = ".out";
//    static final String LSF_ERR_EXT = ".err";
//    static final int MAX_LSF_JOBS = 20000;
//    static final String BSUB_ON_ANTHILL = "/ebi/lsf/production/6.1/linux2.6-glibc2.3-amd64/bin/bsub";
//    // Constants used to encode various farms used by Onion in onion.active_analyses table. Note that these are
//    // the names of farms in onion.cv_farm table, whose farm codes map to those in onion.active_analyses.
//    static final String PCFARM1 = "pcfarm1";
//    static final String ANTHILL = "anthill";
//    static final String RESEARCH = "research";
//    // Constants used to indicate whether a particular analysis type is active, ie jobs can be submitted,
//    //  or inactive, ie no further can be submitted for this analysis type.
//    // This is the value in the 'active' column of the onion.scheduled_analyses table
//    static final int ACTIVE = 1;
//    static final int INACTIVE = 0;
//    // common table names
//    static final String HWM_STAT2 = "onion.hwm_stat2";
//    static final String JOBS_RUNNING2 = "onion.jobs_running2";
//    static final String CV_ANALYSIS_TYPE = "onion.cv_analysis_type";
//
//    // Load balancing-related constants
//    // How many jobs to submit in one call to scheduler
//    static final int MAX_SINGLE_SUB_JOBS = 100;  // seq vs seq on pcfarm1 and anthill
//    static final int MAX_SINGLE_SUB_JOBS_RESEARCH = 100;  // seq vs seq  on the research_farm
//    static final int MAX_SINGLE_SUB_JOBS_SEQ_PCFARM1 = 600;  // seq vs alg
//    //static final int MAX_SINGLE_SUB_JOBS_SEQ_RESEARCH = 600;  // seq vs alg
//    static final int MAX_SINGLE_SUB_JOBS_SEQ_RESEARCH = 600; // IBU-730
//    static final int MAX_SINGLE_SUB_JOBS_SEQ_ANTHILL = 1000;  // seq vs alg
//    // How many jobs can be in the state of being submitted to lsf
//    // This serves as a safety valve in the following cases:
//    // 1. heavy pcfarm usage, lots of jobs pending (we need to stop submitting jobs until the usage subsides)
//    // 2. There was a problem with importing jobs by the tidyup call to scheduler. In this case also we need to stop
//    // submitting to the queue as otherwise we may exceed disk or number of files in a single dir quotas
//    static final int MAX_JOBS_SUBMITTED = 1000; // seq vs seq
//    static final int MAX_JOBS_SUBMITTED_SEQ_PCFARM1 = 1000; // seq vs alg
//    //static final int MAX_JOBS_SUBMITTED_SEQ_RESEARCH = 1000; // seq vs alg
//    static final int MAX_JOBS_SUBMITTED_SEQ_RESEARCH = 1000;
//    static final int MAX_JOBS_SUBMITTED_SEQ_ANTHILL = 1000; // seq vs alg
//
//    static final int SEQ_LINE_LIMIT_FOR_FASTA = 60;
//    // The <jobid>.rep flag prevent Onion from repeatedly reporting a failed job
//    static final String ERROR_ALREADY_REPORTED_EXT = ".rep";
//    // Calculations-related constants
//    static final String CUR_JAVA_HOME           = "/ebi/research/software/Linux/opt/java/jdk1.5/jre/";
//    static final String CUR_JAVA_HOME_ANTHILL   = "/ebi/research/software/Linux_x86_64/opt/java/jdk1.5/jre/";
//    //static final String BASE_DIR              = "/ebi/sp/pro1/Onion/";
//    //static final String ANTHILL_BASE_DIR      = "/ebi/production/interpro/Onion/";
//    static final String BASE_DIR                = getBaseDir("/ebi/sp/pro1/Onion/", false);
//    static final String ANTHILL_BASE_DIR        = getBaseDir("/ebi/production/interpro/Onion/", true);
//    public static final String INTERPRO_DATA_MEMBERS   = "/ebi/sp/pro1/interpro/data/members/";
//    static final String LOGS_DIR                = BASE_DIR + "logs/";
//    static final String ANTHILL_LOGS_DIR        = ANTHILL_BASE_DIR + "logs/";
//    static final String CALCS_DIR               = BASE_DIR + "calcs/";
//    static final String ANTHILL_CALCS_DIR       = ANTHILL_BASE_DIR + "calcs/";
//    static final String SEQ_CALCS_DIR           = BASE_DIR + "calcs/";
//    static final String SRC_DIR                 = getSrcDir("/ebi/sp/pro1/Onion/prod/scripts/");
//    static final String ANTHILL_CLUSTR_DIR      = "/ebi/production/interpro/CluSTr/new_update/swvals_load/";
//    static final String ANTHILL_CLUSTR_ENV_DIR  = ANTHILL_CLUSTR_DIR + "ENV/";
//    static final String ANALYSIS_NAME_NOT_SPECIFIED = "";
//    static final int ALG_TYPE_UNSPECIFIED       = 0;
//    // e.g. SWMC_NEW - only half of the all against all matrix needs to be calculated
//    static final int SEQ_VS_SEQ = 3;
//    public static final int SEQ_VS_ALG = 2;
//
//    // Oracle-specific constants
//    static final String ORACLE_ONION_INST_PREP  = "jdbc:oracle:thin:onion/";
//    static final String ORACLE_ONION_INST_POST  = "@guaro.ebi.ac.uk:1531:IPRO";
//    public static final String ORACLE_ONION_PKG        = "onion_pp";
//    static final String MARCH_SCHEMA            = "march";
//    /**
//     * If Oracle connection cannot be obtained within the timout period, the process which is trying to open it quits.
//     * This is designed to prevent multiple processes blocking or conflicting with each other trying to open connections
//     * at the same time. That kind of thing can bring an Oracle instance down.
//     */
//    static final long ORACLE_CONNECTION_TIMEOUT  = 60000l; // (ms) = 1 min
//    static final long WAIT_WHILE_THREADS_RUNNING = 1000l;  // (ms) = 1 sec
//
//    // UPI related constants
//    static final String HEX_RANGE           = "0123456789ABCDEF";
//    static final int HEX_PART_LENGTH_IN_UPI = 10;
//    static final String UPI_PREP            = "UPI";
//    static final String UNIPROT_POST        = "_UNIPROT";
//    static final String SWMC_PRE            = "SWMC_";
//    static final int NOT_FOUND              = -1;
//    public static final String NONEXISTENT_UPI     = UPI_PREP + "0000000000";
//    static final String MIN_UPI             = UPI_PREP + "0000000001";
//    // ENV-related constant - the minimum 'UniParc ID', derived from the minimum protein_id in the ENV fasta
//    // files from CAMERA
//    static final String MIN_ENV_UPI = UPI_PREP + "FF565DDCED";
//
//
//    // Error-reporting constants
//    static final String HANDLED = "handled";
//    public static final Exception EXCEPTION_ALREADY_HANDLED = new Exception(HANDLED);
//    static final String COULD_NOT_OPEN_LOG_FILE = "Couldn't open log file";
//    static final String COULD_NOT_START_THREAD = "Couldn't start thread";
//    static final String EXTERNAL_CMD_ERROR = "Error for command: '";
//    static final int EXTERNAL_PROCESS_EXIT_SUCC = 0;
//    static final String DATE_FORMAT = "dd-MMM-yy hh:mm:ss z";
//    static final String DATE_FORMAT_AUX = "dd-MMM-yy_hh:mm:ss";
//    static final String ERROR_MAIL_SUBJECT_PREFIX = "Onion Production: ";
//
//    // SignalP calculations-related constants
//    static final String SIGNALP_TMP_FILE_PRE = "TMP";
//    static final String SIGNALP_TMP_DIR = CALCS_DIR + "signalp_tmp/";
//    static final String SIGNALP_TMP_DATE_FORMAT = "yymmddHH";
//    static final int HOUR_POS = 6;
//    static final String SIGNALP_NO_STRAIGHT_RESUBMIT_COND1 = "error running mergeoutput.awk";
//    static final String SIGNALP_NO_STRAIGHT_RESUBMIT_COND2 = "error running HOW";
//
//    // PROSITE_PT-specific constants
//    static final String C_TERMINAL_REGEX_CHAR = ">";
//    static final String N_TERMINAL_REGEX_CHAR = "<";
//
//    static final String PROSITE_DAT_FILE(boolean onAnthill) {
//        return (onAnthill ? ANTHILL_CALCS_DIR : CALCS_DIR) + "PROSITE_PT/prosite.dat";
//    }
//
//    static final String PROSITE_CONFIRMATION_PATTERNS_FILE(boolean onAnthill) {
//        return (onAnthill ? ANTHILL_CALCS_DIR : CALCS_DIR) + "PROSITE_PT/sp_confirmed_patterns.dat";
//    }
//
//    static final char[] AA_ALPHABET =
//            // X stands for unknown AA - PROSITE_PT will match any character in pattern to 'X' in the matches sequence
//            {'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V', 'B', 'Z', 'X'};
//
//    // PRINTS-specific constants
//    static final String PRINTS_NO_STRAIGHT_RESUBMIT_COND1 = "ERROR: Calculation has exceeded maximum allowed complexity";
//    static final String PRINTS_NO_STRAIGHT_RESUBMIT_COND2 = "Segmentation fault";
//    static final String PRINTS_NO_HITS = "1TBN NO SIGNIFICANT RESULTS";
//    static final String PRINTS_SUCCESS_RESFILE_END = "4TBS";
//    static final String PRINTS_FALSPOS_RESFILE_END = "3TBF";
//    static final int MAX_PRINTS_GRAPHSCAN_LENGTH = 16;
//    static final char PRINTS_FINGER_CHAR = 'I';
//    static final int PRINTS_GOOD_AND_UNSUPPORTED_METHODS_ONLY = 0;
//    static final int PRINTS_SUPPORTED1_METHODS_ONLY = 1;
//    static final int PRINTS_SUPPORTED2_METHODS_ONLY = 2;
//    static final int PRINTS_ALL_METHODS = 3;
//    // This command line (plus the necessary arguments to be added later) will be used to submit a sequence by sequence job
//    static final String JAVAMEM_PCFARM = "500m";
//    static final String PRINTS_PRE_CMD_LINE =
//            SRC_DIR + "/scheduler_cron.sh " + CUR_JAVA_HOME;
//
//    // The following constants are needed for reading the PRINTS post-processing file
//    static final int READ_PRINTS_SIBLINGS = 5;
//    // Note that READ_PRINTS_HIERARCHY and READ_PRINTS_2_SHORTEST_MOTIFS_WIDTH are both equal to 4, because
//    // they refer to the same column but in two different files (former in PRINTS_HIERARCHY_BASED_PARSING_FILE,
//    // and the latter in PRINTS_PARSING_FILE)
//    static final int READ_PRINTS_HIERARCHY = 4;
//    static final int READ_PRINTS_2_SHORTEST_MOTIFS_WIDTH = 4;
//    static final int READ_PRINTS_EVAL_CUTOFFS = 2;
//    static final int READ_PRINTS_MIN_NUM_OF_MOTIFS = 3;
//    static final int PRINTS_INVALID_MODE = -1;
//    static final float GLOBAL_PRINTS_EVAL_CUTOFF = 0.0001f;
//
//    static final boolean OUTPUT_TO_STDOUT = false; // Used for seqbyseq calculations
//    static final boolean OUTPUT_TO_RES_FILE = true;
//
//    // Smith-Waterman related constants
//    static final String ANTHILL_SWMC_DIR = ANTHILL_CALCS_DIR + "SWMC_NEW/";
//    static final String PCFARM_SWMC_DIR = CALCS_DIR + "SWMC_NEW/";
//    static final String DAT_FILE_POST = ".dat";
//
//    // SSF Post-processing constants
//    public static final double SSF_E_CUTOFF = log10(0.02d);
//    // Constants used for processing alignments in SSF and GENE3D analyses
//    //0035032: domain 1 of 1, from 19 to 106: score -2.3, E = 15
//    public static final String ALIGN_INFO = "^(\\S+):\\s+domain\\s+(\\d+)\\s+of\\s+(\\d+),\\s+from\\s+(\\d+)\\s+to\\s+(\\d+):\\s+score\\s+(\\d+).*";
//
//    //regex String indicating a line containing the alignment of the upi sequence
//    //UPI0000000    22    LPTYFGPRLMMRgeALVYAWLRRLCERYNG-AYWHYYTLSDGGFYLA 67
//    public static final String UPI_ALIGN_SEQ = "^\\s*UPI(\\S+)\\s+\\d+\\s+\\S+\\s+(\\d+)\\s*";
//
//    //Default percentSame value
//    public static final double SSF_PERCENT_SAME = 0.35;
//    //Default coreSize value
//    public static final int SSF_CORE_SIZE = 15;
//    // Constants used for SSF when partitioning raw HMMER files when reading them for post-processing
//    public static final int MIN_UPI_POS = 0;
//    public static final int MAX_UPI_POS = 1;
//    public static final int MIN_PARTITION_NO = 0;
//
//    // Analysis type job sizes hashmaps
//    static final Map<Analysis, Integer> qryJobSizes = new HashMap<Analysis, Integer>();
//
//    // Store optimal sizes of job batches for each analysisTypeId in subjJobSizes
//    static {
//        qryJobSizes.put(Analysis.SWMC_NEW, 10000);
//    }
//
//    // Scheduler call parameters
//    static final String UPASS           = "-pass"; // prcedes the Onion schema password
//    static final String MODE            = "-mode"; // precedes either one of TidyUpTopUpModes, or one of the other mode constants below
//    static final String PARAMS          = "-params"; // generic argument (its value may depend on context)
//    static final String ARG_BATCH_SIZE  = "-batchsize";
//    static final String ARG_UPI_START   = "-start";
//    static final String ARG_UPI_END     = "-end";
//    static final String ARG_CONNECTION_STRING = "-conn";    // Oracle connection string
//    static final String ARG_LOG_LEVEL   = "-loglevel";
//    // The argument required to specify the memory for jvm; java jobs running on pcfarm1 need their memory restricted,
//    // whereas post-processing jobs may require as much memory as they can lay their sticky paws on
//
//    // TopUp/TidyUp modes for Onion for different farms
//    static enum TidyUpTopUpModes {
//        topup, topup_anthill, topup_research, seqtopup, seqtopup_anthill,
//        seqtopup_research, tidyup, tidyup_anthill, seqtidyup,
//        seqtidyup_anthill, seqtidyup_research
//    };
//
//    // Various Onion scheduler modes. In the actual Onion scheduler call a lot of these modes will be followed by an
//    // analysisTypeId to narrow down which analysis the particular operation refers to (e.g. pp_14 will stand for 'prints
//    // post-processing)
//    static final String POST_PROCESS = "pp";
//    static final String OUTPUT_HITS = "hits";
//    static final String EXTRACT_PROSITE_PT_CONF_PATTERNS = "pt_sp";
//    static final String CALCULATE_SEQ_BY_SEQ = "seqbyseq";
//    static final String OUTPUT_HMMCMDS = "hmmcmds";
//    static final String EXTRACT_NEW_CHANGED_PROFILE_LIBRARY = "ncprofiles";
//    static final String COLLECT_HMMER_HITS = "hitshmm";
//    static final String HMMPFAM = "hmmpfam";
//    static final String HMMSEARCH = "hmmsearch";
//    static final String IMPORT = "import";
//    static final String PRINTS_MOTIF2METHOD = "printsmotif2method_";
//    static final String PRINTS_SIBLINGS = "printssiblings_";
//    static final String PRINTS_MIN_NUM_OF_MOTIFS = "printsminmotifs_";
//    static final String ENV_SEQS = "envseqs";
//    static final String UPDATE_PFAM_EVALS_FOR_UNCHANGED_METHODS = "pfam_evals_unchanged";
//    static final String UPDATE_PFAM_NULL_EVALS = "pfam_null_evals";
//    static final String BUILD_SMART_LIBRARY = "build_smartlib";
//    static final String GET_HAMAP_CUTOFFS = "gethamapcutoffs";
//    static final String LOAD_PFAM_HMMER3_SEEDS = "loadpfamhmmer3seeds";
//
//    // Catch-all auxiliary mode
//    static final String TMP = "tmp";
//
//    // Values returned by Consts.getHmmSearchOrPfam(mode)
//    static enum HmmerMode {
//        ISHMMPFAM, ISHMMSEARCH
//    };
//
//
//    // SQL Statements
//    // Onion speed-audit variables
//    static void UPDATE_SPEED_AUDIT(SpeedAudit startEnd, Connection conn) throws SQLException {
//        String updateS = null;
//        if (startEnd == SpeedAudit.START_PROCESSING_BATCH)
//            updateS = "INSERT INTO onion.speed_audit VALUES (sysdate, null)";
//        else if (startEnd == SpeedAudit.END_PROCESSING_BATCH) {
//            // Set batch end to the latest batch_start in the table
//            updateS = "UPDATE onion.speed_audit SET batch_end = sysdate WHERE batch_end is null AND batch_start = " +
//                    "(SELECT max(batch_start) FROM onion.speed_audit)";
//        }
//        PreparedStatement updateSpeedAudit = conn.prepareStatement(updateS);
//        updateSpeedAudit.executeUpdate();
//    }
//
//    static final String JOBS_RUNNING = "onion.jobs_running"; // table storing seqvsalg jobs running on pcfarm1
//    static final String JOBS_RUNNING_ANTHILL = "onion.jobs_running_anthill"; // table storing seqvsalg jobs running on anthill
//    static final String JOBS_RUNNING_RESEARCH = "onion.jobs_running_research"; // table storing seqvsalg jobs running on the research farm
//
//    // Query to retrieve max processed upis for seqvsalg jobs
//    static final String RETRIEVE_ALL_HWM =
//            "SELECT analysis_type_id, hwm FROM onion.hwm_stat";
//    // Query to retrieve max processed upi for a seqvsalg job
//    static final String RETRIEVE_HWM =
//            "SELECT hwm FROM onion.hwm_stat WHERE analysis_type_id = ?";
//    // Update max processed upi for a seqvsalg job
//    static final String UPDATE_HWM =
//            "UPDATE hwm_stat SET hwm = ?, timestamp = sysdate WHERE analysis_type_id = ?";
//    // Query to retrieve max processed query and database upis for seqvsseq jobs
//    static final String RETRIEVE_ALL_HWM_ASM =
//            "SELECT analysis_type_id, hwm, asm FROM " + HWM_STAT2;
//    // Query to retrieve max processed query and database upis for a seqvsseq job; Note that
//    // under the current all against all scheme for SWMC_NEW, each query chunk UPIX..UPIX+batchsize (hwm)
//    // is compared to all asm chunks between minupi and maxupi (see query below). Scheduler proceeds to the
//    // next query chunk when asm reaches maxupi; the overall processing stops when hwm reaches maxupi.
//    static final String RETRIEVE_HWM_ASM =
//            "SELECT hwm, asm FROM " + HWM_STAT2 + " WHERE analysis_type_id = ?";
//
//    static final String GET_MAX_UPI(Analysis analysis, boolean seqVsSeq) {
//        if (analysis == Analysis.PIRSFBLAST)
//        /** This special case is for PIRSF blast, which is a part of PIRSF post-processing, and therefore is a slave
//         *  of PIRSF_ANALYSIS and is never allowed to overtake the max_processed_upi for pirsf HMMER
//         */
//            return "SELECT MIN(max_processed) FROM (SELECT MAX(upi) AS max_processed FROM onion.pirsf_analysis UNION SELECT MIN(s_upi_min) AS max_processed FROM jobs_running_research WHERE analysis_type_id = " + Analysis.PIRSF.getAnalysisTypeId() + " UNION SELECT MIN(s_upi_min) AS max_processed FROM jobs_running_anthill WHERE analysis_type_id = " + Analysis.PIRSF.getAnalysisTypeId() + ") ";
//        else if (analysis == Analysis.PIRSFBLAST_ENV)
//
//        /** This special case is for PIRSF_ENV blast, which is a part of PIRSF post-processing, and therefore is a slave
//         *  of PIRSF_ANALYSIS_ENV and is never allowed to overtake the max_processed_upi for pirsf HMMER
//         */
//            return "SELECT MIN(max_processed) FROM (SELECT MAX(upi) AS max_processed FROM onion.pirsf_analysis" + ENV + " UNION SELECT MIN(s_upi_min) AS max_processed FROM jobs_running WHERE analysis_type_id = " + Analysis.PIRSF_ENV.getAnalysisTypeId() + ") ";
//        else if (seqVsSeq && ! analysis.isEnvironmental()) {
//            return "SELECT max(uniparc_id) FROM onion.proteins";
//        }
//        return "SELECT max(upi) FROM " + (analysis.isEnvironmental() ? "onion.env_proteins" : "uniparc_protein");
//    }
//
//    // Update max processed upi for hwm and asm for a seqvsseq job
//    static final String UPDATE_HWM_ASM =
//            "UPDATE " + HWM_STAT2 + " SET hwm = ?, asm = ?, timestamp = sysdate WHERE analysis_type_id = ?";
//
//    // A query to check if a submitted analysisTypeId is valid, i.e. exists in onion.cv_analysis_type table
//    static final String IS_ANALYSIS_TYPE_VALID =
//            "SELECT analysis_type_id FROM " + CV_ANALYSIS_TYPE + " WHERE analysis_type_id = ?";
//
//    /**
//     * A query to get call parameters for a given analysis
//     * parameters column contains the main command line;
//     * name column contains the analysis name
//     * other_info contains additional call parameters for SIGNALP_*, and the member database release number for
//     * InterProScan analyses
//     */
//    static final String GET_CALL_PARAMETERS =
//            "SELECT parameters, name, other_info FROM " + CV_ANALYSIS_TYPE + " WHERE analysis_type_id = ?";
//
//    /**
//     * @param mode Consts.TidyUpTopUpModes
//     * @return Query to retrieve jobs running on the farm specified by mode
//     */
//    static String GET_JOBS_RUNNING(Consts.TidyUpTopUpModes mode) {
//        String table = null;
//        if (mode == Consts.TidyUpTopUpModes.seqtopup_anthill || mode == Consts.TidyUpTopUpModes.seqtidyup_anthill) {
//            table = JOBS_RUNNING_ANTHILL;
//        } else if (mode == Consts.TidyUpTopUpModes.seqtopup_research || mode == Consts.TidyUpTopUpModes.seqtidyup_research) {
//            table = JOBS_RUNNING_RESEARCH;
//        } else {
//            table = JOBS_RUNNING;
//        }
//
//        return "SELECT jr.s_upi_min, jr.s_upi_max, at.analysis_type_id, at.name " +
//                "FROM " + table + " jr, " + CV_ANALYSIS_TYPE + " at " +
//                "WHERE at.analysis_type_id = jr.analysis_type_id";
//    }
//
//    /**
//     * @param mode Consts.TidyUpTopUpModes
//     * @return Query to count jobs running on the farm specified by mode
//     */
//    static String COUNT_JOBS_RUNNING(Consts.TidyUpTopUpModes mode) {
//        String table = null;
//        if (mode == Consts.TidyUpTopUpModes.seqtopup_anthill || mode == Consts.TidyUpTopUpModes.seqtidyup_anthill) {
//            table = JOBS_RUNNING_ANTHILL;
//        } else if (mode == Consts.TidyUpTopUpModes.seqtopup_research) {
//            table = JOBS_RUNNING_RESEARCH;
//        } else {
//            table = JOBS_RUNNING;
//        }
//        return "SELECT count(*) FROM " + table;
//    }
//
//    // A query to count all seqvsalg jobs running
//    static final String COUNT_ALL_SEQ_JOBS_RUNNING =
//            "SELECT (SELECT count(*) FROM " + JOBS_RUNNING + ") + " +
//            "(SELECT count(*) FROM " + JOBS_RUNNING_ANTHILL + ") + " +
//            "(SELECT count(*) FROM " + JOBS_RUNNING_RESEARCH + ") FROM dual";
//
//
//    // A query to count all seqvsseq jobs running
//    static final String GET_JOBS_RUNNING2 =
//            "SELECT jr.s_upi_min, jr.s_upi_max, jr.q_upi_min, jr.q_upi_max, at.analysis_type_id, at.name " +
//            "FROM " + JOBS_RUNNING2 + " jr, " + CV_ANALYSIS_TYPE + " at " +
//            "WHERE at.analysis_type_id = jr.analysis_type_id";
//
//    /**
//     * @param seqVsSeq - true if seqvsseq analysis; false if seqvsalg
//     * @param mode     - TidyUpTopUpModes
//     * @return A query to insert a new job entry into a appropriate _running table, depending on TidyUpTopUpModes
//     */
//    static String INSERT_JOBS(boolean seqVsSeq, Consts.TidyUpTopUpModes mode) {
//        String tabName = null;
//        if (seqVsSeq) {
//            tabName = JOBS_RUNNING2;
//        } else {
//            if (mode == Consts.TidyUpTopUpModes.seqtopup) {
//                tabName = JOBS_RUNNING;
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_anthill) {
//                tabName = JOBS_RUNNING_ANTHILL;
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_research) {
//                tabName = JOBS_RUNNING_RESEARCH;
//            }
//        }
//        return "INSERT INTO " + tabName +
//                " (s_upi_min, s_upi_max, " +
//                (seqVsSeq ? "q_upi_min, q_upi_max," : "") +
//                "analysis_type_id, timestamp) " +
//                "VALUES (?, ?, " + (seqVsSeq ? "?, ?, " : "") + "?, sysdate)";
//    }
//
//    /**
//     * @param seqVsSeq - true if seqvsseq analysis; false if seqvsalg
//     * @param mode     - TidyUpTopUpModes
//     * @return A query to delete a job entry into a appropriate _running table, depending on TidyUpTopUpModes
//     */
//    static String DELETE_RUNNING_JOB(boolean seqVsSeq, Consts.TidyUpTopUpModes mode) {
//        String tabName = null;
//        if (seqVsSeq) {
//            tabName = JOBS_RUNNING2;
//        } else {
//            if (mode == Consts.TidyUpTopUpModes.seqtopup || mode == Consts.TidyUpTopUpModes.seqtidyup) {
//                tabName = JOBS_RUNNING;
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_research || mode == Consts.TidyUpTopUpModes.seqtidyup_research) {
//                tabName = JOBS_RUNNING_RESEARCH;
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_anthill || mode == Consts.TidyUpTopUpModes.seqtidyup_anthill) {
//                tabName = JOBS_RUNNING_ANTHILL;
//            }
//
//        }
//        return "DELETE FROM " + tabName + " " +
//                "WHERE analysis_type_id = ? " +
//                "AND s_upi_min = ? AND s_upi_max = ?" +
//                (seqVsSeq ? " AND q_upi_min = ? AND q_upi_max = ?" : "");
//    }
//
//    // PROSITE, HAMAP AND PRODOM-specific persistence
//    static String INSERT_SIMPLE_SQL(Analysis analysis) {
//        String tabId = "";
//        String scoreCol = "";
//        String scoreParam = "";
//        String statusCol = "";
//        String statusParam = "";
//        String alignmentCol = "";
//        String alignmentParam = "";
//        if (analysis == Analysis.PROSITE_PF || analysis == Analysis.PROSITE_PF_ENV) {
//            tabId = "prosite_pf_";
//            scoreCol = "score, ";
//            scoreParam = "?, ";
//            alignmentCol = ", alignment";
//            alignmentParam = ", ?";
//        } else if (analysis == Analysis.HAMAP || analysis == Analysis.HAMAP_NEW) {
//            tabId = "hamap_";
//            if (analysis == Analysis.HAMAP_NEW)
//                tabId += "new_";
//            scoreCol = "score, ";
//            scoreParam = "?, ";
//            alignmentCol = ", alignment";
//            alignmentParam = ", ?";
//        } else if (analysis == Analysis.PROSITE_PT || analysis == Analysis.PROSITE_PT_ENV) {
//            tabId = "prosite_pt_";
//            statusCol = ", status";
//            statusParam = ", ?";
//            alignmentCol = ", alignment";
//            alignmentParam = ", ?";
//        } else if (analysis == Analysis.PRODOM || analysis == Analysis.PRODOM_ENV) {
//            tabId = "prodom_";
//            scoreCol = "score, ";
//            scoreParam = "?, ";
//        }
//
//        return "INSERT INTO onion." + tabId + "analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(analysis_type_id, upi, method_ac, " +
//                "relno_major, relno_minor, " +
//                "seq_start, seq_end, " +
//                scoreCol +
//                "timestamp" +
//                statusCol +
//                alignmentCol +
//                ") " +
//                "VALUES ( ?, ?, ?, ?, ?, ?, ?, " + scoreParam + " sysdate" + statusParam + alignmentParam + ")";
//    } // INSERT_SIMPLE_SQL - end
//
//    // Panther-specific persistence
//    static String INSERT_PANTHER_SQL(Analysis analysis) {
//        return "INSERT INTO onion.panther_analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(analysis_type_id, upi, fam, subfam, " +
//                "relno_major, relno_minor, " +
//                "seq_start, seq_end, " +
//                "score, evalue, " +
//                "timestamp) " +
//                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)";
//    }
//
//    // HMMER-specific persistence
//    static String INSERT_HMMER_SQL(Analysis analysis) {
//        String lsFs = "";
//        String param = "";
//        String seqScoreParam = "?, ";
//        String tableName = null;
//        String postTN = " ";
//
//        if (analysis == Analysis.PFAM_LS || analysis == Analysis.PFAM_FS) {
//            lsFs = "ls_fs, ";
//            param = "?, ";
//            tableName = "pfam";
//            postTN = "_c ";
//
//        } else if (analysis == Analysis.SMART) {
//            tableName = "smart";
//            postTN = "_c ";
//
//        } else if (analysis == Analysis.TIGRFAM) {
//            tableName = "tigrfam";
//            postTN = "_c ";
//
//        } else if (analysis == Analysis.GENE3D) {
//            tableName = "gene3d";
//            postTN = "_c ";
//
//        } else if (analysis == Analysis.SSF) {
//            tableName = "ssf";
//            postTN = "_c ";
//            postTN += (analysis.isEnvironmental() ? ENV : " ");
//
//        } else if (analysis == Analysis.PIRSF || analysis == Analysis.PIRSF_ENV) {
//            tableName = "pirsf";
//            postTN = (analysis.isEnvironmental() ? ENV : "");
//
//        }
//
//        return "INSERT INTO onion." + tableName + "_analysis" + postTN +
//                "(analysis_type_id, upi, method_ac, " + lsFs +
//                "relno_major, relno_minor, " +
//                "seq_start, seq_end, " +
//                "hmm_start, hmm_end, " +
//                "hmm_bounds, " +
//                "score, " +
//                "seqscore, " +
//                "evalue" +
//                (analysis != Analysis.GENE3D && analysis != Analysis.PFAM_FS && analysis != Analysis.PFAM_LS ? ", timestamp" : "") + ") " +
//                "VALUES ( ?, ?, ?, " + param + "?, ?, ?, ?, ?, ?, ?, ?, " + seqScoreParam + " ?" +
//                (analysis != Analysis.GENE3D && analysis != Analysis.PFAM_FS && analysis != Analysis.PFAM_LS ? ", sysdate" : "") + ")";
//    }
//
//
//    // pirsfblast-specific persistence
//    static String INSERT_PIRSFBLAST_SQL(Analysis analysis) {
//        return "INSERT INTO onion.pirsfblast_analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(relno_major, relno_minor, upi, valid, method_ac) " +
//                "VALUES ( ?, ?, ?, ?, ? )";
//    }
//
//    // prints-specific persistence
//    static String INSERT_PRINTS_SQL(Analysis analysis) {
//        return "INSERT INTO onion.prints_" + (analysis == Analysis.PRINTS_NEW ? "new_" : "")  + "analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(analysis_type_id, upi, motif_name, motif_no, num_motifs, " +
//                "relno_major, relno_minor, " +
//                "idscore, pval, seq_start, seq_end, graphscan, evalue," +
//                "timestamp) " +
//                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)";
//    }
//
//    // SignalP-specific persistence
//    static String INSERT_SIGNALP_SQL(Analysis analysis) {
//        return "INSERT INTO onion.signalp_analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(maxc_pos, maxc_val, maxc_pred, " +
//                "maxy_pos, maxy_val, maxy_pred, " +
//                "maxs_pos, maxs_val, maxs_pred, " +
//                "means_val, means_pred, " +
//                "meand_pos, meand_val, meand_pred, " +
//                "hmm_pred, hmm_sp_prob, hmm_sa_prob, hmm_maxc_prob, hmm_maxc_pos, " +
//                "timestamp, upi, analysis_type_id) " +
//                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?)";
//    }
//
//    //TMHMM-specific persistence
//    static String INSERT_TMHMM_SQL(Analysis analysis) {
//        return "INSERT INTO onion.tmhmm_analysis" + (analysis.isEnvironmental() ? ENV : "") + " " +
//                "(pred, start_pos, end_pos, score, timestamp, upi) " +
//                "VALUES ( ?, ?, ?, ?, sysdate, ? )";
//    }
//
//    // Post-Processing SQL
//
//    // Query to find out the maximum UPI which has been post-processed for a given analysis
//    static final String GET_LAST_POSTPROCESSED_UPI_SQL =
//            "SELECT max(upi) FROM onion.iprscan ipr, onion.iprscan_releases rel " +
//            "WHERE ipr.analysis_type_id = ? " +
//            "AND ipr.analysis_type_id = rel.analysis_type_id " +
//            "AND ipr.relno_major = rel.cur_relno_major " +
//            "AND ipr.relno_minor = rel.cur_relno_minor";
//
//    static String ANALYSIS_COMPLETE_SQL(Analysis analysis) {
//        return "begin ? := onion_pp.f_analysis_complete( " + analysis.getAnalysisTypeId() + "); end;";
//    }
//
//    // INSERT a post-processed hit into onion.iprscan (the destination table for all psot-processed InterProScan hits)
//    static final String INSERT_PP_MATCH =
//            "INSERT INTO onion.iprscan " +
//            "(analysis_type_id, upi, method_ac, " +
//            "relno_major, relno_minor, " +
//            "seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, " +
//            "score, seqscore, evalue, status, " +
//            "timestamp) " +
//            "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)";
//
//    // Default status for hits in onion.iprscan
//    static final String TRUE_STATUS = "T";
//
//    // Logging levels
//    static final int VERY_VERBOSE = 2;
//    public static final int VERBOSE = 1;
//    static final int SILENT = 0;
//
//    // Flag to stipulate the post-processing of the full set of raw hits
//    static final boolean FULL_SET = false;
//    // Flag to stipulate post-processing of a small test set of raw hits
//    static final boolean TEST_BATCH = true;
//
//    // At HMMER-based member database release time, in all cases other than SSF hmmsearch (due to a speed advantage of
//    // hmmpfam) is used to calculate raw hits. For this individual model files need top be generated. The flags below
//    // indicate of single model files should be generated for all the model in the library, or only models which are new
//    // in the currently processed release (according to Method Archive)
//    static final boolean ALL_HMMS = false;
//    static final boolean NEW_HMMS_ONLY = true;
//
//    static final boolean BLAST_RUN_SEPARATELY = true; // Used for PIRSF post-processing
//
//    // SMART Post-processing
//    // Note that SMART never in the history of Onion has updated its release number (SMART appears to only add new models occasionally,
//    // under the same release number),  it has been hardcoded here. Lazy, I know.
//    static final String SMART_CURRENT_RELEASE_LOC(String relNo) {
//        return INTERPRO_DATA_MEMBERS + Analysis.SMART.getAnalysisDirectoryName().toLowerCase() + "/" + relNo;
//    }
//
//    static final String SMART_THRESHOLDS_EMPTY_FIELD_MARKER = "-";
//
//    static final String SMART_THRESHOLDS_FILE(String relNo) {
//        return SMART_CURRENT_RELEASE_LOC(relNo) + "/THRESHOLDS";
//    }
//
//    static final String SMART_DESCRIPTIONS_FILE(String relNo) {
//        return SMART_CURRENT_RELEASE_LOC(relNo) + "/DESCRIPTIONS";
//    }
//
//    static final String SMART_OVERLAPS_FILE(String relNo) {
//        return SMART_CURRENT_RELEASE_LOC(relNo) + "/overlapping";
//    }
//
//    static final String SMART_SER_THR_KINASE_METHOD = "SM00220";
//    static final String SMART_TYR_KINASE_METHOD = "SM00219";
//    static final String SMART_REPEATS_KW = "repeat";
//    static final String SMART_SPLIT_RESOLUTION_TYPE = "split";
//    static final String SMART_MERGE_RESOLUTION_TYPE = "merge";
//    static String SMART_TYR_REGEX = ".*HRD[LIV][AR]\\w\\wN.*";
//    static String SMART_SER_THR_REGEX = ".*D[LIVM]K\\w\\wN.*";
//
//    // Retrieve raw SSF results for post-processing
//    static String GET_SSF_RAW_RESULTS_SQL(boolean testBatch) {
//        return "SELECT analysis_type_id, upi, method_ac, relno_major, relno_minor, seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, score, seqscore, evalue " +
//                " FROM ssf_analysis_c WHERE upi < ? and upi > ? " +
//                // For a given upi, the family which hits it with the lowest evalue comes first (best family hit)
//                (testBatch ? " and upi < 'UPI00000003EF'" : "") + " ORDER BY upi, evalue ASC";
//    }
//
//    // Retrieve raw SMART results for post-processing
//    static String GET_SMART_RAW_RESULTS_SQL(boolean testBatch) {
//        return "SELECT analysis_type_id, upi, method_ac, relno_major, relno_minor, seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, score, seqscore, evalue " +
//                "FROM smart_analysis_c c " +
//                "WHERE (relno_major, relno_minor) IN " +
//                "(SELECT cur_relno_major, cur_relno_minor FROM onion.iprscan_releases WHERE analysis_type_id = c.analysis_type_id) " +
//                "AND upi < ? and upi > ? " +
//                (testBatch ? " and upi < 'UPI00000F4240'" : "") + " ORDER BY upi, evalue ASC";
//        /**
//         * Best evalues first, regardless of methodAc. Note that this will ensure that family members with best e-values
//         * are persisted first. However, for repeats methods hits a tally has to be held until a new upi is encountered
//         * and only then work out which method hits prevUpi more times than its threshold
//         */
//    }
//
//    // Retrieve raw GENE3D results for post-processing
//    // provides hint for optimiser to use the post-processing index
//    static String GET_GENE3D_RAW_RESULTS_SQL(boolean testBatch) {
//        return "SELECT  /*+ index(a I_GENE3D_ANALYSIS_C$4PP) */ analysis_type_id, upi, method_ac, relno_major, relno_minor, seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, score, seqscore, evalue " +
//                " FROM gene3d_analysis_c a " +
//                " WHERE upi > ? AND upi <= ? " +
//                " AND evalue <= " + Gene3DPP.DOMAINFINDER_EVALUE_LOG_CUTOFF + " " +
//                " AND method_ac IN (SELECT model from onion.gene3d_model2sf) " + // We don't post-process models which have no mappings to CATH
//                (testBatch ? " AND upi < 'UPI00004C4B40'" : "") + " ORDER BY upi, evalue, seq_end - seq_start + 1 ASC";
//    }
//
//
//    // Retrieve raw PIRSF results for post-processing
//    static final String PIRSF_PRE = "PIR";
//
//    static String GET_PIRSF_RAW_RESULTS_SQL(boolean testBatch) {
//        return "SELECT analysis_type_id, upi, method_ac, relno_major, relno_minor, seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, score, seqscore, evalue " +
//                " FROM pirsf_analysis c WHERE " +
//                "(relno_major, relno_minor) IN " +
//                "(SELECT cur_relno_major, cur_relno_minor FROM onion.iprscan_releases WHERE analysis_type_id = c.analysis_type_id) " +
//                (testBatch ? " upi IN (SELECT upi FROM iprscan_pirsf_deleted_methods) and " : "") +
//                "AND upi <= ? and upi > ? " +
//                // For a given upi, the family which hits it with the lowest evalue comes first (best family hit)
//                " ORDER BY upi, evalue, method_ac ASC";
//    }
//
//    // In order to parallelise PIRSF-computation, we run BLAST confirmation (PIRSFBLAST) separately from PIRSF HMMER run.
//    // However, to save on calculation, we run PIRSFBLAST only for sequences hit by PIRSF HMMER models in the first place
//    // (i.e. for sequences which are in onion.pirsf_analysis)
//    static String GET_PIRSF_SEQS_NEEDING_BLAST(boolean testBatch) {
//        return "SELECT distinct upi " +
//                " FROM pirsf_analysis c WHERE "+
//                 "(relno_major, relno_minor) IN " +
//                "(SELECT cur_relno_major, cur_relno_minor FROM onion.iprscan_releases WHERE analysis_type_id = c.analysis_type_id) " +
//                " AND upi <= ? and upi > ? " +
//                // For a given upi, the family which hits it with the lowest evalue comes first (best family hit)
//                (testBatch ? " and upi >= 'UPI0000000E9D' and upi <'UPI0000000E9E'" : "");
//    }
//
//    /**
//     * @param seqVsSeqOrNot
//     * @return true if analysis is seqvsseq
//     */
//    static boolean isSeqSeq(int seqVsSeqOrNot) {
//        return seqVsSeqOrNot == Consts.SEQ_VS_SEQ;
//    }
//
//    /**
//     * @param upi
//     * @return a decimal equivalent of the hexadecimal portion of UPI (UPI<10 hexadecimal digits>)
//     */
//    static int upi2DecInt(String upi) {
//        return (int) upi2Dec(upi);
//    }
//
//    /**
//     * @param upi
//     * @return as upi2DecInt, except returns long - if the decimal equivalent is too big for int to hold
//     */
//    public static long upi2Dec(String upi) {
//        long dec = 0;
//        upi = upi.substring(Consts.UPI_PREP.length());
//        for (int i = 0; i < upi.length(); i++) {
//            dec = (dec * 16) + Consts.HEX_RANGE.indexOf(upi.charAt(i));
//        }
//        return dec;
//    }
//
//    /**
//     * @param dec
//     * @return UPI whose hexadecimal part is an equivalent to the int argument
//     */
//    static String dec2Upi(int dec) {
//        return dec2Upi((long) dec);
//    }
//
//
//    /**
//     * @param dec
//     * @return as dec2Upi(int dec), excpt takes double as an argument
//     */
//    public static String dec2Upi(double dec) {
//        String upi = "";
//        int aux = 0;
//
//        while (dec > 15) {
//            aux = (int) (dec - (Math.floor(dec / 16) * 16));
//            upi = Consts.HEX_RANGE.charAt(aux) + upi;
//            dec = Math.floor(dec / 16);
//        }
//        upi = Consts.HEX_RANGE.charAt((int) dec) + upi;
//
//        // Prepend upi with zeros if necessary
//        int i = upi.length();
//        while (i < Consts.HEX_PART_LENGTH_IN_UPI) {
//            upi = "0" + upi;
//            i++;
//        }
//        upi = Consts.UPI_PREP + upi;
//
//        return upi;
//    }
//
//
////    public static String dec2Upi(long dec) {
////        String upi = Long.toHexString(dec);
////        StringBuffer buf = new StringBuffer(Consts.UPI_PREP);
////        for (int i = 10; i > upi.length(); i--){
////            buf.append('0');
////        }
////        buf.append(upi);
////        return buf.toString();
////    }
//
//
//    /**
//     * Sends email to Onion admins with subject, message and exception stack trace
//     *
//     * @param subject
//     * @param message
//     * @param exception
//     * @param logOut
//     */
//    public static void reportError(String subject,
//                            String message,
//                            Exception exception,
//                            PrintWriter logOut) {
//        // Get stack trace as StringWriter
//        if (exception != null)  {
//            StringWriter sw = new StringWriter();
//            exception.printStackTrace(new PrintWriter(sw));
//            // Append stack trace to message
//            message += "\n\n" + sw.toString();
//        }
//        reportError(subject, message, logOut);
//    }
//
//    /**
//     * Standardised error reporting utility - logs error to logOut as well as sending an email to Onion administrator,
//     * with the subject: errSubject and message: errMsg
//     *
//     * @param errSubject
//     * @param errMsg
//     * @param logOut
//     */
//    public static void reportError(String errSubject,
//                            String errMsg,
//                            PrintWriter logOut) {
//
//        if (!errMsg.endsWith(HANDLED)) {
//
//            // Write errors to logOut if possible
//            if (logOut != null) {
//                logOut.println(errMsg);
//                logOut.flush();
//            }
//
//            /* Mail errors */
//            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(DATE_FORMAT);
//            java.util.Date d = new java.util.Date();
//            MailDispatcher.sendMail(errSubject + " - " + df.format(d), errMsg);
//        }
//    }
//
//    /**
//     * Standard routine for opening log files
//     *
//     * @param logFileName
//     * @param errMailSubject
//     * @return
//     */
//    public static PrintWriter openLogFile(String logFileName, String errMailSubject) {
//        // Default: write to standard out
//        PrintWriter log = new PrintWriter(System.out);
//        String message = COULD_NOT_OPEN_LOG_FILE + " - will try to log messages to standard out";
//        try {
//            log = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true))); // open for append
//        }
//        catch (FileNotFoundException e) {
//            reportError(errMailSubject, message, e, log);
//        }
//        catch (IOException e) {
//            reportError(errMailSubject, message, e, log);
//        }
//        return log;
//    }
//
//    /**
//     * Standard routine for running external system processes
//     *
//     * @param cmd
//     * @param logOut
//     * @param errMailSubject
//     * @return process output
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    static String runExternalProcess(String cmd, PrintWriter logOut, String errMailSubject)
//            throws IOException, InterruptedException {
//        Runtime mRuntime = Runtime.getRuntime();
//        String mOut = "";
//        StringBuffer mOutSB = new StringBuffer();
//        Process mProcess = mRuntime.exec(cmd);
//        // logOut.println("Process: " + cmd + " has finished!");
//        // logOut.flush();
//
//        // Whether the external process failed or not, write its output to logFile
//        BufferedInputStream procOutStream =
//                new BufferedInputStream(mProcess.getInputStream());
//        // Only open it here explicitly, because evidently it gets opened by mRuntime.exec() call anyway,
//        // and if it is not explicitly closed, 'IOException: too many open files' can be thrown
//        OutputStream procInputStream = mProcess.getOutputStream();
//
//        // Do the following only if we're expecting any output from the external process
//        int iOut = procOutStream.read();
//        while (iOut != -1) {
//            mOutSB.append((char) iOut);
//            iOut = procOutStream.read();
//        }
//
//        mProcess.waitFor();
//        int mExitValue = mProcess.exitValue();
//        mOut = mOutSB.toString();
//        procOutStream.close();
//        procInputStream.close();
//        // logOut.println("Output for: " + cmd + "\n'" + mOut + "'");
//
//        if (mExitValue != EXTERNAL_PROCESS_EXIT_SUCC) {
//            BufferedInputStream errStream =
//                    new BufferedInputStream(mProcess.getErrorStream());
//            int leng = errStream.available();
//            byte b[] = new byte[leng];
//            errStream.read(b);
//            String mError = new String(b);
//            mError = mError.trim();
//
//            // For some reason, presumably to do with nfs, sqlldr command
//            // is sometimes not found - in that case we sleep for 1 second and try
//            // to run the command again.
//            if (errMailSubject != null)
//                reportError(errMailSubject,
//                        EXTERNAL_CMD_ERROR + cmd + (!mError.equals("") ? "' : " + mError : "'"),
//                        logOut);
//            mOut = null;
//            errStream.close();
//        }
//        return mOut;
//    }
//
//    /**
//     * A routine for extracting of UniParc sequences into a fasta-formatted file, of name upiMinMax
//     *
//     * @param upiMinMax         - as well as acting as the destination fasta file name, it has to be of the format
//     *                          UPIX_UPIY. UPIX, UPIY then form the upper and lower bounds of the Oracle query.
//     * @param conn              - DB Connection
//     * @param outputOnlyUniProt - flag to retrieve sequences with UniProt KB accessions, rather than UPIs
//     * @param swmc              - flag to retrieve sequences with protein_ids (see upi2Dec() for explanation of protein_ids)
//     *                          instead of UPIs
//     * @return
//     * @throws IOException
//     */
//    static String retrieveSeqs(String upiMinMax,
//                               Connection conn,
//                               boolean outputOnlyUniProt,
//                               boolean swmc)
//            throws IOException {
//
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        String upiMin;
//        String upiMax;
//
//        try {
//
//            if (swmc)
//                upiMinMax = upiMinMax.substring(upiMinMax.indexOf("_") + 1); // trim Consts.SWMC_PRE
//
//            if (outputOnlyUniProt) {
//                upiMinMax = upiMinMax.substring(0, upiMinMax.indexOf(Consts.UNIPROT_POST));
//            }
//
//            int underscoreInd = upiMinMax.indexOf("_");
//            upiMin = upiMinMax.substring(0, underscoreInd);
//            upiMax = upiMinMax.substring(underscoreInd + 1);
//
//            ProteinWriter writer = new ProteinWriter(CALCS_DIR + upiMin + "_" + upiMax);
//            boolean env = false;
//            int cnt = 0;
//            if (env) {
//                Utils.outputENVSequences(null, conn, upiMin, upiMax, CALCS_DIR + upiMin + "_" + upiMax, swmc);
//            } else {
//                ProteinQuery query = new ProteinQuery(conn,
//                        upiMin,
//                        upiMax,
//                        (swmc ? true : false),
//                        false,
//                        outputOnlyUniProt,
//                        false,
//                        false);
//                ProteinRow nextRow = query.next();
//
//                while (nextRow != null) {
//                    cnt++;
//                    writer.write(nextRow, swmc);
//                    nextRow = query.next();
//                }
//                query.close();
//            }
//
//            System.out.println("Ad-hoc fasta sequence file generation: generated " + cnt + " sequences for " + upiMin + ".." + upiMax);
//            System.out.flush();
//
//            if (writer != null)
//                writer.close();
//
//        } catch (SQLException sqle) {
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//            exMsg = sqle.getMessage();
//        }
//        return exMsg;
//    } // retrieveSeqs() - end
//
//    /**
//     * A utility to retrieve post-processed hits from onion.iprscan for analysis of interest
//     *
//     * @param analysis - the analysis of interest for which hits should be retrieved
//     * @param con            - DB Connection
//     * @param hitsOut        - The destination file name, into which the hits should be output
//     * @param logOut         - log file
//     * @return Exception message if an exception had be thrown (and caught) inside the routine
//     * @throws Exception
//     */
//    static String retrievePPHits(Analysis analysis, Connection con, String hitsOut, PrintWriter logOut)
//            throws Exception {
//
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        String query = null;
//        PrintWriter hitsOutPW
//                = new PrintWriter(new BufferedWriter(new FileWriter(hitsOut)));
//        String ac;
//        String methodAc;
//        int seqStart = Integer.MIN_VALUE;
//        int seqEnd = Integer.MIN_VALUE;
//        float eValue = Float.NaN;
//
//        try {
//            if (analysis == Analysis.PANTHER) {
//
//                query = "SELECT xref.ac, method_ac, seq_start, seq_end, power(10,evalue) " +
//                        "FROM onion.iprscan ipr, uniparc.xref@UAPRO xref " +
//                        "WHERE ipr.analysis_type_id = " + analysis.getAnalysisTypeId() + " " +
//                        "AND (ipr.relno_major, ipr.relno_minor) IN (SELECT cur_relno_major, cur_relno_minor FROM onion.iprscan_releases where analysis_type_id = ipr.analysis_type_id) " +
//                        "AND ipr.upi = xref.upi " +
//                        "AND xref.dbid IN (SELECT id FROM uniparc.cv_database@UAPRO WHERE descr IN ('SWISSPROT', 'TREMBL')) " +
//                        "AND xref.deleted = 'N'";
//
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                while (result.next()) {
//                    ac = null;
//                    methodAc = null;
//                    seqStart = Integer.MIN_VALUE;
//                    seqEnd = Integer.MIN_VALUE;
//                    eValue = Float.NaN;
//                    int pos = 1;
//                    ac = result.getString(pos++);
//                    methodAc = result.getString(pos++);
//                    seqStart = result.getInt(pos++);
//                    seqEnd = result.getInt(pos++);
//                    eValue = result.getFloat(pos++);
//                    if (
//                            ac != null &&
//                            methodAc != null &&
//                            seqStart != Integer.MIN_VALUE &&
//                            seqEnd != Integer.MIN_VALUE &&
//                            eValue != Double.NaN)
//                        hitsOutPW.println(ac + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd + "\t" + eValue);
//                    else {
//                        throw new Exception("Some value(s) are missing in: '" + ac + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd + "\t" + eValue + "'");
//                    }
//                } // while (result.next()) {
//
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            } else if (analysis == Analysis.PRINTS || analysis == Analysis.PRINTS_NEW) {
//
//                String graphScan = null;
//                String confGraphScan = null;
//                query = "SELECT distinct xref.ac, ipr.method_ac, ipr.hmm_bounds" +
//                        "FROM onion.iprscan ipr, uniparc.xref@UAPRO xref " +
//                        "WHERE ipr.analysis_type_id = " + analysis.getAnalysisTypeId() + " " +
//                        "AND status = 'F' " +
//                        "AND ipr.upi = xref.upi " +
//                        "AND xref.dbid IN (SELECT id FROM uniparc.cv_database@UAPRO WHERE descr IN ('SWISSPROT', 'TREMBL', 'SWISSPROT_VARSPLIC')) " +
//                        "AND xref.deleted = 'N'";
//
//                logOut.println("About to run the hits query: " + query);
//                logOut.flush();
//
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                int pos = 1;
//
//                logOut.println("Successfully executed the hits query!");
//                logOut.flush();
//
//                while (result.next()) {
//                    pos = 1;
//                    ac = null;
//                    methodAc = null;
//                    graphScan = null;
//                    confGraphScan = null;
//
//                    ac = result.getString(pos++);
//                    methodAc = result.getString(pos++);
//                    graphScan = result.getString(pos++);
//                    confGraphScan = result.getString(pos++);
//
//                    if (ac != null && methodAc != null && graphScan != null && confGraphScan != null) {
//                        hitsOutPW.println(ac + "\t" + methodAc + "\t" + graphScan + "\t" + confGraphScan);
//                    } else {
//                        throw new Exception("Some value(s) are missing in: '" + ac + "\t" + methodAc + "\t" + graphScan + "\t" + confGraphScan + "'");
//                    }
//                } // while (result.next()) {
//
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            } else if (analysis == Analysis.PROSITE_PT) {
//                query = "SELECT upi, method_ac, seq_start, seq_end " +
//                        "FROM onion.iprscan partition (prosite_pt) " +
//                        "WHERE upi IN (SELECT upi FROM uniparc.xref@UAPRO WHERE dbid = 2 AND deleted = 'N')";
//
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                int pos = 1;
//                while (result.next()) {
//                    pos = 1;
//                    ac = null;
//                    methodAc = null;
//                    ac = result.getString(pos++);
//                    methodAc = result.getString(pos++);
//                    seqStart = result.getInt(pos++);
//                    seqEnd = result.getInt(pos++);
//                    hitsOutPW.println(ac + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd);
//                } // while (result.next()) {
//
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            }
//
//        } catch (SQLException sqle) {
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//            exMsg = sqle.getMessage();
//        }
//        return exMsg;
//    } // retrievePPhits() - end
//
//    /**
//     * A utility to retrieve raw hits for an analysis of interest
//     *
//     * @param analysis - the analysis of interest
//     * @param con            - DB Connection
//     * @param hitsOut        - The destination file name, into which the hits should be output
//     * @return Exception message if an exception had be thrown (and caught) inside the routine
//     * @throws Exception
//     */
//    static String retrieveRawHits(Analysis analysis, Connection con, String hitsOut)
//            throws Exception {
//
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        String query = null;
//        PrintWriter hitsOutPW
//                = new PrintWriter(new BufferedWriter(new FileWriter(hitsOut)));
//        String upi;
//        int len = Consts.NOT_FOUND; // length of the query sequence (upi)
//        String methodAc;
//        int seqStart = Integer.MIN_VALUE;
//        int seqEnd = Integer.MIN_VALUE;
//        int hmmStart = Integer.MIN_VALUE;
//        int hmmEnd = Integer.MIN_VALUE;
//        String hmmBounds = null;
//        float score = Float.NaN;
//        float seqScore = Float.NaN;
//        double eValue = Double.NaN;
//
//        int analTypeId = 0;
//        String lsFs = null;
//        int relnoMajor = 0;
//        int relnoMinor = 0;
//
//
//        Set<String> upUpis = new HashSet<String>();
//        try {
//            if (analysis == Analysis.GENE3D) {
//                // First retrieve all UniProt upis
//                query = "SELECT DISTINCT upi FROM uniparc.xref@UAPRO WHERE dbid in (2,3) AND deleted = 'N'";
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                while (result.next()) {
//                    upi = result.getString(1);
//                    upUpis.add(upi);
//                }
//                result.close();
//
//                query = "SELECT c.upi " + /*, p.len */ ",0, method_ac, seq_start, seq_end, score, evalue " +
//                        "FROM onion.gene3d_analysis_c partition(pp) c ";// ,uniparc_protein p " +
//                // "WHERE evalue <= -3 " +
//                //"WHERE c.upi IN (SELECT upi FROM uniparc.xref WHERE dbid in (2,3) AND deleted = 'N')";
//                // "AND c.upi = p.upi";
//
//                stmt = con.prepareCall(query);
//                stmt.execute();
//                res = stmt.executeQuery();
//                result = (OracleResultSet) res;
//                while (result.next()) {
//                    upi = null;
//                    len = Consts.NOT_FOUND;
//                    methodAc = null;
//                    seqStart = Integer.MIN_VALUE;
//                    seqEnd = Integer.MIN_VALUE;
//                    eValue = Double.NaN;
//                    score = Float.NaN;
//                    int pos = 1;
//                    upi = result.getString(pos++);
//                    if (!upUpis.contains(upi))
//                        continue; // skip row if upi not in UniProt
//
//                    len = result.getInt(pos++);
//                    methodAc = result.getString(pos++);
//                    seqStart = result.getInt(pos++);
//                    seqEnd = result.getInt(pos++);
//                    score = result.getInt(pos++);
//                    eValue = result.getDouble(pos++);
//                    if (
//                            upi != null &&
//                            len != Consts.NOT_FOUND &&
//                            methodAc != null &&
//                            seqStart != Integer.MIN_VALUE &&
//                            seqEnd != Integer.MIN_VALUE &&
//                            score != Float.NaN &&
//                            eValue != Double.NaN)
//                        hitsOutPW.println(upi + "\t" + methodAc + "\t" + len + "\t0\t" + (seqEnd - seqStart + 1) + "\t0\t" +
//                                seqStart + "\t" + seqEnd + "\t0\t0\t" + eValue + "\t" + score + "\t0\t1\t" + seqStart + ":" + seqEnd);
//                    else {
//                        throw new Exception("Some value(s) are missing in: '" + upi + "\t" + len + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd + "\t" + score + "\t" + eValue + "'");
//                    }
//                } // while (result.next()) {
//
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            } else if (analysis == Analysis.SSF) {
//                query = "SELECT upi, method_ac, seq_start, seq_end, hmm_start, hmm_end, score, evalue " +
//                        "FROM onion.ssf_analysis_c " +
//                        "WHERE analysis_type_id = " + analysis.getAnalysisTypeId() + " " +
//                        "AND upi IN (SELECT upi FROM onion.ssf_sam_hmmer_sp)";
//
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                while (result.next()) {
//                    upi = null;
//                    methodAc = null;
//                    seqStart = Integer.MIN_VALUE;
//                    seqEnd = Integer.MIN_VALUE;
//                    hmmStart = Integer.MIN_VALUE;
//                    hmmEnd = Integer.MIN_VALUE;
//                    eValue = Double.NaN;
//                    score = Float.NaN;
//                    int pos = 1;
//
//                    upi = result.getString(pos++);
//                    methodAc = result.getString(pos++);
//                    seqStart = result.getInt(pos++);
//                    seqEnd = result.getInt(pos++);
//                    hmmStart = result.getInt(pos++);
//                    hmmEnd = result.getInt(pos++);
//                    score = result.getInt(pos++);
//                    eValue = result.getDouble(pos++);
//
//                    if (
//                            upi != null &&
//                            methodAc != null &&
//                            seqStart != Integer.MIN_VALUE &&
//                            seqEnd != Integer.MIN_VALUE &&
//                            hmmStart != Integer.MIN_VALUE &&
//                            hmmEnd != Integer.MIN_VALUE &&
//                            score != Float.NaN &&
//                            eValue != Double.NaN)
//
//                        hitsOutPW.println(upi + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd + "\t" +
//                                hmmStart + "\t" + hmmEnd + "\t" +
//                                score + "\t" + eValue);
//                    else {
//                        throw new Exception("Some value(s) are missing in: '" +
//                                upi + "\t" + methodAc + "\t" + seqStart + "\t" + seqEnd + "\t" +
//                                hmmStart + "\t" + hmmEnd + "\t" + score + "\t" + eValue + "'");
//                    }
//                }
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            } else if (analysis == Analysis.PFAM_LS) {
//                query = "SELECT analysis_type_id, upi, method_ac, ls_fs, relno_major, relno_minor, " +
//                        "seq_start, seq_end, hmm_start, hmm_end, hmm_bounds, score, seqscore, evalue  " +
//                        "FROM onion.pfam_analysis_c ";
//
//                CallableStatement stmt = con.prepareCall(query);
//                stmt.execute();
//                ResultSet res = stmt.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//                int pos = 1;
//                while (result.next()) {
//                    pos = 1;
//                    methodAc = null;
//
//                    analTypeId = result.getInt(pos++);
//                    upi = result.getString(pos++);
//                    methodAc = result.getString(pos++);
//                    lsFs = result.getString(pos++);
//                    relnoMajor = result.getInt(pos++);
//                    relnoMinor = result.getInt(pos++);
//                    seqStart = result.getInt(pos++);
//                    seqEnd = result.getInt(pos++);
//                    hmmStart = result.getInt(pos++);
//                    hmmEnd = result.getInt(pos++);
//                    hmmBounds = result.getString(pos++);
//                    score = result.getFloat(pos++);
//                    seqScore = result.getFloat(pos++);
//                    eValue = result.getFloat(pos++);
//
//                    hitsOutPW.println(analTypeId + "\t" + upi + "\t" + methodAc + "\t" + lsFs + "\t" + relnoMajor + "\t" + relnoMinor + "\t" +
//                            seqStart + "\t" + seqEnd + "\t" + hmmStart + "\t" + hmmEnd + "\t" + hmmBounds + "\t" +
//                            score + "\t" + seqScore + "\t" + eValue + "\t");
//                } // while (result.next()) {
//
//                hitsOutPW.close();
//                res.close();
//                stmt.close();
//            }
//
//
//        } catch (SQLException sqle) {
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//            exMsg = sqle.getMessage();
//        }
//        return exMsg;
//    }
//
//    /**
//     * Perform calculations on each sequence separately, collating results into the main job result file. Also, produce
//     * a spoof .lsf out file which mimics a successful lsf job.
//     *
//     * @param logOut
//     * @param jobID1Loc          - Location of the query fasta file
//     * @param cmdLine            - command line for running the analysis sequence by sequences
//     * @param ERROR_MAIL_SUBJECT - error msg subject in any potential error message
//     * @param analysisName       - the name of the analysis
//     * @param analysis
//     * @param jobID              - the lsf jobID of the jobs (for seqvsalg jobs, it is jobID1Loc without the path name)
//     * @param useResFile         - the result file into which single-sequence job results should be collated.
//     * @return false if the analysis failed for at least one sequence
//     */
//    public static boolean performJobSeqBySeq(PrintWriter logOut,
//                                             String jobID1Loc,
//                                             String cmdLine,
//                                             String ERROR_MAIL_SUBJECT,
//                                             String analysisName,
//                                             Analysis analysis,
//                                             String jobID,
//                                             boolean useResFile) {
//        boolean ret = true;
//        String in;
//        String currSeq = null;
//        String jobIDLoc = null;
//        jobIDLoc = jobID1Loc;
//
//        try {
//            File delF = null;
//            PrintWriter resFile = null;
//
//            if (useResFile) {
//                // Delete previous calc result file
//                delF = new File(jobIDLoc + SeqAnalysisCalc.RES_FILE_EXT);
//                if (delF.exists())
//                    delF.delete();
//
//                // Open result file for append
//                resFile = new PrintWriter(new BufferedWriter(new FileWriter(delF, false)));
//            }
//
//            // Read in job's input .fasta file
//            BufferedReader fReader = new BufferedReader(new FileReader(jobID1Loc));
//            PrintWriter singleSeqInFile = null;
//            while ((in = fReader.readLine()) != null) {
//                if (in.startsWith(">")) {
//                    if (singleSeqInFile != null) {
//                        singleSeqInFile.close();
//                        // Now perform the calculation  on submission host
//                        if (logOut != null) {
//                            logOut.println("performJobSeqBySeq - about to call: " + cmdLine);
//                            logOut.flush();
//                        }
//                        String calcOutput = Consts.runExternalProcess(cmdLine,
//                                logOut,
//                                ERROR_MAIL_SUBJECT);
//
//                        if (calcOutput == null) {
//                            if (logOut != null) {
//                                logOut.println("performJobSeqBySeq failed for the current UPI: ");
//                                logOut.flush();
//                            }
//
//                            // TODO - Check this if statement - this will ALWAYS return true.  Phil.
//
//                            if (analysis != Analysis.PRINTS || analysis != Analysis.PRINTS_ENV || analysis != Analysis.PRINTS_NEW) {
//                                /**
//                                 * PRINTS seems not to be able to cope with some sequences - we don't
//                                 * have any choice but to log the error and ignore them.
//                                 */
//                                ret = false;
//                                break;
//                            }
//                        } else {
//                            /*
//                            logOut.println("performJobSeqBySeq returned the following output: \n" + calcOutput);
//                            logOut.flush(); */
//                            if (useResFile) {
//                                resFile.println(calcOutput);
//                                resFile.flush();
//                            } else {
//                                System.out.println(calcOutput);
//                                System.out.flush();
//                            }
//                        }
//                    }
//                    singleSeqInFile =
//                            new PrintWriter(new BufferedWriter(new FileWriter(jobID1Loc + Consts.SINGLE_SEQ_POST)));
//                    singleSeqInFile.println(in);
//                    if (logOut != null) {
//                        logOut.println("Current in: " + in);
//                        logOut.flush();
//                    }
//                    currSeq = in.substring(1);
//                } else {
//                    singleSeqInFile.println(in);
//                }
//            } // while ((in = fReader.readLine()) != null) {
//
//
//            // Deal with the last sequence
//            if (singleSeqInFile != null && ret) {
//                singleSeqInFile.close();
//                // Now perform the calculation  on submission host
//                if (logOut != null) {
//                    logOut.println("performJobSeqBySeq - about to call: " + cmdLine);
//                    logOut.flush();
//                }
//                String calcOutput = Consts.runExternalProcess(cmdLine,
//                        logOut,
//                        ERROR_MAIL_SUBJECT);
//
//                if (calcOutput == null) {
//                    ret = false;
//                } else {
//                    if (useResFile) {
//                        resFile.println(calcOutput);
//                        resFile.flush();
//                    } else {
//                        System.out.println(calcOutput);
//                        System.out.flush();
//                    }
//                }
//                if (
//                        analysisName.equals(Analysis.PRINTS.getAnalysisDirectoryName()) ||
//                        analysisName.equals(Analysis.PRINTS_ENV.getAnalysisDirectoryName()) ||
//                        analysisName.equals(Analysis.PRINTS_NEW.getAnalysisDirectoryName())
//                ) {
//                    // Immitate successful file end ( in the case the last SSEQ job failed - to prevent performing a job
//                    // sequence by sequence over and over again)
//                    if (useResFile) {
//                        resFile.println(Consts.PRINTS_SUCCESS_RESFILE_END);
//                    } else {
//                        System.out.println(Consts.PRINTS_SUCCESS_RESFILE_END);
//                    }
//
//                }
//            }
//
//            if (ret) {
//                // Now create a spoof lsf file to pretend to Scheduler that that job
//                // had been performed via lsf and was successful
//                PrintWriter spoofLsfOut =
//                        new PrintWriter(new BufferedWriter(new FileWriter(jobIDLoc + Consts.LSF_OUT_EXT)));
//                spoofLsfOut.println(Consts.LSF_JOB_COMPLETED_SUCCESSFULLY);
//                spoofLsfOut.println(Consts.LSF_OUTPUT_FILE_WRITTEN_OUT);
//                spoofLsfOut.close();
//                if (useResFile) {
//                    resFile.close();
//                }
//            }
//
//        } catch (FileNotFoundException fnfe) {
//            Consts.reportError(ERROR_MAIL_SUBJECT,
//                    "Error for " + analysisName + " and : " + jobID + " - " +
//                    " Single Sequence calculation failed for " + currSeq +
//                    ": " + fnfe.getMessage(), fnfe,
//                    logOut);
//            ret = false;
//        } catch (IOException ioe) {
//            Consts.reportError(ERROR_MAIL_SUBJECT,
//                    "Error for " + analysisName + " and : " + jobID + " - " +
//                    " Single Sequence calculation failed for " + currSeq +
//                    ": " + ioe.getMessage(), ioe,
//                    logOut);
//            ret = false;
//        } catch (InterruptedException ie) {
//            Consts.reportError(ERROR_MAIL_SUBJECT,
//                    "Error for " + analysisName + " and : " + jobID + " - " +
//                    "Calculation failed for " + currSeq +
//                    ": " + ie.getMessage(), ie,
//                    logOut);
//            ret = false;
//        } finally {
//            // Tidy up after sequence by sequence calculation
//            File delF = new File(jobID1Loc + Consts.SINGLE_SEQ_POST);
//            if (delF.exists())
//                delF.delete();
//        }
//
//        return ret;
//    }
//
//
//    private static final Map<Analysis, String> LOGGING_BASE_DIRS = new HashMap<Analysis, String>();
//    private static final Map<Analysis, String> NON_LOGGING_BASE_DIRS = new HashMap<Analysis, String>();
//    /**
//     * Used for non Consts.TidyUpTopUpModes scheduler tasks
//     *
//     * @param analysis - the analysis in question
//     * @param isLogs         - true if logs directory is required; false - if the calcs directory
//     * @return The appropriate directory for calculations/logs,
//     *         depending on the analysis concerned (and specifically which farm/disk storage volume the analysis is running)
//     */
//
//    public static String getBaseDir4Analysis(Analysis analysis, boolean isLogs, Connection conn)
//            throws SQLException {
//        Map<Analysis, String> cacheMap = (isLogs) ? LOGGING_BASE_DIRS : NON_LOGGING_BASE_DIRS;
//        if (! cacheMap.containsKey(analysis)){
//            String baseDir = null;
//            String farm = null;
//            PreparedStatement stmt = conn.prepareStatement("SELECT c.name FROM onion.scheduled_analyses a, onion.cv_farm c WHERE a.analysis_type_id = ? AND a.farm = c.farm");
//            if (analysis != null) {
//                stmt.setInt(1, analysis.getAnalysisTypeId());
//                ResultSet result = stmt.executeQuery();
//                if (result.next()) {
//                    farm = result.getString(1);
//                    if (farm.equals(Consts.ANTHILL)) {
//                        baseDir = (isLogs ? Consts.ANTHILL_LOGS_DIR : Consts.ANTHILL_CALCS_DIR);
//                    } else { // // farm.equals(Consts.PCFARM1) || farm.equals(Consts.RESEARCH)
//                        baseDir = (isLogs ? Consts.LOGS_DIR : Consts.SEQ_CALCS_DIR);
//                    }
//                }
//                result.close();
//                stmt.close();
//
//            } // if (analysis != Consts.NOT_FOUND) - end
//            cacheMap.put(analysis, baseDir);
//        }
//        return cacheMap.get(analysis);
//    } // getBaseDir4Analysis() - end
//
//    /**
//     * This method is necessary for analyses such us SWMC_NEW which are run/collected results for on different farms.
//     * It is not possible to get correct jobs locations based solely on analysis, therefore TidyUpTopUpModes mode
//     * needs to be taken into account
//     *
//     * @param analysis
//     * @param mode
//     * @param isLogs
//     * @return
//     */
//    public static String getBaseDir4TTMode(Analysis analysis, TidyUpTopUpModes mode, boolean isLogs) {
//        String baseDir = null;
//        if (analysis == Analysis.SWMC_NEW) {
//            if (mode == TidyUpTopUpModes.topup_anthill || mode == TidyUpTopUpModes.tidyup_anthill) {
//                baseDir = (isLogs ? Consts.ANTHILL_LOGS_DIR : Consts.ANTHILL_CALCS_DIR);
//            } else { // default
//                baseDir = (isLogs ? Consts.LOGS_DIR : Consts.SEQ_CALCS_DIR);
//            }
//        }
//
//        return baseDir;
//    }
//
//
//    /**
//     * @param analysisTypeId
//     * @param activeAnalyses - stores analysis which are being calculated in a particular context (i.e. on a given
//     *                       farm)
//     * @return true - if analysisTypeId is found in activeAnalyses
//     */
////    private static boolean findInArray(int analysisTypeId, int[] activeAnalyses) {
////        boolean present = false;
////        int i = 0;
////        while (i < activeAnalyses.length && !present) {
////            if (activeAnalyses[i] == analysisTypeId) {
////                present = true;
////            }
////            i++;
////        }
////        return present;
////    }
//
//    // E-value calculation methods
//    /**
//     * Function used for HMMER-like calculation o evalues
//     *
//     * @param x
//     * @return
//     */
//    public static float log10(double x) {
//        return (float) (Math.log(x) / Math.log(10.0));
//
//    }
//
//
//    //
//    /**
//     * Code adapted from HMMER 2.3.2 to calculate E-values
//     *
//     * @param bitScore
//     * @param mu
//     * @param lambda
//     * @return
//     */
//    private static double extremeValueP(float bitScore, double mu, double lambda) {
//        double ret;
//        // avoid exceptions near P=1.0
//        // typical 32-bit sys: if () < -3.6, return 1.0
//        if ((lambda * (bitScore - mu)) <= -1.0 * Math.log(-1.0 * Math.log(Num.DBL_EPSILON)))
//            return 1.0;
//        // avoid underflow fp exceptions near P=0.0*/
//        else if ((lambda * (bitScore - mu)) >= 2.3 * (double) Num.DBL_MAX_10_EXP)
//            return 0.0;
//
//        // a roundoff issue arises; use 1 - e^-x --> x for small x */
//        ret = Math.exp(-1.0 * lambda * (bitScore - mu));
//        if (ret < 1e-7)
//            return ret;
//        else
//            return (1.0 - Math.exp(-1.0 * ret));
//    }
//
//    /**
//     * Code adapted from HMMER 2.3.2 to calculate E-values
//     *
//     * @param x
//     * @return
//     */
//    private static double sreLOG2(double x) {
//        return ((x) > 0 ? Math.log(Double.MAX_VALUE) * 1.44269504 : -9999.0);
//    }
//
//    /**
//     * Code adapted from HMMER 2.3.2 to calculate E-values
//     *
//     * @param x
//     * @return
//     */
//    private static double sreEXP2(double x) {
//        return (Math.exp((x) * 0.69314718));
//    }
//
//    /**
//     * Code adapted from HMMER 2.3.2 to calculate E-values
//     *
//     * @param bitScore
//     * @param mu
//     * @param lambda
//     * @return
//     */
//    private static double getPValue(float bitScore, String mu, String lambda) {
//        double pVal, pVal2;
//
//        // the bound from Bayes
//        if (bitScore >= sreLOG2(Double.MAX_VALUE))
//            pVal = 0.0;
//        else if (bitScore <= -1.0 * sreLOG2(Double.MAX_VALUE))
//            pVal = 1.0;
//        else
//            pVal = 1.0 / (1.0 + sreEXP2(bitScore));
//
//        // try for a better estimate from EVD fit
//        if (mu != null && lambda != null) {// If We have EVD values mu and lambda
//            pVal2 = extremeValueP(bitScore, Double.parseDouble(mu), Double.parseDouble(lambda));
//            if (pVal2 < pVal) pVal = pVal2;
//        }
//        return pVal;
//    }
//
//
//    /**
//     * Code adapted from HMMER 2.3.2 to calculate E-values
//     * Example calls:
//     * NumberFormat nf = NumberFormat.getInstance();
//     * nf.setMaximumFractionDigits(2);
//     * System.out.println(nf.format(tmp) + " : " + Consts.log10(Consts.hmmerCalcEValue(68.2f, "-97.896332", "0.178806", 308751, logOut)) +
//     * Consts.log10(Double.parseDouble("9.10e-16")));
//     *
//     * @param bitScore
//     * @param mu
//     * @param lambda
//     * @param dbSize
//     * @return
//     */
//    public static double hmmerCalcEValue(float bitScore, String mu, String lambda, int dbSize) {
//        double pVal = getPValue(bitScore, mu, lambda);
//        double eVal = (double) dbSize * pVal;
//        return eVal;
//    }
//
//
//    /**
//     * @param upi
//     * @param conn
//     * @return - individual sequence for upi
//     * @throws SQLException
//     */
//    static String getUniParcSequence(String upi, Connection conn)
//            throws SQLException {
//        String seq = null;
//        // First retrieve sequence from UniParc
//        ProteinQuery query = new ProteinQuery(conn,
//                upi,
//                Consts.dec2Upi(Consts.upi2Dec(upi) + 1), // extract sequence just for upi
//                false, // don't restrict seqs to those with len >= 20 AA
//                false, //  Don't Include sequences which are currently marked in CluSTr as deleted
//                false, // Don't restrict to "only SwissProt/TrEMBL, SwissProt Varsplic sequences; use UniProt KB Accessions instead of UPIs"
//                false, // Don't include the PIRSFBLAST-specific restriction
//                false // Don't include the ENV proteins restriction
//        );
//
//        ProteinRow nextRow = query.next();
//        while (nextRow != null) {
//            if (seq == null)
//                seq = nextRow.getSequence();
//            else
//                seq += nextRow.getSequence();
//            nextRow = query.next();
//        }
//        return seq;
//    } // getUniParcSequence() - end
//
//    /**
//     * In modes other than Consts.TidyUpTopUpModes, the command is followed by an analysisTypeId (e.g. 'pp_14')
//     *
//     * @param mode
//     * @return The analysisTypeId encoded at the end of mode
//     */
//    public static Analysis getAnalysisTypeId(String mode) {
//        int ret = Consts.NOT_FOUND;
//        int indU = mode.lastIndexOf("_");
//        if (indU != -1)
//            ret = Integer.parseInt(mode.substring(indU + 1));
//        return Analysis.getAnalysisByAnalysisTypeId(ret);
//    } // getAnalysisTypeId(String mode) - end
//
//    /**
//     * In modes other than Consts.TidyUpTopUpModes, the command is prepended by the release number of an InterPro
//     * member database (e.g. '21.0_hmmsearch...')
//     *
//     * @param mode
//     * @return The release number for the InterPro member database which will have been encoded in mode
//     */
//    public static String getReleaseNo(String mode) {
//        String ret = null;
//        int indU = mode.indexOf("_");
//        if (indU != -1)
//            ret = mode.substring(0, indU);
//        return ret;
//    }
//
//    /**
//     * @param mode
//     * @return HmmerMode which corresponds to String mode
//     */
//    public static HmmerMode getHmmSearchOrPfam(String mode) {
//        if (mode.indexOf(Consts.HMMPFAM) != -1)
//            return HmmerMode.ISHMMPFAM;
//        else // if (mode.indexOf(Consts.HMMSEARCH)
//            return HmmerMode.ISHMMSEARCH;
//
//    }
//
//    /**
//     * @param a
//     * @param b
//     * @return smaller of a and b
//     */
//    public static int min(int a, int b) {
//        if (a < b) return a;
//        return b;
//    }
//
//
//    /**
//     * Round a double value to a specified number of decimal
//     * places.
//     *
//     * @param val    the value to be rounded.
//     * @param places the number of decimal places to round to.
//     * @return val rounded to places decimal places.
//     */
//    public static double round(double val, int places) {
//        long factor = (long) Math.pow(10, places);
//
//        // Shift the decimal the correct number of places
//        // to the right.
//        val = val * factor;
//
//        // Round to the nearest integer.
//        long tmp = Math.round(val);
//
//        // Shift the decimal the correct number of places
//        // back to the left.
//        return (double) tmp / factor;
//    }
//
//    /**
//     * Round a float value to a specified number of decimal
//     * places.
//     *
//     * @param val    the value to be rounded.
//     * @param places the number of decimal places to round to.
//     * @return val rounded to places decimal places.
//     */
//    public static float round(float val, int places) {
//        return (float) round((double) val, places);
//    }
//
//
//    /**
//     * Used by Onion scheduler to check if the current command is already running
//     *
//     * @param   con
//     * @param   flag    For example "pp_13"
//     * @return  true of flag already exists in onion.scheduler_flags
//     */
//    public static boolean flagExists(Connection con, String flag) {
//        boolean ret = false;
//        try {
//            PreparedStatement getFlag =
//                    con.prepareStatement("select timestamp from onion.scheduler_flags where flag = ?");
//            getFlag.setString(1, flag);
//            ResultSet res = getFlag.executeQuery();
//            OracleResultSet result = (OracleResultSet) res;
//
//            if (result.next()) {
//                ret = true;
//            }
//        } catch (SQLException ex) {
//            // If there was an error in retrieving the flag, it is safest to assume that it exists, so that scheduler
//            // quits
//            // TODO: write message to error log
//            ret = true;
//        }
//
//        return ret;
//    }
//
//    /**
//     * Creates flag in onion.scheduler_flags
//     *
//     * @param con
//     * @param flag
//     * @throws SQLException
//     */
//    public static void createFlag(Connection con, String flag) throws SQLException {
//        PreparedStatement insertFlag =
//                con.prepareStatement("insert into onion.scheduler_flags values (?, sysdate)");
//        if (flag.length() > 20)
//            flag = flag.substring(0, 20);
//        insertFlag.setString(1, flag);
//        insertFlag.executeUpdate();
//        con.commit();
//    }
//
//    /**
//     * Deletes flag from onion.scheduler_flags
//     *
//     * @param con
//     * @param flag
//     * @throws SQLException
//     */
//    public static void deleteFlag(Connection con, String flag) throws SQLException {
//        PreparedStatement deleteFlag =
//                con.prepareStatement("delete from onion.scheduler_flags where flag = ?");
//        if (flag.length() > 20)
//            flag = flag.substring(0, 20);
//        deleteFlag.setString(1, flag);
//        deleteFlag.executeUpdate();
//        con.commit();
//    }
//
//
//    /**
//     * File.renameTo method doesn't appear to work across different (nfs-mounted)
//     * disk volumes. Therefore we need to implement our own routine which gets round
//     * this problem by simply reading the file and outputting in to the new location
//     * line by line.
//     *
//     * @param from     - source file
//     * @param to       - the name to rename the file to
//     * @param copyOnly - if true, don;t delete the 'from' file
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    static void moveFileAcrossNfsMountedDisks(File from, File to, boolean copyOnly)
//            throws FileNotFoundException, IOException {
//        // Read in file first
//        BufferedReader fReader = new BufferedReader(new FileReader(from));
//        PrintWriter fWriter = new PrintWriter(new BufferedWriter(new FileWriter(to)));
//        String in;
//        while ((in = fReader.readLine()) != null) {
//            fWriter.println(in);
//        }
//        fReader.close();
//        fWriter.close();
//
//        if (!copyOnly)
//            from.delete();
//    }
//
//    /**
//     * Inserts the query fasta file location into place marked by '@' in the original command line from cv_analysis_type
//     * Used only for PRINTS and PRODOM; for all the other analyses the query fasta file comes at the end.
//     *
//     * @param jobIDLoc - the query fasta file location
//     * @param job      for which the cmdline is to be ammended
//     * @return
//     */
//    public static String processCmdLine(String jobIDLoc, JobInfo job) {
//        StringBuffer cmdLine = new StringBuffer();
//        int inputFilePos = job.cmdLine.indexOf("@");
//        String pre = job.cmdLine.substring(0, inputFilePos);
//        String post = job.cmdLine.substring(inputFilePos + 1);
//        cmdLine.append(pre).append(" ").append(jobIDLoc).append(" ").append(post);
//        return cmdLine.toString();
//    } // processCmdLine() - end
//
//    /**
//     * Narrow a String mode to element of TidyUpTopUpModes enum if the corresponding element exists
//     *
//     * @param mode - submiited to Onion scheduler call
//     * @return TidyUpTopUpModes element equivalent to mode parameter if one exists
//     */
//    public static TidyUpTopUpModes isTidyUpTopUpMode(String mode) {
//        TidyUpTopUpModes ttMode = null;
//
//        TidyUpTopUpModes[] elements = Consts.TidyUpTopUpModes.values();
//        int i = 0;
//        while (i < elements.length && ttMode == null) {
//            if (mode.equals(elements[i].toString())) {
//                ttMode = elements[i];
//            }
//            i++;
//        }
//
//        return ttMode;
//    } // isTidyUpTopUpMode(String mode)
//
//
//    /**
//     * Returns Onion base directory using either system property or default value.
//     *
//     * Temporary arrangement until we handle properties properly in Onion.
//     *
//     * @param   defaultValue Default directory
//     * @return  Onion base directory
//     */
//    private static String getBaseDir(String defaultValue, boolean isAntHill)    {
//        final String PROPERTY_BASE_DIR          = "onion.base.dir";
//        final String PROPERTY_ANTHILL_BASE_DIR  = "onion.anthill.base.dir";
//        if (isAntHill)   {
//            return System.getProperty(PROPERTY_ANTHILL_BASE_DIR, defaultValue);
//        }
//        else    {
//            return System.getProperty(PROPERTY_BASE_DIR, defaultValue);
//        }
//    }
//     /**
//     * Returns Onion source directory using either system property or default value.
//     *
//     * Temporary arrangement until we handle properties properly in Onion.
//     *
//     * @param   defaultValue Default directory
//     * @return  Onion source directory
//     */
//
//    private static String getSrcDir(String defaultValue) {
//        return System.getProperty("onion.src.dir", defaultValue);
//
//    }
//
//}
//
