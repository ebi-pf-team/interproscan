package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.nio.file.Path;
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
public class ProteinMatchesTSVResultWriter extends ProteinMatchesResultWriter {

    private TSVWriter tsvWriter;

    public ProteinMatchesTSVResultWriter(Path path) throws IOException {
        super(path);
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
        List<String> proteinAcs = getProteinAccessions(protein);
        int length = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();
        for (String proteinAc: proteinAcs) {
            for (Match match : matches) {
//                Utilities.verboseLog("print-match: " + match);
                final Signature signature = match.getSignature();
                final String signatureAc = signature.getAccession();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = signatureLibrary.getName();
                final String description = signature.getDescription();

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
                        } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
                            score = Double.toString( ((RPSBlastMatch.RPSBlastLocation) location).getEvalue() );
                        }

                        final List<String> mappingFields = new ArrayList<>();
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
                                    List<GoXref> goXRefs = new ArrayList<>(interProEntry.getGoXRefs());
                                    Collections.sort(goXRefs,  new GoXrefComparator());
                                    if (goXRefs.size() > 0) {
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
                                if (mapToPathway) {
                                    List<PathwayXref> pathwayXRefs = new ArrayList<>(interProEntry.getPathwayXRefs());
                                    Collections.sort(pathwayXRefs, new PathwayXrefComparator());
                                    if (pathwayXRefs.size() > 0) {
                                        StringBuilder sb = new StringBuilder();
                                        for (PathwayXref xref : pathwayXRefs) {
                                            if (sb.length() > 0) {
                                                sb.append(VALUE_SEPARATOR);
                                            }
                                            sb.append(xref.getDatabaseName())
                                                    .append(": ")
                                                    .append(xref.getIdentifier());
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
        }
        return locationCount;
    }
}
