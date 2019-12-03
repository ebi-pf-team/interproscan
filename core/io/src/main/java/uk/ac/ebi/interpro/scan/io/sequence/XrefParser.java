package uk.ac.ebi.interpro.scan.io.sequence;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utility class, which parses all XRef attribute out of a single cross reference (FASTA header).
 * TODO: A more generic version using type Xref is on holder until Xref is public available
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class XrefParser {

    private static final Logger LOGGER = Logger.getLogger(XrefParser.class.getName());

    private static final Pattern PIPE_REGEX = Pattern.compile("\\|");

    private static final Pattern GETORF_HEADER_PATTERN = Pattern.compile("^(.+)\\s+(\\[\\d+\\s+\\-\\s+\\d+].*)$");

    /**
     * Everything before the first whitespace will be recognised as the identifier and everything afterwards will be kind of description.
     */
    private static final Pattern DEFLINE_ID_PATTERN = Pattern.compile("(\\S+)\\s+(.*)");

    private static final String ENA_DB_NAME = "ENA|";

    private static final String SWISSPROT_DB_NAME = "sp|";

    private static final String TREMBL_DB_NAME = "tr|";

    private static final String GENERAL_IDENTIFIER = "gi|";
//    private static final String GENEBANK_DB_NAME = "gb";
//    private static final String REFSEQ_DB_NAME = "ref";
//    private static final String EMBL_DB_NAME = "emb";
//    private static final String DDBJ_DB_NAME = "dbj";


    /**
     * NB All ENA parsing has been commented out for the moment
     * This is to allow the short-term fix to the nucleotide header problem (IBU-2426)
     * TODO - reimplement when the long-term fix is in place
     * <p>
     * This parser only supports FASTA headers from ENA at the moment. All other FASTA headers are returned un-parsed.
     * If you are extending it don't forget to update the unit test at the same time.
     * <p/>
     * ENA pattern: ENA|accession|name
     * <p/>
     * ENA example FASTA header;
     * <p/>
     * ENA|AACH01000026|AACH01000026.1 Saccharomyces mikatae IFO 1815 YM4906-Contig2858, whole genome shotgun sequence.
     * <p/>
     *
     * @param crossReference
     * @return
     */
    public static NucleotideSequenceXref getNucleotideSequenceXref(String crossReference) {
        if (crossReference != null) {
            /*


            if (crossReference.startsWith(ENA_DB_NAME)) {
                String[] chunks = PIPE_REGEX.split(crossReference);
                if (chunks.length == 3) {
                    String database = chunks[0];
                    String identifier = chunks[1].trim();
                    String description = chunks[2];

                    return new NucleotideSequenceXref(database, identifier, description);
                }
            } */
            return stripUniqueIdentifierAndTrimForNucleotideSeq(crossReference);
        }
        return null;
    }

    /**
     * This parser only supports FASTA headers from UniProtKB at the moment. All other FASTA headers are returned un-parsed.
     * Please note, that the protein name is un-parsed for the moment.
     * <p/>
     * You will find information on FASTA header descriptions at the following web sites (status 19/04/2012);
     * <p/>
     * http://www.uniprot.org/help/fasta-headers
     * <p/>
     * https://en.wikipedia.org/wiki/FASTA_format#Header_line
     * <p/>
     * http://www.ncbi.nlm.nih.gov/books/NBK21097/table/A632/?report=objectonly
     * <p/>
     * Excepted examples:
     * <p/>
     * >sp|Q8I6R7|ACN2_ACAGO Acanthoscurrin-2 (Fragment) OS=Acanthoscurria gomesiana GN=acantho2 PE=1 SV=1
     * <p/>
     * >tr|Q8N2H2|Q8N2H2_HUMAN CDNA FLJ90785 fis, clone THYRO1001457, moderately similar to H.sapiens protein kinase C mu OS=Homo sapiens PE=2 SV=1
     * <p/>
     * >gi|6679827|ref|NP_032062.1| protein fosB [Mus musculus]
     * <p/>
     * >gi|49457155|emb|CAG46898.1| FOSB [Homo sapiens]
     * <p/>
     * >gi|351706989|gb|EHB09908.1| Protein fosB [Heterocephalus glaber]
     *
     * @param crossReference
     * @return
     */
    public static ProteinXref getProteinXref(String crossReference) {
        if (crossReference != null) {

            /*
            The getorf headers are identified first to allow the short-term fix for nucleotide
            sequence headers to work  (IBU-2426). Otherwise the protein identifiers may not correspond with the
            stored nucleotide identifiers
            TODO - possibly move back to original position in code (see commented-out code below) when the long-term fix is implemented

             */
            final Matcher matcher = GETORF_HEADER_PATTERN.matcher(crossReference);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Checking for match to GETORF regex, crossRef: " + crossReference);

            String originalHeaderName = crossReference.trim();

            if (matcher.find()) {
                //Utilities.verboseLog("MATCHES GETORF_HEADER_PATTERN");
                //Utilities.verboseLog("originalHeaderName: " + originalHeaderName + " and now xref-id : " + matcher.group(1));
                return new ProteinXref(null, matcher.group(1), originalHeaderName, matcher.group(2));
            }

	        // this eventually should be the only way to parse the header
            if (originalHeaderName.length() > 1) {
                //Test using the header
                //Utilities.verboseLog("originalHeaderName: " + originalHeaderName);
                return stripUniqueIdentifierAndTrimForProteinSeqDefault(originalHeaderName);
            }

            //The rest of the code below will be ignored

            if (matcher.find()) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("MATCHES");
                return new ProteinXref(null, matcher.group(1), null, matcher.group(2));
            }

            if (crossReference.startsWith(SWISSPROT_DB_NAME) || crossReference.startsWith(TREMBL_DB_NAME)) {
                String[] chunks = PIPE_REGEX.split(crossReference);
                if (chunks.length >= 3) {
                    String database = chunks[0];
                    String identifier = chunks[1].trim();
                    String description = chunks[2];
                    String proteinName = getProteinName(description);
                    return new ProteinXref(database, identifier, proteinName, description);
                } else {
                    return stripUniqueIdentifierAndTrimForProteinSeq(crossReference);
                }
            } else if (crossReference.startsWith(GENERAL_IDENTIFIER)) {
                System.out.println("Protein xref: " + crossReference);
                String[] chunks = PIPE_REGEX.split(crossReference);
                if (chunks.length >= 5) {
                    String database = chunks[2];
                    String identifier = chunks[3].trim();
                    String description = chunks[4];
                    String proteinName = getProteinName(description);
                    return new ProteinXref(database, identifier, proteinName, description.trim());
                } else {
                    return stripUniqueIdentifierAndTrimForProteinSeq(crossReference);
                }
            } else {
                /*final Matcher matcher = GETORF_HEADER_PATTERN.matcher(crossReference);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Checking for match to GETORF regex, crossRef: " + crossReference);
                if (matcher.find()) {
                    if (LOGGER.isDebugEnabled()) LOGGER.debug("MATCHES");
                    return new ProteinXref(null, matcher.group(1), null, matcher.group(2));
                } else {
                    if (LOGGER.isDebugEnabled()) LOGGER.debug("No Match.");
                    */
                return stripUniqueIdentifierAndTrimForProteinSeq(crossReference);
                //}
            }
        }
        return null;
    }

    private static NucleotideSequenceXref stripUniqueIdentifierAndTrimForNucleotideSeq(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Found an identifier in a fasta file which is null or empty???");
        } else {
            id = id.trim();
            Matcher matcher = DEFLINE_ID_PATTERN.matcher(id);
            if (matcher.find()) {
                return new NucleotideSequenceXref(null, matcher.group(1), id);
            } else {
                return new NucleotideSequenceXref(null, id, id);
            }
        }
    }

    private static ProteinXref stripUniqueIdentifierAndTrimForProteinSeqDefault(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Found an identifier in a fasta file which is null or empty???");
        } else {
            id = id.trim();
            Matcher matcher = DEFLINE_ID_PATTERN.matcher(id);
            if (matcher.find()) {
                return new ProteinXref(null, matcher.group(1), id, matcher.group(2));
            } else {
                return new ProteinXref(null, id, id);
            }
        }
    }

    private static ProteinXref stripUniqueIdentifierAndTrimForProteinSeq(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("Found an identifier in a fasta file which is null or empty???");
        } else {
            id = id.trim();
            Matcher matcher = DEFLINE_ID_PATTERN.matcher(id);
            if (matcher.find()) {
                return new ProteinXref(null, matcher.group(1), matcher.group(2));
            } else {
                return new ProteinXref(id);
            }
        }
    }

    private static String getProteinName(String description) {
        int start = description.indexOf(" ");
        int end = description.indexOf(" OS");
        if (end == -1) {
            end = description.indexOf(" [");
        }
        if (start > -1 && end > start) {
            return description.substring(start, end).trim();
        }
        return null;
    }

    /**
     * Strips identifiers from
     * ABC.1_1 to ABC.1
     * OR
     * Blob_1 to Blob.
     *
     * @param identifier
     * @return
     */
    public static String stripOfFinalUnderScore(final String identifier) {
        final int finalUndescorePos = identifier.lastIndexOf('_');
        if (finalUndescorePos < 1) {
            throw new IllegalStateException("Appear to have a protein accession without _N appended to it!: " + identifier);
        }
        return identifier.substring(0, finalUndescorePos);
    }

    /**
     * Strips identifiers from
     * ABC.1 to ABC
     * OR
     * Blob.2 to Blob.
     *
     * @param identifier
     * @return
     */
    public static String stripOfVersionNumberIfExists(final String identifier) {
        int finalDotPos = identifier.lastIndexOf('.');
        if (finalDotPos > 0) {
            return identifier.substring(0, finalDotPos);
        }
        return identifier;
    }


}
