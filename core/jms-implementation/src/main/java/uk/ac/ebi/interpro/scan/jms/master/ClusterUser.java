package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Created with IntelliJ IDEA.
 * User: craigm
 * Date: 26/04/13
 * Time: 11:03
 * To change this template use File | Settings | File Templates.
 */
public interface ClusterUser {

    public void setTcpUri(String tcpUri);

    public String getTcpUri();

    public void setProjectId(String projectId);

    public String getProjectId();

    public void setSubmissionWorkerRunnerProjectId(String projectId);

    public void setSubmissionWorkerRunnerUserDir(String userDir);

}
