package uk.ac.ebi.interpro.scan.benchmarking;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import uk.ac.ebi.interpro.scan.jms.main.Mode;
import uk.ac.ebi.interpro.scan.jms.main.Run;

public class RunBenchmark {

    /**
     *  Use a state object to provide inputs to the calculations to avoid the
     *  "constant folding" JVM optimisation being applied to the microbenchmarking tests.
     *  http://tutorials.jenkov.com/java-performance/jmh.html#why-are-java-microbenchmarks-hard
     */
    @State(Scope.Thread)
    public static class BenchmarkState {
        public String mode = "convert";
    }

    @Benchmark
    public Mode testMethod(BenchmarkState state) {
        Mode mode = Run.getMode(state.mode);
        return mode; // Avoid JVM "dead code elimination" optimisation by returning the result!
    }

}