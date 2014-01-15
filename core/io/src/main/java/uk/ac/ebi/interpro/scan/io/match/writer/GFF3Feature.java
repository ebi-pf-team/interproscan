package uk.ac.ebi.interpro.scan.io.match.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a GFF3 feature (of whatever type). Contains all attributes, which are necessary to write a single feature line in GFF3.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @since 1.0-SNAPSHOT
 */
public class GFF3Feature {

    final static String NAME_ATTR = "Name";

    final static String ID_ATTR = "ID";

    final static String MD5_ATTR = "md5";

    final static String TARGET_ATTR = "Target";

    private final String UNDEFINIED_FIELD = ".";

    //Not nullable
    private String seqId;

    private String source = UNDEFINIED_FIELD;

    private String type = UNDEFINIED_FIELD;

    private int start;

    private int end;

    private String score = UNDEFINIED_FIELD;

    private String strand = UNDEFINIED_FIELD;

    private String phase = UNDEFINIED_FIELD;

    private final Map<String, String> attributes = new HashMap<String, String>();


    public GFF3Feature(String seqId, String source, String type, int start, int end, String strand) {
        this.seqId = seqId;
        this.source = source;
        this.type = type;
        this.start = start;
        this.end = end;
        this.strand = strand;
    }

    public void addAttribute(String key, String value) {
        if (value != null) {
            attributes.put(key, value);
        }
    }

    public List<String> getGFF3FeatureLine() {
        final List<String> referenceLine = new ArrayList<String>();
        referenceLine.add(getSeqId());
        referenceLine.add(getSource());
        referenceLine.add(getType());
        referenceLine.add(Integer.toString(getStart()));
        referenceLine.add(Integer.toString(getEnd()));
        referenceLine.add(getScore());
        referenceLine.add(getStrand());
        referenceLine.add(getPhase());
        referenceLine.add(getAttributes());
        return referenceLine;
    }


    public void setScore(String score) {
        this.score = score;
    }

    public String getSeqId() {
        return seqId;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getScore() {
        return score;
    }

    public String getStrand() {
        return strand;
    }

    public String getPhase() {
        return phase;
    }

    public String getAttributes() {
        StringBuilder sb = new StringBuilder();
        for (String key : attributes.keySet()) {
            String value = attributes.get(key);
            if (key.equals(ID_ATTR)) {
                value = ProteinMatchesGFFResultWriter.getValidGFF3SeqId(value);
            } else if (key.equals(NAME_ATTR)) {
                value = ProteinMatchesGFFResultWriter.getValidGFF3AttributeName(value);
            }
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }
}
