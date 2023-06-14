package uk.ac.ebi.interpro.scan.io.match.writer;

import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.*;
import java.nio.file.Path;
import java.util.*;


/**
 * Write matches as output for InterProScan user.
 *
 * @author David Binns, EMBL-EBI, InterPro
 * @author Phil Jones, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Matthias Blum, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesTSVResultWriter extends ProteinMatchesResultWriter {

    private TSVWriter tsvWriter;

    public ProteinMatchesTSVResultWriter(Path path, boolean proteinSequence) throws IOException {
        super(path);
        this.proteinSequence = proteinSequence;
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
        List<String> proteinAcs = getProteinAccessions(protein, proteinSequence);
        int length = protein.getSequenceLength();
        String md5 = protein.getMd5();
        String date = dmyFormat.format(new Date());

        Set<Match> matches = protein.getMatches();

        Set<String> signaturesWithNoDescription = new HashSet<>();

        for (String proteinAc : proteinAcs) {
//            Utilities.verboseLog(1100, "sequence mapping: " + proteinAc + " -> " + protein.getId() + "  length: " +  protein.getSequenceLength() ) ;

            for (Match match : matches) {
//                Utilities.verboseLog(1100, "print-match: " + match);
                final Signature signature = match.getSignature();

                final String signatureAc = signature.getAccession();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = signatureLibrary.getName();
                //for PIRSR predicted regions without the corrresponding sites dont mean much
                if (signatureLibrary.getName().equals(SignatureLibrary.PIRSR.getName())){
                    continue;
                }
                final String description = signature.getDescription();
                String signatureName = signature.getName();

                Set<Location> locations = match.getLocations();
                if (locations != null) {
                    locationCount += locations.size();
                    for (Location location : locations) {
                        //Default score
                        String score = "-";
                        String status = "T";

                        Set<GoXref> goXrefs = new HashSet<>();

                        // To maintain compatibility, we output the same value for the score column as I4
                        // In some cases we have to take the value from the match
                        if (match instanceof SuperFamilyHmmer3Match) {
                            score = Double.toString(((SuperFamilyHmmer3Match) match).getEvalue());
                        } else if (match instanceof PantherMatch) {
                            score = Double.toString(((PantherMatch) match).getEvalue());
//                            goXrefs.addAll(((PantherMatch) match).getGoXRefs());
                        } else if (match instanceof FingerPrintsMatch) {
                            score = Double.toString(((FingerPrintsMatch) match).getEvalue());
                        }
                        //In other cases we have to take the value from the location
                        if (location instanceof HmmerLocation) {
                            score = Double.toString(((HmmerLocation) location).getEvalue());
                        } else if (location instanceof HmmerLocationWithSites) {
                            score = Double.toString(((HmmerLocationWithSites) location).getEvalue());
                        } else if (location instanceof BlastProDomMatch.BlastProDomLocation) {
                            score = Double.toString(((BlastProDomMatch.BlastProDomLocation) location).getEvalue());
                        } else if (location instanceof ProfileScanMatch.ProfileScanLocation) {
                            score = Double.toString(((ProfileScanMatch.ProfileScanLocation) location).getScore());
                        } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
                            score = Double.toString(((RPSBlastMatch.RPSBlastLocation) location).getEvalue());
                        }

                        final List<String> mappingFields = new ArrayList<>();
                        mappingFields.add(proteinAc);
                        mappingFields.add(md5);
                        mappingFields.add(Integer.toString(length));
                        mappingFields.add(analysis);
                        mappingFields.add(signatureAc);
                        String displayDescription = description == null ? signatureName : description;
                        //displayDescription = displayDescription == null ? altDescription : displayDescription;

                        //if (displayDescription == null || displayDescription.isBlank() || displayDescription.contains("FAMILY NOT NAMED")) { revert ot Blank
                        if (displayDescription == null || displayDescription.isEmpty() || displayDescription.contains("FAMILY NOT NAMED")) {
                            displayDescription = "-";
                        }
                        if (displayDescription.equals("-")) {
                            signaturesWithNoDescription.add(signatureAc);
                        }
                        mappingFields.add(displayDescription);
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
                                    goXrefs.addAll(interProEntry.getGoXRefs());
                                    List<GoXref> goXRefsList = new ArrayList<>(goXrefs);
                                    Collections.sort(goXRefsList, new GoXrefComparator());
                                    if (goXRefsList.size() > 0) {
                                        StringBuilder sb = new StringBuilder();
                                        for (GoXref xref : goXRefsList) {
                                            if (sb.length() > 0) {
                                                sb.append(VALUE_SEPARATOR);
                                            }
                                            sb.append(xref.getIdentifier()); // Just writeComment the GO identifier to the output
                                        }
                                        mappingFields.add(sb.toString());
                                    } else {
                                        mappingFields.add("-");
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
                                        mappingFields.add("-");
                                    }
                                }
                            } else {
                                mappingFields.add("-"); // for accession
                                mappingFields.add("-"); // for description
                            }
                        }
                        this.tsvWriter.write(mappingFields);
                    }
                }
            }
        }
        Utilities.verboseLog(120, "signatures without descriptions: " + signaturesWithNoDescription.size());
        if (Utilities.verboseLogLevel >= 120) {
            for (String signatureWithNoDescription : signaturesWithNoDescription) {
                Utilities.verboseLog(120, signatureWithNoDescription);
            }
        }
        return locationCount;
    }
}
