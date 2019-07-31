package uk.ac.ebi.interpro.scan.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.interpro.scan.model.Chunker.CHUNK_SIZE;

/**
 * Tests for boundary errors on the Chunker implementation.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ChunkerTest {

    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();

    private static final String SEED_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Selection of various String lengths to test boundary conditions on the chunker.
     */
    private static final List<Integer> TEST_LENGTHS = Arrays.asList(
            0,
            CHUNK_SIZE / 3,
            CHUNK_SIZE -1,
            CHUNK_SIZE,
            CHUNK_SIZE + 1,
            CHUNK_SIZE * 2 - 1,
            CHUNK_SIZE * 2,
            CHUNK_SIZE * 2 + 1,
            CHUNK_SIZE * 3 - 1,
            CHUNK_SIZE * 3,
            CHUNK_SIZE * 3 + 1
    );

    private static final Map<Integer, String> LENGTH_TO_STRING = new HashMap<Integer, String>(TEST_LENGTHS.size());

    static{
        for (Integer length : TEST_LENGTHS){
            StringBuffer buf = new StringBuffer(length);
            while (buf.length() < length){
                final int remaining = length - buf.length();
                if (remaining > SEED_STRING.length()){
                    buf.append(SEED_STRING);
                }
                else {
                    buf.append(SEED_STRING.substring(0, remaining));
                }
            }
            assertEquals(length.intValue(), buf.length());
            LENGTH_TO_STRING.put(length, buf.toString());
        }
    }

    @Test
    public void testSizeEqualToChunkSize(){
        for (Integer length : LENGTH_TO_STRING.keySet()){
            final String testString = LENGTH_TO_STRING.get(length);
            assertEquals(length.intValue(), testString.length());
            TestChunks testChunks = new TestChunks(testString);
            if (testString.length() == 0){
                assertNull(testChunks.getValue());
            }
            else{
                assertEquals(testString, testChunks.getValue());
                if (length == 0){
                    assertNull(testChunks.getFirstChunk());
                    assertNull(testChunks.getValue());
                    assertNull(testChunks.getSubsequentChunks());
                }
                else if (length < CHUNK_SIZE){
                    // Smaller than chunks size, so should be just in firstChunk
                    assertNotNull(testChunks.getFirstChunk());
                    assertNotNull(testChunks.getValue());
                    assertEquals(testString,testChunks.getValue());
                    assertEquals(testString,testChunks.getFirstChunk());
                    assertNull(testChunks.getSubsequentChunks());
                }
                else if (length >= CHUNK_SIZE && length % CHUNK_SIZE == 0){
                    // Exact multiple of CHUNK_SIZE
                    final int expectedChunkNumber = length / CHUNK_SIZE;
                    assertTrue(expectedChunkNumber > 0 && testChunks.getFirstChunk() != null && testChunks.getFirstChunk().length() == CHUNK_SIZE);
                    if (expectedChunkNumber == 1){
                        assertNull(testChunks.getSubsequentChunks());
                    }
                    else {
                        assertNotNull(testChunks.getSubsequentChunks());
                        assertEquals(expectedChunkNumber - 1, testChunks.getSubsequentChunks().size());
                        for (String chunk : testChunks.getSubsequentChunks()){
                            assertNotNull(chunk);
                            assertEquals(CHUNK_SIZE, chunk.length());
                        }
                    }
                }
                else if (length >= CHUNK_SIZE && length % CHUNK_SIZE == 1){
                    // Boundary - String length is 1 more than a multiple of CHUNK_SIZE
                    final int expectedChunkNumber = length / CHUNK_SIZE;
                    assertTrue(expectedChunkNumber > 0 && testChunks.getFirstChunk() != null && testChunks.getFirstChunk().length() == CHUNK_SIZE);
                    assertNotNull(testChunks.getSubsequentChunks());
                    // Get the last chunk - should be 1 character long.
                    String lastChunk = testChunks.getSubsequentChunks().get(testChunks.getSubsequentChunks().size() -1);
                    assertNotNull(lastChunk);
                    assertEquals(1, lastChunk.length());
                }
                else if (length > CHUNK_SIZE && length % CHUNK_SIZE == (CHUNK_SIZE - 1)){
                    // Boundary - String length is 1 less than a multiple of CHUNK_SIZE
                    final int expectedChunkNumber = length / CHUNK_SIZE;
                    assertTrue(expectedChunkNumber > 0 && testChunks.getFirstChunk() != null && testChunks.getFirstChunk().length() == CHUNK_SIZE);
                    assertNotNull(testChunks.getSubsequentChunks());
                    // Get the last chunk - should be 1 character long.
                    String lastChunk = testChunks.getSubsequentChunks().get(testChunks.getSubsequentChunks().size() -1);
                    assertNotNull(lastChunk);
                    assertEquals(CHUNK_SIZE - 1, lastChunk.length());

                }
            }
        }
    }

    private class TestChunks {

        private List<String> subsequentChunks;

        private String firstChunk;

        private String wholeThing;

        TestChunks(String value){
            this.wholeThing = value;
            List<String> chunks = CHUNKER.chunkIntoList(value);
            this.firstChunk = CHUNKER.firstChunk(chunks);
            this.subsequentChunks = CHUNKER.latterChunks(chunks);
        }

        String getValue(){
            return CHUNKER.concatenate(firstChunk, subsequentChunks);
        }

        public List<String> getSubsequentChunks() {
            return subsequentChunks;
        }

        public String getFirstChunk() {
            return firstChunk;
        }

        public String getWholeThing() {
            return wholeThing;
        }
    }
}
