package uk.ac.ebi.interpro.scan.io.match.prints.parsemodel;

/**
 * Simple model class to hold details of PRINTS motif details for the building of PrintsRawMatch objects where these details are added to with
 * the required parameters for each individual motif.
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
// NOT REQUIRED HERE:
// 3TBH/3TBN 		→		motifName (1), motifNo (2), motifTotal (4), idScore (5), pValue (7)*, length (9), position (11)**
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


public class PrintsMotif {

    private String proteinIdentifier;

    private String motifName;

    private String modelId;

    private String graphScan;

    private Double eValue;

    public PrintsMotif(String proteinIdentifier, String motifName, double eValue, String modelId) {
        this.proteinIdentifier = proteinIdentifier;
        this.motifName = motifName;
        this.eValue = eValue;
        this.modelId = modelId;
    }

    public PrintsMotif(String proteinIdentifier, String motifName, double eValue) {
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

    public String getGraphScan() {
        return graphScan;
    }

    public Double geteValue() {
        return eValue;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setProteinIdentifier(String proteinIdentifier) {
        this.proteinIdentifier = proteinIdentifier;
    }

    public void setMotifName(String motifName) {
        this.motifName = motifName;
    }

    public void setGraphScan(String graphScan) {
        this.graphScan = graphScan;
    }

    public void seteValue(double eValue) {
        this.eValue = eValue;
    }
}
