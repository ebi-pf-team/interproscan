package uk.ac.ebi.interpro.scan.io.match.writer;

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

                        Map<GoXref,GoXrefSources> goXrefs = new HashMap<>();
                        List<PathwayXref> pathwayXrefs = new ArrayList<>();

                        // To maintain compatibility, we output the same value for the score column as I4
                        // In some cases we have to take the value from the match
                        if (match instanceof SuperFamilyHmmer3Match) {
                            score = Double.toString(((SuperFamilyHmmer3Match) match).getEvalue());
                        } else if (match instanceof PantherMatch) {
                            score = Double.toString(((PantherMatch) match).getEvalue());

                            for (GoXref xref : ((PantherMatch) match).getGoXRefs()) {
                                goXrefs.put(xref, new GoXrefSources(false, true));
                            }
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

                                for (GoXref xref : interProEntry.getGoXRefs()) {
                                    GoXrefSources sourcedXref = goXrefs.get(xref);
                                    if (sourcedXref != null) {
                                        sourcedXref.setInInterPro(true);
                                    } else {
                                        sourcedXref = new GoXrefSources(true, false);
                                        goXrefs.put(xref, sourcedXref);
                                    }
                                }

                                pathwayXrefs = new ArrayList<>(interProEntry.getPathwayXRefs());
                            } else {
                                mappingFields.add("-");
                                mappingFields.add("-");
                            }
                        } else {
                            mappingFields.add("-");
                            mappingFields.add("-");
                        }

                        if (mapToGO && !goXrefs.isEmpty()) {
                            List<GoXref> goXRefsList = new ArrayList<>(goXrefs.keySet());
                            goXRefsList.sort(new GoXrefComparator());
                            StringBuilder sb = new StringBuilder();
                            for (GoXref xref : goXRefsList) {
                                if (sb.length() > 0) {
                                    sb.append(VALUE_SEPARATOR);
                                }
                                sb.append(xref.getIdentifier());
                                GoXrefSources sourcedXref = goXrefs.get(xref);
                                sb.append(sourcedXref.toString());
                            }
                            mappingFields.add(sb.toString());
                        } else {
                            mappingFields.add("-");
                        }

                        if (mapToPathway && !pathwayXrefs.isEmpty()) {
                            pathwayXrefs.sort(new PathwayXrefComparator());
                            StringBuilder sb = new StringBuilder();
                            for (PathwayXref xref : pathwayXrefs) {
                                if (sb.length() > 0) {
                                    sb.append(VALUE_SEPARATOR);
                                }
                                sb.append(xref.getDatabaseName())
                                        .append(":")
                                        .append(xref.getIdentifier());
                            }
                            mappingFields.add(sb.toString());
                        } else {
                            mappingFields.add("-");
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

    private static class GoXrefSources {
        private boolean inInterPro;
        private boolean inPanther;

        public GoXrefSources(boolean inInterPro, boolean inPanther) {
            this.inInterPro = inInterPro;
            this.inPanther = inPanther;
        }

        public void setInInterPro(boolean inInterPro) {
            this.inInterPro = inInterPro;
        }

        public void setInPanther(boolean inPanther) {
            this.inPanther = inPanther;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();

            if (inInterPro && inPanther) {
                stringBuilder.append("(InterPro,PANTHER)");
            } else if (inInterPro) {
                stringBuilder.append("(InterPro)");
            } else if (inPanther) {
                stringBuilder.append("(PANTHER)");
            }

            return stringBuilder.toString();
        }
    }
}
