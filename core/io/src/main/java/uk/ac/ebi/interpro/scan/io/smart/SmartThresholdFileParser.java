package uk.ac.ebi.interpro.scan.io.smart;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern SMART_THRESHOLD_PATTERN = Pattern.compile("^SM[0-9]{5}.+$");

    public Map<String, SmartThreshold> parse(Resource thresholdFileResource) throws IOException {
        String errorMessage = checkForResourceProblems(thresholdFileResource);
        if (errorMessage != null) {
            throw new IllegalStateException(errorMessage);
        }

        final Map<String, SmartThreshold> accessionThresholds = new HashMap<String, SmartThreshold>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(thresholdFileResource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher thresholdData = SMART_THRESHOLD_PATTERN.matcher(line);
                if (thresholdData.find()) {
                    String[] lineInput = line.split("\\s+");
                    accessionThresholds.put(lineInput[INDEX_MODEL_ACCESSION], new SmartThreshold(lineInput));
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return accessionThresholds;
    }

    /**
     * Ensure the provided file resource is OK (exists and can be read).
     * @param thresholdFileResource The resource to check
     * @return An error string if there was a problem, or NULL if all was OK
     */
    public String checkForResourceProblems(Resource thresholdFileResource) {
        if (thresholdFileResource == null) {
            return "Smart threshold file resource is null";
        }
        if (!thresholdFileResource.exists()) {
            return "Smart threshold file resource " + thresholdFileResource.getFilename() + " does not exist";
        }
        if (!thresholdFileResource.isReadable()) {
            return "Smart threshold file resource " + thresholdFileResource.getFilename() + " is not readable";
        }
        return null; // All is OK!
    }

    public class SmartThreshold {

        private String id;

        private String domainName;

        private String familyName;

        private int dbSize;

        private String muValue;

        private String lambdaValue;

        private Double cutoff;

        private Double cut_low;

        private Double family = null;

        private String repeats = null;

        private Double repeat_cut = null;

        public SmartThreshold(String[] input) {
            this.id = input[INDEX_MODEL_ACCESSION].trim();
            this.domainName = input[INDEX_DOMAIN_NAME].trim();
            this.familyName = input[INDEX_FAMILY_NAME].trim();
            this.dbSize = Integer.parseInt(input[INDEX_DBSIZE].trim());
            this.muValue = input[INDEX_MU].trim();
            this.lambdaValue = input[INDEX_LAMBDA].trim();
            this.cutoff = Double.parseDouble(input[INDEX_CUTOFF].trim());
            this.cut_low = Double.parseDouble(input[INDEX_CUT_LOW].trim());
            if (!input[INDEX_FAMILY_EVAL].trim().equals(NO_VALUE_MARKER)) {
                this.family = Double.parseDouble(input[INDEX_FAMILY_EVAL].trim());
            }
            if (!input[INDEX_REPEATS].trim().equals(NO_VALUE_MARKER)) {
                this.repeats = input[INDEX_REPEATS].trim();
            }
            if (!input[INDEX_REPEAT_CUT].trim().equals(NO_VALUE_MARKER)) {
                this.repeat_cut = Double.parseDouble(input[INDEX_REPEAT_CUT].trim());
            }
        }

        public String getId() {
            return id;
        }

        public String getDomainName() {
            return domainName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public int getDbSize() {
            return dbSize;
        }

        public String getMuValue() {
            return muValue;
        }

        public String getLambdaValue() {
            return lambdaValue;
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

        public String getRepeats() {
            return repeats;
        }

        public Double getRepeat_cut() {
            return repeat_cut;
        }
    }
}
