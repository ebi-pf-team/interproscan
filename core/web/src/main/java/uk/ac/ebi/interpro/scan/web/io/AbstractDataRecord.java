package uk.ac.ebi.interpro.scan.web.io;

/**
 * TODO: Description
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AbstractDataRecord {

    private final String proteinAc;
    private final String proteinId;
    private final String proteinDescription;
    private final int proteinLength;
    private final String crc64;
    private final int posFrom;
    private final int posTo;
    private final boolean isProteinFragment;

    public AbstractDataRecord(String proteinAc, String proteinId, String proteinDescription, int proteinLength,
                              String crc64, int posFrom, int posTo, boolean proteinFragment) {
        this.proteinAc = proteinAc;
        this.proteinId = proteinId;
        this.proteinDescription = proteinDescription;
        this.proteinLength = proteinLength;
        this.crc64 = crc64;
        this.posFrom = posFrom;
        this.posTo = posTo;
        isProteinFragment = proteinFragment;
    }

    public String getProteinAc() {
        return proteinAc;
    }

    public String getProteinId() {
        return proteinId;
    }

    public String getProteinDescription() {
        return proteinDescription;
    }

    public int getProteinLength() {
        return proteinLength;
    }

    public String getCrc64() {
        return crc64;
    }

    public int getPosFrom() {
        return posFrom;
    }

    public int getPosTo() {
        return posTo;
    }

    public boolean isProteinFragment() {
        return isProteinFragment;
    }
}
