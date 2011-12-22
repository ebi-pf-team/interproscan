package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Write matches as output in GFF (Generic Feature Format) version 3.
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GFFResultWriterForNucSeqs extends ProteinMatchesGFFResultWriter {


    public GFFResultWriterForNucSeqs(File file) throws IOException {
        super(file);
    }


    /**
     * Writes out a Protein object to a GFF version 3 file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws java.io.IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
//        List<String> columns = new ArrayList<String>();
//        tsvWriter.writeComment(StringUtils.collectionToDelimitedString(columns, "\t"));
//        tsvWriter.writeComment("\n");
        //Get protein accession
        String proteinAc = getProteinAccession(protein);
        Set<Match> matches = protein.getMatches();
        //Write sequence region information
        if (matches.size() > 0) {
            super.gffWriter.write("##sequence-region " + proteinAc + " 1 " + protein.getSequenceLength() + "\n");
            writeReferenceLine(proteinAc);
        }
        for (Match match : matches) {
            final Signature signature = match.getSignature();
            final String signatureAc = signature.getAccession();
            final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
            final String analysis = signatureLibrary.getName();
            final String description = match.getSignature().getDescription();

            Set<Location> locations = match.getLocations();
            if (locations != null) {
                for (Location location : locations) {
                    String score = "-";
                    String status = "T";

                    if (location instanceof HmmerLocation) {
                        score = Double.toString(((HmmerLocation) location).getEvalue());
                    }

                    final List<String> gffFeature = new ArrayList<String>();
                    gffFeature.add(proteinAc);
                    gffFeature.add(".");

//                    gffFeature.add(md5);
//                    mappingFields.add(Integer.toString(length));
//                    mappingFields.add(analysis);
//                    mappingFields.add(signatureAc);
//                    mappingFields.add((description == null ? "" : description));
//                    mappingFields.add(Integer.toString(location.getStart()));
//                    mappingFields.add(Integer.toString(location.getEnd()));
//                    mappingFields.add(score);
//                    mappingFields.add(status);
//                    mappingFields.add(date);
//
//                    if (mapToInterProEntries) {
//                        Entry interProEntry = signature.getEntry();
//                        if (interProEntry != null) {
//                            mappingFields.add(interProEntry.getAccession());
//                            mappingFields.add(interProEntry.getDescription());
//                            if (mapToGO) {
//                                Collection<GoXref> goXRefs = interProEntry.getGoXRefs();
//                                if (goXRefs != null && goXRefs.size() > 0) {
//                                    StringBuffer sb = new StringBuffer();
//                                    for (GoXref xref : goXRefs) {
//                                        if (sb.length() > 0) {
//                                            sb.append(VALUE_SEPARATOR);
//                                        }
//                                        sb.append(xref.getIdentifier()); // Just writeComment the GO identifier to the output
//                                    }
//                                    mappingFields.add(sb.toString());
//                                } else {
//                                    mappingFields.add("");
//                                }
//                            }
//                            if (mapToPathway) {
//                                Collection<PathwayXref> pathwayXRefs = interProEntry.getPathwayXRefs();
//                                if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
//                                    StringBuffer sb = new StringBuffer();
//                                    for (PathwayXref xref : pathwayXRefs) {
//                                        if (sb.length() > 0) {
//                                            sb.append(VALUE_SEPARATOR);
//                                        }
//                                        sb.append(xref.getDatabaseName() + ": " + xref.getIdentifier());
//                                    }
//                                    mappingFields.add(sb.toString());
//                                } else {
//                                    mappingFields.add("");
//                                }
//                            }
//                        }
//                    }
//                    tsvWriter.writeComment(mappingFields);
                }
            }
        }
        return 0;
    }

    private void writeReferenceLine(String accession) {
        final List<String> referenceLine = new ArrayList<String>();
        referenceLine.add(accession);
        referenceLine.add(".");
    }
}