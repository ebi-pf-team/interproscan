package uk.ac.ebi.interpro.scan.io.superfamily.match;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Parse a SuperFamily results file.</p>
 * <p>For example:</p>
 * <pre>
 * 1	0038302	256-433,536-611,650-802,930-1146	8.13e-63	33	ELLKLLNDEYNYPQLTEDILKEIstrvfNAKDTTGPKAISNFLIKLSELSPGIMLRQMNLVITLLNNSSITLRCSVVEACGNIvaelaQDPQTMEhykqqiaVLIELLEERFQDSNPYVRTKAIQGCSKICDLSSKFNKSkAKFTSLAVRSLQDRSSLVRRNSVKLLSKLLLKHPFKAihgsqlrlseweeylkgsesqlnstlkkvesqetlndtierslieeeveqdegqcrtelegsfnksaelsrieneveninatntsvlmklklmivyykdaisFIKEIHKSIELISNLLFSKNRNEVLESMDFLVLADAFDIELSEFGIKKMLHLVwmkgtNDEGTSISVHLIECYKQLfltapdscnmqekaahiaknlinlsigasiadlasleqLLGMMYEQKLIDqHVINILWAIYNSAS---KasmqkeqnvnnrdsekgfskEQIHGSIIILGMLSLADNEIALKGLEsLLNIGlgavGLKDLTLCRYSCLALERMVpkrstiitkaiNQELEDVAVKKLYAIIinytkDNEYYPMCEQALSALFTIsskpdilatdlirektmmtfgkpeeedsilsleqssrvvslsqllfivgqvaiktlvylekceaefkkrkieaetrngkvknqgadvtnttqdnggdkelemiggtneddftdaiqfvkenellfgeKSILGKFCPIVEEIVSNSSRFSDPMLQRTATLCLEKLMCLSSKYCEKSLPLLITVMEKSPDPTIRSNAVLGLGDMAVCFNNlVDENTDYLYRRLHDENLMVQRTCLMTVTFLILAGQVKVKGQLGEM-AKCLDNPDQ-GISDMCRLFFTELASKDNAIYNGFIDIFSNLssddllgkesfkkiiKFLLTFIDKERHQKQLNEklVGRLRKCETQKQWDD	0.013	70630	74771
 * 2	0042771	1719-2041,2074-2147	1.28e-09	244	LLTNLSESVKAKIPL-LLHMSICLLDHYVPLIHESACKIASTLIFgLAPSHEKS------------------------------------------EETvkLLRNKHALWSYDNLMKKgaRSPKTMDLLIRNIISIFSDL---------------------DE---------------------FqvtwqrialkwattcsVRHIACRSFQIFRSLLTFLDQ--EMLRDMLHRLSNTISDgNVDIQGFAMQILMTLNAIMAELD-------------PTNLisFPQLFWSITACL---SSIHEQEFIEVLSCLSKFISKIDLDSPDTVQCLVAIFPSNWEGR------------------------------------------FDGLQQIVMTGLRS----ANSLEITWKFLDKLNLLKDSRIIANTESrllfaliaNLPRFLNAMD-RKDFTGIQVAADSLIELANAYKQPslsrlidslaknkfrskkdfmsqvvsfisrnyFPSYSAQTLVFLLGLLFNKIG--WIRVQTLEILKYVFPLIDlrrPEFIGVGADLISPLLRLLFTEYEAKALEVLDC	0.080	19135	48385
 * 2	0042771	884-973,1159-1321,1348-1420,1457-1668,1707-1763	1.41e-09	38	ECWLEEFQSSNKEENKKETGLDGIRLLPIDAEQEESNETEKLEWKNTVTVIEEVEGNGLFFLCSHDAKIRRlGIQILRIIFKFDEAMMEKteklsnghsrssshfaadrgtrlidllnecntttlinphkatlsavektrfsrlnskykrglliklaeseygvdaalwqrafpkllalvfktcpmamalcrsivcirlvqvheiilrvandvdfklknvlpetivnqwklyliaactsltstfdqklhipsnipqhgrkksqqiftvqhqkiksaKSIFKMVLPLLNAKY--IMIRDAIITGLSSMNINIFKAYVEA-----IDVFLVAWKEGSSNNQIRVEMFHILTILSPYLKSDmifndEWILRKLSEFLQKTKQFLEKDSVQI------------SYEYQSLRSYFAGLILSYYMAVREHPlidelfPFQARASCFNFLKEWCGyGEYEPISEeryaimikntesgrdrtaittgiefqK----------NRLQMIVLETMVVLCSDPITQTLDDDLELPivisfD-------------------------TEDLLAWIEALFDSDNTTVKNLGVRALENLLDKNREnfklfrdvafqcvshhshpsvavlyyttlcksvlklDNLVLDEDELVSLGLYGLVADKEDTRTFAVDLLSAVETKLHN--------SSYTKVFKERLANsSKTVYKSTAKEISSIFAELLSQDLC----------------LRIFSSLVRIL---DLFPFEIKRDLLVLMVPWVNKFTLKSLEELDtfMVLNNLFYIT---------------IDLNDSLPNEVEQLWISLGKGNSFQNIHVSLEYIINSSMNHCNP-----LFVQYARDIVLYLANIPGG------IGLLDTLLNNLE---PKymvplakhtfnepmnnnkysflgniwerlnyngkriifSKAQLSIIFLVNLLTNLSESVKAKIPLLLHMSICLLDHYVP--LIHESACKIASTLIFG	0.090	70629	74771
 * 3	-	-	-	-	-	-	-	-
 * 4	0046054	152-300,381-484	3.24e-51	1	RPNCLVVDIGHDTCSVSPIVDGMTLSKSTRRNFIAGKFINHLIKKALEPKEIiplfaikqrkpefikktfdyevdksLYDYANNRGFFQECKETLCHICPTKTLEETKTELSSTAKRSIESPWNEEIVFdNETRYGFAEELFLPKEDDIpanwprsnsgvvktwrndyvplkrtkpsgvnksdkkvtpteekeqeavskstspaansadtpnetgkrpleeekppkennELIGLADLVYSSIMSSDVDLRATLAHNVVLTGGTSSIPGLSDRLMTELNKILPSL-KFRILTTGhtiERQYQSWLGGSILTSLGTFHQLWVGKKEYEEVGVERLL	0.00021	33430	53068
 * 4	0039962	12-158	1.04e-37	1	DEVSAVVIDPGSYTTNIGYSGSDFPQSILPSVYGKYTADEG-------------NKKIFSEQSIGiPRKDYELKPIIENGLVIDWDTAQEQWQWALQNELYLNSNsGIPALLTEPVWNSTENRKKSLEVLLEGMQFEACYLAPTSTCVSFAAGRPNCLVV	0.00085	33443	53068
 * 5	0037019	48-331	6.15e-40	12	DRPFQKLVFVIIDALRSDFLFDS-QISHFNNVHQWLNTGEAWGytSFANPPTVTLPRLKSITTGSTP--------SFIDLLLNVAQDIDSNDLSEHDSWLQQFIQHNNTIRFM----------GDDTWLKLFPQQWFD----FADPTHS--------FFVSDFTQVDNNVTRNLPGKLFQEWaqWDVaIlHYLGLDHIGHKDGPHSKFMAAKHQEMDSILKSIYDevleheddDDTLICVLGDHGMNEL----------------------------------------------------------------------------------------------------------------------GNHGGSSagETSAGLLFLSPKLAqfarpesqvnytlpinaspdwNFqYLETVQQIDIVPTIAALFG	0.083	90443	102651
 * </pre>
 * <p>Where output format:</p>
 * <ul>
 * <li>Sequence ID</li>
 * <li>SUPERFAMILY model ID</li>
 * <li>Match region</li>
 * <li>Evalue score</li>
 * <li>Model match start position</li>
 * <li>Alignment to model</li>
 * <li>Family evalue</li>
 * <li>SCOP domain ID of closest structure (px value)</li>
 * <li>SCOP Family ID</li>
 * </p>
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyHmmer3MatchParser {

    private static final Logger LOGGER = Logger.getLogger(SuperFamilyHmmer3MatchParser.class.getName());

    private static final Pattern MATCH_REGIONS_PATTERN = Pattern.compile("^\\d+\\-\\d+(\\,\\d+\\-\\d+)*$"); // E.g. "39-245,316-411"

    /**
     * Parse the temporary file.
     * If a line cannot be parsed (not the expected format) the line is ignored (but is logged).
     *
     * @param is The input stream to parse
     * @return The set of raw protein objects described within the file
     * @throws java.io.IOException
     */
    public Set<RawProtein<SuperFamilyHmmer3RawMatch>> parse(InputStream is) throws IOException {

        Map<String, RawProtein<SuperFamilyHmmer3RawMatch>> data = new HashMap<String, RawProtein<SuperFamilyHmmer3RawMatch>>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                Set<SuperFamilyHmmer3RawMatch> rawMatches = parseLine(line);
                SuperFamilyHmmer3RawMatch rawMatch;
                for (SuperFamilyHmmer3RawMatch rawMatche : rawMatches) {
                    rawMatch = rawMatche;
                    String sequenceId = rawMatch.getSequenceIdentifier();
                    if (data.containsKey(sequenceId)) {
                        RawProtein<SuperFamilyHmmer3RawMatch> rawProtein = data.get(sequenceId);
                        rawProtein.addMatch(rawMatch);
                    } else {
                        RawProtein<SuperFamilyHmmer3RawMatch> rawProtein = new RawProtein<SuperFamilyHmmer3RawMatch>(sequenceId);
                        rawProtein.addMatch(rawMatch);
                        data.put(sequenceId, rawProtein);
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return new HashSet<RawProtein<SuperFamilyHmmer3RawMatch>>(data.values());
    }


    /**
     * Create raw matches from this line of text from the binary output.
     * Multiple start/stop locations will cause multiple raw match objects to be created.
     * Any parsing issues with the line will cause a warning message to be logged and an empty or incomplete collection
     * to be returned.
     *
     * @param line Line read from input file.   @return {@link uk.ac.ebi.interpro.scan.model.raw.RawMatch} instance using values from parameters
     * @return Raw matches
     */
    private Set<SuperFamilyHmmer3RawMatch> parseLine(String line) {

        Set<SuperFamilyHmmer3RawMatch> rawMatches = new HashSet<SuperFamilyHmmer3RawMatch>();

        if (line == null || line.equals("")) {
            LOGGER.warn("Ignoring null or empty line!");
            return rawMatches;
        }

        String token;

        String sequenceId = null;
        String modelId = null;
        String matchRegions = null;
        double evalue = 0.0;
        int modelMatchStartPos = 0;
        String aligmentToModel = null;
        double familyEvalue = 0.0;
        int scopDomainId = 0;
        int scopFamilyId = 0;

        line = line.trim();
        String[] values = line.split("\\s+");
        if (values.length == 9) {
            int i = 0;
            while (i < values.length) {

                token = values[i];

                if (i == 1 && token.equals("-")) {
                    // This is OK, it just means that there are no matches for this sequence Id
                    // E.g. the line could be "3	-	-	-	-	-	-	-	-"
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("No SuperFamily matches found for sequence Id: " + sequenceId);
                    }
                    return rawMatches; // Empty
                }

                try {

                    switch (i) {
                        case 0:
                            sequenceId = token;
                            break;
                        case 1:
                            modelId = token;
                            break;
                        case 2:
                            matchRegions = token;
                            break;
                        case 3:
                            evalue = Double.parseDouble(token);
                            break;
                        case 4:
                            modelMatchStartPos = Integer.parseInt(token);
                            break;
                        case 5:
                            aligmentToModel = token;
                            break;
                        case 6:
                            familyEvalue = Double.parseDouble(token);
                            break;
                        case 7:
                            scopDomainId = Integer.parseInt(token);
                            break;
                        case 8:
                            scopFamilyId = Integer.parseInt(token);
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error("Error parsing SuperFamily match output file line (ignoring): " + line + " - Exception " + e.getMessage());
                    return rawMatches; // Empty
                }

                i++;
            }
        } else {
            // Wrong number of items in the output
            LOGGER.warn("Ignoring line with unexpected format: " + line);
            return rawMatches; // Empty
        }

        // Now parse the comma separated list of start/stop positions, e.g. "39-245,316-411" and prepare raw matches

        Matcher matchRegionMatcher = MATCH_REGIONS_PATTERN.matcher(matchRegions);
        if (matchRegionMatcher.find()) {
            final UUID splitGroup = UUID.randomUUID(); // Ensures that locations that are split from the
            // same match are tied together with the same "splitGroup".
            String[] matchRegionArray = matchRegions.split(",");
            for (String aMatchRegionArray : matchRegionArray) {
                String[] matchStartStop = aMatchRegionArray.split("-");
                if (matchStartStop.length == 2) {
                    int from;
                    int to;
                    try {
                        from = Integer.parseInt(matchStartStop[0]);
                        to = Integer.parseInt(matchStartStop[1]);
                    } catch (NumberFormatException e) {
                        LOGGER.warn("Ignoring line with unexpected format (of match region): " + line);
                        continue; // Raw matches will therefore be empty or incomplete
                    }
                    SuperFamilyHmmer3RawMatch match = new SuperFamilyHmmer3RawMatch(sequenceId, modelId,
                            "1.75", from, to, evalue, modelMatchStartPos,
                            aligmentToModel, familyEvalue, scopDomainId, scopFamilyId, splitGroup);
                    rawMatches.add(match);
                } else {
                    LOGGER.warn("Ignoring line with unexpected format (of match region): " + line);
                }
            }
        } else {
            // Invalid format
            LOGGER.warn("Ignoring line with unexpected format (of match region): " + line);
            return rawMatches; // Empty
        }

        return rawMatches; // Not empty - successfully parsed and therefore complete!
    }

}
