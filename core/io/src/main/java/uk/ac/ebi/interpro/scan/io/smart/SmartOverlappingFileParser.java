package uk.ac.ebi.interpro.scan.io.smart;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartOverlappingFileParser implements Serializable {
//current file and location: /nfs/seqdb/production/interpro/data/members/smart/6.1/overlapping
// Example content:
//     name     |    domain    |  typ  | number
//     --------------+--------------+-------+--------
//     DEXDc        | DEXDc2       | merge |      2
//     .              ^^^^^^         ^^^^^        ^
//     DEXDc        | DEXDc        | merge |      1
//     DEXDc        | DEXDc3       | merge |      3

    private static final int INDEX_FAMILY_NAME = 0;
    private static final int INDEX_DOMAIN_NAME = 1;
    private static final int INDEX_RESOLUTION_TYPE = 2;
    private static final int INDEX_PRIORITY = 3;

    private static final Pattern VALID_LINE_PATTERN = Pattern.compile("^[^|]+\\|[^|]+\\|[^|]+\\|\\s+\\d+\\s*$");


    public SmartOverlaps parse(Resource overlappingFileResource, SmartThresholds smartThresholds) throws IOException {
        final SmartOverlaps smartOverlaps = new SmartOverlaps();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(overlappingFileResource.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                final Matcher matcher = VALID_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String[] lineInput = line.split("\\s+\\|\\s+");
                    smartOverlaps.addSmartOverlap(smartThresholds, new SmartOverlap(lineInput));
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return smartOverlaps;
    }


    public class SmartOverlap {

        private String familyName;

        private String domainName;

        private String resolutionType;

        private int priority;

        public SmartOverlap(String[] input) {
            this.familyName = input[INDEX_FAMILY_NAME].trim();
            this.domainName = input[INDEX_DOMAIN_NAME].trim();
            this.resolutionType = input[INDEX_RESOLUTION_TYPE].trim();
            this.priority = Integer.parseInt(input[INDEX_PRIORITY].trim());
        }

        public String getFamilyName() {
            return familyName;
        }

        public String getDomainName() {
            return domainName;
        }

        public String getResolutionType() {
            return resolutionType;
        }

        public int getPriority() {
            return priority;
        }
    }
}
