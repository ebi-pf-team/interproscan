package uk.ac.ebi.interpro.scan.io.match.phobius.parsemodel;

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

    private String name;

    private String qualifier;

    public static final Pattern FT_LINE_PATTERN = Pattern.compile ("^FT\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s*(.*)$");

    public PhobiusFeature(Matcher ftLineMatcher) {
        this.start = Integer.parseInt(ftLineMatcher.group(2));
        this.stop = Integer.parseInt(ftLineMatcher.group(3));
        this.name = ftLineMatcher.group(1);
        String group4 = ftLineMatcher.group(4);
        if (group4 != null && group4.trim().length() > 0){
            this.qualifier = group4;
        }
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public String getName() {
        return name;
    }

    public String getQualifier() {
        return qualifier;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('\n').append(name).append(", ");
        sb.append(qualifier).append(' ');
        sb.append(start);
        sb.append(" - ").append(stop);
        return sb.toString();
    }
}
