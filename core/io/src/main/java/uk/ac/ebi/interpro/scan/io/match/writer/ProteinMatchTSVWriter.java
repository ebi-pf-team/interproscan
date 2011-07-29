package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.TSVWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Write matches as output for InterProScan user.
 *
 * @author ?, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ProteinMatchTSVWriter {


    private TSVWriter tsvWriter;

    DateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
    private boolean mapToInterProEntries;
    private boolean mapToGO;
    private boolean mapToPathway;

    public ProteinMatchTSVWriter(File file) throws IOException {
        tsvWriter = new TSVWriter(new BufferedWriter(new FileWriter(file)));
    }


    public void write(Protein protein) throws IOException {

        String proteinAc = makeProteinAc(protein);
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
                mappingFields.add(description);
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
                        if (mapToPathway) {
                            Collection<PathwayXref> pathwayXRefs = interProEntry.getPathwayXRefs();
                            if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
                                StringBuffer sb = new StringBuffer();
                                for (PathwayXref xref : pathwayXRefs) {
                                    if (sb.length() > 0) {
                                        sb.append(", ");
                                    }
                                    sb.append(PathwayXref.PathwayDatabase.parseDatabaseCode(xref.getDatabaseName().charAt(0)) + ": " + xref.getIdentifier());
                                }
                                mappingFields.add(sb.toString());
                            } else {
                                mappingFields.add("N/A");
                            }
                        }
                        if (mapToGO) {
                            Collection<GoXref> goXRefs = interProEntry.getGoXRefs();
                            if (goXRefs != null && goXRefs.size() > 0) {
                                StringBuffer sb = new StringBuffer();
                                for (GoXref xref : goXRefs) {
                                    if (sb.length() > 0) {
                                        sb.append(", ");
                                    }
                                    sb.append(xref.getIdentifier()); // Just write the GO identifier to the output
                                }
                                mappingFields.add(sb.toString());
                            }
                        }
                    }
                }

                tsvWriter.write(mappingFields);
            }
        }

    }

    public void setMapToInterProEntries(boolean mapToInterProEntries) {
        this.mapToInterProEntries = mapToInterProEntries;
    }

    public void setMapToGo(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    public void setMapToPathway(boolean mapToPathway) {
        this.mapToPathway = mapToPathway;
    }

    private String makeProteinAc(Protein protein) {
        StringBuilder proteinXRef = new StringBuilder();
        Set<ProteinXref> crossReferences = protein.getCrossReferences();
        for (ProteinXref crossReference : crossReferences) {
            if (proteinXRef.length() > 0) proteinXRef.append("|");
            proteinXRef.append(crossReference.getIdentifier());
        }
        return proteinXRef.toString();
    }

    public void close() throws IOException {
        tsvWriter.close();
    }
}
