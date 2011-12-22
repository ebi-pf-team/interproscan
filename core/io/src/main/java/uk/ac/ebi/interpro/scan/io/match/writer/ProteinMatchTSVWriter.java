package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Write matches as output for InterProScan user.
 *
 * @author David Binns, EMBL-EBI, InterPro
 * @author Phil Jones, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchTSVWriter extends ProteinMatchesResultWriter {

    private TSVWriter tsvWriter;

    public ProteinMatchTSVWriter(File file) throws IOException {
        super(file);
        this.tsvWriter = new TSVWriter(super.fileWriter);
    }


    /**
     * Writes out a Protein object to a TSV file
     *
     * @param protein containing matches to be written out
     * @return the number of rows printed (i.e. the number of Locations on Matches).
     * @throws IOException in the event of I/O problem writing out the file.
     */
    public int write(Protein protein) throws IOException {
        int locationCount = 0;
        String proteinAc = getProteinAccession(protein);
        int length = protein.getSequence().length();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();
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
                    String score = "-";
                    String status = "T";

                    if (location instanceof HmmerLocation) {
                        score = Double.toString(((HmmerLocation) location).getEvalue());
                    }

                    final List<String> mappingFields = new ArrayList<String>();
                    mappingFields.add(proteinAc);
                    mappingFields.add(md5);
                    mappingFields.add(Integer.toString(length));
                    mappingFields.add(analysis);
                    mappingFields.add(signatureAc);
                    mappingFields.add((description == null ? "" : description));
                    mappingFields.add(Integer.toString(location.getStart()));
                    mappingFields.add(Integer.toString(location.getEnd()));
                    mappingFields.add(score);
                    mappingFields.add(status);
                    mappingFields.add(date);

                    if (mapToInterProEntries) {
                        Entry interProEntry = signature.getEntry();
                        if (interProEntry != null) {
                            mappingFields.add(interProEntry.getAccession());
                            mappingFields.add(interProEntry.getDescription());
                            if (mapToGO) {
                                Collection<GoXref> goXRefs = interProEntry.getGoXRefs();
                                if (goXRefs != null && goXRefs.size() > 0) {
                                    StringBuffer sb = new StringBuffer();
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
                            if (mapToPathway) {
                                Collection<PathwayXref> pathwayXRefs = interProEntry.getPathwayXRefs();
                                if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
                                    StringBuffer sb = new StringBuffer();
                                    for (PathwayXref xref : pathwayXRefs) {
                                        if (sb.length() > 0) {
                                            sb.append(VALUE_SEPARATOR);
                                        }
                                        sb.append(xref.getDatabaseName() + ": " + xref.getIdentifier());
                                    }
                                    mappingFields.add(sb.toString());
                                } else {
                                    mappingFields.add("");
                                }
                            }
                        }
                    }
                    this.tsvWriter.write(mappingFields);
                }
            }
        }
        return locationCount;
    }
}