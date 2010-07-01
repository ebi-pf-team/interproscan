package uk.ac.ebi.interpro.scan.io.match.prints;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.prints.parsemodel.PrintsMotif;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public Set<RawProtein<PrintsRawMatch>> parse(InputStream is, String fileName, String signatureReleaseLibrary) throws IOException {
        Map<String, RawProtein<PrintsRawMatch>> rawResults = new HashMap<String, RawProtein<PrintsRawMatch>>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            Map<String, PrintsMotif> motifPrintsProteinMap = new HashMap<String, PrintsMotif>();
            String proteinIdentifier = null;
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.startsWith(END_ENTRY_ANNOT_MARKER)) {
                    motifPrintsProteinMap.clear();
                } else if (line.startsWith(PROTEIN_ID_MARKER)) {
                    //Sn; 1
                    proteinIdentifier = line.split("\\s+")[1].trim();
                } else if (line.startsWith(FIRST_LEVEL_ANNOTATION)) {
                    //1TBH GLU5KINASE      1.103000e-49   Glutamate 5-kinase family signature     PR00474
                    String[] lineSplit = line.split("\\s+");
                    String motifName = lineSplit[1];
                    String model = lineSplit[lineSplit.length - 1];
                    double eValue = Math.log10(Double.parseDouble(lineSplit[2]));
                    if (proteinIdentifier == null) {
                        throw new ParseException("FingerPrintScan output parsing: Trying to parse raw output but don't appear to have a protein ID.", fileName, line, lineNumber);
                    }
                    PrintsMotif motifMatch = new PrintsMotif(proteinIdentifier, motifName, eValue, model);
                    motifPrintsProteinMap.put(motifName, motifMatch);
                } else if (line.startsWith(SECOND_LEVEL_ANNOTATION_H) || line.startsWith(SECOND_LEVEL_ANNOTATION_N)) {
                    //2TBT FingerPrint     No.Motifs SumId    AveId    ProfScore  Ppvalue     Evalue      GraphScan
                    //2TBH GLU5KINASE      5  of  5  2.6e+02  52       2861       1.5e-55     1.1e-49     IIIII
                    //2TBN ASNGLNASE       2  of  3  53.34    26.67    326        5.5e-06     15          I.i
                    String[] matchSplit = line.split("\\s+");
                    String motifName = matchSplit[1];
                    String graphScan = matchSplit[10];

                    if (motifPrintsProteinMap.containsKey(motifName)) {
                        PrintsMotif motifMatch = motifPrintsProteinMap.get(motifName);
                        motifMatch.setGraphScan(graphScan);
                        motifPrintsProteinMap.put(motifName, motifMatch);
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
                        PrintsMotif protein = motifPrintsProteinMap.get(motifName);
                        //TODO - REMOVE: fudge for testing only - in testing so far, no significant hits are found WITHOUT a Prints model id, but just in case.....
                        if (protein != null) {
                            int motifNumber = Integer.parseInt(matchSplit[2]);
                            int motifCount = Integer.parseInt(matchSplit[4]);
                            double score = Double.parseDouble(matchSplit[5]);
                            double pvalue = Math.log10(Double.parseDouble((matchSplit[7])));
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
                            int locationStart = Integer.parseInt(seqStartPosStr);
                            if (locationStart < 1) {
                                locationStart = 1;
                            }
                            //TODO: see note below on Onion sanity check
                            int locationEnd = locationStart + seqLength - 1;

                            RawProtein<PrintsRawMatch> sequenceIdentifier = rawResults.get(proteinIdentifier);
                            if (sequenceIdentifier == null) {
                                sequenceIdentifier = new RawProtein<PrintsRawMatch>(proteinIdentifier);
                                rawResults.put(proteinIdentifier, sequenceIdentifier);
                            }

                            final PrintsRawMatch match = new PrintsRawMatch(proteinIdentifier, protein.getModelId(), signatureReleaseLibrary, locationStart,
                                    locationEnd, protein.geteValue(), protein.getGraphScan(), motifCount, motifNumber,
                                    pvalue, score);//, motifName);

                            sequenceIdentifier.addMatch(match);
                        }
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return new HashSet<RawProtein<PrintsRawMatch>>(rawResults.values());
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
