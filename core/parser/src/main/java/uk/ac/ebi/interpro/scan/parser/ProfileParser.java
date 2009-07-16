package uk.ac.ebi.interpro.scan.parser;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * Profile parser, for example ProSite and HAMAP output files.
 *
 * @author  Manjula Thimma
 * @version $Id$
 * @since   1.0
 */
public class ProfileParser implements Parser{
     /**
     * Logger for Junit logging. Log messages will be associated with the ProdomParser class.
     */
    private static Logger LOGGER = Logger.getLogger(ProfileParser.class);
    /**
     * example input line for Profile output file
     * tr|A4QLD7|A4QLD7_LEPVR  ps_scan|v1.57   PS51003 65      160     16.129  .
.       Name "CYTB_CTER" ; Level 0 ; RawScore 1285 ; FeatureFrom 49 ; FeatureTo
-28 ; Sequence "------------------------------------------------PADPFATPLEILPEWY
FFPVFQILRTVPNKLLGVLLMVSVPAGLLTVPFLENVNKFQNPFrRPVATTVFLIGTA-VALWLGIGATLPIDKSLTLGL
F---------------------------" ; SequenceDescription "tr|A4QLD7|A4QLD7_LEPVR Cyto
chrome b6-f complex subunit 4 OS=Lepidium virginicum GN=petD PE=3 SV=1 " ; Known
FalsePos 0

     */


    //private static final Pattern PROFILE_PATTERN = Pattern.compile(
      //      "^[a-z]{2}\\|(\\S+)\\|\\w+\\tps_scan\\|v[.0-9]+\\t(\\S+)\\t(\\d+)\\t(\\d+)\\t([.0-9]+)\\t\\.\\t\\.\\t(.+)"
    //);
    private static final Pattern PROFILE_PATTERN = Pattern.compile(
            "^(\\S+)\\tps_scan\\|v[.0-9]+\\t(\\S+)\\t(\\d+)\\t(\\d+)\\t([.0-9]+)\\t\\.\\t\\.\\t(.+)"
    );

    public Set<SequenceIdentifier> parse(InputStream is) throws IOException {
        Set<SequenceIdentifier> seqIds = new HashSet<SequenceIdentifier>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            while (reader.ready()){
                String line = reader.readLine();
                Matcher matcher = PROFILE_PATTERN.matcher(line);
                if (matcher.matches()){
                    LOGGER.debug("Match: " + line);
                    String[] tokens = matcher.group(1).split("\\|");
                    String accession = tokens[1].intern();
                    //String accession = matcher.group(1);
                    String method_ac = matcher.group(2);
                    int seqStart = Integer.parseInt(matcher.group(3));
                    int seqStop = Integer.parseInt(matcher.group(4));
                    double score = Double.parseDouble(matcher.group(5));
                    String psDesc = matcher.group(6);
                    if (LOGGER.isDebugEnabled()){
                        LOGGER.debug("RowData: " +
                            "  accession: " +           accession              +
                            "  method accession: " +     method_ac             +
                            "  from : " +            seqStart               +
                            "  to: " +             seqStop                +
                            "  score: " +               score                  +
                            "  description : " +        psDesc             
                        );
                    }

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
