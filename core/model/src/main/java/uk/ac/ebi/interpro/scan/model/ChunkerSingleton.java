package uk.ac.ebi.interpro.scan.model;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation to assist with 'chunking' of long text fields.
 * Performs the tasks of splitting very long strings into
 * chunks and putting them back together again.
 * <p/>
 * Implemented as a Singleton, as this class has no state.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
@XmlTransient
public class ChunkerSingleton implements Chunker {

    private static ChunkerSingleton ourInstance = new ChunkerSingleton();

    public static ChunkerSingleton getInstance() {
        return ourInstance;
    }

    private ChunkerSingleton() {
    }


    /**
     * Concatenates a single String and a List of String into a single long
     * String.
     *
     * @param start  first part of the String
     * @param chunks being the List<String> to concatenate
     * @return the concatenated String.
     */
    public String concatenate(String start, List<String> chunks) {
        StringBuffer buf;
        if (start == null) {
            buf = new StringBuffer();
        } else {
            buf = new StringBuffer(start);
        }
        if (chunks != null) {
            for (String chunk : chunks) {
                if (chunk != null) {
                    buf.append(chunk);
                }
            }
        }
        return (buf.length() == 0) ? null : buf.toString();
    }

    /**
     * Takes the String 'text' argument and splits
     * it into chunks, placed into the chunks List<String>
     *
     * @param text the long String to be 'chunked'.
     * @return List<String> into which the chunks are placed.
     */
    public List<String> chunkIntoList(String text) {
        List<String> chunks = new ArrayList<String>();
        if (text == null) {
            return Collections.emptyList();
        }
        int chunkCount = (text.length() - 1) / CHUNK_SIZE;   // The resulting value is one less than the number of chunks, but using this in the loop which starts at 0.
        for (int offset = 0; offset <= chunkCount; offset++) {
            if (offset < chunkCount) {
                chunks.add(
                        text.substring(offset * CHUNK_SIZE, offset * CHUNK_SIZE + CHUNK_SIZE)
                );
            } else {
                chunks.add(
                        text.substring(offset * CHUNK_SIZE)
                );
            }
        }
        return chunks;
    }

    /**
     * Takes a List<String> and returns the first
     * element, or null if not present.
     *
     * @param chunks from which first will be returned
     * @return the first
     *         element, or null if not present.
     */
    @Override
    public String firstChunk(List<String> chunks) {
        if (chunks == null || chunks.size() == 0) {
            return null;
        }
        return chunks.get(0);
    }

    /**
     * Takes a List<String> and returns all but the
     * first element in a new List<String>.
     * <p/>
     * Checks that the argument is not null and contains
     * more than one element, otherwise returns null.
     *
     * @param chunks to split.
     * @return a List<String> and returns all but the
     *         first element in a new List<String>.
     *         <p/>
     *         Checks that the argument is not null and contains
     *         more than one element, otherwise returns null.
     */
    @Override
    public List<String> latterChunks(List<String> chunks) {
        if (chunks == null || chunks.size() < 2) {
            return null;
        }
        return new ArrayList<String>(chunks.subList(1, chunks.size()));
    }

}
