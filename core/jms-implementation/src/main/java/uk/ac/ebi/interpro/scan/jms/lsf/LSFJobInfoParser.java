package uk.ac.ebi.interpro.scan.jms.lsf;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 14/08/12
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
public class LSFJobInfoParser {

    private final String HEADER_LINE = "JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME";
    private final int JOB_INFORMATION_LINE_LENGTH = 10;
    private final int JOB_INFORMATION_LINE_LENGTH_RELEVANT = 3;
    private final int JOBID_POS = 0;
    private final int USER_POS = 1;
    private final int STATUS_POS = 2;
    private final int QUEUE_POS = 3;
    private final int FROM_HOST_POS = 4;
    private final int EXEC_HOST_POS = 5;
    private final int JOB_NAME_POS = 6;
    private final int SUBMIT_TIME_MONTH_POS = 7;
    private final int SUBMIT_TIME_DAY_POS = 8;
    private final int SUBMIT_TIME_TIME_POS = 9;

    private static final Logger LOGGER = LogManager.getLogger(LSFJobInfoParser.class.getName());

    private static final LSFJobInfoParser instance = new LSFJobInfoParser();

    private LSFJobInfoParser() {
    }

    public static LSFJobInfoParser getInstance() {
        return instance;
    }


    public Map<String, Set<LSFJobInformation>> parse(InputStream is) {
        Map<String, Set<LSFJobInformation>> jobInformations = new HashMap<String, Set<LSFJobInformation>>();
        BufferedInputStream buffer = new BufferedInputStream(is);
        BufferedReader textout = new BufferedReader(new InputStreamReader(buffer));
        String line;
        try {
            while ((line = textout.readLine()) != null) {
                if (line.startsWith("JOBID")) {
                    if (!line.equals(HEADER_LINE)) {
                        throw new ParseException("The header line format is not as expected. Expected format:" + HEADER_LINE, line);
                    }
                } else {
                    String[] splitLine = line.split("\\s+");
                    if (!(splitLine.length >= JOB_INFORMATION_LINE_LENGTH_RELEVANT)) {
                        throw new ParseException("The job information line does not have the expected number of fields", line);
                    }
                    String jobId = splitLine[JOBID_POS];
                    String user = splitLine[USER_POS];
                    String status = splitLine[STATUS_POS];

                    // the following fields are not used in this module
                    String queueName = ""; //splitLine[QUEUE_POS];
                    String fromHost = ""; //splitLine[FROM_HOST_POS];
                    String execHost = ""; // splitLine[EXEC_HOST_POS];
                    String jobName = ""; //splitLine[JOB_NAME_POS];
                    Date date = new Date();  //getDate(splitLine);

                    LSFJobInformation jobInformation = new LSFJobInformation(jobId, user, status, queueName,
                            fromHost, execHost, jobName, date);
                    if (jobInformations.containsKey(jobName)) {
                       Set jobInformationSet = jobInformations.get(jobName);
                        jobInformationSet.add(jobInformation);

                    }  else {
                        Set jobInformationSet = new HashSet();
                        jobInformationSet.add(jobInformation);
                        jobInformations.put(jobName, jobInformationSet);
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }  finally {
            try {
                textout.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return jobInformations;


    }

    private Date getDate(String[] splitLine) {

        String delimiter = " ";
        StringBuilder sb = new StringBuilder();
        sb.append(splitLine[SUBMIT_TIME_MONTH_POS]).append(delimiter)
                .append(splitLine[SUBMIT_TIME_DAY_POS]).append(delimiter)
                .append(splitLine[SUBMIT_TIME_TIME_POS]);
        SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm");
        try {
            return df.parse(sb.toString());

        } catch (java.text.ParseException e) {
            throw new ParseException("Could not convert the SUBMIT_TIME field into a date. Expected format = MMM dd EEE HH:mm.", sb.toString());

        }
    }
}