package uk.ac.ebi.interpro.scan.io.match;

import org.apache.log4j.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

/**
 * Parser for the output from Prodom
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
public class ProdomMatchParser implements MatchParser {

    /**
     * Logger for Junit logging. Log messages will be associated with the ProdomParser class.
     */
    private static Logger LOGGER = Logger.getLogger(ProdomMatchParser.class);


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

    public Set<RawProtein<ProDomRawMatch>> parse(InputStream is) throws IOException {
        Set<RawProtein<ProDomRawMatch>> seqIds = new HashSet<RawProtein<ProDomRawMatch>>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            while (reader.ready()){
                String line = reader.readLine();
                Matcher matcher = PRODOM_LINE_PATTERN.matcher(line);
                if (matcher.matches()){
                    LOGGER.debug("Match: " + line);
                    String accession = matcher.group(1);
                    int seqStart = Integer.parseInt(matcher.group(2));
                    int seqStop = Integer.parseInt(matcher.group(3));
                    String prodomAccesion = matcher.group(4);
                    int sigStart = Integer.parseInt(matcher.group(5));
                    int sigStop = Integer.parseInt(matcher.group(6));
                    double score = Double.parseDouble(matcher.group(7));
                    double eValue = Double.parseDouble(matcher.group(11));
                    int bracketedValue = Integer.parseInt(matcher.group(15));

                    if (LOGGER.isDebugEnabled()){
                        LOGGER.debug("RowData: " +
                            "  accession: " +           accession              +
                            "  seqStart: " +            seqStart               +
                            "  seqStop: " +             seqStop                +
                            "  prodomAccesion: " +      prodomAccesion         +
                            "  sigStart: " +            sigStart               +
                            "  sigStop: " +             sigStop                +
                            "  score: " +               score                  +
                            "  eValue: " +              eValue                 +
                            "  bracketedValue: " +      bracketedValue
                        );
                    }

                    // TODO - Not finished - currently don't seem to be adding any RawMatch objects to the RawProtein.
                    
                }
                else {
                    LOGGER.debug("NO MATCH: " + line);
                }
            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }

        return seqIds;
    }
}
