package uk.ac.ebi.interpro.scan.io.match.prints;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.prints.parsemodel.PrintsProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 * @author John Maslen
 * @version $Id$
 * @since 1.0
 */

public class PrintsMatchParser {

    public static final String PROTEIN_ID_MARKER = "Sn";

    public static final String FIRST_LEVEL_ANNOTATION = "1TBH";

    public static final String SECOND_LEVEL_ANNOTATION_H = "2TBH";

    public static final String SECOND_LEVEL_ANNOTATION_N = "2TBN";

    public static final String THIRD_LEVEL_ANNOTATION_H = "3TBH";

    public static final String THIRD_LEVEL_ANNOTATION_N = "3TBN";

    public static final String END_ENTRY_ANNOT_MARKER = "3TBF";

    //TODO pass as parameter from properties file?
    public static final float PRINTS_DEFAULT_CUTOFF = log10(1e-04);

    public Set<PrintsProtein> parse(InputStream is, String fileName, Map evalCutOffs) throws IOException {
        Set<PrintsProtein> proteinsWithMatches = new HashSet<PrintsProtein>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            Map<String, PrintsProtein> motifPrintsProteinMap = new HashMap<String, PrintsProtein>();
            String proteinIdentifier = null;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith(END_ENTRY_ANNOT_MARKER)) {
                    if (motifPrintsProteinMap != null && motifPrintsProteinMap.size() > 0) {
                        for (PrintsProtein p : motifPrintsProteinMap.values()) {
                            //TODO sanity check on entry?
                            proteinsWithMatches.add(p);
                        }
                    }
                    motifPrintsProteinMap.clear();
                } else if (line.startsWith(PROTEIN_ID_MARKER)) {
                    //Sn; 1
                    proteinIdentifier = line.split("\\s+")[1].trim();
                } else if (line.startsWith(FIRST_LEVEL_ANNOTATION)) {
                    //1TBH GLU5KINASE      1.103000e-49   Glutamate 5-kinase family signature     PR00474
                    String[] lineSplit = line.split("\\s+");
                    String motifName = lineSplit[1];
                    Float eValue = log10(Double.parseDouble(lineSplit[2]));
                    if (evaluateEvalue(motifName, eValue, evalCutOffs)) {
                        if (proteinIdentifier == null) {
                            throw new ParseException("FingerPrintScan output parsing: Trying to parse raw output but don't appear to have a protein ID.", fileName, line, lineNumber);
                        }
                        PrintsProtein protein = new PrintsProtein(proteinIdentifier, motifName, eValue);
                        motifPrintsProteinMap.put(motifName, protein);
                    }
                } else if (line.startsWith(SECOND_LEVEL_ANNOTATION_H) || line.startsWith(SECOND_LEVEL_ANNOTATION_N)) {
                    //2TBT FingerPrint     No.Motifs SumId    AveId    ProfScore  Ppvalue     Evalue      GraphScan
                    //2TBH GLU5KINASE      5  of  5  2.6e+02  52       2861       1.5e-55     1.1e-49     IIIII
                    //2TBN ASNGLNASE       2  of  3  53.34    26.67    326        5.5e-06     15          I.i
                    String[] matchSplit = line.split("\\s+");
                    String motifName = matchSplit[1];
                    Float eValue = log10(Double.parseDouble(matchSplit[9]));
                    String graphScan = matchSplit[10];
                    if (evaluateEvalue(motifName, eValue, evalCutOffs)) {
                        PrintsProtein protein;
                        if (motifPrintsProteinMap.containsKey(motifName)) {
                            protein = motifPrintsProteinMap.get(motifName);
                        } else {
                            protein = new PrintsProtein(proteinIdentifier, motifName, eValue);
                        }
                        protein.setGraphScan(graphScan);
                        motifPrintsProteinMap.put(motifName, protein);
                    }
                } else if (line.startsWith(THIRD_LEVEL_ANNOTATION_H) || line.startsWith(THIRD_LEVEL_ANNOTATION_N)) {
                    //3TBH/3TBN 	→		motifName (1), motifNo matching (2), motifTotal (4), idScore (5), pValue (7)*, length (9), position (11)**
                    //3TBT MotifName       No.Mots   IdScore PfScore Pvalue    Sequence                Len  low  pos   high
                    //3TBH GLU5KINASE      1  of  5  60.49   466     2.19e-09  GSDVVIVSSGAIAAG         15   0    52    0
                    //3TBB
                    //3TBN ASNGLNASE       1  of  3  32.54   196     1.91e-03  VVIVSSGAIAAG            12   0    55    0
                    String[] matchSplit = line.split("\\s+");
                    String motifName = matchSplit[1];
                    if (motifPrintsProteinMap.containsKey(motifName)) {
                        PrintsProtein protein = motifPrintsProteinMap.get(motifName);
                        protein.setMotifNo(Integer.parseInt(matchSplit[2]));
                        protein.setMotifTotal(Integer.parseInt(matchSplit[4]));
                        protein.setIdScore(Float.parseFloat(matchSplit[5]));
                        protein.setpValue(log10(Double.parseDouble((matchSplit[7]))));
                        int seqLength = Integer.parseInt(matchSplit[9]);
                        // Inherited from Onion:
                        // The hack below is here because of The FingerPrintScan, for starting positions that are in
                        // 5 figures, fails to separate pos and high columns with a space, then we just have to
                        // pick out the  correct start position manually
                        //  Len  low  pos  high
                        //  19   81   101689976
                        String seqStartPosStr = matchSplit[11];
                        if (seqStartPosStr.length() > 5) {
                            seqStartPosStr = seqStartPosStr.substring(0, 6);
                        }
                        int seqStartPos = Integer.parseInt(seqStartPosStr);
                        if (seqStartPos < 1) {
                            seqStartPos = 1;
                        }
                        protein.setSeqStartPos(seqStartPos);
                        //TODO: see note below on Onion sanity check
                        protein.setSeqEndPos(seqStartPos + seqLength - 1);
                        motifPrintsProteinMap.put(motifName, protein);
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return proteinsWithMatches;
    }

    public static float log10(double x) {
        return (float) (Math.log(x) / Math.log(10.0));
    }

    public static boolean evaluateEvalue(String motifName, Float eValue, Map evalCutOffs) {
        if (evalCutOffs.containsKey(motifName)) {
            return eValue < (Float) evalCutOffs.get(motifName);
        } else {
            return eValue < PRINTS_DEFAULT_CUTOFF;
        }
    }

}

//      ONION sanity check for sequence start and end positions - end one not yet implemented:
//
//                    /** Now the sanity check on the positions - PRINTS motifs can cross the
//                     * sequence boundary at both N- and C-terminals.
//                     * If seqEndPos > seq.length, then seqEndPos needs to be set to seq.length
//                     * If seqStartPos < 1, then it needs to be set to 1.
//                     */
//                    if (seqStartPos < 1)
//                        seqStartPos = 1;
//                    Integer upiLen = seqLengths.get(upi);
//                    if (upiLen != null) {
//                        if (seqEndPos > upiLen)
//                            seqEndPos = upiLen;
//                    }