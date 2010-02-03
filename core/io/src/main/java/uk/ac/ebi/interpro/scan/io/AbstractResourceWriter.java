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
public abstract class AbstractResourceWriter<T> implements ResourceWriter<T> {

    @Override public void write(Resource resource, Collection<T> records) throws IOException {
        write(resource, records, false);
    }

    @Override public void write(Resource resource, Collection<T> records, boolean append)
            throws IOException  {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        // Bizarre Javadoc for createNewFile:
        // <code>true</code> if the named file does not exist and was successfully created;
        // <code>false</code> if the named file already exists
        boolean exists = !resource.getFile().createNewFile();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resource.getFile(), append));
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