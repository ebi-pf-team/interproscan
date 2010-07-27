package uk.ac.ebi.interpro.scan.io.match;

import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.ProSiteProfileRawMatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ProSite Profiles match parser.
 * TODO: This can be generalised to parse ProSite Pattern and HAMAP files.
 *
 * @author Manjula Thimma
 * @author Antony Quinn
 * @version $Id$
 */
public class ProSiteProfileMatchParser extends AbstractLineMatchParser<ProSiteProfileRawMatch> {

    /**
     * example input line for Profile output file
     * tr|A4QLD7|A4QLD7_LEPVR  ps_scan|v1.57   PS51003 65      160     16.129  .
     * .       Name "CYTB_CTER" ; Level 0 ; RawScore 1285 ; FeatureFrom 49 ; FeatureTo
     * -28 ; Sequence "------------------------------------------------PADPFATPLEILPEWY
     * FFPVFQILRTVPNKLLGVLLMVSVPAGLLTVPFLENVNKFQNPFrRPVATTVFLIGTA-VALWLGIGATLPIDKSLTLGL
     * F---------------------------" ; SequenceDescription "tr|A4QLD7|A4QLD7_LEPVR Cyto
     * chrome b6-f complex subunit 4 OS=Lepidium virginicum GN=petD PE=3 SV=1 " ; Known
     * FalsePos 0
     */
    private static final Pattern PROFILE_PATTERN = Pattern.compile(
            "^(\\S+)\\tps_scan\\|v[.0-9]+\\t(\\S+)\\t(\\d+)\\t(\\d+)\\t([.0-9]+)\\t\\.\\t\\.\\t(.+)"
    );

    public ProSiteProfileMatchParser(SignatureLibrary signatureLibrary, String signatureLibraryRelease) {
        super(signatureLibrary, signatureLibraryRelease);
    }

    @Override
    protected ProSiteProfileRawMatch createMatch(SignatureLibrary signatureLibrary,
                                                 String signatureLibraryRelease,
                                                 String line) {
        Matcher matcher = PROFILE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            //throw new IllegalStateException("Unrecognised line: " + line);
            return null;
        }
        String[] tokens = matcher.group(1).split("\\|");
        String accession = tokens[1].intern();
        String method_ac = matcher.group(2);
        int seqStart = Integer.parseInt(matcher.group(3));
        int seqStop = Integer.parseInt(matcher.group(4));
        double score = Double.parseDouble(matcher.group(5));
        String psDesc = matcher.group(6);
        // TODO - the null is the cigar format alignment - needs to be added.
        return new ProSiteProfileRawMatch(accession, method_ac,
                signatureLibraryRelease, seqStart, seqStop, null, score);
    }

}
