package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Simple immutable POJO holding the details of a single Seed alignment
 * Use the basic SeedAlignment class for retrieval from the database for
 * post processing.
 *
 * @author Phil Jones
 * @version $Id: SeedAlignment.java,v 1.1 2009/10/23 15:05:00 pjones Exp $
 * @since 1.0
 */
public class SeedAlignment implements Serializable {

    private final String modelAccession;

    private final String MD5;

    private final int alignmentStart;

    private final int alignmentEnd;

    SeedAlignment(final String modelAccession,
                  final String MD5,
                 final int alignmentStart,
                 final int alignmentEnd){
        this.modelAccession = modelAccession;
        this.alignmentStart = alignmentStart;
        this.alignmentEnd = alignmentEnd;
        this.MD5 = MD5;
    }

    public String getModelAccession() {
        return modelAccession;
    }

    public int getAlignmentStart() {
        return alignmentStart;
    }

    public int getAlignmentEnd() {
        return alignmentEnd;
    }

    public String getMD5() {
        return MD5;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("modelAccession", modelAccession).
                append("MD5", MD5).
                append("alignmentStart", alignmentStart).
                append("alignmentEnd", alignmentEnd).
                toString();
    }
}
