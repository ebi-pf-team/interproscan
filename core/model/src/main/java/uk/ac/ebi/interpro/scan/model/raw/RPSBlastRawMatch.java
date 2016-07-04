package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
public abstract class RPSBlastRawMatch extends RawMatch {

    /** Example CDD output file:
        DATA
        SESSION	1	RPSBLAST 2.2.31+	db/Cdd_NCBI	BLOSUM62	0.01
        QUERY	Query_1	Peptide	590	sp|Q96N58|ZN578_HUMAN Zinc finger protein 578 OS=Homo sapiens GN=ZNF578 PE=2 SV=2
        DOMAINS
        1	Query_1	Specific	143639	24	60	3.46102e-15	69.5006	cd07765	KRAB_A-box	-	271597
        ENDDOMAINS
        SITES
        1	Query_1	Specific	Zn binding site	C373,C376,H389,H393	4	4	0

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
    String definitionLine;

    @Column
    HitType hitType;

    @Column
    String pssmId;

    @Column
    private double bitScore;

    @Column
    private double evalue;



    @Column
    String shortName;

    @Column
    String incomplete;

    @Column
    String superfamilyPSSMId;

    protected RPSBlastRawMatch() {
    }

    //#<session-ordinal>	<query-id[readingframe]>	<hit-type>	<PSSM-ID>	<from>	<to>	<E-Value>	<bitscore>	<accession>	<short-name>	<incomplete>	<superfamily PSSM-ID>


    public RPSBlastRawMatch(String sequenceIdentifier,
                            String definitionLine,
                            int sessionIdentifier,
                            HitType hitType,
                            String pssmId,
                            String model,
                            int locationStart,
                            int locationEnd,
                            double evalue,
                            double bitScore,
                            String shortName,
                            String incomplete,
                            String superfamilyPSSMId,
                            SignatureLibrary signatureLibrary,
                            String signatureLibraryRelease) {
        super(sequenceIdentifier, model, signatureLibrary, signatureLibraryRelease, locationStart, locationEnd);
        this.sessionNumber = sessionIdentifier;
        this.hitType = hitType;
        this.pssmId = pssmId;
        this.bitScore = bitScore;
        this.evalue = evalue;
        this.shortName = shortName;
        this.incomplete = incomplete;
        this.superfamilyPSSMId =  superfamilyPSSMId;

    }

    public String getDefinitionLine() {
        return definitionLine;
    }

    public void setDefinitionLine(String definitionLine) {
        this.definitionLine = definitionLine;
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

    public double getBitScore() {
        return bitScore;
    }

    public void setBitScore(double bitScore) {
        this.bitScore = bitScore;
    }

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public int getAessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(String incomplete) {
        this.incomplete = incomplete;
    }

    public String getSuperfamilyPSSMId() {
        return superfamilyPSSMId;
    }

    public void setSuperfamilyPSSMId(String superfamilyPSSMId) {
        this.superfamilyPSSMId = superfamilyPSSMId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RPSBlastRawMatch))
            return false;
        final RPSBlastRawMatch m = (RPSBlastRawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(pssmId, m.pssmId)
                .append(sessionNumber, m.sessionNumber)
                .append(hitType, m.hitType)
                .append(definitionLine, m.definitionLine)
                .append(bitScore, m.bitScore)
                .append(evalue, m.evalue)
                .append(superfamilyPSSMId, m.superfamilyPSSMId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 81)
                .appendSuper(super.hashCode())
                .append(pssmId)
                .append(sessionNumber)
                .append(hitType)
                .append(definitionLine)
                .append(bitScore)
                .append(evalue)
                .append(superfamilyPSSMId)
                .toHashCode();
    }

}

