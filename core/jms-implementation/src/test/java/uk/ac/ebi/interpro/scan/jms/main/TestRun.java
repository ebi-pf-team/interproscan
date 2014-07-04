package uk.ac.ebi.interpro.scan.jms.main;

import org.junit.Test;
import uk.ac.ebi.interpro.scan.management.model.Job;

import java.util.*;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertNull;

/**
 * Will test some of the static main run class methods.
 *
 * @author Maxim Scheremetjew
 */
public class TestRun {

    @Test
    public void checkAnalysisJobsVersionsTest() {
        //TODO: Implement
        assertTrue(true);
    }

    @Test
    public void getDefaultAnalysesToRunTest() {
        //Case 1: ./interproscan.sh -i test_proteins.fasta -appl pirsf -dp
        //Define test method input
        Map<String, Set<Job>> parsedAnalysesRealAnalysesMap = new HashMap<String, Set<Job>>();
        Set<Job> jobs = loadDummyJobs(false);
        parsedAnalysesRealAnalysesMap.put("pirsf", jobs);
        //Test the method
        String[] actualArray = Run.getVisibleAnalysesToRun(parsedAnalysesRealAnalysesMap);
        assertTrue(actualArray.length == 1);
        assertEquals("jobPIRSF-2.84", actualArray[0]);

        //Case 2: ./interproscan.sh -i test_proteins.fasta -appl pirsf-2.84, pirsf-2.85 -dp
    }

    @Test
    public void getRealAnalysesNamesTest() {
        //Define test method input
        String[] parsedAnalyses = new String[]{"pirsf", "pfam-27.0"};
        final List<Job> realJobs = new ArrayList<Job>(loadDummyJobs(true));
        //Test the method
        Map<String, Set<Job>> actualMap = Run.getRealAnalysesNames(parsedAnalyses, realJobs);
        assertTrue("We should get jobs for 2 analyses.", actualMap.size() == 2);
        assertEquals("We should get 1 job for PIRSF (default job only).", 1, actualMap.get("pirsf").size());
        assertEquals("We should get 1 job for Pfam.", 1, actualMap.get("pfam-27.0").size());

        //Define test method input
        parsedAnalyses = new String[]{"pirsf-2.84", "pirsf-2.85", "pfam-27.0"};
        //Test the method
        actualMap = Run.getRealAnalysesNames(parsedAnalyses, realJobs);
        assertEquals("We should get 3 jobs for 3 keys.", 3, actualMap.size());
        assertNull("We should get 0 jobs for key pirsf.", actualMap.get("pirsf"));
        assertEquals("We should get 1 job for Pfam.", 1, actualMap.get("pfam-27.0").size());
        assertEquals("We should get 1 job for pirsf-2.84.", 1, actualMap.get("pirsf-2.84").size());
        assertNotNull("We should get 1 job for pirsf-2.85.", actualMap.get("pirsf-2.85"));
    }

    private Set<Job> loadDummyJobs(boolean includePfam) {
        Set<Job> dummyJobs = new HashSet<Job>();
        //PIRSF version 2.84 job
        Job pirsf284Job = new Job();
        pirsf284Job.setBeanName("jobPIRSF-2.84");
        pirsf284Job.setAnalysis(true);
        dummyJobs.add(pirsf284Job);
        //PIRSF version 2.85 job
        Job pirsf285Job = new Job();
        pirsf285Job.setBeanName("jobPIRSF-2.85");
        pirsf285Job.setAnalysis(false);
        pirsf285Job.setVisible(false);
        dummyJobs.add(pirsf285Job);
        //Pfam version 27.0 job
        if (includePfam) {
            Job pfamJob = new Job();
            pfamJob.setBeanName("jobPFAM-27.0");
            pfamJob.setAnalysis(false);
            dummyJobs.add(pfamJob);
        }
        return dummyJobs;
    }
}