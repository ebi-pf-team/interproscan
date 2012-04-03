package uk.ac.ebi.interpro.scan.management.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Represents Unit test for {@link Jobs}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class JobStatusCheckerTest {

    @Test
    public void testGetJobStatus() {
        Job job1 = new Job();

        Map<String, String> mandatoryParams = new HashMap<String, String>();
        mandatoryParams.put("param1", "value1");
        Job job2 = new Job();
        job2.setBeanName("job2");
        job2.setMandatoryParameters(mandatoryParams);

        mandatoryParams = new HashMap<String, String>();
        mandatoryParams.put("param1", "");
        Job job3 = new Job();
        job3.setBeanName("job3");
        job3.setMandatoryParameters(mandatoryParams);

        Jobs jobs = new Jobs();
        assertEquals("Job status of job 1 should be ACTIVE!", JobStatusChecker.getJobStatus(job1).getJobStatus(), JobStatusWrapper.JobStatus.ACTIVE);
        assertEquals("Job status of job 2 should be ACTIVE!", JobStatusChecker.getJobStatus(job2).getJobStatus(), JobStatusWrapper.JobStatus.ACTIVE);
        assertEquals("Job status of job 3 should be DEACTIVATED!", JobStatusChecker.getJobStatus(job3).getJobStatus(), JobStatusWrapper.JobStatus.DEACTIVATED);

        List<Job> jobsList = new ArrayList<Job>();
        jobsList.add(job1);
        jobsList.add(job2);
        jobsList.add(job3);
        jobs = new Jobs(jobsList, true);
        assertNotNull(jobs.getJobList());
        assertEquals(2, jobs.getJobList().size());
        assertTrue("Job1 should exist in the job list!", jobs.getJobList().contains(job1));
        assertTrue("Job2 should exist in the job list!", jobs.getJobList().contains(job2));
    }
}
