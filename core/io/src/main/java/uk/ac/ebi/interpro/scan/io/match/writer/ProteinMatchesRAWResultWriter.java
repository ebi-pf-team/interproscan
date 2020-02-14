package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesRAWResultWriter extends ProteinMatchesResultWriter {

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesRAWResultWriter.class.getName());

    private TSVWriter tsvWriter;

    private static long _crc64Array [] = new long [256];

    /**
     * Initialization of _crc64Array.
     */
    static {

        for( int i = 0 ; i <= 255 ; ++i ) {
            long k = i;
            for( int j = 0 ; j < 8 ; ++j ) {
                if( (k & 1) != 0 ) {
                    k = (k >>> 1) ^ 0xd800000000000000L;
                }
                else {
                    k = k >>> 1;
                }
            }
            _crc64Array[ i ] = k;
        }
    }

    private static Map<String, String> analysisI5toI4 = new HashMap<>();
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put(SignatureLibrary.PRODOM.getName(), "BlastProDom");
        tempMap.put(SignatureLibrary.COILS.getName(), "Coils");
        tempMap.put(SignatureLibrary.PRINTS.getName(), "FPrintScan");
        tempMap.put(SignatureLibrary.GENE3D.getName(), "Gene3D");
        tempMap.put(SignatureLibrary.HAMAP.getName(), "HAMAP");
        tempMap.put(SignatureLibrary.PANTHER.getName(), "HMMPanther");
        tempMap.put(SignatureLibrary.PIRSF.getName(), "HMMPIR");
        tempMap.put(SignatureLibrary.SMART.getName(), "HMMSmart");
        tempMap.put(SignatureLibrary.TIGRFAM.getName(), "HMMTigr");
        tempMap.put(SignatureLibrary.PFAM.getName(), "HMMPfam");
        tempMap.put(SignatureLibrary.PROSITE_PATTERNS.getName(), "PatternScan");
        tempMap.put(SignatureLibrary.TMHMM.getName(), "TMHMM");
        tempMap.put(SignatureLibrary.PROSITE_PROFILES.getName(), "ProfileScan");
        tempMap.put(SignatureLibrary.SUPERFAMILY.getName(), "Superfamily"); // New SuperFamily version in I5, but results map cleanly to old so it's OK
        //tempMap.put(SignatureLibrary.SIGNALP_EUK.getName(), "SignalPHMM"); // New SignalP version does not map cleanly to old
        //tempMap.put(SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName(), "SignalPHMM"); // New SignalP version does not map cleanly to old
        //tempMap.put(SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName(), "SignalPHMM"); // New SignalP version does not map cleanly to old
        //tempMap.put(SignatureLibrary.PHOBIUS.getName(), ""); // Phobius was new in I5, doesn't exist in I4!

        analysisI5toI4 = Collections.unmodifiableMap(tempMap);
    }

    public ProteinMatchesRAWResultWriter(Path path) throws IOException {
        super(path);
        this.dmyFormat = new SimpleDateFormat("dd-MMM-yyyy"); // Override the superclasses default date format
        this.tsvWriter = new TSVWriter(super.fileWriter);
    }

    /**
     * Writes out a Protein object to an InterProScan 4 RAW (TSV) file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        int locationCount = 0;
        List<String> proteinAcs = getProteinAccessions(protein, proteinSequence);

        final int length = protein.getSequenceLength();
        final String sequence = protein.getSequence();
        final String crc64 = getCrc64(sequence);
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();
        for (String proteinAc: proteinAcs) {
            for (Match match : matches) {
                final Signature signature = match.getSignature();
                final String signatureAc = signature.getAccession();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = analysisI5toI4.get(signatureLibrary.getName());
                if (analysis == null) {
                    // Analysis invalid or not in the map, so don't include in the RAW output
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Skipping " + signatureAc + " match, RAW output does not support matches from analysis " + signatureLibrary.getName());
                    }
                    continue;
                }
                final String signatureName = signature.getName();

                Set<Location> locations = match.getLocations();
                if (locations != null) {
                    locationCount += locations.size();
                    for (Location location : locations) {
                        //Default score
                        String score = "-";
                        String status = "T";

                        // To maintain compatibility, we output the same value for the score column as I4
                        // In some cases we have to take the value from the match
                        if (match instanceof SuperFamilyHmmer3Match) {
                            score = Double.toString( ((SuperFamilyHmmer3Match) match).getEvalue());
                        } else if (match instanceof PantherMatch) {
                            score = Double.toString( ((PantherMatch) match).getEvalue());
                        } else if (match instanceof FingerPrintsMatch) {
                            score = Double.toString(((FingerPrintsMatch) match).getEvalue());
                        }
                        //In other cases we have to take the value from the location
                        if (location instanceof HmmerLocation) {
                            score = Double.toString(((HmmerLocation) location).getEvalue());
                        } else if (location instanceof BlastProDomMatch.BlastProDomLocation) {
                            score = Double.toString( ((BlastProDomMatch.BlastProDomLocation) location).getEvalue() );
                        }  else if (location instanceof ProfileScanMatch.ProfileScanLocation)  {
                            score = Double.toString( ((ProfileScanMatch.ProfileScanLocation) location).getScore() );
                        }

                        final List<String> mappingFields = new ArrayList<String>();
                        mappingFields.add(proteinAc); // The ID of the input sequence
                        mappingFields.add(crc64); // The crc64 (checksum) of the protein sequence (supposed to be unique)
                        mappingFields.add(Integer.toString(length)); // The length of the sequence (in AA)
                        mappingFields.add(analysis); // The analysis method launched
                        mappingFields.add(signatureAc); // The member database signature accession for this match
                        mappingFields.add((signatureName == null ? "" : signatureName)); // The member database short name for the signature
                        mappingFields.add(Integer.toString(location.getStart())); // The start of the domain match
                        mappingFields.add(Integer.toString(location.getEnd())); // The end of the domain match
                        mappingFields.add(score); // The score of the match (reported by member database method)
                        mappingFields.add(status); // The status of the match (T: true, ?: unknown)
                        mappingFields.add(date); // The date of the run

                        // START if (mapToInterProEntries)
                        final Entry interProEntry = signature.getEntry();
                        if (interProEntry != null) {
                            mappingFields.add(interProEntry.getAccession()); // The corresponding InterPro entry (if iprlookup requested by the user)
                            mappingFields.add(interProEntry.getDescription()); // The corresponding entry description (if iprlookup requested by the user)
                            // START if (mapToGO)
                            // Example GO term format:
                            // Molecular Function:prephenate dehydratase activity (GO:0004664), Biological Process:L-phenylalanine biosynthesis (GO:0009094)
                            List<GoXref> goXRefs = new ArrayList<>(interProEntry.getGoXRefs());
                            Collections.sort(goXRefs, new GoXrefComparator());
                            if (goXRefs.size() > 0) {
                                StringBuilder sb = new StringBuilder();
                                for (GoXref xref : goXRefs) {
                                    if (sb.length() > 0) {
                                        sb.append(", "); // GO term separator
                                    }
                                    final String category = xref.getCategory().getName();
                                    sb.append(category);
                                    sb.append(":");
                                    sb.append(xref.getName());
                                    sb.append(" (").append(xref.getIdentifier()).append(")");
                                }
                                mappingFields.add(sb.toString());
                            }
                            // ElseNo GO terms associated
                            // END if (mapToGO)
                        }
                        else {
                            mappingFields.add("NULL"); // No entry accession
                            mappingFields.add("NULL"); // No entry name
                        }
                        // END if (mapToInterProEntries)
                        this.tsvWriter.write(mappingFields);
                    }
                }
            }
        }
        return locationCount;
    }

    /**
     * Returns the crc64 checksum for the given sequence.
     *
     * @param sequence sequence
     * @return the crc64 checksum for the sequence
     */
    private static String getCrc64 ( String sequence ) {
        long crc64Number = 0;
        for( int i = 0; i < sequence.length(); ++i ) {
            char symbol = sequence.charAt( i );
            long a = ( crc64Number >>> 8 );
            long b = (crc64Number ^ symbol) & 0xff;
            crc64Number = a ^ _crc64Array[ (int) b ];
        }

        String crc64String = Long.toHexString( crc64Number ).toUpperCase();
        StringBuffer crc64 = new StringBuffer( "0000000000000000" );
        crc64.replace( crc64.length() - crc64String.length(),
                crc64.length(),
                crc64String );

        return crc64.toString();
    }

}
