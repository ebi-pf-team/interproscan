package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Default implementation.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public abstract class AbstractResourceWriter<T> implements ResourceWriter<T> {

    @Override
    public void write(Resource resource, Collection<T> records) throws IOException {
        write(resource, records, false);
    }

    @Override
    public void write(Resource resource, Collection<T> records, boolean append)
            throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }

        if (resource.exists()) {
            if (!resource.getFile().delete()) {
                throw new IllegalStateException("File " + resource.getFilename() + " already exists, but cannot be deleted.");
            }
        }

        // Bizarre Javadoc for createNewFile:
        // <code>true</code> if the named file does not exist and was successfully created;
        // <code>false</code> if the named file already exists
        resource.getFile().createNewFile();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(resource.getFile(), append));
            final Collection<T> sortedRecords = sort(records);
            for (T record : sortedRecords) {
                writer.write(createLine(record));
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Returns sorted records. This implementation simply returns the records without sorting.
     *
     * @param records Collection to sort
     * @return Sorted records.
     */
    protected Collection<T> sort(Collection<T> records) {
        return records;
    }

    protected abstract String createLine(T record);

}
