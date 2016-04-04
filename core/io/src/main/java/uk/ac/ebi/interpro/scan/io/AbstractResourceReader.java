package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public abstract class AbstractResourceReader<T> implements ResourceReader<T> {

    @Override
    public Collection<T> read(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists()) {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }
        final List<T> records = new ArrayList<T>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                T record = createRecord(line);
                if (record != null) {
                    records.add(record);
                }
            }
        }
        return Collections.unmodifiableList(records);
    }

    protected abstract T createRecord(String line);

}
