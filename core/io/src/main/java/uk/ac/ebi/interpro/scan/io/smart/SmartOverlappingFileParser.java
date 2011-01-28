package uk.ac.ebi.interpro.scan.io.smart;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    private static final String HEADER_LINE = "-----";

    public Map<String, SmartOverlap> parse(Resource overlappingFileResource) throws IOException {
        if (overlappingFileResource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!overlappingFileResource.exists()) {
            throw new IllegalStateException(overlappingFileResource.getFilename() + " does not exist");
        }
        if (!overlappingFileResource.isReadable()) {
            throw new IllegalStateException(overlappingFileResource.getFilename() + " is not readable");
        }
        final Map<String, SmartOverlap> accessionOverlaps = new HashMap<String, SmartOverlap>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(overlappingFileResource.getInputStream()));
            String line;
            boolean dataReady = false;
            while ((line = reader.readLine()) != null) {
                if (dataReady) {
                    String[] lineInput = line.split("\\s+\\|\\s+");
//                    for (int i = 0; i < lineInput.length; i++) {
//                        System.out.println("lineInput[" + i + "] = " + lineInput[i]);
//                    }
                    accessionOverlaps.put(lineInput[INDEX_DOMAIN_NAME], new SmartOverlap(lineInput));
                }
                else if (line.startsWith(HEADER_LINE)) {
                    dataReady = true;
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return accessionOverlaps;
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

//    public static void readOverlaps(Map<String, String> methods2OvlResolutionTypes,
//                                    Map<String, Integer> methods2OvlResolutionPriorities,
//                                    Map<String, String> methods2OvlMergeMethods,
//                                    Map<String, String> domainName2Methods,
//                                    String relNo,
//                                    PrintWriter logOut,
//                                    String ERROR_MAIL_SUBJECT,
//                                    int verbose)
//            throws Exception {
//
//
//        String in = null;
//        String methodAc = null;
//        String tok;
//        String curFamily = null;
//        List<String> curFamilyMethods = new ArrayList<String>();
//        String curMergeMethod = null;
//        String curOvlResType = null;
//        String prevFamily = null;
//        boolean dataStarted = false;
//
//        BufferedReader fReader =
//                new BufferedReader(new FileReader(Consts.SMART_OVERLAPS_FILE(relNo)));
//
//        /**
//         * Example content:
//         *
//         name     |    domain    |  typ  | number
//         --------------+--------------+-------+--------
//         DEXDc        | DEXDc2       | merge |      2
//         .              ^^^^^^         ^^^^^        ^
//         DEXDc        | DEXDc        | merge |      1
//         DEXDc        | DEXDc3       | merge |      3
//         */
//
//        while ((in = fReader.readLine()) != null) {
//            if (in.startsWith("------")) {
//                dataStarted = true;
//            } else if (dataStarted) {
//                String[] line = in.split("\\|");
//                int len = line.length;
//                int cnt = 0;
//                while (cnt < len) {
//                    tok = line[cnt].trim();
//                    if (cnt == 0) {
//                        curFamily = tok;
//                        if (prevFamily != null && !curFamily.equals(prevFamily)) {
//                            if (curOvlResType.equals(Consts.SMART_MERGE_RESOLUTION_TYPE)) {
//                                // Store curMergeMethod for all methods in prevFamily
//                                Iterator<String> iter = curFamilyMethods.iterator();
//                                while (iter.hasNext()) {
//                                    methods2OvlMergeMethods.put(iter.next(), curMergeMethod);
//                                }
//                            }
//                            curMergeMethod = null;
//                            curOvlResType = null;
//                            curFamilyMethods = new ArrayList<String>();
//                        }
//                        prevFamily = curFamily;
//                    } else if (cnt == 1) {
//                        if (domainName2Methods.keySet().contains(tok)) {
//                            methodAc = (String) domainName2Methods.get(tok);
//                            /** Store in case this family is of SMART_MERGE_RESOLUTION_TYPE, and later the merge method
//                             * for this method_ac needs to be stored.
//                             */
//                            curFamilyMethods.add(methodAc);
//                        } else {
//                            /** Report error, but not fail - in the current overlapping file the ones missing
//                             * are either deleted domain names or family names - all with low (or for families, always
//                             * the lowest) priority.
//                             */
//                            logOut.println("smartPP: Failed to retrieve methodAc for domainName: " + tok);
//                            logOut.flush();
//                            cnt++;
//                            break; // skip to next line
//                        }
//                    } else if (cnt == 2) {
//                        if (!tok.equals(Consts.SMART_SPLIT_RESOLUTION_TYPE) && !tok.equals(Consts.SMART_MERGE_RESOLUTION_TYPE)) {
//                            Consts.reportError(ERROR_MAIL_SUBJECT,
//                                    "smartPP: Unknown overlap resolution type encountered: " + tok,
//                                    logOut);
//                            throw Consts.EXCEPTION_ALREADY_HANDLED;
//                        } else {
//                            curOvlResType = tok;
//                            methods2OvlResolutionTypes.put(methodAc, tok);
//                            if (verbose > Consts.VERBOSE) {
//                                logOut.println("Res type for : " + methodAc + " = " + tok);
//                                logOut.flush();
//                            }
//                        }
//                    } else if (cnt == 3) {
//                        try {
//                            Integer pr = Integer.parseInt(tok);
//                            methods2OvlResolutionPriorities.put(methodAc, pr);
//                            if (verbose > Consts.VERBOSE) {
//                                logOut.println("Res priority for : " + methodAc + " = " + tok);
//                                logOut.flush();
//                            }
//                            if (pr.intValue() == 1 && curOvlResType.equals(Consts.SMART_MERGE_RESOLUTION_TYPE))
//                                curMergeMethod = methodAc;
//                        } catch (NumberFormatException e) {
//                            Consts.reportError(ERROR_MAIL_SUBJECT,
//                                    "smartPP: Overlap priority not a number: " + tok,
//                                    logOut);
//                            throw Consts.EXCEPTION_ALREADY_HANDLED;
//                        }
//                    }
//                    cnt++;
//                } // while (cnt < len) { - end
//            } // if (dataStarted) - end
//        } //
//    }
