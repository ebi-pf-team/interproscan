package uk.ac.ebi.interpro.scan.io.smart;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 */

public class SmartThresholdFileParser implements Serializable {
//current file and location: /nfs/seqdb/production/interpro/data/members/smart/6.1/THRESHOLDS
///* Example entry:
//      0            1              2            3            4          5             6          7         8          9           10
//    Acc       Domain         Family      DB size           mu      lamda        cutoff    cut_low    family    repeats   repeat_cut
//SM00181          EGF       EGF_like       260388   -35.903236   0.201675      2.70e+01   2.80e+01  2.70e+01         -         75.00
//SM00185          ARM            ARM       263816   -21.947109   0.297354      1.00e+00   1.40e+00         -         3        100.00

    private static final int INDEX_MODEL_ACCESSION = 0;
    private static final int INDEX_DOMAIN_NAME = 1;
    private static final int INDEX_FAMILY_NAME = 2;
    private static final int INDEX_DBSIZE = 3;
    private static final int INDEX_MU = 4;
    private static final int INDEX_LAMBDA = 5;
    private static final int INDEX_CUTOFF = 6;
    private static final int INDEX_CUT_LOW = 7;
    private static final int INDEX_FAMILY_EVAL = 8;
    private static final int INDEX_REPEATS = 9;
    private static final int INDEX_REPEAT_CUT = 10;

    private static final String NO_VALUE_MARKER = "-";

    private static final double DBL_EPSILON = 2.220446049250313E-16;
    private static final int DBL_MAX_10_EXP = 308;


    /**
     * Returns a Map of model accession to a Threshold record.
     *
     * @param thresholdFileResource to be parsed
     * @return a Map of model accession to a Threshold record.
     * @throws IOException
     */
    public SmartThresholds parse(final Resource thresholdFileResource) throws IOException {
        final SmartThresholds holder = new SmartThresholds();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(thresholdFileResource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SM")) {
                    String[] lineInput = line.split("\\s+");
                    holder.addThreshold(new SmartThreshold(lineInput));
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return holder;
    }

    public class SmartThreshold {

        private String modelId;

        private String domainName;

        private String familyName;

        private int dbSize;

        private double mu;

        private double lambda;

        private Double cutoff;

        private Double cut_low;

        private Double family = null;

        private Integer repeats = null;

        private Double repeat_cut = null;

        public SmartThreshold(String[] input) {
            this.modelId = input[INDEX_MODEL_ACCESSION];
            this.domainName = input[INDEX_DOMAIN_NAME];
            this.familyName = input[INDEX_FAMILY_NAME];
            this.dbSize = Integer.parseInt(input[INDEX_DBSIZE]);
            this.mu = Double.parseDouble(input[INDEX_MU]);
            this.lambda = Double.parseDouble(input[INDEX_LAMBDA]);
            this.cutoff = Double.parseDouble(input[INDEX_CUTOFF]);
            this.cut_low = Double.parseDouble(input[INDEX_CUT_LOW]);
            this.family = doubleOrNull(input[INDEX_FAMILY_EVAL]);
            if (!input[INDEX_REPEATS].trim().equals(NO_VALUE_MARKER)) {
                this.repeats = Integer.parseInt(input[INDEX_REPEATS]);
            }
            this.repeat_cut = doubleOrNull(input[INDEX_REPEAT_CUT]);
        }

        private Double doubleOrNull(String val) {
            if (val == null || NO_VALUE_MARKER.equals(val)) {
                return null;
            }
            return Double.parseDouble(val);
        }

        public String getModelId() {
            return modelId;
        }

        public String getDomainName() {
            return domainName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public Double getCutoff() {
            return cutoff;
        }

        public Double getCut_low() {
            return cut_low;
        }

        public Double getFamily() {
            return family;
        }

        public Integer getRepeats() {
            return repeats;
        }

        public Double getRepeat_cut() {
            return repeat_cut;
        }

        public double getDerivedEvalue(double score) {
            return (double) dbSize * getPValue(score);
        }

        private double getPValue(double score) {
            double pVal;

            // the bound from Bayes
            if (score >= sreLOG2(Double.MAX_VALUE)) {
                pVal = 0.0;
            } else if (score <= -1.0 * sreLOG2(Double.MAX_VALUE)) {
                pVal = 1.0;
            } else {
                pVal = 1.0 / (1.0 + sreEXP2(score));
            }

            // try for a better estimate from EVD fit
            return Math.min(pVal, extremeValueP(score));
        }

        private double sreLOG2(double x) {
            return ((x) > 0 ? Math.log(Double.MAX_VALUE) * 1.44269504 : -9999.0);
        }

        private double sreEXP2(double x) {
            return (Math.exp((x) * 0.69314718));
        }

        private double extremeValueP(double score) {
            // avoid exceptions near P=1.0
            // typical 32-bit sys: if () < -3.6, return 1.0
            if ((lambda * (score - mu)) <= -1.0 * Math.log(-1.0 * Math.log(DBL_EPSILON))) {
                return 1.0;
            }

            // avoid underflow fp exceptions near P=0.0*/
            if ((lambda * (score - mu)) >= 2.3 * (double) DBL_MAX_10_EXP) {
                return 0.0;
            }

            // a roundoff issue arises; use 1 - e^-x --> x for small x */
            final double ret = Math.exp(-1.0 * lambda * (score - mu));

            if (ret < 1e-7) {
                return ret;
            } else {
                return (1.0 - Math.exp(-1.0 * ret));
            }
        }
    }
}
