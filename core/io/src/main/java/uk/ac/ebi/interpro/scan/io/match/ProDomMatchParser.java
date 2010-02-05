package uk.ac.ebi.interpro.scan.io.match;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;

/**
 * Parser for the output from Prodom
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
public class ProDomMatchParser extends AbstractLineMatchParser<ProDomRawMatch> {

    /**
     * Pattern to match a line from the raw Prodom output, such as:
     *
     * UPI0000009C90_status=active      4     61 //  pd_PD003417;sp_RL35_VIBPA_Q87Q69;       4     61 // S=296    E=3e-27  //  (157)  RIBOSOMAL L35 50S RIBONUCLEOPROTEIN CHLOROPLAST YNL122C SEQUENCING DIRECT CEREVISIAE P53921  	 Length = 58
     *
     * Groups:
     * 1 Protein Accession (as in Fasta File) (String)
     * 2 Sequence match start coordinate (Integer)
     * 3 Sequence match end coordinate (Integer)
     * 4 Prodom signature accession (String)
     * 5 Signature match start coordinate (Integer) TODO Check this is the correct assignment.
     * 6 Signature match stop coordinate (Integer) TODO Check this is the correct assignment.
     * 7 Score (Integer????) TODO Check this - currently using regex for any real number - may be able to simplify
     * 11 E value (Floating point - could be exponent)
     * 15 Integer value in brackets before signature description !? TODO - Find out what this is!  Is it an integer?
     *
     * This regex is currently not grouping the signature description or the "Length" value at the end of the line TODO - Check these are not required.
     */
    private static final Pattern PRODOM_LINE_PATTERN = Pattern.compile(
            "^(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+//\\s+pd_(.+?);.+?;\\s+(\\d+)\\s+(\\d+)\\s+//\\s+S=(((\\b[0-9]+)?\\.)?\\b[0-9]+([eE][-+]?[0-9]+)?\\b)\\s+E=(((\\b[0-9]+)?\\.)?\\b[0-9]+([eE][-+]?[0-9]+)?\\b)\\s+//\\s+\\((\\d+)\\)\\s+.+?Length = \\d+$"
    );

    public ProDomMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(signatureLibrary, signatureLibraryRelease);
    }

    @Override protected ProDomRawMatch createMatch(SignatureLibrary signatureLibrary,
                                                   String signatureLibraryRelease,
                                                   String line) {
        Matcher matcher = PRODOM_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            //throw new IllegalStateException("Unrecognised line: " + line);
            return null;
        }
        String sequenceIdentifier   = matcher.group(1);
        int sequenceStart           = Integer.parseInt(matcher.group(2));
        int sequenceEnd             = Integer.parseInt(matcher.group(3));
        String model                = matcher.group(4);
        int modelStart              = Integer.parseInt(matcher.group(5));
        int modelEnd                = Integer.parseInt(matcher.group(6));
        double score                = Double.parseDouble(matcher.group(7));
        double evalue               = Double.parseDouble(matcher.group(11));
        int unknown                 = Integer.parseInt(matcher.group(15));
        return new ProDomRawMatch(sequenceIdentifier, model, signatureLibraryRelease,
                                  sequenceStart, sequenceEnd, score);
    }


}
