package uk.ac.ebi.interpro.scan.io;

import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Default implementation.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
abstract class AbstractResourceReader<T> implements ResourceReader<T> {

    @Override public Collection<T> read(Resource resource) throws IOException {
        if (resource == null) {
            throw new NullPointerException("Resource is null");
        }
        if (!resource.exists())  {
            throw new IllegalStateException(resource.getFilename() + " does not exist");
        }
        if (!resource.isReadable())  {
            throw new IllegalStateException(resource.getFilename() + " is not readable");
        }
        final Collection<T> records = new HashSet<T>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            while (reader.ready()) {
                records.add(createRecord(reader.readLine()));
            }
        }
        finally {
            if (reader != null){
                reader.close();
            }
        }
        return Collections.unmodifiableCollection(records);
    }
    
    protected abstract T createRecord(String line);

}