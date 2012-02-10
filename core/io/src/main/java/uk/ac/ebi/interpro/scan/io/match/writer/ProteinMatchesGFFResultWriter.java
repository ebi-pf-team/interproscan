package uk.ac.ebi.interpro.scan.io.match.writer;

import uk.ac.ebi.interpro.scan.io.GFFWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Writes matches in GFF (Generic Feature Format) version 3 formatted output.
 * <p/>
 * GFF3 description (http://www.sequenceontology.org/gff3.shtml):
 * The format consists of 9 columns, separated by tabs (NOT spaces) etc.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class ProteinMatchesGFFResultWriter extends ProteinMatchesResultWriter {

    protected static final Pattern SEQID_FIELD_PATTERN = Pattern.compile("[^a-zA-Z0-9.:^*$@!+_?\\-|]+");

    protected final static String MATCH_STRING_SEPARATOR = "$";

    protected final static String MATCH_STRING = "match" + MATCH_STRING_SEPARATOR;

    protected final String VALUE_SEPARATOR_GFF3 = ",";

    protected GFFWriter gffWriter;

    private int matchCounter;

    //    protected final Map<String, String> identifierToSeqMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    protected final Map<String, String> identifierToSeqMap = new TreeMap<String, String>(new Gff3FastaSeqIdComparator());


    protected ProteinMatchesGFFResultWriter() {
        super();
        this.matchCounter = 0;
    }

    public ProteinMatchesGFFResultWriter(File file) throws IOException {
        super(file);
        this.gffWriter = new GFFWriter(super.fileWriter);
        //Write first line of file - always the same
        this.gffWriter.write("##gff-version 3");
        //##feature-ontology URI
        //This directive indicates that the GFF3 file uses the ontology of feature types located at the indicated URI or URL.
        this.gffWriter.write("##feature-ontology http://song.cvs.sourceforge.net/viewvc/song/ontology/sofa.obo?revision=1.269");
    }

    protected int getMatchCounter() {
        matchCounter++;
        return matchCounter;
    }

    protected void addFASTASeqToMap(String key, String value) {
        if (identifierToSeqMap != null) {
            identifierToSeqMap.put(key, value);
        }
    }

    public Map<String, String> getIdentifierToSeqMap() {
        return identifierToSeqMap;
    }

    public void writeFASTASequence(String identifier, String sequence) throws IOException {
        gffWriter.writeFASTASequence(identifier, sequence);
    }

    protected void addAdditionalAttr(Entry interProEntry, final GFF3Feature matchFeature) {
        if (interProEntry != null) {
            StringBuilder dbxrefAttributeValue = new StringBuilder("\"InterPro:" + interProEntry.getAccession() + "\"");
//            gffAttributes.add("interPro_entry_desc=" + interProEntry.getDescription());
            if (mapToPathway) {
                Collection<PathwayXref> pathwayXRefs = interProEntry.getPathwayXRefs();
                if (pathwayXRefs != null && pathwayXRefs.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (PathwayXref xref : pathwayXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR_GFF3);
                        }
                        sb.append("\"" + xref.getDatabaseName() + ":" + xref.getIdentifier() + "\"");
                    }
                    dbxrefAttributeValue.append(VALUE_SEPARATOR_GFF3 + sb.toString());
                }
            }
            matchFeature.addAttribute("Dbxref", dbxrefAttributeValue.toString());
            if (mapToGO) {
                Collection<GoXref> goXRefs = interProEntry.getGoXRefs();
                if (goXRefs != null && goXRefs.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (GoXref xref : goXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR_GFF3);
                        }
                        sb.append("\"" + xref.getIdentifier() + "\""); // Just writeComment the GO identifier to the output
                    }
                    matchFeature.addAttribute("Ontology_term", sb.toString());
                }
            }
        }
    }

    public static class Gff3FastaSeqIdComparator implements Comparator<String>, Serializable {

        public int compare(String s1, String s2) {
            if (s1.startsWith(MATCH_STRING) && s2.startsWith(MATCH_STRING)) {
                String[] chunksOfS1 = s1.split("\\" + MATCH_STRING_SEPARATOR);
                String[] chunksOfS2 = s2.split("\\" + MATCH_STRING_SEPARATOR);
                if (chunksOfS1.length == 2 && chunksOfS2.length == 2) {
                    String[] numericalChunksS1 = chunksOfS1[1].split("_");
                    String[] numericalChunksS2 = chunksOfS2[1].split("_");
                    int numChunksS1Length = numericalChunksS1.length;
                    int numChunksS2Length = numericalChunksS2.length;
                    if (numChunksS1Length > 0 && numChunksS2Length > 0) {
                        int matchIdS1 = Integer.parseInt(numericalChunksS1[0]);
                        int matchIdS2 = Integer.parseInt(numericalChunksS2[0]);
                        if ((numChunksS1Length == 3 && numChunksS2Length == 3)
                                && (matchIdS1 == matchIdS2)) {
                            int startIndexS1 = Integer.parseInt(numericalChunksS1[1]);
                            int startIndexS2 = Integer.parseInt(numericalChunksS2[1]);
                            return startIndexS1 - startIndexS2;
                        } else {
                            return matchIdS1 - matchIdS2;
                        }
                    }
                }
                return 0;
            } else {
                int seqLength1 = s1.length(), n2 = s2.length();
                //Compares character by character
                for (int i1 = 0, i2 = 0; i1 < seqLength1 && i2 < n2; i1++, i2++) {
                    char c1 = s1.charAt(i1);
                    char c2 = s2.charAt(i2);
                    if (c1 != c2) {
                        c1 = Character.toUpperCase(c1);
                        c2 = Character.toUpperCase(c2);
                        if (c1 != c2) {
                            c1 = Character.toLowerCase(c1);
                            c2 = Character.toLowerCase(c2);
                            if (c1 != c2) {
                                return c1 - c2;
                            }
                        }
                    }
                }
                return seqLength1 - n2;
            }
        }
    }

    /**
     * There are a couple of restrictions for the first column (seqid) of a GFF file. This method makes sure that these rules are followed.
     */
    protected static String getValidGFF3SeqId(String gffId) {
        return gffId
                .replaceAll(" ", "_")
                .replaceAll(SEQID_FIELD_PATTERN.pattern(), "");
    }

    /**
     * URL escaping rules are used for tags or values containing the following characters: ",=;".
     */
    protected static String getValidGFF3AttributeName(String attributeName) {
        return attributeName
                .replaceAll(",", "%2C")
                .replaceAll("=", "%3D")
                .replaceAll(";", "%3B");
    }

    protected GFF3Feature buildMatchFeature(String seqId, String analysis, int locStart, int locEnd, String score,
                                            String description, String status, String date, String matchId,
                                            String targetId, String signatureAcc, Entry entry) {
        GFF3Feature matchFeature = new GFF3Feature(seqId, analysis, "protein_match", locStart, locEnd, "+");
        matchFeature.setScore(score);
        //Build attributes for the last column in the GFF table
        matchFeature.addAttribute("ID", matchId);
        matchFeature.addAttribute("Target", targetId + " " + locStart + " " + locEnd);
        matchFeature.addAttribute("Name", signatureAcc);
        if (description != null) {
            matchFeature.addAttribute("signature_desc", description);
        }
        matchFeature.addAttribute("status", status);
        matchFeature.addAttribute("date", date);

        if (mapToInterProEntries) {
            addAdditionalAttr(entry, matchFeature);
        }
        return matchFeature;
    }

    protected void processMatches(final Set<Match> matches, final String targetId,
                                  final String date, final Protein protein, final String seqId) throws IOException {
        for (Match match : matches) {
            final Signature signature = match.getSignature();
            final String signatureAc = signature.getAccession();
            final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
            final String analysis = signatureLibrary.getName();
            final String description = match.getSignature().getDescription();
            final String matchId = new String(MATCH_STRING + getMatchCounter());

            final Set<Location> locations = match.getLocations();
            if (locations != null) {
                for (Location location : locations) {
                    String score = ".";
                    String status = "T";
                    if (location instanceof HmmerLocation) {
                        score = Double.toString(((HmmerLocation) location).getEvalue());
                    }
                    //Build match feature line
                    int locStart = location.getStart();
                    int locEnd = location.getEnd();
                    final StringBuilder matchIdLocation = new StringBuilder();
                    if (locations.size() > 1) {
                        matchIdLocation.append("_").append(locStart).append("_").append(locEnd);
                    }
                    GFF3Feature matchFeature = buildMatchFeature(seqId, analysis, locStart, locEnd, score, description, status,
                            date, matchId + matchIdLocation, targetId, signatureAc, signature.getEntry());
                    //Write match feature to file
                    gffWriter.write(matchFeature.getGFF3FeatureLine());
                    //Add match sequence to the map
                    addFASTASeqToMap(matchId + matchIdLocation, protein.getSequence().substring(locStart, locEnd));
                }
            }
        }
    }
}