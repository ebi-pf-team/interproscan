package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

public class Batch {
    private final String fromMD5;
    private final String toMD5;

    public String getFromMD5() {
        return fromMD5;
    }

    public String getToMD5() {
        return toMD5;
    }

    public Batch(String fromMD5, String toMD5) {
        this.fromMD5 = fromMD5;
        this.toMD5 = toMD5;
    }
}
