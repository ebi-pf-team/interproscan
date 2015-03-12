package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import java.util.HashMap;
import java.util.Map;

/**
 * <a href="http://myhits.isb-sib.ch/cgi-bin/motif_scan">ProfileScan</a> raw match.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 */
@Entity
//@Table(name="profile_scan_raw_match")
public abstract class ProfileScanRawMatch extends PfScanRawMatch {

    public enum Level {
        MINUS_ONE("-1"),
        ZERO("0"),
        ONE("1");

        private static Map<String, Level> STRING_TO_LEVEL = new HashMap<String, Level>(Level.values().length);

        static {
            for (Level level : Level.values()) {
                STRING_TO_LEVEL.put(level.levelValue, level);
            }
        }

        String levelValue;

        Level(String levelValue) {
            this.levelValue = levelValue;
        }

        public static Level byLevelString(String levelString) {
            return STRING_TO_LEVEL.get(levelString);
        }
    }

    protected ProfileScanRawMatch() {
    }

    @Column//(name = "score")
    private double score;

    @Enumerated(javax.persistence.EnumType.STRING)
    @Column(name = "pf_scan_level", nullable = false)  // level is an SQL reserved word.
    private Level level;

    protected ProfileScanRawMatch(String sequenceIdentifier, String model,
                                  SignatureLibrary signatureLibrary, String signatureLibraryRelease,
                                  int locationStart, int locationEnd,
                                  String cigarAlignment, double score, Level level) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd, cigarAlignment);
        setScore(score);
        setLevel(level);
    }

    public double getScore() {
        return score;
    }

    private void setScore(double score) {
        this.score = score;
    }

    public Level getLevel() {
        return level;
    }

    private void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileScanRawMatch)) return false;
        if (!super.equals(o)) return false;

        ProfileScanRawMatch that = (ProfileScanRawMatch) o;

        if (Double.compare(that.score, score) != 0) return false;
        if (level != that.level) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = score != +0.0d ? Double.doubleToLongBits(score) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + level.hashCode();
        return result;
    }
}
