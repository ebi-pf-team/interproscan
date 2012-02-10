package uk.ac.ebi.interpro.scan.io.sequence;

import uk.ac.ebi.interpro.scan.model.NucleotideSequenceXref;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * Simple utility class, which parses all XRef attribute out of a single cross reference (FASTA Id).
 * TODO: A more generic version using type Xref is on holder until Xref is public available
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class XrefParser {

    private static final String SEPARATOR = "|";

    private static final String REGEX = "\\|";

    public static NucleotideSequenceXref getNucleotideSequenceXref(String crossReference) {
        if (crossReference != null) {
            if (crossReference.contains(SEPARATOR)) {
                String[] chunks = crossReference.split(REGEX);
                if (chunks.length >= 3) {
                    String database = chunks[0];
                    String identifier = chunks[1];
                    String name = chunks[2];
                    return new NucleotideSequenceXref(database, identifier, name);

                }
            } else {
                return new NucleotideSequenceXref(crossReference);
            }
        }
        return null;
    }

    public static ProteinXref getProteinXref(String crossReference) {
        if (crossReference != null) {
            if (crossReference.contains(SEPARATOR)) {
                String[] chunks = crossReference.split(REGEX);
                if (chunks.length >= 3) {
                    String database = chunks[0];
                    String identifier = chunks[1];
                    String name = chunks[2];
                    return new ProteinXref(database, identifier, name);

                }
            } else {
                return new ProteinXref(crossReference);
            }
        }
        return null;
    }
}