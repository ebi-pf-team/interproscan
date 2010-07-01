package uk.ac.ebi.interpro.scan.io.prints;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 29-Jun-2010
 * Time: 12:10:19
 */
public class FingerPRINTSHierarchyDBParser implements Serializable {

    // FINGERPRINT_IS_DOMAIN is a flag which only applies when HIERARCHY_BASED_PP = true and indicates that the hit is
    //  unaffected by any hierarchy relationships and should be let through no matter what.
    private static final String FINGERPRINT_IS_DOMAIN = "*";

    //The charatcer used to indicate comments (only occurs after the hierarchy information on the line)
    private static final String COMMENT_CHARACTER = "#";

    private static final int INDEX_MODEL_ID = 0;
    private static final int INDEX_MODEL_ACCESSION = 1;
    private static final int INDEX_EVALUE_CUTTOFF = 2;
    private static final int INDEX_MINIMUM_MOTIF_COUNT = 3;
    private static final int INDEX_SIBLING_LIST = 4;

    /**
     * Returns a map of PRINTS model accession to HierachyDBEntry object.
     *
     * @param resource being the hierarchy database file
     * @return a map of PRINTS model accession to HierachyDBEntry object.
     * @throws IOException due to underlying filesystem access problem.
     */
    public Map<String, HierachyDBEntry> parse(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists()) {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }
        final Map<String, HierachyDBEntry> printsIdToDBEntry = new HashMap<String, HierachyDBEntry>() {
        };
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                //If a comment exists then get rid of all comment text on the line
                if (line.contains(COMMENT_CHARACTER)) {
                    line = line.substring(0, line.indexOf(COMMENT_CHARACTER));
                }

                final String[] lineElements = line.split("\\|");
                if (lineElements.length > 3) {
                    final String modelId = lineElements[INDEX_MODEL_ID].trim();
                    final String modelAccession = lineElements[INDEX_MODEL_ACCESSION].trim();
                    final double evalueCutoff = Double.parseDouble(lineElements[INDEX_EVALUE_CUTTOFF].trim());
                    final int minimumMotifCount = Integer.parseInt(lineElements[INDEX_MINIMUM_MOTIF_COUNT].trim());
                    boolean domain = false;
                    List<String> hierarchy = Collections.emptyList();
                    if (lineElements.length > 4) {
                        String siblingListString = lineElements[INDEX_SIBLING_LIST].trim();
                        if (FINGERPRINT_IS_DOMAIN.equals(siblingListString)) {
                            domain = true;
                        } else if (siblingListString.length() > 0) {
                            String[] siblingIds = siblingListString.split("\\,");
                            hierarchy = Arrays.asList(siblingIds);
                        }
                    }
                    printsIdToDBEntry.put(modelAccession, new HierachyDBEntry(modelId, modelAccession, evalueCutoff, minimumMotifCount, hierarchy, domain));
                }
            }
        }
        finally {
            if (reader != null) reader.close();
        }
        return printsIdToDBEntry;
    }

    public class HierachyDBEntry {

        private String id;

        private String accession;

        private double evalueCutoff;

        private int minimumMotifCount;

        private List<String> hierarchicalRelations;

        private boolean domain;

        private HierachyDBEntry(String id, String accession, double evalueCutoff, int minimumMotifCount, List<String> hierarchicalRelations, boolean domain) {
            if (hierarchicalRelations == null) {
                throw new IllegalArgumentException("The hierarchicalRelations Collection must not be null.");
            }
            this.id = id;
            this.accession = accession;
            this.evalueCutoff = evalueCutoff;
            this.minimumMotifCount = minimumMotifCount;
            this.hierarchicalRelations = hierarchicalRelations;
            this.domain = domain;
        }

        public String getId() {
            return id;
        }

        public String getAccession() {
            return accession;
        }

        public double getEvalueCutoff() {
            return evalueCutoff;
        }

        public List<String> getHierarchicalRelations() {
            return hierarchicalRelations;
        }

        public int getMinimumMotifCount() {
            return minimumMotifCount;
        }

        public boolean isDomain() {
            return domain;
        }
    }
}
