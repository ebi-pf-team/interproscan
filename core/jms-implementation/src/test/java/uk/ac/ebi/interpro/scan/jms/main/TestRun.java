package uk.ac.ebi.interpro.scan.jms.main;


import uk.ac.ebi.interpro.scan.management.model.Job;

import java.util.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Will test some of the static main run class methods.
 *
 * @author Maxim Scheremetjew
 * @author Gift Nuka
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
        String[] actualArray = Run.getActiveAnalysesToRun(parsedAnalysesRealAnalysesMap);
        assertTrue(actualArray.length == 1);
        assertEquals("jobPIRSF-2.84", actualArray[0]);

        //Case 2: ./interproscan.sh -i test_proteins.fasta -appl pirsf-2.84, pirsf-3.01 -dp
    }

    @Test
    public void getRealAnalysesNamesTest() {
        //Define test method input
        String[] parsedAnalyses = new String[]{"pirsf", "pfam-27.0"};
        final List<Job> realJobs = new ArrayList<Job>(loadDummyJobs(true));
        //Test the method
        Map<String, Set<Job>> actualMap = Run.getRealAnalysesNames(parsedAnalyses, realJobs);
        assertTrue(actualMap.size() == 2,"We should get jobs for 2 analyses.");
        assertEquals(1, actualMap.get("pirsf").size(),"We should get 1 job for PIRSF (default job only).");
        assertEquals(1, actualMap.get("pfam-27.0").size(), "We should get 1 job for Pfam.");

        //Define test method input
        parsedAnalyses = new String[]{"pirsf-2.84", "pfam-27.0"};
        //Test the method
        actualMap = Run.getRealAnalysesNames(parsedAnalyses, realJobs);
        assertEquals(2, actualMap.size(), "We should get 2 jobs for 2 keys.");
        assertNull(actualMap.get("pirsf"), "We should get 0 jobs for key pirsf.");
        assertEquals(1, actualMap.get("pfam-27.0").size(), "We should get 1 job for Pfam.");
        assertEquals(1, actualMap.get("pirsf-2.84").size(), "We should get 1 job for pirsf-2.84.");
    }

    private Set<Job> loadDummyJobs(boolean includePfam) {
        Set<Job> dummyJobs = new HashSet<Job>();
        //PIRSF version 2.84 job
        Job pirsf284Job = new Job();
        pirsf284Job.setBeanName("jobPIRSF-2.84");
        pirsf284Job.setAnalysis(true);
        dummyJobs.add(pirsf284Job);
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