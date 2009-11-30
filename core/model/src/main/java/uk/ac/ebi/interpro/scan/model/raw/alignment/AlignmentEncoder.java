package uk.ac.ebi.interpro.scan.model.raw.alignment;

/**
 * Encode alignment.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface AlignmentEncoder {

    public String encode(String alignment);

    public interface Parser {
        public int getMatchCount();
        public int getInsertCount();
        public int getDeleteCount();
    }

}
