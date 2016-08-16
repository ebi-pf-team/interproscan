package uk.ac.ebi.interpro.scan.io.match.writer;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.GFFWriter;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
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

    private static final Logger LOGGER = Logger.getLogger(ProteinMatchesGFFResultWriter.class.getName());

    /**
     * This matcher matches any character that is NOT allowed in a sequence ID in GFF 3 format.
     * It is used to allow replacement of any disallowed char with an empty String (see below).
     */
    protected static final Pattern SEQID_DISALLOWED_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9.:^*$@!+_?\\-|]+");

    protected final static String MATCH_STRING = "match$";

    protected final String VALUE_SEPARATOR_GFF3 = ",";

    protected GFFWriter gffWriter;

    private int matchCounter;

    //    protected final Map<String, String> identifierToSeqMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    protected final Map<String, String> identifierToSeqMap = new TreeMap<>(new Gff3FastaSeqIdComparator());


    protected ProteinMatchesGFFResultWriter() {
        super();
        this.matchCounter = 0;
    }

    public ProteinMatchesGFFResultWriter(Path path) throws IOException {
         this(path, true);
    }

    public ProteinMatchesGFFResultWriter(Path path, boolean writeFullGFF) throws IOException {
        super(path);
        this.gffWriter = new GFFWriter(super.fileWriter);
        if (writeFullGFF) {
            //Write first line of file - always the same
            this.gffWriter.write("##gff-version 3");
            //##feature-ontology URI
            //This directive indicates that the GFF3 file uses the ontology of feature types located at the indicated URI or URL.
            this.gffWriter.write("##feature-ontology http://song.cvs.sourceforge.net/viewvc/song/ontology/sofa.obo?revision=1.269");
        }
    }


    protected int getMatchCounter() {
        matchCounter++;
        return matchCounter;
    }

    protected void addFASTASeqToMap(String key, String value) {
        identifierToSeqMap.put(key, value);
    }

    public Map<String, String> getIdentifierToSeqMap() {
        return identifierToSeqMap;
    }

    public void writeFASTASequence(String identifier, String sequence) throws IOException {
        gffWriter.writeFASTASequence(identifier, sequence);
    }

    protected void addAdditionalAttr(Entry interProEntry, final GFF3Feature matchFeature) {
        if (interProEntry != null) {
            StringBuilder dbxrefAttributeValue = new StringBuilder("\"InterPro:");
            dbxrefAttributeValue
                    .append(interProEntry.getAccession())
                    .append('"');
//            gffAttributes.add("interPro_entry_desc=" + interProEntry.getDescription());
            if (mapToPathway) {
                List<PathwayXref> pathwayXRefs = new ArrayList<>(interProEntry.getPathwayXRefs());
                Collections.sort(pathwayXRefs, new PathwayXrefComparator());
                if (pathwayXRefs.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (PathwayXref xref : pathwayXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR_GFF3);
                        }
                        sb.append('"')
                                .append(xref.getDatabaseName())
                                .append(':')
                                .append(xref.getIdentifier()).append('"');
                    }
                    dbxrefAttributeValue.append(VALUE_SEPARATOR_GFF3).append(sb.toString());
                }
            }
            matchFeature.addAttribute("Dbxref", dbxrefAttributeValue.toString());
            if (mapToGO) {
                List<GoXref> goXRefs = new ArrayList<>(interProEntry.getGoXRefs());
                Collections.sort(goXRefs, new GoXrefComparator());
                if ((goXRefs.size() > 0)) {
                    StringBuilder sb = new StringBuilder();
                    for (GoXref xref : goXRefs) {
                        if (sb.length() > 0) {
                            sb.append(VALUE_SEPARATOR_GFF3);
                        }
                        sb.append('"').append(xref.getIdentifier()).append('"'); // Just writeComment the GO identifier to the output
                    }
                    matchFeature.addAttribute("Ontology_term", sb.toString());
                }
            }
        }
    }

    /**
     * Pattern matchers for match id.
     * <p/>
     * Group 1: signature code (not the signature accession)
     * Group 2: start coordinate
     * Group 3: end coordinate
     */
    private static final Pattern MATCH_ID_PATTERN = Pattern.compile("^match\\$(\\d+)_(\\d+)_(\\d+)$");

    public static class Gff3FastaSeqIdComparator implements Comparator<String>, Serializable {

        /**
         * Sorts on the following:
         * <p/>
         * 1. The integer used to uniquely identify the signature
         * 2. earliest start position
         * 3. earliest stop position
         *
         * @param s1
         * @param s2
         * @return
         */
        public int compare(String s1, String s2) {
            final Matcher match1 = MATCH_ID_PATTERN.matcher(s1);
            final Matcher match2 = MATCH_ID_PATTERN.matcher(s2);

            final boolean match1matches = match1.matches();
            final boolean match2matches = match2.matches();

            // Put full sequences above partial match sequences
            if (match1matches ^ match2matches) {
                return (match1matches) ? 1 : -1;
            }
            // Order match sequences
            else if (match1matches && match2matches) {
                // Attempt to sort on match (signature) id number
                int signature1 = Integer.parseInt(match1.group(1));
                int signature2 = Integer.parseInt(match2.group(1));
                int comparison = (signature1 < signature2) ? -1 : (signature1 > signature2) ? 1 : 0;

                if (comparison == 0) {
                    // match (signature) id number equal - attempt to sort on start position
                    int start1 = Integer.parseInt(match1.group(2));
                    int start2 = Integer.parseInt(match2.group(2));
                    comparison = (start1 < start2) ? -1 : (start1 > start2) ? 1 : 0;
                }

                if (comparison == 0) {
                    // Start positions are equal - attempt to sort on end position
                    int end1 = Integer.parseInt(match1.group(3));
                    int end2 = Integer.parseInt(match2.group(3));
                    comparison = (end1 < end2) ? -1 : (end1 > end2) ? 1 : 0;
                }
                return comparison;
            }
            // Order whole sequence matches.
            else {
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
     * Pre-compiled regexes for the replacements in the following two methods.
     */
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern EQUALS = Pattern.compile("=");
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final Pattern SPACE = Pattern.compile(" ");

    /**
     * There are a couple of restrictions for the first column (seqid) of a GFF file.
     * This method makes sure that these rules are followed.
     *
     * @param gffId to be filtered
     */
    protected static String getValidGFF3SeqId(String gffId) {
        gffId = gffId.trim();
        if (gffId.indexOf(' ') != -1) {
            gffId = SPACE.matcher(gffId).replaceAll("_");
        }
        gffId = SEQID_DISALLOWED_CHAR_PATTERN.matcher(gffId).replaceAll("");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("gffId = " + gffId);
        }
        return gffId;
    }


    /**
     * URL escaping rules are used for tags or values containing the following characters: ",=;".
     *
     * @param attributeName to be filtered.
     */
    protected static String getValidGFF3AttributeName(String attributeName) {
        // Testing with indexOf before attempting the replaceAll, as it is much faster
        // than running the regex engine

        if (attributeName.indexOf(',') != -1) {
            attributeName = COMMA.matcher(attributeName).replaceAll("%2C");
        }
        if (attributeName.indexOf('=') != -1) {
            attributeName = EQUALS.matcher(attributeName).replaceAll("%3D");
        }
        if (attributeName.indexOf(';') != -1) {
            attributeName = SEMICOLON.matcher(attributeName).replaceAll("%3B");
        }
        return attributeName;
    }

    protected GFF3Feature buildMatchFeature(String seqId, String analysis, int locStart, int locEnd, String score,
                                            String description, String status, String date, String matchId,
                                            String targetId, String signatureAcc, Entry entry, boolean writeAllAttributes) {
        GFF3Feature matchFeature = new GFF3Feature(seqId, analysis, "protein_match", locStart, locEnd, "+");
        matchFeature.setScore(score);
        //Build attributes for the last column in the GFF table
        if (writeAllAttributes) {
            matchFeature.addAttribute("ID", matchId);
            matchFeature.addAttribute("Target", targetId + " " + locStart + " " + locEnd);
        }
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

        processMatches(matches, targetId, date, protein, seqId, true);

    }

    public void writeFASTADirective() throws IOException {
        gffWriter.writeDirective("FASTA");
    }

    protected void processMatches(final Set<Match> matches, final String targetId,
                                  final String date, final Protein protein, final String seqId, boolean writeAllAttributes) throws IOException {
        for (Match match : matches) {
            final Signature signature = match.getSignature();
            final String signatureAc = signature.getAccession();
            final SignatureLibrary signatureLibrary = signature.getSignatureLibraryRelease().getLibrary();
            final String analysis = signatureLibrary.getName();
            final String description = signature.getDescription();
            final String matchId = MATCH_STRING + getMatchCounter();

            final Set<Location> locations = match.getLocations();
            if (locations != null) {
                for (Location location : locations) {
                    String score = ".";
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
                    } else if (location instanceof BlastProDomMatch.BlastProDomLocation) {
                        score = Double.toString(((BlastProDomMatch.BlastProDomLocation) location).getEvalue());
                    } else if (location instanceof ProfileScanMatch.ProfileScanLocation) {
                        score = Double.toString(((ProfileScanMatch.ProfileScanLocation) location).getScore());
                    } else if (location instanceof RPSBlastMatch.RPSBlastLocation) {
                        score = Double.toString(((RPSBlastMatch.RPSBlastLocation) location).getEvalue());
                    }
                    //Build match feature line
                    final int locStart = location.getStart();
                    final int locEnd = location.getEnd();
                    final StringBuilder matchIdLocation = new StringBuilder(matchId);
                    matchIdLocation
                            .append('_')
                            .append(locStart)
                            .append('_')
                            .append(locEnd);
                    GFF3Feature matchFeature = buildMatchFeature(seqId, analysis, locStart, locEnd, score, description, status,
                            date, matchIdLocation.toString(), targetId, signatureAc, signature.getEntry(), writeAllAttributes);
                    //Write match feature to file
                    gffWriter.write(matchFeature.getGFF3FeatureLine());
                    //Add match sequence to the map

                    // Sometimes the end location output by the search algorithm can be after the end of
                    // the actual sequence - make sure we don't go off the end of the sequence.
                    final int sequenceLength = protein.getSequence().length();
                    final int endIndex = (locEnd > sequenceLength) ? sequenceLength : locEnd;

                    addFASTASeqToMap(matchIdLocation.toString(), protein.getSequence().substring(locStart - 1, endIndex));
                }
            }
        }
    }
}
