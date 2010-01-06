package uk.ac.ebi.interpro.scan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation to assist with 'chunking' of long text fields.
 * Performs the tasks of splitting very long strings into
 * chunks and putting them back together again.
 *
 * Implemented as a Singleton, as this class has no state.
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
public class ChunkerSingleton implements Chunker{

    private static ChunkerSingleton ourInstance = new ChunkerSingleton();

    public static ChunkerSingleton getInstance() {
        return ourInstance;
    }

    private ChunkerSingleton() {
    }

    /**
     * Concatenates a List of String into a single long
     * String.
     * @param chunks being the List<String> to concatenate
     * @return the concatenated String, or null if the.
     */
    public String concatenate(List<String> chunks) {
        if (chunks == null){
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (String chunk : chunks){
            buf.append(chunk);
        }
        return (buf.length() == 0) ? null : buf.toString();
    }

    /**
     * Takes the String 'text' argument and splits
     * it into chunks, placed into the chunks List<String>
     * @return List<String> into which the chunks are placed.
     * @param text the long String to be 'chunked'.
     */
    public List<String> chunkIntoList(String text) {
        List<String> chunks = new ArrayList<String>();
        if (text == null){
            return Collections.emptyList();
        }
        int chunkCount = text.length() / CHUNK_SIZE;   // The resulting value is one less than the number of chunks, but using this in the loop which starts at 0.
        for (int offset = 0; offset <= chunkCount; offset++) {
            if (offset < chunkCount) {
                chunks.add(
                        text.substring(offset * CHUNK_SIZE, offset * CHUNK_SIZE + CHUNK_SIZE)
                );
            }
            else {
                chunks.add(
                        text.substring(offset * CHUNK_SIZE)
                );
            }
        }
        return chunks;
    }

}
