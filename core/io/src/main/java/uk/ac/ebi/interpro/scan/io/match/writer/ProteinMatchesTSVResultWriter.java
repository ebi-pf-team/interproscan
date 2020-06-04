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
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchesTSVResultWriter extends ProteinMatchesResultWriter {

    private TSVWriter tsvWriter;
    private String prefix = "G3DSA:";
    private Map<String, String> familyRecords;

    public ProteinMatchesTSVResultWriter(Path path, boolean proteinSequence) throws IOException {
        super(path);
        this.proteinSequence = proteinSequence;
        this.tsvWriter = new TSVWriter(super.fileWriter);
        familyRecords = parseCathFamilyFile();
        System.out.println("familyRecords: # " + familyRecords.keySet().size());
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


        for (String proteinAc : proteinAcs) {
//            Utilities.verboseLog(1100, "sequence mapping: " + proteinAc + " -> " + protein.getId() + "  length: " +  protein.getSequenceLength() ) ;

            for (Match match : matches) {
//                Utilities.verboseLog(1100, "print-match: " + match);
                final Signature signature = match.getSignature();
                final String signatureAc = signature.getAccession();
                final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
                final String analysis = signatureLibrary.getName();
                final String description = signature.getDescription();
                String signatureName = signature.getName();
                if (signatureAc.contains(prefix)){
                    signatureName = familyRecords.get(signatureAc);
                }

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
                            score = Double.toString(((SuperFamilyHmmer3Match) match).getEvalue());
                        } else if (match instanceof PantherMatch) {
                            score = Double.toString(((PantherMatch) match).getEvalue());
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

                        if (displayDescription == null || displayDescription.isBlank() || displayDescription.contains("FAMILY NOT NAMED")) {
                            displayDescription = "-";
                        }
                        if (displayDescription.equals("-")) {
                            System.out.println("no description found for " + signatureAc);
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
                                    List<GoXref> goXRefs = new ArrayList<>(interProEntry.getGoXRefs());
                                    Collections.sort(goXRefs, new GoXrefComparator());
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


    public Map<String, String> parseCathFamilyFile() throws IOException {


        final Map<String, String> records = new HashMap<>();

        // Some example lines to parse:
        // 3.30.56.60      "XkdW-like"
        //3.40.1390.30    "NIF3 (NGG1p interacting factor 3)-like"
        //3.90.330.10     "Nitrile Hydratase; Chain A"
        String cathFamilyFile = "data/gene3d/4.2.0/cath-family-names.txt";

        BufferedReader reader = null;
        try {
            FileInputStream familyFile = new FileInputStream(cathFamilyFile);
            reader = new BufferedReader(new InputStreamReader(familyFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\\s+", 2);
//                if (splitLine[0].isEmpty()) {
//                    throw new IllegalStateException("Unexpected format on line: " + line + " \n " + splitLine.toString());
//                }

                String accession = splitLine[0];
                String newAccession = prefix + accession;
                String familyName = "";
                if(splitLine.length < 2 || splitLine[1].contains("-")){
                    familyName = "";
                }else {
                    familyName = splitLine[1];
                }

                records.put(newAccession, familyName);  // model#accession, name
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return records;
    }
}
