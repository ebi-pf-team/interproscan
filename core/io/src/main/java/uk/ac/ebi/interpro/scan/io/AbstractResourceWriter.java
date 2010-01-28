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
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists())  {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resource.getFile()));
            for (T record : records)    {
                writer.write(createLine(record));
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