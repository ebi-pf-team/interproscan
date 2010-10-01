package uk.ac.ebi.interpro.scan.io.serialization;

import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Generic class for Serializing / Deserializing an Serializable object.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ObjectSerializerDeserializer<T> {

    private String fileName;

    private boolean overWrite;

    private boolean compressedUsingGzip;

    /**
     * The absolute or relative filepath / name.
     * <p/>
     * Note this has been implemented as a String because the Spring Resource class cannot
     * be used if the file does not already exist.
     *
     * @param fileName The absolute or relative filepath / name.
     */
    @Required
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * If it is OK to overwrite an existing file, set to true.
     * NOTE - Generally a BAD idea if a multithreaded / multiprocess application
     * is doing the serializing.
     *
     * @param overWrite true if it is OK to overwrite an existing file
     */
    @Required
    public void setOverWrite(boolean overWrite) {
        this.overWrite = overWrite;
    }

    /**
     * If this flag is set, the serialized file will be / is compressed using GZIP.  It is essential of course
     * that this flag is set the same for both serialization and deserialization!
     * <p/>
     * Note - there is a pay-off.  Zipping the data is quite expensive computationally, so if you are looking for speed,
     * set this to false.
     *
     * @param compressedUsingGzip flag to indicated if the serialized file will be / is compressed using GZIP.
     */
    @Required
    public void setCompressedUsingGzip(boolean compressedUsingGzip) {
        this.compressedUsingGzip = compressedUsingGzip;
    }

    /**
     * This method serializes an object of class <T extends Serializable> out to the file specified as 'fileName'.
     *
     * @param data An object of class <T extends Serializable>
     * @throws IOException in the event of a problem closing
     */
    public void serialize(T data) {
        ObjectOutputStream oos = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                if (!overWrite) {
                    throw new IllegalStateException("Attempting to serialize to a file at path " + fileName + ", however the file already exists and the 'overWrite' flag has been set to false.");
                }
                if (!file.isFile()) {
                    throw new IllegalStateException("Attempting to serialize to a file at path " + fileName + ", however the file already exists and is not a 'normal' file (it may be a directory).");
                }
            }
            oos = (compressedUsingGzip)
                    ? new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))))
                    : new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

            oos.writeObject(data);
            oos.flush();
        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException("A FileNotFoundException has been thrown when attempting to serialize out an object to path " + fileName);
        } catch (IOException e) {
            throw new IllegalStateException("An IOException occurred when attempting to serialized an object.", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close the handle on the ObjectOutputStream.");
                }
            }
        }
    }

    /**
     * This method deserializes and returns an object of class <T extends Serializable> from the file specified as 'fileName'.
     *
     * @return An object of class <T extends Serializable>
     */
    @SuppressWarnings("unchecked")
    public T deserialize() {
        ObjectInputStream ois = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new IllegalStateException("Attempting to deserialize from a file + " + fileName + ", however no file exists by this name.");
            }
            if (!file.canRead()) {
                throw new IllegalStateException("The file " + file.getAbsolutePath() + " is not readable.  Please check file permissions on this file.");
            }
            if (!file.isFile()) {
                throw new IllegalStateException("The file " + file.getAbsolutePath() + " is not a normal file (it may be a directory or other file type) and so cannot be deserialized to an object");
            }
            ois = (compressedUsingGzip)
                    ? new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))))
                    : new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

            return (T) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException("The class of object being deserialized cannot be found.", e);
        }
        catch (IOException e) {
            throw new IllegalStateException("An IOException occurred when attempting to read in a Serialized object.", e);
        }
        finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close the handle on the ObjectInputStream.");
                }
            }
        }
    }
}
