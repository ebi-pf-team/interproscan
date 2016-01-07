package uk.ac.ebi.interpro.scan.model.raw;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Index;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 *
 * @author Gift Nuka
 * @version $Id$
 */
@Entity
@javax.persistence.Table(name = CDDRawMatch.TABLE_NAME)
@org.hibernate.annotations.Table(appliesTo = CDDRawMatch.TABLE_NAME, indexes = {
        @Index(name = "CDD_RW_SEQ_IDX", columnNames = {RawMatch.COL_NAME_SEQUENCE_IDENTIFIER}),
        @Index(name = "CDD_RW_NUM_SEQ_IDX", columnNames = {RawMatch.COL_NAME_NUMERIC_SEQUENCE_ID}),
        @Index(name = "CDD_RW_MODEL_IDX", columnNames = {RawMatch.COL_NAME_MODEL_ID}),
        @Index(name = "CDD_RW_SIGLIB_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY}),
        @Index(name = "CDD_RW_SIGLIB_REL_IDX", columnNames = {RawMatch.COL_NAME_SIGNATURE_LIBRARY_RELEASE})
})
public class CDDRawMatch extends RawMatch implements Serializable {

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

    @Column
    int sessionNumber;

    @Column
    String definitionLine;

    @Column
    String hitType;

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

    protected CDDRawMatch() {
    }

    //#<session-ordinal>	<query-id[readingframe]>	<hit-type>	<PSSM-ID>	<from>	<to>	<E-Value>	<bitscore>	<accession>	<short-name>	<incomplete>	<superfamily PSSM-ID>


    public CDDRawMatch(String sequenceIdentifier,
                       String definitionLine,
                       int sessionIdentifier,
                       String hitType,
                       String pssmId,
                       String model,
                       int locationStart,
                       int locationEnd,
                       double evalue,
                       double bitScore,
                       String shortName,
                       String incomplete,
                       String superfamilyPSSMId,
                       String signatureLibraryRelease) {
        super(sequenceIdentifier, model, SignatureLibrary.CDD, signatureLibraryRelease, locationStart, locationEnd);
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

    public String getHitType() {
        return hitType;
    }

    public void setHitType(String hitType) {
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
        if (!(o instanceof CDDRawMatch))
            return false;
        final CDDRawMatch m = (CDDRawMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(pssmId, m.pssmId)
                .append(sessionNumber, m.sessionNumber)
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
                .append(definitionLine)
                .append(bitScore)
                .append(evalue)
                .append(superfamilyPSSMId)
                .toHashCode();
    }

}

