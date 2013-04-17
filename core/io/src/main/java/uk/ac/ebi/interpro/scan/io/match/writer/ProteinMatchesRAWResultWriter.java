package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
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
                    k = (k >>> 1) ^ 0xd800000000000000l;
                }
                else {
                    k = k >>> 1;
                }
            }
            _crc64Array[ i ] = k;
        }
    }

    public ProteinMatchesRAWResultWriter(File file) throws IOException {
        super(file);
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
        List<String> proteinAcs = getProteinAccessions(protein);

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
                final String analysis = signatureLibrary.getName();
                final String description = match.getSignature().getDescription();

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
                        mappingFields.add(signatureAc); // The database members entry for this match
                        mappingFields.add((description == null ? "" : description)); // The database member description for the entry
                        mappingFields.add(Integer.toString(location.getStart())); // The start of the domain match
                        mappingFields.add(Integer.toString(location.getEnd())); // The end of the domain match
                        mappingFields.add(score); // The score of the match (reported by member database method)
                        mappingFields.add(status); // The status of the match (T: true, ?: unknown)
                        mappingFields.add(date); // The date of the run

                        if (mapToInterProEntries) {
                            Entry interProEntry = signature.getEntry();
                            if (interProEntry != null) {
                                mappingFields.add(interProEntry.getAccession()); // The corresponding InterPro entry (if iprlookup requested by the user)
                                mappingFields.add(interProEntry.getDescription()); // The corresponding entry description (if iprlookup requested by the user)
                                if (mapToGO) {
                                    List<GoXref> goXRefs = new ArrayList<GoXref>(interProEntry.getGoXRefs());
                                    Collections.sort(goXRefs, new GoXrefComparator());
                                    if (goXRefs != null && goXRefs.size() > 0) {
                                        StringBuilder sb = new StringBuilder();
                                        for (GoXref xref : goXRefs) {
                                            if (sb.length() > 0) {
                                                sb.append(VALUE_SEPARATOR);
                                            }
                                            sb.append(xref.getIdentifier()); // Just writeComment the GO identifier to the output
                                        }
                                        mappingFields.add(sb.toString());
                                    } else {
                                        mappingFields.add("");
                                    }
                                }
//                                if (mapToPathway) {
//                                    List<PathwayXref> pathwayXRefs = new ArrayList<PathwayXref>(interProEntry.getPathwayXRefs());
//                                    Collections.sort(pathwayXRefs, new PathwayXrefComparator());
//                                    if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
//                                        StringBuilder sb = new StringBuilder();
//                                        for (PathwayXref xref : pathwayXRefs) {
//                                            if (sb.length() > 0) {
//                                                sb.append(VALUE_SEPARATOR);
//                                            }
//                                            sb
//                                                    .append(xref.getDatabaseName())
//                                                    .append(": ")
//                                                    .append(xref.getIdentifier());
//                                        }
//                                        mappingFields.add(sb.toString());
//                                    } else {
//                                        mappingFields.add("");
//                                    }
//                                }
                            }
                        }
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
