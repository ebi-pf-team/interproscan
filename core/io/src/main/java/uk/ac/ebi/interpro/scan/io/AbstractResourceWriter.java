package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.io.*;
import java.util.Collection;

/**
 * Default implementation.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
abstract class AbstractResourceWriter<T> implements ResourceWriter<T> {

    @Override public void write(Resource resource, Collection<T> records) throws IOException {
        write(resource, records, true);
    }

    @Override public void write(Resource resource, Collection<T> records, boolean canOverwrite)
            throws IOException  {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!canOverwrite && resource.exists())  {
            throw new IllegalStateException(resource.getFilename() + " already exists and cannot be overwritten");
        }
        // Bizarre Javadoc for createNewFile:
        // <code>true</code> if the named file does not exist and was successfully created;
        // <code>false</code> if the named file already exists
        boolean exists = !resource.getFile().createNewFile();
        if (!canOverwrite && exists) {
            throw new IllegalStateException(resource.getFilename() + " already exists and cannot be overwritten");
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resource.getFile()));
            for (T record : records)    {
                writer.write(createLine(record));
                writer.newLine();
            }
        }
        finally {
            if (writer != null){
                writer.close();
            }
        }
    }

    protected abstract String createLine(T record);

}