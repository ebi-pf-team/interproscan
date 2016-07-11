package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
public abstract class RPSBlastRawSite extends RawSite implements Serializable {

    /** Example CDD output file:
        DATA
        SESSION	1	RPSBLAST 2.2.31+	db/Cdd_NCBI	BLOSUM62	0.01
        QUERY	Query_1	Peptide	590	sp|Q96N58|ZN578_HUMAN Zinc finger protein 578 OS=Homo sapiens GN=ZNF578 PE=2 SV=2
        DOMAINS
        1	Query_1	Specific	143639	24	60	3.46102e-15	69.5006	cd07765	KRAB_A-box	-	271597
        ENDDOMAINS
        SITES
        1	Query_1	Specific	Zn binding site	C373,C376,H389,H393	4	4	143639
        ENDSITES
    */

    public enum HitType {
        SPECIFIC("Specific"),
        NONSPECIFIC("Non-specific"),
        SUPERFAMILY("Superfamily"),
        MULTIDOMAIN("Multi-domain");

        private static Map<String, HitType> STRING_TO_HITTYPE = new HashMap<String, HitType>(HitType.values().length);

        static {
            for (HitType hitType : HitType.values()) {
                STRING_TO_HITTYPE.put(hitType.hitType, hitType);
            }
        }

        String hitType;

        HitType(String hitType) {
            this.hitType = hitType;
        }

        public static HitType byHitTypeString(String hitTypeString) {
            return STRING_TO_HITTYPE.get(hitTypeString);
        }
    }

    @Column
    int sessionNumber;


    @Column
    HitType hitType;

    @Column
    String pssmId;


    protected RPSBlastRawSite() {
    }

    //#<session-ordinal>	<query-id[readingframe]>	<annot-type>	<title>	<residue(coordinates)>	<complete-size>	<mapped-size>	<source-domain>

    public RPSBlastRawSite(String sequenceIdentifier,
                           int sessionIdentifier,
                           HitType hitType,
                           String title,
                           String residues,
                           int firstStart,
                           int lastEnd,
                           String pssmId,
                           String model,
                           int completeSize,
                           int mappedSize,
                           SignatureLibrary signatureLibrary,
                           String signatureLibraryRelease) {
        super(sequenceIdentifier, model, title, residues, firstStart, lastEnd,signatureLibrary, signatureLibraryRelease);
        this.sessionNumber = sessionIdentifier;
        this.hitType = hitType;
        this.pssmId = pssmId;


    }

    public HitType getHitType() {
        return hitType;
    }

    public void setHitType(HitType hitType) {
        this.hitType = hitType;
    }

    public String getPssmId() {
        return pssmId;
    }

    public void setPssmId(String pssmId) {
        this.pssmId = pssmId;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RPSBlastRawSite))
            return false;
        final RPSBlastRawSite m = (RPSBlastRawSite) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(pssmId, m.pssmId)
                .append(sessionNumber, m.sessionNumber)
                .append(hitType, m.hitType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 81)
                .appendSuper(super.hashCode())
                .append(pssmId)
                .append(sessionNumber)
                .append(hitType)
                .toHashCode();
    }

}

