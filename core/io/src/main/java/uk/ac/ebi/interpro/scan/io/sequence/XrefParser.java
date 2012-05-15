package uk.ac.ebi.interpro.scan.io.sequence;

import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * Simple utility class, which parses all XRef attribute out of a single cross reference (FASTA header).
 * TODO: A more generic version using type Xref is on holder until Xref is public available
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class XrefParser {

    private static final String SEPARATOR = "|";

    private static final String REGEX = "\\|";

    private static final String ENA_DB_NAME = "ENA";

    private static final String SWISSPROT_DB_NAME = "sp";

    private static final String TREMBL_DB_NAME = "tr";

    private static final String GENERAL_IDENTIFIER = "gi";
//    private static final String GENEBANK_DB_NAME = "gb";
//    private static final String REFSEQ_DB_NAME = "ref";
//    private static final String EMBL_DB_NAME = "emb";
//    private static final String DDBJ_DB_NAME = "dbj";

    /**
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
            if (crossReference.startsWith(ENA_DB_NAME + SEPARATOR)) {
                String[] chunks = crossReference.split(REGEX);
                if (chunks.length == 3) {
                    String database = chunks[0];
                    String identifier = chunks[1];
                    String description = chunks[2];
                    return new NucleotideSequenceXref(database, identifier, description);
                }
            }
            return new NucleotideSequenceXref(crossReference);
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
            if (crossReference.startsWith(SWISSPROT_DB_NAME + SEPARATOR) || crossReference.startsWith(TREMBL_DB_NAME + SEPARATOR)) {
                String[] chunks = crossReference.split(REGEX);
                if (chunks.length == 3) {
                    String database = chunks[0];
                    String identifier = chunks[1];
                    String description = chunks[2];
                    String proteinName = getProteinName(description);
                    return new ProteinXref(database, identifier, proteinName, description);
                }
            } else if (crossReference.startsWith(GENERAL_IDENTIFIER + SEPARATOR)) {
                String[] chunks = crossReference.split(REGEX);
                if (chunks.length == 5) {
                    String database = chunks[2];
                    String identifier = chunks[3];
                    String description = chunks[4];
                    String proteinName = getProteinName(description);
                    return new ProteinXref(database, identifier, proteinName, description.trim());
                }
            }
            return new ProteinXref(crossReference);
        }
        return null;
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
}