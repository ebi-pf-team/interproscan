package uk.ac.ebi.interpro.scan.model.raw;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
@Table(name = CDDRawSite.TABLE_NAME,  indexes = {
        @Index(name = "CDD_RW_SEQ_IDX", columnList = RawMatch.COL_NAME_SEQUENCE_IDENTIFIER),
        @Index(name = "CDD_RW_NUM_SEQ_IDX", columnList = RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID),
        @Index(name = "CDD_RW_MODEL_IDX", columnList = RawMatch.COL_NAME_MODEL_ID),
        @Index(name = "CDD_RW_SIGLIB_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY),
        @Index(name = "CDD_RW_SIGLIB_REL_IDX", columnList = RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE)
})
public class CDDRawSite extends RPSBlastRawSite implements Comparable<CDDRawSite> {

    public static final String TABLE_NAME = "CDD_RAW_MATCH";

    /** Example CDD output file:
        DATA
        SESSION	1	RPSBLAST 2.2.31+	/ebi/production/interpro/programmers/scp/cdd/rpsb/db/Cdd_NCBI	BLOSUM62	0.01
        QUERY	Query_1	Peptide	590	sp|Q96N58|ZN578_HUMAN Zinc finger protein 578 OS=Homo sapiens GN=ZNF578 PE=2 SV=2
        DOMAINS
        1	Query_1	Specific	143639	24	60	3.46102e-15	69.5006	cd07765	KRAB_A-box	-	271597
        ENDDOMAINS
        SITES
        1	Query_1	Specific	Zn binding site	C373,C376,H389,H393	4	4	0

    */

    protected CDDRawSite() {
    }

    public CDDRawSite(String sequenceIdentifier,
                      int sessionIdentifier,
                      HitType hitType,
                      String title,
                      String residue,
                      int siteStart,
                      int siteEnd,
                      String pssmId,
                      String model,
                      int completeSize,
                      int mappedSize,
                      String signatureLibraryRelease) {
        super(sequenceIdentifier, sessionIdentifier,
                hitType, title, residue, siteStart, siteEnd, pssmId, model,
                completeSize, mappedSize, SignatureLibrary.CDD, signatureLibraryRelease);
    }


//    @Override
//    public String toString() {
//        return super.toString();
//
//    }

    /**
     * TODO define an ordering on CDD matches or RPSBlast matches
     *
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than , equal to, or greater than that object.
     * <p/>
     * Maybe the required ordering for CDD post processing is:
     * <p/>
     * evalue ASC, BitScore DESC
     *
     * @param that being the CDDRawMatch to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(CDDRawSite that) {
        if (this == that) return 0;
        if (this.getSiteStart() < that.getSiteStart()) return -1;     // First, sort by state ASC
        if (this.getSiteStart() > that.getSiteStart()) return 1;
        if (this.getSiteEnd() > that.getSiteEnd()) return -1;                     // then by score ASC
        if (this.getSiteEnd() < that.getSiteEnd()) return 1;
        if (this.hashCode() > that.hashCode())
            return -1;                     // then by hashcode to be consistent with equals.
        if (this.hashCode() < that.hashCode()) return 1;
        return 0;
    }



}

