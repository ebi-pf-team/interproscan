package uk.ac.ebi.interpro.scan.model;

import java.util.List;

/**
 * Interface to assist with 'chunking' of long text fields.
 * Performs the tasks of splitting very long strings into
 * chunks and putting them back together again.
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
public interface Chunker {

    public static int CHUNK_SIZE = 4000;
    /**
     * Concatenates a List of String into a single long
     * String.
     * @param chunks being the List<String> to concatenate
     * @return the concatenated String.
     */
    String concatenate (List<String> chunks);

    /**
     * Takes the String 'text' argument and splits
     * it into chunks, placed into the chunks List<String>
     * @return List<String> into which the chunks are placed.
     * @param text the long String to be 'chunked'.
     */
    List<String> chunkIntoList(String text);
}
