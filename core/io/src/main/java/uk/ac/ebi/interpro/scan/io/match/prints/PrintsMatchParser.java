package uk.ac.ebi.interpro.scan.io.match.prints;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusFeature;
import uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel.PhobiusProtein;
import uk.ac.ebi.interpro.scan.io.match.prints.parsemodel.PrintsProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 *
 * @author John Maslen
 * @version $Id$
 * @since 1.0
 */

public class PrintsMatchParser {

        public Set<PrintsProtein> parse(InputStream is, String fileName, Map evalCutOffs) throws IOException {
        Set<PrintsProtein> proteinsWithMatches = new HashSet<PrintsProtein>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            PrintsProtein protein = null;
            int lineNumber = 0;
            while (reader.ready()){
                lineNumber++;
                String line = reader.readLine();
                //Sn 			→		proteinID
                //1TBH 			→		motifName (1), eValue (2)#
                //2TBH/2TBN		→		motifName (1), eValue(9), graphScan (10)
                //3TBH/3TBN 	→		motifName (1), motifNo (2), motifTotal (4), idScore (5), pValue (7)*, length (9), position (11)**


            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return proteinsWithMatches;
    }

}

// ONION CODE FOR PARSING THE OUTPUT FILE:
//
//
// /**
// *
// *
// *
// * public final class PRINTSCalc extends SeqAnalysisCalc {
//    protected static final String ERROR_MAIL_SUBJECT =
//            Consts.ERROR_MAIL_SUBJECT_PREFIX + "PRINTSCalc Error - ";
//
//    //TODO sort out these constructors!
//    /**
//     * Initialises the class with a user-defined logfile
//     *
//     * @param analysis  The analysis type we are using
//     * @param conn  Connection to database
//     * @param logFile The logfile you want to use
//     * @throws SQLException If problem connecting to database
//     */
//    public PRINTSCalc(Analysis analysis, Connection conn, String logFile) throws SQLException {
//        super(analysis, Consts.SEQ_VS_ALG, Consts.getBaseDir4Analysis(analysis, true, conn) + logFile);
//    }
//
//    /**
//     * Initialises the class with the default logfile
//     *
//     * @param analysis The analysis type that we are using
//     * @param conn Connection to database
//     * @throws SQLException If problem connecting to database
//     */
//    public PRINTSCalc(Analysis analysis, Connection conn) throws SQLException {
//         this(analysis, conn, "prints.log");
//    }
//
//    /**
//     * Obtain command to instruct the Onion scheduler to perform this job sequence by sequence - see the description
//     * of the getCmd() method below for more info.
//     * @param jobIDLoc
//     * @param pass
//     * @return
//     */
//    public String processCmdLine(String jobIDLoc, String pass) {
//        //TODO alter this line so that it could run on anthill!
//        return Consts.PRINTS_PRE_CMD_LINE + " " + pass + " " + Consts.CALCULATE_SEQ_BY_SEQ + "_" + job.analysis + " " + jobID + " " + Consts.JAVAMEM_PCFARM;
//    }
//
//
//    /**
//     * @param jobIDLoc
//     * @return The actual command to be used by the Onion scheduler to perform this job sequence by sequence - see the
//     * description of the getCmd() method below for more info.
//     */
//    public String processCmdLineSeqBySeq(String jobIDLoc) {
//             return Consts.processCmdLine(jobIDLoc, job);
//    } // processCmdLineSeqBySeq() - end
//
//
//    /**
//     * See CalcJob for mode info.
//     * Note that PRINTS, PRINTS_ENV and PRINTS_NEW are calculated sequence by sequence by default, as FingerPrintScan proved
//     * too flaky to process multi-sequence query files. The rather roundabout way of running PRINTS sequence by sequence
//     * is to submit an lsf call to Onion scheduler to run this analysis seqbyseq - the command for that is got by
//     * calling this.processCmdLine() - (this functionality already exists for the scheduler to process sequence by sequence
//     * persistently failing SIGNALP_* jobs - we just re-use it to run PRINTS).
//     *
//     * Upon receipt of a request to process this analysis sequence by sequence, the scheduler calls this.processCmdLineSeqBySeq()
//     * to slot it jobID1Loc into the space marked as '@' in the PRINTS cmdline in onion.cv_analysis_type table, and then
//     * performs the job sequence by sequence by calling Consts.performJobSeqBySeq() method.
//     * @param jobPriority
//     * @param queue
//     * @param pass
//     * @param optJavaLoc
//     * @return
//     */
//    public String getCmd(String jobPriority, String queue, String pass, String optJavaLoc) {
//        String printsCmdLine = processCmdLine(jobID1Loc, pass);
//        // For PRINTS, we also need to submit "null" instead of jobID1Loc
//
//        return getCmdString(printsCmdLine,  jobPriority,  queue, jobID, jobIDLoc);
//
//        /*
//        String cmd = SUBMIT2LSF_COMMAND +
//                SUBMIT2LSF_COMMAND_JID_OPT + jobID +
//                SUBMIT2LSF_COMMAND_JIDLOC_OPT + jobIDLoc +
//                SUBMIT2LSF_COMMAND_QUEUE_OPT + queue +
//                SUBMIT2LSF_COMMAND_INPUT_OPT + "null" +
//                SUBMIT2LSF_COMMAND_INPUTB_OPT + (Consts.isSeqSeq(job.seqVsSeq) ? jobID2Loc : "null") +
//                SUBMIT2LSF_JOB_PRIORITY_OPT + jobPriority +
//                SUBMIT2LSF_COMMAND_BSUB_OPT +
//                "bsub" +
//                SUBMIT2LSF_COMMAND_CMDLINE_OPT + printsCmdLine;
//
//        return cmd;
//        */
//    }
//
//    public String getCmdString(String printsCmdLine, String jobPriority, String queue, String jobID, String jobIDLoc) {
//        // For PRINTS, we need to submit "null" instead of jobID1Loc
//        return SUBMIT2LSF_COMMAND +
//                SUBMIT2LSF_COMMAND_JID_OPT + jobID +
//                SUBMIT2LSF_COMMAND_JIDLOC_OPT + jobIDLoc +
//                SUBMIT2LSF_COMMAND_QUEUE_OPT + queue +
//                SUBMIT2LSF_COMMAND_INPUT_OPT + "null" +
//                SUBMIT2LSF_COMMAND_INPUTB_OPT +  "null" +
//                SUBMIT2LSF_JOB_PRIORITY_OPT + jobPriority +
//                SUBMIT2LSF_COMMAND_BSUB_OPT +
//                "bsub" +
//                SUBMIT2LSF_COMMAND_CMDLINE_OPT + printsCmdLine;
//
//    }
//
//    /**
//     * See CalcJob for more info.
//     * @param conn
//     * @throws SQLException
//     * @throws FileNotFoundException
//     * @throws IOException
//     * @throws Exception
//     */
//    public void persistJobData(Connection conn)
//            throws SQLException, FileNotFoundException, IOException, Exception {
//        String in;
//        String upi = null;
//        String motifName = null;
//        int motifNo = -1;
//        int noMotifs = -1;
//        float idScore = -1f;
//        float pVal = -1f;
//        int dbRelMajor = -1;
//        int dbRelMinor = -1;
//        int seqStartPos = -1;
//        int seqEndPos = -1;
//        int motifLen = 1;
//        String graphScan = null;
//        String eValueS = null;
//        BufferedReader fReader = new BufferedReader(new FileReader(jobIDLoc + RES_FILE_EXT));
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        PRINTSPersistence tp = null;
//        Map<String, String> method2Graph = null;
//        Map<String, String> method2Eval = null;
//        int k = 0; // token counter in raw output line processing
//
//        try {
//
//            // evalCutoffs maps each PRINTS motif to its corresponding E-value cutoff
//            Map evalCutoffs = PrintsPP.readPrintsParsingFile(logOut, Consts.VERBOSE, Consts.READ_PRINTS_EVAL_CUTOFFS);
//
//            // Get sequence lengths of all sequences in the query file
//            Map<String, Integer> seqLengths = Utils.getSeqsLength(jobIDLoc);
//
//            // Instantiate PRINTS persistence class for storing the results
//            tp = new PRINTSPersistence(conn, Consts.INSERT_PRINTS_SQL(job.analysis));
//
//            if (job.analysis == null) {
//                Consts.reportError(ERROR_MAIL_SUBJECT,
//                        "Error for " + job.analysisName + " and : " + jobID +
//                        " : analysis type : " + job.analysis + " does not exist in Onion!",
//                        logOut);
//                throw Consts.EXCEPTION_ALREADY_HANDLED;
//            }
//            in = fReader.readLine();
//            /* Example raw output file from FingerPRINTScan.
//Sn; UPI0000F3BEEC
//Si; Fasta sequence
//1TBS
//1TBN NO SIGNIFICANT RESULTS ABOVE 1.000e-04
//1TBF
//2TBS
//2TBT FingerPrint     No.Motifs SumId    AveId    ProfScore  Ppvalue     Evalue      GraphScan
//
//2TBN GLHYDRLASE10    2  of  4  52       26       342        0.001       3.6e+03     .ii.
//
//2TBN ADRENRGCA1DR    2  of  7  42.81    21.41    292        0.0026      6e+03       i.i....
//
//2TBF
//3TBS
//3TBT MotifName       No.Mots   IdScore PfScore Pvalue    Sequence
//                 Len  low  pos  high
//3TBN GLHYDRLASE10    2  of  4  28.79   207     1.34e-02  INSWDLVDSTKH
//                 12   96   30   766
//3TBN GLHYDRLASE10    3  of  4  23.48   135     7.70e-02  GKITFNKKNMDK
//                 12   143  99   816
//3TBB
//3TBN ADRENRGCA1DR    1  of  7  26.56   150     6.28e-02  FVCLWSILLPGQEADA
//                 16   3    11   3
//3TBN ADRENRGCA1DR    3  of  7  16.25   142     4.10e-02  ISANNNINATTYSNGKITFN
//                 20   69   85   80
//3TBB
//3TBF
//            */
//
//            String dbReleaseNo = job.otherInfo;
//            int dotInd = dbReleaseNo.indexOf(".");
//            dbRelMajor = Integer.parseInt(dbReleaseNo.substring(0, dotInd));
//            dbRelMinor = Integer.parseInt(dbReleaseNo.substring(dotInd + 1));
//            while (in != null) {
//                in = in.trim();
//                // Get upi
//                if (in.startsWith("Sn")) {
//                    method2Graph = new Hashtable<String, String>();
//                    method2Eval = new Hashtable<String, String>();
//                    String[] line = in.split("\\s+");
//                    int len = line.length;
//                    k = 0;
//                    while (k < len) {
//                        if (k == 1) {
//                            upi = line[k];
//                        }
//                        k++;
//                    } // while (k < len) { - end
//                } else if (in.startsWith("1TBH")) {
//                    // Get method name and evalue
//                    String[] line = in.split("\\s+");
//                    int len = line.length;
//                    k = 0;
//                    motifName = null;
//                    eValueS = null;
//
//                    while (k < len) {
//                        if (k == 1) {
//                            motifName = line[k];
//                        } else if (k == 2) {
//                            eValueS = line[k];
//                        }
//                        k++;
//                    } //  while (k < len) { - end
//
//                    if (motifName != null && eValueS != null) {
//                        method2Eval.put(motifName, eValueS);
//                    } else {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "Error for " + job.analysisName + " and : " + jobID +
//                                "Not enough tokens in line: " + in,
//                                logOut);
//                        throw Consts.EXCEPTION_ALREADY_HANDLED;
//                    }
//                } else if ((in.startsWith("2TBN") || in.startsWith("2TBH")) && in.indexOf("of ") != -1) {
//                    // Get method name and Graphscan
//                    String[] line = in.split("\\s+");
//                    int len = line.length;
//                    k = 0;
//                    motifName = null;
//                    graphScan = null;
//                    eValueS = null;
//
//                    while (k < len) {
//                        if (k == 1) {
//                            motifName = line[k];
//                        } else if (k == 9) {
//                            eValueS = line[k];
//                        } else if (k == 10) {
//                            graphScan = line[k];
//                        }
//                        k++;
//                    } //  while (k < len) { - end
//
//                    if (motifName != null && graphScan != null) {
//                        method2Graph.put(motifName, graphScan);
//                        if (eValueS != null)
//                            method2Eval.put(motifName, eValueS);
//                    } else {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "Error for " + job.analysisName + " and : " + jobID +
//                                "Not enough tokens in line: " + in,
//                                logOut);
//                        throw Consts.EXCEPTION_ALREADY_HANDLED;
//                    }
//                } else if ((in.startsWith("3TBN") || in.startsWith("3TBH")) && in.indexOf("of ") != -1) {
//
//                  String[] line = in.split("\\s+");
//                    int len = line.length;
//                    k = 0;
//
//                    while (k < len) {
//                        switch (k) {
//                            case 1:
//                                motifName = line[k];
//                                break;
//                            case 2:
//                                motifNo = Integer.parseInt(line[k]);
//                                break;
//                            case 4:
//                                noMotifs = Integer.parseInt(line[k]);
//                                break;
//                            case 5:
//                                idScore = Float.parseFloat(line[k]);
//                                break;
//                            case 7:
//                                pVal = Float.parseFloat(line[k]);
//                                pVal = (float) Consts.log10((double) pVal);  // Convert pVal to log10
//                                break;
//                            case 9:
//                                motifLen = Integer.parseInt(line[k]);
//                                break;
//                            case 11:
//                                String seqStartPosS = line[k];
//                                // This hack is here because of The FingerPrintScan, for starting positions that are in
//                                // 5 figures, fails to separate pos and high columns with a space, then we just have to
//                                // pick out the  correct start position manually
//                                //  Len  low  pos  high
//                                //  19   81   101689976
//                                if (line[k].length() > 5) seqStartPosS = seqStartPosS.substring(0, 6);
//                                seqStartPos = Integer.parseInt(seqStartPosS);
//
//                                break;
//                            default:
//                        }
//                        k++;
//                    } //  while (k < len) { - end
//
//                    // Calculate the end position fot the motif
//                    seqEndPos = seqStartPos + motifLen - 1;
//
//                    /** Now the sanity check on the positions - PRINTS motifs can cross the
//                     * sequence boundary at both N- and C-terminals.
//                     * If seqEndPos > seq.length, then seqEndPos needs to be set to seq.length
//                     * If seqStartPos < 1, then it needs to be set to 1.
//                     */
//                    if (seqStartPos < 1)
//                        seqStartPos = 1;
//                    Integer upiLen = seqLengths.get(upi);
//                    if (upiLen != null) {
//                        if (seqEndPos > upiLen)
//                            seqEndPos = upiLen;
//                    } else {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "Unable to retrieve sequence length for: " + upi,
//                                logOut);
//                        throw Consts.EXCEPTION_ALREADY_HANDLED;
//                    }
//
//                    if (method2Graph.containsKey(motifName)) {
//                        graphScan = method2Graph.get(motifName);
//                    } else {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "Error for " + job.analysisName + " and : " + jobID +
//                                "No graphScan info present for: " + motifName,
//                                logOut);
//                        throw Consts.EXCEPTION_ALREADY_HANDLED;
//
//                    }
//
//                    // First check if the hit qualifies for being stored (if its evalue is better than the cutoff for
//                    // the motif in question)
//                    boolean hitQualifies = false;
//                    if (evalCutoffs.keySet().contains(motifName)) {
//                        Float motifCutoffF = (Float) evalCutoffs.get(motifName);
//                        float motifEval = Float.parseFloat((method2Eval.get(motifName)));
//                        logOut.println("upi: " + upi + "; motifName: " + motifName + "; motifCutoff" + motifCutoffF.floatValue() + "; motifEval: " + motifEval);
//                        logOut.flush();
//                        if (motifEval < motifCutoffF) {
//                            logOut.println("Qualified upi: " + upi + "; motifName: " + motifName + "; motifCutoff" + motifCutoffF.floatValue() + "; motifEval: " + motifEval);
//                            logOut.flush();
//                            hitQualifies = true;
//                        }
//                    } else {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "Error for " + job.analysisName + " and : " + jobID +
//                                " No eValue found for motifName: " + motifName,
//                                logOut);
//                        throw Consts.EXCEPTION_ALREADY_HANDLED;
//                    }
//
//                    if (hitQualifies) {
//                        /*
//                        logOut.println("Storing: '" + job.analysisTypeId + ": " + upi + ": " + motifName + ": " + motifNo + ": " + noMotifs + ": " +
//                                ": " + dbRelMajor + ": " + dbRelMinor + ": " + idScore + ": " + pVal + ": " +
//                                seqStartPos + ": " + seqEndPos + ": " + graphScan + ": " + eValueS + "'");
//                        logOut.flush(); */
//
//                        tp.store(job.analysis, upi, motifName, motifNo, noMotifs,
//                                dbRelMajor, dbRelMinor,
//                                idScore, pVal, seqStartPos, seqEndPos,
//                                graphScan, method2Eval.get(motifName));
//                    } // if (hitQualifies) {
//                } // ((in.startsWith("3TBN") || in.startsWith("3TBH")) && in.indexOf("of ") != -1) {
//                in = fReader.readLine();
//            } // while (in != null)
//
//
//        } catch (SQLException
//                sqle) {
//            exMsg = sqle.getMessage();
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//        } finally {
//            if (tp != null)
//                tp.close();
//            if (exMsg != null)
//                throw new SQLException(exMsg + " : " +
//                        "(" + this.getClass().getName() + ") " +
//                        lastSQL + "\n" +
//                        stackTrace.toString());
//        }
//    } // persistJobData() - end
//} // PrintsCalc - end
//
//
//package uk.ac.ebi.onion;
//
//import oracle.jdbc.driver.OracleResultSet;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.sql.ResultSet;
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//
//import uk.ac.ebi.uniparc.database.ProteinRow;
//
///**
// * Generic class for Onion analyses
// *
// * @author  Robert Petry
// * @author  Antony Quinn
// * @version $Id: SeqAnalysisCalc.java,v 1.28 2010/02/10 13:48:48 craigm Exp $
// * @since   1.0
// */
//public class SeqAnalysisCalc implements CalcJob {
//
//    //
//    public JobInfo job = null;
//    public String jobID;
//    protected String jobID1Loc;
//    protected String jobID2Loc;
//    public String jobIDLoc;
//    protected static final String ERROR_MAIL_SUBJECT =
//            Consts.ERROR_MAIL_SUBJECT_PREFIX + "SeqAnalysisCalc Error - ";
//    public static final String RES_FILE_EXT = "_out.txt";
//    protected static final String RES_FILE_EXT1 = ".out";
//
//    // submission via lsf-related constants
//    protected static final String SUBMIT2LSF_COMMAND = Consts.SRC_DIR + "submit2lsf";
//    protected static final String SUBMIT2LSF_COMMAND_JID_OPT = " -jid ";
//    protected static final String SUBMIT2LSF_COMMAND_JIDLOC_OPT = " -jidloc ";
//    protected static final String SUBMIT2LSF_COMMAND_CMDLINE_OPT = " -in1 ";
//    protected static final String SUBMIT2LSF_COMMAND_QUEUE_OPT = " -q ";
//    protected static final String SUBMIT2LSF_COMMAND_INPUT_OPT = " -in ";
//    protected static final String SUBMIT2LSF_COMMAND_INPUTB_OPT = " -inb ";
//    protected static final String SUBMIT2LSF_JOB_PRIORITY_OPT = " -jp "; // default: 50, max: 100
//    public static final String SUBMIT2LSF_TOPJOB_PRIORITY = "100";
//    public static final String SUBMIT2LSF_JOB_PRIORITY = "80";
//    protected static final String SUBMIT2LSF_COMMAND_BSUB_OPT = " -b ";
//
//    protected PrintWriter logOut;
//    protected String minUpi = "UPI0000000001";
//    protected String maxUpi = null;
//    protected String lastSQL = null;
//
//    public SeqAnalysisCalc(Analysis analysis, int seqVsSeq, String logFile) {
//
//
//        this.job = new JobInfo(analysis);
//        job.seqVsSeq = seqVsSeq;
//        if (seqVsSeq != Consts.SEQ_VS_SEQ)
//            // The high submission volumes for SWMC_NEW are likely to 'too many files open' unix error, hence the restriction above
//            logOut = Consts.openLogFile(logFile, ERROR_MAIL_SUBJECT);
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param job
//     */
//    public void updateJobInfo(JobInfo job) {
//        this.job = job;
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * This situation normally happens under the following circumstances:
//     * 1. subjUpiMin has exceeded max(upi) in uniparc_protein table,
//     * 2. for seqvsseq algorithms if all upis in qry chunk are smaller than subj seq's upi
//     * 3. If there exist gaps in UPI numbering (due to e.g. UniParc loading errors - UPIs
//     * are generated by Oracle sequence - if there are errors, the 'lost' UPIs cannot be reused.
//     *
//     * @return true if the job should be ignored.
//     */
//    public final boolean ignoreJob() {
//        return job.subjUpiMin == null && job.subjUpiMax == null;
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param jobPriority
//     * @param queue
//     * @param pass
//     * @param optJavaLoc
//     * @return
//     */
//    public String getCmd(String jobPriority, String queue, String pass, String optJavaLoc) {
//        return SUBMIT2LSF_COMMAND +
//                SUBMIT2LSF_COMMAND_JID_OPT + jobID +
//                SUBMIT2LSF_COMMAND_JIDLOC_OPT + jobIDLoc +
//                SUBMIT2LSF_COMMAND_QUEUE_OPT + queue +
//                SUBMIT2LSF_COMMAND_INPUT_OPT + jobID1Loc +
//                SUBMIT2LSF_COMMAND_INPUTB_OPT + (Consts.isSeqSeq(job.seqVsSeq) ? jobID2Loc : "null") +
//                SUBMIT2LSF_JOB_PRIORITY_OPT + jobPriority +
//                SUBMIT2LSF_COMMAND_BSUB_OPT +
//                "bsub" +
//                SUBMIT2LSF_COMMAND_CMDLINE_OPT + (optJavaLoc != null ? optJavaLoc : "") + job.cmdLine;
//    }
//
//    /**
//     * See CalcJob for documentation; the default: query fasta file follows the command; this method is overriden
//     * where this si not the case (e.g. PRODOM)
//     *
//     * @param jobIDLoc
//     * @param pass
//     * @return
//     */
//    public String processCmdLine(String jobIDLoc, String pass) {
//        String ret = job.cmdLine;
//        if (jobIDLoc != null)
//            ret += " " + jobIDLoc;
//        return ret;
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param jobPriority
//     * @param cnt
//     * @param mode
//     * @param pass
//     * @return
//     */
//    public boolean submit2LSF(String jobPriority, int cnt, Consts.TidyUpTopUpModes mode, String pass, boolean largeMemoryRequirement) {
//        try {
//
//            File jobID1LocF = new File(jobID1Loc);
//            if (jobID1LocF.length() == 0) {
//                jobID1LocF.delete();
//                return false;
//            }
//            if (Consts.isSeqSeq(job.seqVsSeq)) {
//                File jobID2LocF = new File(jobID2Loc);
//                if (jobID2LocF.length() == 0) {
//                    return false;
//                }
//            }
//
//            // SWMC-specific code to generate paralign-able database files if not already present
//            if (job.analysis == Analysis.SWMC_NEW) {
//                File db1F = new File(jobID2Loc + Paralign.PARALIGN_DB_POSTFIX + Paralign.PARALIGN_DB_EXT1);
//                File db2F = new File(jobID2Loc + Paralign.PARALIGN_DB_POSTFIX + Paralign.PARALIGN_DB_EXT2);
//                File db3F = new File(jobID2Loc + Paralign.PARALIGN_DB_POSTFIX + Paralign.PARALIGN_DB_EXT3);
//
//                if (!db1F.exists() || !db2F.exists() || !db3F.exists()) {
//                    // Only attempt to generate _db files for ParAlign if any of them
//                    // don't exist
//
//                    if (Consts.runExternalProcess(Paralign.PARALIGN_FORMATDB +
//                            " -n " + jobID2Loc + Paralign.PARALIGN_DB_POSTFIX +
//                            " -i " + jobID2Loc +
//                            " -p T",
//                            null,
//                            ERROR_MAIL_SUBJECT) == null) {
//                        System.err.println("Failed to run ParAlign's " + Paralign.PARALIGN_FORMATDB + " command for: " + jobID2Loc);
//                        System.err.flush();
//                        System.exit(1);
//                    }
//                } else {
//                    // If all files files, to indicate to the tidy-up crontab that they are still needed
//                    long cTime = System.currentTimeMillis();
//                    db1F.setLastModified(cTime);
//                    db2F.setLastModified(cTime);
//                    db3F.setLastModified(cTime);
//                }
//            } // if (job.analysisTypeId == Consts.SWMC) {
//
//            // Alternate queues
//            String queue = null;
//            String optJavaLoc = null;
//            if (mode == Consts.TidyUpTopUpModes.seqtopup || mode == Consts.TidyUpTopUpModes.seqtidyup) {
//                if (cnt % 2 == 0)
//                    queue = Consts.PRIMARY_LSF_QUEUE;
//                else
//                    queue = Consts.PRODUCTION_LSF_QUEUE;
//
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_anthill || mode == Consts.TidyUpTopUpModes.seqtidyup_anthill) {
//                queue = Consts.PRODUCTION_LSF_QUEUE;
//
//            } else if (mode == Consts.TidyUpTopUpModes.topup || mode == Consts.TidyUpTopUpModes.tidyup) {
//                optJavaLoc = Consts.javaExeOn32BitLinux;
//                if (cnt % 2 == 0)
//                    queue = Consts.PRODUCTION_LSF_QUEUE;
//                else
//                    queue = Consts.PRIMARY_LSF_QUEUE;
//
//            } else if (mode == Consts.TidyUpTopUpModes.seqtopup_research) {
//                queue = Consts.RESEARCH_FARM_LSF_QUEUE;
//
//            } else if (mode == Consts.TidyUpTopUpModes.topup_research) {
//                optJavaLoc = Consts.javaExeOn32BitLinux;
//                queue = Consts.RESEARCH_FARM_LSF_QUEUE;
//
//            } else if (mode == Consts.TidyUpTopUpModes.topup_anthill || mode == Consts.TidyUpTopUpModes.tidyup_anthill) {
//                optJavaLoc = Consts.javaExeOn64BitLinux;
//                queue = Consts.PRODUCTION_LSF_QUEUE;
//            }
//            String cmd = getCmd(jobPriority, queue, pass, optJavaLoc);
//            if (largeMemoryRequirement){
//                cmd = cmd + " BIG_MEM";
//            }
//            Consts.runExternalProcess(cmd, logOut, ERROR_MAIL_SUBJECT);
//        } catch (IOException ioe) {
//            Consts.reportError(ERROR_MAIL_SUBJECT,
//                    "Exception: " + ioe.getClass() + " : " + ioe.getMessage(), ioe,
//                    logOut);
//        } catch (InterruptedException ie) {
//            Consts.reportError(ERROR_MAIL_SUBJECT,
//                    "Exception: " + ie.getClass() + " : " + ie.getMessage(), ie,
//                    logOut);
//
//        }
//        return true;
//    } // submit2LSF() - end
//
//    private static final Map<Analysis, JobParamCache> ANALYSIS_TYPE_TO_JOB_PARAM_MAP = new HashMap<Analysis, JobParamCache>();
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn
//     * @throws SQLException
//     */
//    public synchronized final void getCmdLine(Connection conn)
//            throws SQLException {
//
//        if (! ANALYSIS_TYPE_TO_JOB_PARAM_MAP.containsKey(job.analysis)) {
//            JobParamCache jobParamCache = new JobParamCache();
//            String exMsg = null;
//            StringWriter stackTrace = null;
//            ResultSet res = null;
//            PreparedStatement getCallParams = null;
//
//            try {
//                lastSQL = Consts.GET_CALL_PARAMETERS;
//                getCallParams = conn.prepareStatement(Consts.GET_CALL_PARAMETERS);
//                getCallParams.setInt(1, job.analysis.getAnalysisTypeId());
//                res = getCallParams.executeQuery();
//                OracleResultSet result = (OracleResultSet) res;
//
//                if (result.next()) { // We're only expecting one result
//                    jobParamCache.setCommandLine(result.getString(1));
//                    // for simplicity, analysisName is also the name of the subdir of Consts.CALCS_DIR
//                    // where fasta-formatted files for currently running jobs are stores
//                    jobParamCache.setAnalysisName(result.getString(2).trim());
//                    String otherInfo = result.getString(3);
//                    if (otherInfo != null)
//                        jobParamCache.setOtherInfo(otherInfo.trim());
//                }
//                ANALYSIS_TYPE_TO_JOB_PARAM_MAP.put(job.analysis, jobParamCache);
//
//
//
//
//            } catch (SQLException sqle) {
//                stackTrace = new StringWriter();
//                sqle.printStackTrace(new PrintWriter(stackTrace));
//                exMsg = sqle.getMessage();
//            } finally {
//                if (res != null)
//                    res.close();
//                if (getCallParams != null)
//                    getCallParams.close();
//                if (exMsg != null)
//                    throw new SQLException(exMsg + " : " +
//                            "(" + this.getClass().getName() + ") " +
//                            lastSQL + "\n" +
//                            stackTrace.toString());
//            }
//        }
//        JobParamCache jobParamCache = ANALYSIS_TYPE_TO_JOB_PARAM_MAP.get(job.analysis);
//        job.cmdLine = jobParamCache.getCommandLine();
//        job.analysisName = jobParamCache.getAnalysisName();
//        job.otherInfo = jobParamCache.getOtherInfo();
//    } // getCmdLine() - end
//
//    /**
//     * See CalcJob for documentation
//     */
//    public void populateFileLocations(Consts.TidyUpTopUpModes mode, Connection conn) throws SQLException {
//
//        String baseDir = null;
//        if (job.analysis == Analysis.SWMC_NEW) {
//            baseDir = Consts.getBaseDir4TTMode(job.analysis, mode, false);
//        } else {
//            baseDir = Consts.getBaseDir4Analysis(job.analysis, false, conn);
//        }
//
//        // Populate relevant jobID names and file locations variables
//        jobID1Loc = job.subjUpiMin + "_" + job.subjUpiMax;//
//        jobID2Loc = job.qryUpiMin + "_" + job.qryUpiMax;
//        jobID = jobID1Loc; // lsf job id
//
//        jobID1Loc = baseDir + job.analysisName + "/" + jobID1Loc; // Input file 1 for seqvsseq
//
//        jobID += (Consts.isSeqSeq(job.seqVsSeq) ? "_" + jobID2Loc : "");
//        jobID2Loc = baseDir + job.analysisName + "/" + jobID2Loc; // Input file 2 for seqvsseq
//
//        jobIDLoc = baseDir + job.analysisName + "/" + jobID; // Input file for seqvsalg
//    } // populateFileLocations - end
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn
//     * @throws SQLException
//     */
//    public void populateBoundaries(Connection conn, PrintWriter logOut, Consts.TidyUpTopUpModes mode)
//            throws SQLException {
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        boolean seqseq = Consts.isSeqSeq(job.seqVsSeq);
//        ResultSet res = null;
//        OracleResultSet result;
//        PreparedStatement getMaxMinUPI = null;
//        PreparedStatement getHWM = null;
//        String minQryUpi = null;
//        int qryJobSize = -1;
//
//        if (seqseq)
//            qryJobSize = ((Integer) Consts.qryJobSizes.get(job.analysis)).intValue();
//        int subjJobSize = job.analysis.getSubjectJobSize();
//
//        try {
//            lastSQL = Consts.GET_MAX_UPI(job.analysis, seqseq);
//            getMaxMinUPI = conn.prepareStatement(lastSQL);
//
//            res = getMaxMinUPI.executeQuery();
//            result = (OracleResultSet) res;
//            if (result.next()) { // We're only expecting one result
//                maxUpi = result.getString(1);
//                minUpi = (job.analysis.isEnvironmental() ? Consts.MIN_ENV_UPI : Consts.MIN_UPI);
//            }
//            res.close();
//            getMaxMinUPI.close();
//
//            lastSQL = (seqseq ? Consts.RETRIEVE_HWM_ASM : Consts.RETRIEVE_HWM);
//            getHWM = conn.prepareStatement(lastSQL);
//            getHWM.setInt(1, job.analysis.getAnalysisTypeId());
//
//            res = getHWM.executeQuery();
//            result = (OracleResultSet) res;
//            if (result.next()) { // We're only expecting one result
//                job.subjUpiMin = result.getString(1);
//
//                if (seqseq)
//                    minQryUpi = result.getString(2);
//
//                // First check if the job is worth being submitted, i.e. are there any sequences actually available
//                if (Consts.upi2Dec(job.subjUpiMin) > Consts.upi2Dec(maxUpi)) {
//                    // All the subj seq upis have been processed
//
//                    job.subjUpiMin = null; // This will serve as a flag that job should not be submitted
//                    job.subjUpiMax = null; // ditto
//
//                } else { // There exists at least one subj sequence - submit the job
//                    job.subjUpiMax = Consts.dec2Upi(Consts.upi2Dec(job.subjUpiMin) + subjJobSize);
//
//                    if (seqseq) { // Continue refining job size until batchSize big enough
//                        job.subjUpiMax = Utils.refineUpperBoundary(job.subjUpiMin, job.subjUpiMax, subjJobSize, conn);
//                    }
//
//                    /**
//                     * if job.subjUpiMax > maxUpi, do a smaller-sized batch, only up to
//                     * maxUpi; otherwise any new upis between maxUpi and job.subjUpiMax
//                     * which arrive subsequently, will not have calculations done for them!
//                     */
//                    if (!seqseq) {
//                        if (Consts.upi2Dec(job.subjUpiMax) > Consts.upi2Dec(maxUpi))
//                            job.subjUpiMax = Consts.dec2Upi(Consts.upi2Dec(maxUpi) + 1);
//                    }
//
//                    if (seqseq) {
//                        job.qryUpiMin = minQryUpi;
//                        job.qryUpiMax = Consts.dec2Upi(Consts.upi2Dec(job.qryUpiMin) + qryJobSize);
//
//
//                        if (Consts.upi2Dec(job.qryUpiMin) > Consts.upi2Dec(maxUpi)) {
//                            // We need to re-set job.subjUpiMin/job.subjUpiMax too
//                            job.subjUpiMin = job.subjUpiMax; // Move on to another chunk for subj
//                            job.subjUpiMax = Consts.dec2Upi(Consts.upi2Dec(job.subjUpiMin) + subjJobSize);
//                            // Continue refining job size until batchSize big enough
//                            job.subjUpiMax = Utils.refineUpperBoundary(job.subjUpiMin, job.subjUpiMax, subjJobSize, conn);
//
//                            // For one-directional SeqVsSeq calculations, if there doesn't currently exist at least one query
//                            // sequence which is greater than the minimum subj sequence, traverse through qry chunks upwards until
//                            // it is the case.
//                            long qryUpiMinDec = Consts.upi2Dec(minUpi);
//                            if (Consts.upi2Dec(job.subjUpiMin) >= qryUpiMinDec + qryJobSize)
//                            // the maxQryUpi <= job.subjUpiMin i.e. the qry chunk contains _only_ upis which are smaller
//                            // than job.subjUpiMin
//                            {
//                                long subjUpiMinDec = Consts.upi2Dec(job.subjUpiMin);
//                                while (subjUpiMinDec >= qryUpiMinDec + qryJobSize)
//                                    qryUpiMinDec += qryJobSize;
//                                if (qryUpiMinDec < Consts.upi2Dec(maxUpi)) {
//                                    minQryUpi = Consts.dec2Upi(qryUpiMinDec);
//                                } else {
//                                    // Note that the following situation should never occur:
//                                    // job.subjUpiMin < maxUpi and qryUpiMinDec (in the loop above) >= maxUpi
//                                    Consts.reportError(ERROR_MAIL_SUBJECT,
//                                            " Was not able to find a query chunk in which at least one sequence was greater than " +
//                                                    job.subjUpiMin +
//                                                    "; note: maxUpi = " + maxUpi,
//                                            logOut);
//                                } // if (qryUpiMinDec < Consts.upi2Dec(maxUpi)) {
//                            } // Consts.upi2Dec(job.subjUpiMin) >= qryUpiMinDec + qryJobSize)
//
//                            job.qryUpiMin = Consts.dec2Upi(qryUpiMinDec);
//                            job.qryUpiMax = Consts.dec2Upi(Consts.upi2Dec(job.qryUpiMin) + qryJobSize);
//
//                        } // if (Consts.upi2Dec(job.qryUpiMin) > Consts.upi2Dec(maxUpi)) {
//
//                        if (Consts.upi2Dec(job.qryUpiMax) > Consts.upi2Dec(maxUpi)) {
//                            /**
//                             * if job.qryUpiMax > maxUpi, do a smaller-sized batch, only up to
//                             * maxUpi; otherwise any new upis between maxUpi and job.qryUpiMax
//                             * which arrive subsequently, will not have calculations done for them!
//                             */
//                            job.qryUpiMax = Consts.dec2Upi(Consts.upi2Dec(maxUpi) + 1);
//                        }
//
//                    } // if (seqseq)
//                    // Populate relevant jobID names and file locations variables
//
//                    populateFileLocations(mode, conn);
//                } // There exists at least one subject sequence - submit the job
//            } // if (result.next()) { // We're only expecting one result
//            result.close();
//            getHWM.close();
//
//        } catch (SQLException sqle) {
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//            exMsg = sqle.getMessage();
//        } finally {
//            if (res != null)
//                res.close();
//            if (getMaxMinUPI != null)
//                getMaxMinUPI.close();
//            if (getHWM != null)
//                getHWM.close();
//            if (exMsg != null)
//                throw new SQLException(exMsg + " : " +
//                        "(" + this.getClass().getName() + ") " +
//                        lastSQL + "\n" +
//                        stackTrace.toString());
//        }
//    } // populateBoundaries() - end
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn
//     * @throws SQLException
//     * @throws IOException
//     */
//    public void retrieveSequences(Connection conn)
//            throws SQLException, IOException {
//
//        String exMsg = null;
//        StringWriter stackTrace = null;
//        PreparedStatement updateHWM_ASM = null;
//
//        // For explanation see the definition of ignoreJob()
//        if (ignoreJob()) {
//            return;
//        }
//
//        try {
//            boolean seqseq = Consts.isSeqSeq(job.seqVsSeq);
//
//            File subjF = new File(jobID1Loc);
//            File qryF = new File(jobID2Loc);
//
//            // SUBJ SEQS
//            if (!seqseq || !subjF.exists()) {
//                // Note that if subjF already exists, we don't attempt to re-create it
//                // as this may interfere with another jobs which may be using it
//                // as the time
//
//                if (job.analysis.isEnvironmental()) {
//                    // This query is specific for environmental sample sequences which are not yet in UniParc
//                    Utils.outputENVSequences(job.analysis, conn, job.subjUpiMin, job.subjUpiMax, jobID1Loc, seqseq);
//                } else {
//                    ProteinQuery query = new ProteinQuery(conn,
//                            job.subjUpiMin,
//                            job.subjUpiMax, // exclusive of jobInfo.subjUpiMax
//                            job.seqVsSeq == Consts.SEQ_VS_SEQ, // restrict seqs to those with len >= 20 AA
//                            true, // Only new proteins for SWMC
//                            false, // Not output just UniProt sequences
//                            job.analysis == Analysis.PIRSFBLAST,
//                            false);
//                    ProteinWriter writer = new ProteinWriter(jobID1Loc);
//                    ProteinRow nextRow = query.next();
//                    while (nextRow != null) {
//                        writer.write(nextRow, seqseq);
//                        nextRow = query.next();
//                    }
//                    if (writer != null)
//                        writer.close();
//                    query.close();
//                }
//
//            } else if (subjF.exists()) {
//                // Touch the file to show to the tidying-up crontab that it is still needed
//                subjF.setLastModified(System.currentTimeMillis());
//            }
//
//            // QRY SEQS
//            if (seqseq && !jobID2Loc.equals(jobID1Loc) && !qryF.exists()) {
//                // Note that if qryF already exists, we don't attempt to re-create it
//                // as this may interfere with another jobs which may be using it
//                // at the samr time
//                // Retrieve sequences for query (if it is in fact the same set of sequences
//                // for subj and query, then don't bother retrieving it again)
//
//                if (job.analysis.isEnvironmental()) {
//                    // This query is specific for environmental sample sequences which are not yet in UniParc
//                    Utils.outputENVSequences(job.analysis, conn, job.qryUpiMin, job.qryUpiMax, jobID2Loc, seqseq);
//                } else {
//                    ProteinQuery query = new ProteinQuery(conn,
//                            job.qryUpiMin,
//                            job.qryUpiMax,
//                            job.seqVsSeq == Consts.SEQ_VS_SEQ,
//                            false,
//                            false,
//                            false,
//                            false);
//
//                    ProteinWriter writer = new ProteinWriter(jobID2Loc);
//
//                    ProteinRow nextRow = query.next();
//                    while (nextRow != null) {
//                        writer.write(nextRow, seqseq);
//                        nextRow = query.next();
//                    }
//                    writer.close();
//                    query.close();
//                }
//
//            } else if (seqseq && !jobID2Loc.equals(jobID1Loc)) { // i.e. qryF.exists()
//                // Touch the file to show to the tidying-up crontab that it is still needed
//                qryF.setLastModified(System.currentTimeMillis());
//            }
//
//            // Now that the sequences were retrieved successfully, we can store the new HWM/ASM values
//            if (seqseq) {
//                lastSQL = Consts.UPDATE_HWM_ASM;
//                updateHWM_ASM = conn.prepareStatement(Consts.UPDATE_HWM_ASM);
//                int pos = 1;
//                updateHWM_ASM.setString(pos++, job.subjUpiMin);   // hwm
//                updateHWM_ASM.setString(pos++, job.qryUpiMax);    // asm
//                updateHWM_ASM.setInt(pos, job.analysis.getAnalysisTypeId());
//                updateHWM_ASM.executeUpdate();
//            } else { // i.e. !seqseq
//                String storeHWM =
//                        (Consts.upi2Dec(job.subjUpiMin) > Consts.upi2Dec(maxUpi)) ?
//                                // If we've exceeded the maximum upi for subj seqs
//                                job.subjUpiMin : // Then we store the previous value
//                                job.subjUpiMax;  // Otherwise move hwm
//                lastSQL = Consts.UPDATE_HWM;
//                updateHWM_ASM = conn.prepareStatement(Consts.UPDATE_HWM);
//                updateHWM_ASM.setString(1, storeHWM);           // hwm
//                updateHWM_ASM.setInt(2, job.analysis.getAnalysisTypeId());
//                updateHWM_ASM.executeUpdate();
//
//            }
//        } catch (SQLException sqle) {
//            stackTrace = new StringWriter();
//            sqle.printStackTrace(new PrintWriter(stackTrace));
//            exMsg = sqle.getMessage();
//        } finally {
//            if (updateHWM_ASM != null)
//                updateHWM_ASM.close();
//            if (exMsg != null)
//                throw new SQLException(exMsg + " : " +
//                        "(" + this.getClass().getName() + ") " +
//                        lastSQL + "\n" +
//                        stackTrace.toString());
//        }
//    } // retrieveSequences() - end
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @throws IOException
//     */
//    public void populateJobStatus()
//            throws IOException {
//
//        BufferedReader fReader;
//        StringBuffer sb;
//        String lsfOutput;
//        String in;
//
//        job.status = Consts.JobStatus.STATUS_UNKNOWN;
//
//        try {
//            // Read in the LSF output file
//            fReader = new BufferedReader(new FileReader(jobIDLoc + Consts.LSF_OUT_EXT));
//            sb = new StringBuffer();
//            while ((in = fReader.readLine()) != null)
//                sb.append(in);
//            lsfOutput = sb.toString();
//
//            // Update its status in status array
//            if (lsfOutput.indexOf(Consts.LSF_OUTPUT_FILE_WRITTEN_OUT) != -1) {
//
//                if (lsfOutput.indexOf(Consts.LSF_JOB_COMPLETED_SUCCESSFULLY) != -1) { // Job has succeeded
//                    job.status = Consts.JobStatus.COMPLETED_SUCCESSFULLY;
//                } else { // Job has failed
//                    job.status = Consts.JobStatus.FAILED;
//                    if (lsfOutput.indexOf(Consts.LSF_JOB_NEED_MORE_MEMORY) != -1) {
//                        job.needsHighMemory = true;
//                    }
//                    // First check if the job has already been reported; if so, do nothing
//                    File alreadyReported = new File(jobIDLoc + Consts.ERROR_ALREADY_REPORTED_EXT);
//                    if (!alreadyReported.exists()) {
//                        Consts.reportError(ERROR_MAIL_SUBJECT,
//                                "The following " + job.analysisName + " job has failed: " + jobID + "\n" + lsfOutput,
//                                logOut);
//
//                        // Store the result file of the failed job with the Consts.ERROR_ALREADY_REPORTED_EXT extension
//                        // instead so that it can be inspected later
//                        File outF = new File(jobIDLoc + RES_FILE_EXT);
//                        if (outF.exists())
//                            outF.renameTo(alreadyReported);
//                        else
//                            alreadyReported.createNewFile();
//
//                    }
//                }
//            }
//        } catch (FileNotFoundException fnfe) {
//            // This could mean that the job is either running (analysis res file exists)
//            // or is still pending
//
//            if (new File(jobIDLoc + RES_FILE_EXT).exists()) {
//                job.status = Consts.JobStatus.RUNNING;
//            } else {
//                job.status = Consts.JobStatus.PENDING;
//            }
//        }
//    } // populateJobStatus() - end
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn - DB Connection
//     * @throws SQLException
//     * @throws FileNotFoundException
//     * @throws IOException
//     * @throws Exception
//     */
//    public void persistJobData(Connection conn)
//            throws SQLException, FileNotFoundException, IOException, Exception {
//        // Algorithm specific - is implemented in sublasses of this
//
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn
//     * @param mode
//     * @throws SQLException
//     */
//    public void updateJobs(Connection conn, Consts.TidyUpTopUpModes mode)
//            throws SQLException {
//        boolean seqVsSeq = Consts.isSeqSeq(job.seqVsSeq);
//        lastSQL = Consts.INSERT_JOBS(seqVsSeq, mode);
//
//        PreparedStatement insertNewJobRunning = conn.prepareStatement(lastSQL);
//        int pos = 1;
//        insertNewJobRunning.setString(pos++, job.subjUpiMin);
//        insertNewJobRunning.setString(pos++, job.subjUpiMax);
//        if (seqVsSeq) {
//            insertNewJobRunning.setString(pos++, job.qryUpiMin);
//            insertNewJobRunning.setString(pos++, job.qryUpiMax);
//        }
//        insertNewJobRunning.setInt(pos++, job.analysis.getAnalysisTypeId());
//
//        insertNewJobRunning.executeUpdate();
//        insertNewJobRunning.close();
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param conn
//     * @param mode
//     * @throws SQLException
//     */
//    public void deleteFromJobsRunning(Connection conn, Consts.TidyUpTopUpModes mode)
//            throws SQLException {
//        boolean seqVsSeq = Consts.isSeqSeq(job.seqVsSeq);
//
//        PreparedStatement deleteJobRunning = null;
//        try {
//            lastSQL = Consts.DELETE_RUNNING_JOB(seqVsSeq, mode);
//
//            deleteJobRunning = conn.prepareStatement(lastSQL);
//            int pos = 1;
//            deleteJobRunning.setInt(pos++, job.analysis.getAnalysisTypeId());
//            deleteJobRunning.setString(pos++, job.subjUpiMin);
//            deleteJobRunning.setString(pos++, job.subjUpiMax);
//            if (seqVsSeq) {
//                deleteJobRunning.setString(pos++, job.qryUpiMin);
//                deleteJobRunning.setString(pos++, job.qryUpiMax);
//            }
//
//            deleteJobRunning.executeUpdate();
//            conn.commit();
//        } catch (SQLException e) {
//            logOut.println("Failed to delete " + job.subjUpiMin + "_" + job.subjUpiMax + "_" + job.qryUpiMin + "_" + job.qryUpiMax);
//            logOut.flush();
//            throw e;
//        } finally {
//            if (deleteJobRunning != null)
//                deleteJobRunning.close();
//        }
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @param all
//     */
//    public void tidyUpJobFiles(boolean all) {
//
//        // First delete LSF's .out and .err files
//        // Since !all option is only used if the job failed, for now preserve the .err
//        // files until the jobs succeeds
//        File delF = new File(jobIDLoc + Consts.LSF_OUT_EXT);
//        if (delF.exists())
//            delF.delete();
//        delF = new File(jobIDLoc + Consts.LSF_ERR_EXT);
//        if (delF.exists())
//            delF.delete();
//
//        if (all) {
//
//            // Now delete the result *_out.txt file and the fasta-formatted input file
//            delF = new File(jobIDLoc + RES_FILE_EXT);
//            if (delF.exists())
//                delF.delete();
//
//
//            if (!Consts.isSeqSeq(job.seqVsSeq)) {
//                delF = new File(jobIDLoc);
//                if (delF.exists())
//                    delF.delete();
//            }
//
//            delF = new File(jobIDLoc + Consts.ERROR_ALREADY_REPORTED_EXT);
//            if (delF.exists())
//                delF.delete();
//        }
//    }
//
//    /**
//     * See CalcJob for documentation; A job is worth re-submitting by default
//     *
//     * @param logOut
//     * @return
//     */
//    public boolean jobWorthReSubmitting(PrintWriter logOut) {
//        return true;
//    }
//
//    /**
//     * See CalcJob for documentation
//     *
//     * @return
//     */
//    public long subjectFileSize() {
//        return (new File(jobID1Loc)).length();
//    }
//
//}
//
//
//
//
//
// *
// *
// *
// *
// */
