package uk.ac.ebi.interpro.scan.io.match.prints.parsemodel;

/**
 * Simple model class to hold details of PRINTS matches
 * during parsing.
 *
 * @author John Maslen
 * @version $Id$
 * @since 1.0
 */

//Summary of search fields and stored fields in Onion:
//
//Sn 			→		proteinID
//1TBH 			→		motifName (1), eValue (2)#
//2TBH/2TBN		→		motifName (1), eValue(9), graphScan (10)
//3TBH/3TBN 		→		motifName (1), motifNo (2), motifTotal (4), idScore (5), pValue (7)*, length (9), position (11)**
//
//# the  eValue is converted to log10 for storing in the database
//      e.g. 	 eValue = Consts.log10(eValue);
//		Method:
//		public static float log10(double x) {
//			return (float) (Math.log(x) / Math.log(10.0));
//		}
//* pValue is converted to log 10 , e.g. in Java : pVal = (float) Consts.log10((double) pVal);
//** position requires a hack due to fingerPrints output (for starting positions that are in
//       5 figures, fails to separate pos and high columns with a space, then we just have to
//       pick out the  correct start position manually), e.g.
//		String seqStartPosS = line[k];
//		 if (line[k].length() > 5) seqStartPosS = seqStartPosS.substring(0, 6);
//                        seqStartPos = Integer.parseInt(seqStartPosS);
//
//Sn; 1
//Si; Fasta sequence
//1TBS
//1TBH GLU5KINASE      1.103000e-49   Glutamate 5-kinase family signature                                      PR00474
//1TBF
//2TBS
//2TBT FingerPrint     No.Motifs SumId    AveId    ProfScore  Ppvalue     Evalue      GraphScan
//2TBH GLU5KINASE      5  of  5  2.6e+02  52       2861       1.5e-55     1.1e-49     IIIII
//2TBN ASNGLNASE       2  of  3  53.34    26.67    326        5.5e-06     15          I.i
//2TBF
//3TBS
//3TBT MotifName       No.Mots   IdScore PfScore Pvalue    Sequence                                                Len  low  pos   high
//3TBH GLU5KINASE      1  of  5  60.49   466     2.19e-09  GSDVVIVSSGAIAAG                                         15   0    52    0
//….
//3TBB
//3TBN ASNGLNASE       1  of  3  32.54   196     1.91e-03  VVIVSSGAIAAG                                            12   0    55    0
//3TBB
//3TBF


public class PrintsProtein {

    private String proteinIdentifier;

    private String motifName;

    private int motifNo;

    private int motifTotal;

    //TODO -> Ascertain how release numbers are handled in I5

    //private int dbRelNoMajor;

    //private int dbRelNoMinor;

    private float idScore;

    private float pValue;

    private int seqStartPos;

    private int seqEndPos;

    private String graphScan;

    private Float eValue;

    public PrintsProtein(String proteinIdentifier, String motifName, Float eValue) {
        this.proteinIdentifier = proteinIdentifier;
        this.motifName = motifName;
        this.eValue = eValue;
    }

    public String getProteinIdentifier() {
        return proteinIdentifier;
    }

    public String getMotifName() {
        return motifName;
    }

    public int getMotifNo() {
        return motifNo;
    }

    public int getMotifTotal() {
        return motifTotal;
    }

    public float getIdScore() {
        return idScore;
    }

    public float getpValue() {
        return pValue;
    }

    public int getSeqStartPos() {
        return seqStartPos;
    }

    public int getSeqEndPos() {
        return seqEndPos;
    }

    public String getGraphScan() {
        return graphScan;
    }

    public Float geteValue() {
        return eValue;
    }

    public void setProteinIdentifier(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public void setMotifName(String motifName) {
        this.motifName = motifName;
    }

    public void setMotifNo(int motifNo) {
        this.motifNo = motifNo;
    }

    public void setMotifTotal(int motifTotal) {
        this.motifTotal = motifTotal;
    }

    public void setIdScore(float idScore) {
        this.idScore = idScore;
    }

    public void setpValue(float pValue) {
        this.pValue = pValue;
    }

    public void setSeqStartPos(int seqStartPos) {
        this.seqStartPos = seqStartPos;
    }

    public void setSeqEndPos(int seqEndPos) {
        this.seqEndPos = seqEndPos;
    }

    public void setGraphScan(String graphScan) {
        this.graphScan = graphScan;
    }

    public void seteValue(Float eValue) {
        this.eValue = eValue;
    }

}
