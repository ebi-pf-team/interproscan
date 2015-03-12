package uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel;

import uk.ac.ebi.interpro.scan.model.PhobiusFeatureType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Description of class...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusFeature {

    public static final String SIGNAL = "SIGNAL";
    public static final String DOMAIN = "DOMAIN";
    public static final String TRANSMEM = "TRANSMEM";
    public static final String NON_CYTOPLASMIC = "NON CYTOPLASMIC.";
    public static final String CYTOPLASMIC = "CYTOPLASMIC.";
    public static final String N_REGION = "N-REGION.";
    public static final String H_REGION = "H-REGION.";
    public static final String C_REGION = "C-REGION.";

    private int start;

    private int stop;

    private PhobiusFeatureType featureType;

    public static final Pattern FT_LINE_PATTERN = Pattern.compile ("^FT\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s*(.*)$");

    public PhobiusFeature(Matcher ftLineMatcher) {

        this.start = Integer.parseInt(ftLineMatcher.group(2));
        this.stop = Integer.parseInt(ftLineMatcher.group(3));
        final String type = ftLineMatcher.group(1);
        final String group4 = ftLineMatcher.group(4);
        final String qualifier =
                (group4 != null && group4.trim().length() > 0)
                        ? group4
                        : null;
        this.featureType = PhobiusFeatureType.getFeatureTypeByTypeAndQualifier(type, qualifier);

    }

    public PhobiusFeatureType getFeatureType() {
        return featureType;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n').append(featureType).append(", ");
        sb.append(start);
        sb.append(" - ").append(stop);
        return sb.toString();
    }
}
