package uk.ac.ebi.interpro.scan.jms.lsf;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 13/08/12
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public class LSFJobInformation {

    private String jobId;
    private String user;
    private LSFJobStatus status;
    private String queueName;
    private String fromHost;
    private String execHost;
    private String jobName;
    private Date submitTime;

    public LSFJobInformation(String jobId, String user, String status,
                             String queueName, String fromHost, String execHost,
                             String jobName, Date submitTime) {
        this.jobId = jobId;
        this.user = user;
        this.status = LSFJobStatus.getJobStatus(status);
        this.queueName = queueName;
        this.fromHost = fromHost;
        this.execHost = execHost;
        this.jobName = jobName;
        this.submitTime = submitTime;
    }

    public String getJobId() {
        return jobId;
    }

    public String getUser() {
        return user;
    }

    public LSFJobStatus getStatus() {
        return status;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getFromHost() {
        return fromHost;
    }

    public String getExecHost() {
        return execHost;
    }

    public String getJobName() {
        return jobName;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    @Override
    public String toString() {
        return "LSFJobInformation{" +
                "jobId='" + jobId + '\'' +
                ", user='" + user + '\'' +
                ", status='" + status + '\'' +
                ", queueName='" + queueName + '\'' +
                ", fromHost='" + fromHost + '\'' +
                ", execHost='" + execHost + '\'' +
                ", jobName='" + jobName + '\'' +
                ", submitTime=" + submitTime +
                '}';
    }
}
